package com.dbfs.jtt.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class NewTaskComposite extends Composite {
    private Text textEstimated;
    private Text textDescription;
    private Button btnAdd;

    public Text getTextEstimated() {
        return textEstimated;
    }

    public void setTextEstimated(Text textEstimated) {
        this.textEstimated = textEstimated;
    }

    public Text getTextDescription() {
        return textDescription;
    }

    public void setTextDescription(Text textDescription) {
        this.textDescription = textDescription;
    }

    public Button getBtnAdd() {
        return btnAdd;
    }

    public void setBtnAdd(Button btnAdd) {
        this.btnAdd = btnAdd;
    }

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public NewTaskComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new FormLayout());
        
        Group grpNewTask = new Group(this, SWT.NONE);
        FormData fd_grpNewTask = new FormData();
        fd_grpNewTask.bottom = new FormAttachment(0, 79);
        fd_grpNewTask.right = new FormAttachment(98);
        fd_grpNewTask.top = new FormAttachment(0);
        fd_grpNewTask.left = new FormAttachment(0, 10);
        grpNewTask.setLayoutData(fd_grpNewTask);
        grpNewTask.setText("New Task");
        grpNewTask.setLayout(new FormLayout());
        
        Label lblEstematedTime = new Label(grpNewTask, SWT.NONE);
        FormData fd_lblEstematedTime = new FormData();
        fd_lblEstematedTime.right = new FormAttachment(0, 105);
        fd_lblEstematedTime.top = new FormAttachment(0, 5);
        fd_lblEstematedTime.left = new FormAttachment(0, 7);
        lblEstematedTime.setLayoutData(fd_lblEstematedTime);
        lblEstematedTime.setText("Estimated Time:");
        
        Label lblTaskDescription = new Label(grpNewTask, SWT.NONE);
        FormData fd_lblTaskDescription = new FormData();
        fd_lblTaskDescription.right = new FormAttachment(0, 105);
        fd_lblTaskDescription.top = new FormAttachment(0, 32);
        fd_lblTaskDescription.left = new FormAttachment(0, 7);
        lblTaskDescription.setLayoutData(fd_lblTaskDescription);
        lblTaskDescription.setText("Task Description:");
        
        btnAdd = new Button(grpNewTask, SWT.NONE);
        btnAdd.setSize(100, 40);
        FormData fd_btnAdd = new FormData();
        fd_btnAdd.left = new FormAttachment(99, -54);
        fd_btnAdd.right = new FormAttachment(99);
        fd_btnAdd.top = new FormAttachment(textEstimated);
        fd_btnAdd.bottom = new FormAttachment(90);
        btnAdd.setLayoutData(fd_btnAdd);
        btnAdd.setText("Add");
        btnAdd.setEnabled(false);
        
        textEstimated = new Text(grpNewTask, SWT.BORDER);
        textEstimated.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                btnAdd.setEnabled(textEstimated.getText().length() > 0 && textDescription.getText().length() > 0);
            }
        });
        textEstimated.addVerifyListener(new VerifyListener() {
            public void verifyText(final VerifyEvent event) {
                switch (event.keyCode) {
                    case SWT.BS: // Backspace
                    case SWT.DEL: // Delete
                    case SWT.HOME: // Home
                    case SWT.END: // End
                    case SWT.ARROW_LEFT: // Left arrow
                    case SWT.ARROW_RIGHT: // Right arrow
                    case SWT.CR:
                        return;
                }

                if (!Character.isDigit(event.character) || textEstimated.getText().length() >= 2) {
                    event.doit = false; // disallow the action
                    return;
                }
            }

        });
        fd_btnAdd.top = new FormAttachment(textEstimated, 0, SWT.TOP);
        FormData fd_text = new FormData();
        fd_text.right = new FormAttachment(0, 286);
        fd_text.top = new FormAttachment(0, 2);
        fd_text.left = new FormAttachment(0, 120);
        textEstimated.setLayoutData(fd_text);
        
        textDescription = new Text(grpNewTask, SWT.BORDER);
        FormData fd_text_1 = new FormData();
        fd_text_1.right = new FormAttachment(0, 286);
        fd_text_1.top = new FormAttachment(0, 29);
        fd_text_1.left = new FormAttachment(0, 120);
        textDescription.setLayoutData(fd_text_1);
        textDescription.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                btnAdd.setEnabled(textEstimated.getText().length() > 0 && textDescription.getText().length() > 0);
            }
        });

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
