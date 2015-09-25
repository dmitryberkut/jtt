package com.dbfs.jtt.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.dbfs.jtt.Application;
import com.dbfs.jtt.JTTVersion;
import com.dbfs.jtt.model.ConnectionDetails;
import com.dbfs.jtt.preferences.GeneralPreferencePage;
import com.dbfs.jtt.resources.ColorSchemes;
import com.dbfs.jtt.swt.LoginMessageComposite;
import com.dbfs.jtt.util.LogManager;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class LoginDialog extends Dialog {

    private static final String PASSWORD = "password";
    private static final String SERVER = "server";
    private static final String SAVED = "saved-connections";
    private static final String LAST_USER = "last-connection";
    private static final String SERVER_URL = "http://jira.ontrq.com";
    
    public  static final String DAYLY_TIME  = "dayly-time";
    public  static final String DAYLY_TODAY = "dayly-today"; // format: yyyy-MM-dd
    public  static final String DAYLY_ROOT = "local_work_log_node";
    
    
    private Combo userNameText;
    private Text serverText;
    private Text passwordText;
    private Image[] images;
    private ConnectionDetails connectionDetails;
    private Map<String, ConnectionDetails> savedDetails = new HashMap<String, ConnectionDetails>();
    private String faultMsg;

    public LoginDialog(Shell parentShell) {
        super(parentShell);
        loadDescriptors();
    }

    public void setLblMsg(String plblMsg) {
        this.faultMsg = plblMsg;
    }

    public void saveDescriptors() {
    	// TODO: working with Preferences move to separate class
        try {
            ISecurePreferences preferences = SecurePreferencesFactory.getDefault();     
            preferences.put(LAST_USER, connectionDetails.getUser(), false);
            ISecurePreferences connections = preferences.node(SAVED);
            for (Entry<String, ConnectionDetails> entry : savedDetails.entrySet()) {
                ConnectionDetails details = entry.getValue();
                ISecurePreferences connection = connections.node(entry.getKey());
                connection.put(SERVER, details.getServer(), false);
                connection.put(PASSWORD, details.getPassword(), false);
            }
            connections.flush();        
        } catch (StorageException e) {
            LogManager.logStack(e);
        } catch (IOException e) {
            LogManager.logStack(e);
        }
    }
    
    private void loadDescriptors() {
        ISecurePreferences userNameNode = null;
        try {
            ISecurePreferences preferences = SecurePreferencesFactory.getDefault();         
            ISecurePreferences connections = preferences.node(SAVED);
            String[] userNames = connections.childrenNames();
            for (int i = 0; i < userNames.length; i++) {
                String userName = userNames[i];
                userNameNode = connections.node(userName);
                savedDetails.put(userName, new ConnectionDetails(
                        userName,
                        userNameNode.get(SERVER, ""),
                        userNameNode.get(PASSWORD, "")));
            }
            connectionDetails = savedDetails.get(preferences.get(LAST_USER, ""));
        }
        catch (StorageException e) {
            LogManager.logStack(e);
            if (userNameNode != null) {
                userNameNode.removeNode();
            }
        }
    }
    
    @Override
    protected void buttonPressed(int buttonId) {
        String userName = userNameText.getText();
        String server = /*SERVER_URL;//*/serverText.getText();
        String password = passwordText.getText();
        
        if (!userName.equals("") && !server.equals("") && !password.equals("")) {
            connectionDetails = new ConnectionDetails(userName, server, password);
            savedDetails.put(userName, connectionDetails);
            
            if (buttonId == IDialogConstants.OK_ID || buttonId == IDialogConstants.CANCEL_ID)
                saveDescriptors();
        }
        
        
        super.buttonPressed(buttonId);
    }   
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Jira Time Tracker ++ v." + JTTVersion.getText());
        IProduct product = Platform.getProduct();
        if (product != null) {
            String bundleId = product.getDefiningBundle().getSymbolicName();
            String imagesUrls[] = parseCSL(product.getProperty(IProductConstants.WINDOW_IMAGES));
            if (imagesUrls.length > 0) {
                images = new Image[imagesUrls.length];
                for (int i = 0; i < imagesUrls.length; i++) {
                    ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(bundleId, imagesUrls[i]);
                    images[i] = descriptor.createImage(true);
                }
                newShell.setImages(images);
            }           
        }
    }

    public static String[] parseCSL(String csl) {
        if (csl == null)
            return null;

        StringTokenizer tokens = new StringTokenizer(csl, ","); //$NON-NLS-1$
        List<String> array = new ArrayList<String>(10);
        while (tokens.hasMoreTokens())
            array.add(tokens.nextToken().trim());

        return (String[]) array.toArray(new String[array.size()]);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);
        
        GridData gd_accountLabel = null;
        if (faultMsg != null) {
            gd_accountLabel = new GridData(SWT.CENTER, GridData.CENTER, false, false, 1, 1);
            gd_accountLabel.widthHint = 340;
            LoginMessageComposite lmc = new LoginMessageComposite(composite, SWT.NONE);
            GridData gd_lmc = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
            gd_lmc.heightHint = 40;
            gd_lmc.widthHint = 325;
            lmc.setLayoutData(gd_lmc);
            lmc.setMsg(faultMsg);
        } else {
            gd_accountLabel = new GridData(SWT.CENTER, GridData.CENTER, false, false, 2, 1);
            gd_accountLabel.heightHint = 34;
            Label accountLabel = new Label(composite, SWT.NONE);
            accountLabel.setFont(new Font(composite.getDisplay(), "Arial", 9, SWT.BOLD));
            accountLabel.setText("                      Welcome to Jira Time Tracker ++.\nPlease provide your Jira username and password below.");
            accountLabel.setLayoutData(gd_accountLabel);
        }
        

        Label userIdLabel = new Label(composite, SWT.NONE);
        userIdLabel.setText("&Username:");
        userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

        userNameText = new Combo(composite, SWT.BORDER);
        //gridData.widthHint = 350;
        userNameText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        userNameText.addListener(SWT.Modify, new Listener() {         
            public void handleEvent(Event event) {
                ConnectionDetails details = savedDetails.get(userNameText.getText());
                if (details != null) {
                    userNameText.setBackground(ColorSchemes.normalTextboxBackgroundColor);
                    serverText.setText(details.getServer());
                    passwordText.setText(details.getPassword());
                }
            }
        });

        Label serverLabel = new Label(composite, SWT.NONE);
        serverLabel.setText("&Server:");
        serverLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

        serverText = new Text(composite, SWT.BORDER);
        serverText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (serverText.getText().length() > 0) {
                    serverText.setBackground(ColorSchemes.normalTextboxBackgroundColor);
                }
            }
        });
        serverText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("&Password:");
        passwordLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));

        passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (passwordText.getText().length() > 0) {
                    passwordText.setBackground(ColorSchemes.normalTextboxBackgroundColor);
                }
            }
        });
        passwordText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        
        @SuppressWarnings("deprecation")
        Preferences preferences = new ConfigurationScope().getNode(Application.PLUGIN_ID);
        new Label(composite, SWT.NONE);
        
        final Button autoLogin = new Button(composite, SWT.CHECK);
        autoLogin.setText("Remember Me (Login &automaticaly at startup)");
        autoLogin.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
        autoLogin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    @SuppressWarnings("deprecation")
                    IEclipsePreferences preferences = new ConfigurationScope().getNode(Application.PLUGIN_ID);
                    preferences.putBoolean(GeneralPreferencePage.AUTO_LOGIN, autoLogin.getSelection());
                    preferences.flush();            
                } catch (BackingStoreException ex) {                    
                    LogManager.logStack(ex);
                }
            }           
        });
        autoLogin.setSelection(preferences.getBoolean(GeneralPreferencePage.AUTO_LOGIN, false));        
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        String lastUser = "none";
        if (connectionDetails != null)
            lastUser = connectionDetails.getUser();
        initializeUsers(lastUser);
        
        return composite;
    }

    protected void initializeUsers(String defaultUser) {
        userNameText.removeAll();
        passwordText.setText("");
        serverText.setText("");
        for (String user : savedDetails.keySet())
            userNameText.add(user);
        int index = Math.max(userNameText.indexOf(defaultUser), 0);
        userNameText.select(index);
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        createButton(parent, IDialogConstants.OK_ID, "&Login", true);
    }
    
    @Override
    protected void okPressed() {
        String userName = userNameText.getText();
        String server = /*SERVER_URL;//*/serverText.getText();
        String password = passwordText.getText();
        boolean isBlank = false;
        if (userName.equals("")) {
            userNameText.setBackground(ColorSchemes.blankTextboxBackgroundColor);
            userNameText.setText("Username field must not be blank");
            //MessageDialog.openError(getShell(), "Invalid Username", "Username field must not be blank.");
            isBlank = true;
        }
        if (server.equals("")) {
            serverText.setBackground(ColorSchemes.blankTextboxBackgroundColor);
            serverText.setMessage("Server field must not be blank");
            //MessageDialog.openError(getShell(), "Invalid Server", "Server field must not be blank.");
            isBlank = true;
        }
        if (password.equals("")) {
            passwordText.setBackground(ColorSchemes.blankTextboxBackgroundColor);
            passwordText.setMessage("Password field must not be blank");
            //MessageDialog.openError(getShell(), "Invalid Password", "Password field must not be blank.");
            isBlank = true;
        }
        if (isBlank) {
            return;
        }
        connectionDetails = new ConnectionDetails(userName, server, password);
        super.okPressed();
    }

    /**
     * Returns the connection details entered by the user, or <code>null</code>
     * if the dialog was canceled.
     */
    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    @Override
    public boolean close() {
        if (images != null) {
            for (int i = 0; i < images.length; i++)
                images[i].dispose();
        }
        
        return super.close();
    }   
}
