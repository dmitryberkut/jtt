package com.dbfs.jtt.dialogs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import com.dbfs.jtt.model.Company;
import com.dbfs.jtt.model.ConnectionDetails;
import com.dbfs.jtt.model.Server;
import com.dbfs.jtt.model.User;
import com.dbfs.jtt.preferences.GeneralPreferencePage;
import com.dbfs.jtt.resources.ColorSchemes;
import com.dbfs.jtt.swt.LoginMessageComposite;
import com.dbfs.jtt.util.LogManager;

public class LoginDialog extends Dialog {

	private static final String SEPARATOR = "-=//=-";
	private static final String SAVED = "jtt_saved_connections";
	private static final String LAST_CONNECTION = "jtt_last_connection";
	// private static final String SERVER_URL = "http://jira.ontrq.com";

	public static final String DAYLY_TIME = "dayly-time";
	public static final String DAYLY_TODAY = "dayly-today"; // format: yyyy-MM-dd
	public static final String DAYLY_ROOT = "local_work_log_node";

	private Combo comboCompany;
	private Combo comboServer;
	private Combo comboUserName;
	private Text textPswd;
	private ConnectionDetails resultConnectionDetails;
	private final LinkedHashMap<String, Company> savedConnectionDetails = new LinkedHashMap<String, Company>();
	private Image[] images;
	private String faultMsg;
	private Button buttonLogin;

	public LoginDialog(Shell parentShell) {
		super(parentShell);
		loadDescriptors();
	}

	private String buildKey() {
		if (resultConnectionDetails == null) {
			return "";
		}
		return new StringBuilder(resultConnectionDetails.getCompany()).append(SEPARATOR).append(resultConnectionDetails.getServer()).append(SEPARATOR).append(resultConnectionDetails.getUser()).toString();
	}

	public void saveDescriptors() {
		String company = comboCompany.getText();
		String url = comboServer.getText();
		String username = comboUserName.getText();
		String password = textPswd.getText();

		resultConnectionDetails = new ConnectionDetails(company, url, username, password);

		try {
			// key = 'Company/URL/Username'
			// value = 'Password'
			String key = buildKey();
			ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences lastConnection = preferences.node(LAST_CONNECTION);
			lastConnection.put(LAST_CONNECTION, key + SEPARATOR + resultConnectionDetails.getPassword(), false);
			lastConnection.flush();
			ISecurePreferences connections = preferences.node(SAVED);
			connections.put(key, resultConnectionDetails.getPassword(), false);
			connections.flush();
			preferences.flush();
		} catch (Exception e) {
			LogManager.logStack(e);
		}
	}

	private void loadDescriptors() {
		try {
			ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences lastConnection = preferences.node(LAST_CONNECTION);

			if ((lastConnection != null) && (lastConnection.keys() != null) && (lastConnection.keys().length > 0)) {
				String[] path = lastConnection.get(LAST_CONNECTION, "").split(SEPARATOR);
				if (path.length == 4) {
					String company = path[0];
					String url = path[1];
					String username = path[2];
					String password = path[3];
					resultConnectionDetails = new ConnectionDetails(company, url, username, password);
				}
			}

			ISecurePreferences connections = preferences.node(SAVED);
			String[] keys = connections.keys();
			for (String key : keys) {
				String[] path = key.split(SEPARATOR);
				String companyName = path[0];
				String url = path[1];
				String username = path[2];
				String password = connections.get(key, null);
				User user = new User(username);
				user.setPassword(password);
				Server server = new Server(url);
				server.addUser(user);
				Company company = new Company(companyName);
				company.addServer(server);
				if (savedConnectionDetails.containsKey(companyName)) {
					savedConnectionDetails.get(companyName).addServer(server);
				} else {
					savedConnectionDetails.put(companyName, company);
				}
				if (resultConnectionDetails == null) {
					resultConnectionDetails = new ConnectionDetails(companyName, url, username, password);
				}
			}
		} catch (Exception e) {
			LogManager.logStack(e);
		}
	}

	private void selectCompanyCombo() {
		Company company = savedConnectionDetails.get(comboCompany.getText());
		Server server = company.getServers().get(0);
		User user = server.getUsers().get(0);
		resultConnectionDetails = new ConnectionDetails(company.getName(), server.getName(), user.getName(), user.getPassword());
		fillAndSelectCombo();
	}

	private void selectServerCombo() {
		Company company = savedConnectionDetails.get(resultConnectionDetails.getCompany());
		Server server = company.getServers().get(comboServer.getSelectionIndex());
		User user = server.getUsers().get(0);
		resultConnectionDetails = new ConnectionDetails(company.getName(), server.getName(), user.getName(), user.getPassword());
		fillAndSelectCombo();
	}

	private void selectUserCombo() {
		Company company = savedConnectionDetails.get(resultConnectionDetails.getCompany());
		Server server = company.getServers().get(comboServer.indexOf(comboServer.getText()));
		User user = server.getUsers().get(comboUserName.getSelectionIndex());
		resultConnectionDetails = new ConnectionDetails(company.getName(), server.getName(), user.getName(), user.getPassword());
		fillAndSelectCombo();
	}

	private void unregisterModifyTextListeners() {
		comboCompany.removeModifyListener(modifyTextListener);
		comboServer.removeModifyListener(modifyTextListener);
		comboUserName.removeModifyListener(modifyTextListener);
		textPswd.removeModifyListener(modifyTextListener);
	}

	private void registerModifyTextListeners() {
		comboCompany.addModifyListener(modifyTextListener);
		comboServer.addModifyListener(modifyTextListener);
		comboUserName.addModifyListener(modifyTextListener);
		textPswd.addModifyListener(modifyTextListener);
	}

	final ModifyListener modifyTextListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			enableOrDisableLoginButton();
		}
	};

	private void enableOrDisableLoginButton() {
		boolean isFieldsCorrect = (comboCompany.getText().trim().length() > 0) && (comboServer.getText().trim().length() > 0) && (comboUserName.getText().trim().length() > 0) && (textPswd.getText().trim().length() > 0);

		if ((buttonLogin != null) && !buttonLogin.isDisposed()) {
			buttonLogin.setEnabled(isFieldsCorrect);
		}
	}

	private void fillAndSelectCombo() {
		unregisterModifyTextListeners();
		if (comboCompany.getItems().length > 0) {
			comboCompany.removeAll();
			comboCompany.setText("");
		}
		if (comboServer.getItems().length > 0) {
			comboServer.removeAll();
			comboServer.setText("");
		}
		if (comboUserName.getItems().length > 0) {
			comboUserName.removeAll();
			comboUserName.setText("");
		}
		if (textPswd.getText().length() > 0) {
			textPswd.setText("");
		}

		for (String company : savedConnectionDetails.keySet()) {
			comboCompany.add(company);
		}
		if (resultConnectionDetails != null) {
			comboCompany.setText(resultConnectionDetails.getCompany());
			Company selectedCompany = savedConnectionDetails.get(resultConnectionDetails.getCompany());

			Server selectedServer = null;
			for (Server svr : selectedCompany.getServers()) {
				comboServer.add(svr.getName());
				if (resultConnectionDetails.getServer().equals(svr.getName())) {
					selectedServer = svr;
				}
			}
			comboServer.setText(resultConnectionDetails.getServer());

			if (selectedServer != null) {
				User selectedUser = null;
				for (User user : selectedServer.getUsers()) {
					comboUserName.add(user.getName());
					if (resultConnectionDetails.getUser().equals(user.getName())) {
						selectedUser = user;
						comboUserName.setText(user.getName());
						textPswd.setText(user.getPassword());
					}
				}

				if (selectedUser != null) {
				}
			}
		}
		registerModifyTextListeners();
		enableOrDisableLoginButton();
	}

	public void setLblMsg(String plblMsg) {
		faultMsg = plblMsg;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
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
		if (csl == null) {
			return null;
		}

		StringTokenizer tokens = new StringTokenizer(csl, ","); //$NON-NLS-1$
		List<String> array = new ArrayList<String>(10);
		while (tokens.hasMoreTokens()) {
			array.add(tokens.nextToken().trim());
		}

		return array.toArray(new String[array.size()]);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		GridData gd_accountLabel = null;
		if (faultMsg != null) {
			gd_accountLabel = new GridData(SWT.CENTER, GridData.CENTER, false, false, 1, 1);
			// gd_accountLabel.widthHint = 340;
			LoginMessageComposite lmc = new LoginMessageComposite(composite, SWT.NONE);
			GridData gd_lmc = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
			gd_lmc.heightHint = 40;
			gd_lmc.widthHint = 405;
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

		Label lblCompanyproject = new Label(composite, SWT.NONE);
		lblCompanyproject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCompanyproject.setText("Company:");

		comboCompany = new Combo(composite, SWT.NONE);
		comboCompany.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCompanyCombo();
			}
		});
		comboCompany.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label serverLabel = new Label(composite, SWT.NONE);
		serverLabel.setText("&Server/Url:");
		serverLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		comboServer = new Combo(composite, SWT.NONE);
		comboServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectServerCombo();
			}
		});
		comboServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&Username:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		comboUserName = new Combo(composite, SWT.BORDER);
		// gridData.widthHint = 350;
		comboUserName.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		comboUserName.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				comboUserName.setBackground(ColorSchemes.normalTextboxBackgroundColor);
				textPswd.setText("");
			}
		});
		comboUserName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectUserCombo();
			}
		});

		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("&Password:");
		passwordLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		textPswd = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		textPswd.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (textPswd.getText().length() > 0) {
					textPswd.setBackground(ColorSchemes.normalTextboxBackgroundColor);
				}
			}
		});
		textPswd.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		@SuppressWarnings("deprecation")
		Preferences preferences = new ConfigurationScope().getNode(Application.PLUGIN_ID);

		final Button btnClean = new Button(composite, SWT.NONE);
		btnClean.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
				ISecurePreferences lastConnection = preferences.node(LAST_CONNECTION);
				lastConnection.removeNode();
				ISecurePreferences connections = preferences.node(SAVED);
				connections.removeNode();
				btnClean.setEnabled(false);
				fillAndSelectCombo();
			}
		});
		btnClean.setText("Clean all");
		btnClean.setToolTipText("Removes all saved credentials in the system");
		btnClean.setEnabled(savedConnectionDetails.size() > 0);

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

		/*String lastUser = "none";
		if (connectionDetails != null) {
			lastUser = connectionDetails.getUser();
		}
		initializeUsers(lastUser);*/
		fillAndSelectCombo();

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		buttonLogin = createButton(parent, IDialogConstants.OK_ID, "&Login", true);
		buttonLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		enableOrDisableLoginButton();
	}

	/**
	 * Returns the connection details entered by the user, or <code>null</code> if the dialog was canceled.
	 */
	public ConnectionDetails getConnectionDetails() {
		return resultConnectionDetails;
	}

	@Override
	public boolean close() {
		if (images != null) {
			for (Image image : images) {
				image.dispose();
			}
		}

		return super.close();
	}
}
