package com.dbfs.jtt.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.dbfs.jtt.Application;
import com.dbfs.jtt.util.LogManager;

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

public static final String AUTO_LOGIN = "prefs_auto_login";
    
    private ScopedPreferenceStore preferences;  
    
    @SuppressWarnings("deprecation")
    public GeneralPreferencePage() {
        super(GRID);
        preferences = new ScopedPreferenceStore(new ConfigurationScope(), Application.PLUGIN_ID);
        setPreferenceStore(preferences);
    }
    
    public void init(IWorkbench workbench) {        
    }

    @Override
    protected void createFieldEditors() {
        BooleanFieldEditor boolEditor = new BooleanFieldEditor(AUTO_LOGIN, 
                "Remember Me (Login automaticaly at startup)", 
                getFieldEditorParent());
        addField(boolEditor);
    }

    @Override
    public boolean performOk() {
        try {
            preferences.save();
        }
        catch (Exception e) {
            LogManager.logStack(e);
        }
        return super.performOk();
    }

}
