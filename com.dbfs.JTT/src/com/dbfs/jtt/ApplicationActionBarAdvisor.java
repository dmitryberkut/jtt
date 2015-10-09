package com.dbfs.jtt;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	private IWorkbenchAction exitAction;
	private IWorkbenchAction preferencesAction;
	private IWorkbenchAction aboutAction;

	@Override
	protected void makeActions(IWorkbenchWindow window) {
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		preferencesAction = ActionFactory.PREFERENCES.create(window);
		preferencesAction.setId("com.dbfs.JTT.preferenceAction");
		preferencesAction.setActionDefinitionId("com.dbfs.JTT.preferenceAction");
		register(preferencesAction);
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
	}

	protected void fillTrayItem(IMenuManager trayItem) {
		trayItem.add(preferencesAction);
		trayItem.add(aboutAction);
		trayItem.add(exitAction);
	}

}
