package com.dbfs.jtt.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.dbfs.jtt.model.Task;

public class ClosedIssueDialog extends Dialog {
	private Image[] images;
	private final Task task;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public ClosedIssueDialog(Shell parentShell, Task task) {
		super(parentShell);
		this.task = task;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);

		int fontSize = 10;
		int style = SWT.NORMAL;
		String spentTime = task.getFormatedTimeSpent() != null ? task.getFormatedTimeSpent() : "no";

		Label lblWarning = new Label(container, SWT.NONE);
		lblWarning.setText("Warning!");
		lblWarning.setFont(new Font(parent.getDisplay(), "Arial", fontSize, SWT.BOLD));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		Label lblLeft = new Label(container, SWT.NONE);
		lblLeft.setFont(new Font(parent.getDisplay(), "Arial", fontSize, style));
		lblLeft.setText("You have " + spentTime + " reported on ");

		Link link = new Link(container, SWT.NONE);
		link.setFont(new Font(parent.getDisplay(), "Arial", fontSize, style));
		link.setText("<a>" + task.getKey() + "</a>");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				org.eclipse.swt.program.Program.launch(task.getUrl());
			}
		});

		Label lblRight = new Label(container, SWT.NONE);
		lblRight.setFont(new Font(parent.getDisplay(), "Arial", fontSize, style));
		lblRight.setText(", and it's no longer open.");

		Label lblCheck = new Label(container, SWT.NONE);
		lblCheck.setFont(new Font(parent.getDisplay(), "Arial", fontSize, style));
		lblCheck.setText("Please check!");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Issue has been closed");
		// newShell.setSize(300, 200);
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

		StringTokenizer tokens = new StringTokenizer(csl, ",");
		List<String> array = new ArrayList<String>(10);
		while (tokens.hasMoreTokens()) {
			array.add(tokens.nextToken().trim());
		}

		return array.toArray(new String[array.size()]);
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(424, 167);
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