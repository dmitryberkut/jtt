package com.dbfs.jtt.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.dbfs.jtt.Application;
import com.dbfs.jtt.util.LogManager;

public class LoggingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String IS_LOGGING = "prefs_is_logging";
	public static final String IS_DIALOGS = "prefs_is_dialogs";

	private final ScopedPreferenceStore preferences;

	@SuppressWarnings("deprecation")
	public LoggingPreferencePage() {
		super(GRID);
		preferences = new ScopedPreferenceStore(new ConfigurationScope(), Application.PLUGIN_ID);
		setPreferenceStore(preferences);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor boolEditor = new BooleanFieldEditor(IS_LOGGING, "Logging to file", getFieldEditorParent());
		addField(boolEditor);
		boolEditor = new BooleanFieldEditor(IS_DIALOGS, "Show dialog with fatal errors", getFieldEditorParent());
		addField(boolEditor);
	}

	@Override
	public boolean performOk() {
		try {
			preferences.save();
		} catch (Exception e) {
			LogManager.logStack(e);
		}
		return super.performOk();
	}
}