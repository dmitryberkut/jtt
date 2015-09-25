package com.dbfs.jtt.dialogs;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.dbfs.jtt.model.DailyTotalCounter;

public class WeekReportDialog extends Dialog{
	private Table table;
	private String username;

	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Week report");
	}
	
	public WeekReportDialog(Shell parent,String username) {
		super(parent);
		setShellStyle(SWT.TITLE);
		this.username = username;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setSize(150, 100);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);
        
        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_table.widthHint = 330;
        table.setLayoutData(gd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn tblclmnDayOfWeek = new TableColumn(table, SWT.CENTER);
        tblclmnDayOfWeek.setText("Day of week");
        tblclmnDayOfWeek.setWidth(200);
 
        TableColumn tblclmnSpentTime = new TableColumn(table, SWT.NONE);
        tblclmnSpentTime.setWidth(150);
        tblclmnSpentTime.setText("Spent time");

        table = populateTable(table);
        
		return composite;
	}
	
	private Table populateTable(Table param) {
		DailyTotalCounter counter = new DailyTotalCounter(username);
		List<String[]> data = counter.getWeekReport();
		for (String[] strings : data) {
			TableItem item = new TableItem(param, SWT.NONE);
		    item.setText(strings);
		}
		return param;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Ok", true);
	}
}
