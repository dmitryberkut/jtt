package com.dbfs.jtt.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.dbfs.jtt.Application;

public class AbstractPreferenceInitializer extends org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer {

	public AbstractPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		@SuppressWarnings("deprecation")
		IEclipsePreferences defaults = new DefaultScope().getNode(Application.PLUGIN_ID);
		defaults.putBoolean(GeneralPreferencePage.AUTO_LOGIN, false);
		defaults.putBoolean(LoggingPreferencePage.IS_LOGGING, true);
		defaults.putBoolean(LoggingPreferencePage.IS_DIALOGS, true);
	}

}
