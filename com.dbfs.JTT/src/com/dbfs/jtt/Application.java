package com.dbfs.jtt;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.Preferences;

import com.atlassian.jira.rest.client.RestClientException;
import com.dbfs.jtt.dialogs.LoginDialog;
import com.dbfs.jtt.model.ConnectionDetails;
import com.dbfs.jtt.model.SOAPSession;
import com.dbfs.jtt.preferences.GeneralPreferencePage;
import com.dbfs.jtt.util.LogManager;

import org.swift.common.soap.jira.RemoteAuthenticationException;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
    private static Logger logger = Logger.getLogger(Application.class.getName());
    public static final String PLUGIN_ID = "com.dbfs.jtt";
    private final static String MSG_BOX_REGEX = "\\.{1}[^.]*Exception:(.*)";
    private static Pattern pattern = Pattern.compile(MSG_BOX_REGEX);
    private static boolean firstTry = true;
    private static boolean startedTask;
    public static LogManager m_logger;
    
    public static void setFirstTry(boolean isFirst) {
        firstTry = isFirst;
    }

	public static boolean isStartedTask() {
        return startedTask;
    }

    public static void setStartedTask(boolean startedTask) {
        Application.startedTask = startedTask;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
	    (new File("log")).mkdir();
	    m_logger = new LogManager();
	    LogManager.setMaxSize(100);
	    
		Display display = PlatformUI.createDisplay();
		context.applicationRunning();
		try {
		    final SOAPSession soapSession = SOAPSession.getInstance();
		    if (!login(soapSession))
                return IApplication.EXIT_OK;
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		} finally {
			display.dispose();
			m_logger.destroy();
		}
		
	}

    /* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
	
	public static boolean login(final SOAPSession soapSession) {
        LoginDialog dialog = new LoginDialog(null);
        while (soapSession.getJiraSoapService() == null || soapSession.getAuthenticationToken() == null) {
            ConnectionDetails details = dialog.getConnectionDetails();
            if (!isAutoLogin() || details == null || !firstTry) {
                if (SOAPSession.getInstance().getConnectionFaultMsg() != null) {
                    dialog.setLblMsg(SOAPSession.getInstance().getConnectionFaultMsg());
                }
                if (dialog.open() != Window.OK)
                    return false;
                details = dialog.getConnectionDetails();
            }
            firstTry = false;
            soapSession.setConnectionDetails(details);
            connectWithProgress(soapSession);
        }
        return true;
    }
	
	private static boolean isAutoLogin() {
        @SuppressWarnings("deprecation")
        Preferences preferences = new ConfigurationScope().getNode(Application.PLUGIN_ID);
        return preferences.getBoolean(GeneralPreferencePage.AUTO_LOGIN, false);
    }
	
	public static String handleConnectionError(String exceptionMessage){
        logger.debug(exceptionMessage);
        String msg = exceptionMessage;
        if (msg != null) {
            Matcher m = pattern.matcher(msg);
            if (m.find()) {
                msg = m.group(1);
            }
        }
        return msg;
	}
	
	private static void connectWithProgress(final SOAPSession soapSession) {
        ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
        progress.setCancelable(true);
        try {
            progress.run(true, true, new IRunnableWithProgress() {              
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        soapSession.connectAndLogin(monitor);                       
                    } catch (ServiceException e) {
                        throw new InvocationTargetException(e, handleConnectionError(e.getMessage()));
                    } catch (RemoteAuthenticationException e) {
                        throw new InvocationTargetException(e, handleConnectionError(e.getFaultString()));
                    } catch (RemoteException e) {
                    	throw new InvocationTargetException(e, handleConnectionError(e.getMessage()));
                    }
                }
            });
        } catch (InvocationTargetException e) {
            logger.debug(e.getMessage());
            SOAPSession.getInstance().setConnectionFaultMsg(e.getMessage());
            //LogManager.logStack(e);
            /*** Message must appear in the LoginDialog MessageDialog.openError(progress.getShell(), "Jira Authentication has failed", e.getMessage());*/
        } catch (InterruptedException e) {
            logger.debug(e.getMessage());
            SOAPSession.getInstance().setConnectionFaultMsg(e.getMessage());
            //LogManager.logStack(e);
            /*** Message must appear in the LoginDialog MessageDialog.openError(progress.getShell(), "Jira Authentication has failed", e.getMessage());*/
        }
    }
	
	public static void refresh() {
	    ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
        progress.setCancelable(false);
        final SOAPSession soapSession = SOAPSession.getInstance();
        try {
            progress.run(true, true, new IRunnableWithProgress() {              
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        soapSession.refresh(monitor);
                    } catch (RemoteAuthenticationException e) {
                    	throw new InvocationTargetException(e, handleConnectionError(e.getFaultString()));
                    } catch (RemoteException e) {
                    	throw new InvocationTargetException(e, handleConnectionError(e.getMessage()));
                    } catch (RestClientException e) {
                    	throw new InvocationTargetException(e, handleConnectionError(e.getMessage()));
					}
                }
            });
        } catch (InvocationTargetException e) {
            logger.warn(e.getMessage());
            SOAPSession.getInstance().setConnectionFaultMsg(e.getMessage());
            //LogManager.logStack(e);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
            SOAPSession.getInstance().setConnectionFaultMsg(e.getMessage());
            //LogManager.logStack(e);
        }
	}
}