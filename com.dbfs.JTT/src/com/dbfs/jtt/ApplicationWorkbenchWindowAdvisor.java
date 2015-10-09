package com.dbfs.jtt;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.dbfs.jtt.resources.IImageKeys;
import com.dbfs.jtt.swt.TasksComposite;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private TrayItem trayItem;
	private Image trayImage;
	private ApplicationActionBarAdvisor actionBarAdvisor;

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		actionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
		return actionBarAdvisor;
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.ON_TOP);
		configurer.setInitialSize(new Point(450, 345));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(false);
		configurer.setShowMenuBar(false);
	}

	@Override
	public void postWindowOpen() {
		final IWorkbenchWindow window = getWindowConfigurer().getWindow();
		trayItem = initTaskItem(window);
		if (trayItem != null) {
			hookPopupMenu(window);
			hookMinimize(window);
		}
		Shell shell = getWindowConfigurer().getWindow().getShell();
		shell.setMinimumSize(500, 345);
	}

	private void hookMinimize(final IWorkbenchWindow window) {
		window.getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellIconified(ShellEvent e) {
				window.getShell().setVisible(false);
			}
		});
		trayItem.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Shell shell = window.getShell();
				if (!shell.isVisible()) {
					shell.setVisible(true);
					window.getShell().setMinimized(false);
				}
			}
		});
	}

	private void hookPopupMenu(final IWorkbenchWindow window) {
		// Add listener for menu pop-up
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				MenuManager trayMenu = new MenuManager();
				Menu menu = trayMenu.createContextMenu(window.getShell());
				actionBarAdvisor.fillTrayItem(trayMenu);
				menu.setVisible(true);
			}
		});
	}

	private TrayItem initTaskItem(IWorkbenchWindow window) {
		final Tray tray = window.getShell().getDisplay().getSystemTray();
		if (tray == null) {
			return null;
		}
		trayItem = new TrayItem(tray, SWT.NONE);
		trayImage = Activator.getImageDescriptor(IImageKeys.CLOCK16).createImage();// AbstractUIPlugin.imageDescriptorFromPlugin("com.dbfs.jtt", IImageKeys.CLOCK16).createImage();
		trayItem.setImage(trayImage);
		trayItem.setToolTipText("Jira Time Tracker");
		return trayItem;
	}

	@Override
	public void dispose() {
		if (trayImage != null) {
			trayImage.dispose();
			trayItem.dispose();
		}
	}

	@Override
	public boolean preWindowShellClose() {
		if (Application.isStartedTask()) {
			Shell shell = getWindowConfigurer().getWindow().getShell();
			MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WORKING);
			messageBox.setText("Jira Time Tracker");
			messageBox.setMessage("Do you want to update worklog for started task?");
			int res = messageBox.open();
			if (res == SWT.CANCEL) {
				return false;
			}
			if (res == SWT.YES) {
				TasksComposite.updateWorklog();
				return false;
			}
			if (res == SWT.NO) {
				TasksComposite.cancelTimer();
			}
		}
		return true;
	}
}
