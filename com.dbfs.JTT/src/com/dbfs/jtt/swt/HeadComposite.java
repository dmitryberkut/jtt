package com.dbfs.jtt.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;

import com.dbfs.jtt.Activator;
import com.dbfs.jtt.resources.IImageKeys;

public class HeadComposite extends Composite {
    Button btnLogInOut;
    Label lblUserName;
    Combo comboFilterPrj;
    Button btnRefresh;
    
    private Combo comboStatusFilter;
    
    public Button getBtnLogInOut() {
        return btnLogInOut;
    }

    public Label getLblUserName() {
        return lblUserName;
    }

    public Combo getComboFilterPrj() {
        return comboFilterPrj;
    }

    
    public Combo getComboStatusFilter() {
        return comboStatusFilter;
    }
    
    public Button getBtnRefresh() {
        return btnRefresh;
    }

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public HeadComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new FormLayout());
        /*** temporary hiding
        Combo comboFilterTasks = new Combo(this, SWT.READ_ONLY | SWT.NO_FOCUS);
        FormData fd_combo = new FormData();
        fd_combo.left = new FormAttachment(0, 10);
        fd_combo.bottom = new FormAttachment(100, -270);
        comboFilterTasks.setLayoutData(fd_combo);
        comboFilterTasks.add("Sorting by");
        comboFilterTasks.add("Number");
        comboFilterTasks.add("Priority");
        comboFilterTasks.select(0); ***/
        
        comboFilterPrj = new Combo(this, SWT.READ_ONLY | SWT.NO_FOCUS);
        FormData fd_comboFilterPrj = new FormData();
        fd_comboFilterPrj.left = new FormAttachment(0, 10);
        /*** temporary ***/fd_comboFilterPrj.right = new FormAttachment(0, 100);
        fd_comboFilterPrj.top = new FormAttachment(0, 5);
        /*** fd_comboFilterPrj.bottom = new FormAttachment(100, -270);*/
        /*** temporary hiding fd_comboFilterPrj.left = new FormAttachment(comboFilterTasks, 10); ***/
        comboFilterPrj.setLayoutData(fd_comboFilterPrj);
        comboFilterPrj.add("All projects");
        comboFilterPrj.select(0);
        
        comboStatusFilter = new Combo(this, SWT.READ_ONLY | SWT.NO_FOCUS);
        FormData fd_combo = new FormData();
        fd_combo.left = new FormAttachment(comboFilterPrj, 3);
        /**** fd_combo.top = new FormAttachment(comboFilterPrj, 0, SWT.TOP);*/
        fd_combo.top = new FormAttachment(comboFilterPrj, 0, SWT.TOP);
        fd_combo.bottom = new FormAttachment(comboFilterPrj, 0, SWT.BOTTOM);
        comboStatusFilter.add("All statuses");// 0 elem
        comboStatusFilter.select(0);
        comboStatusFilter.setLayoutData(fd_combo);

        btnRefresh = new Button(this, SWT.NONE);
        FormData fd_btnRefresh = new FormData();
        fd_btnRefresh.left = new FormAttachment(comboStatusFilter, 2);
        fd_btnRefresh.top = new FormAttachment(comboFilterPrj, 0, SWT.TOP);
        fd_btnRefresh.bottom = new FormAttachment(comboFilterPrj, 0, SWT.BOTTOM);
        btnRefresh.setLayoutData(fd_btnRefresh);
        btnRefresh.setImage(Activator.getImageDescriptor(IImageKeys.REFRESH).createImage());
        btnRefresh.setToolTipText("Refresh");
        
        btnLogInOut = new Button(this, SWT.NONE);
        //fd_comboFilterPrj.right = new FormAttachment(btnLogInOut, -6);
        /*** temporary hiding fd_combo.top = new FormAttachment(btnLogInOut, 2, SWT.TOP); ***/
        /***fd_comboFilterPrj.top = new FormAttachment(btnLogInOut, 2, SWT.TOP);*/
        FormData fd_btnJiraCredentials = new FormData();
        fd_btnJiraCredentials.top = new FormAttachment(comboFilterPrj, 0, SWT.TOP);
        fd_btnJiraCredentials.right = new FormAttachment(100, -22);
        fd_btnJiraCredentials.bottom = new FormAttachment(comboFilterPrj, 0, SWT.BOTTOM);
        btnLogInOut.setLayoutData(fd_btnJiraCredentials);
        //btnLogInOut.setText("Log out");
        
        lblUserName = new Label(this, SWT.NONE);
        //fd_combo.right = new FormAttachment(lblUserName, -269);
        FormData fd_lblDmch = new FormData();
        fd_lblDmch.top = new FormAttachment(comboFilterPrj, 2, SWT.TOP);
        fd_lblDmch.right = new FormAttachment(btnLogInOut, -6);
        fd_lblDmch.bottom = new FormAttachment(comboFilterPrj, 0, SWT.BOTTOM);
        lblUserName.setLayoutData(fd_lblDmch);
        btnLogInOut.setFocus();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
