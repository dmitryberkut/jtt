/*
 * Copyright (c) 2009 EclipseSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Matthias Kempka - initial implementation and API
 */
package com.eclipsesource.ui.partblockmonitor.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * An IUIBlocker implementation that shows a transparent shell over a given control until the {@link IProgressMonitor#done()}
 * is called.
 *
 * @author Matthias Kempka (mkempka@eclipsesource.com)
 */
public class TransparentUIBlocker implements IUIBlocker {

	/**
	 * A progress monitor that expects to it's method call to be called from
	 * without the UI thread.
	 * 
	 * @version $Id: TransparentCompositeBlocker.java,v 1.0 Dec 23, 2008 2:06:44
	 *          PM user Exp $
	 */
	public class ProgressMonitor implements IProgressMonitor {

		private final ProgressIndicator widget;
		private final Label nameLbl;
		private boolean cancelled;

		public ProgressMonitor( ProgressIndicator widget, Label nameLbl ) {
			this.widget = widget;
			this.nameLbl = nameLbl;
		}

		@Override
		public void beginTask( final String name, final int totalWork ) {
			asyncExec( new Runnable() {

				@Override
				public void run() {
					if( !widget.isDisposed() ) {
						setFeedbackText( name );
						widget.beginTask( totalWork );
					}
				}
			} );
		}

		private void asyncExec( Runnable runnable ) {
			if( !widget.isDisposed() ) {
				widget.getDisplay().asyncExec( runnable );
			}
		}

		/** must be called from within the UI thread */
		protected void setFeedbackText( String name ) {
			nameLbl.setText( name );
			nameLbl.getParent().layout();
		}

		@Override
		public void done() {
			asyncExec( new Runnable() {

				@Override
				public void run() {
					if( !widget.isDisposed() ) {
						widget.done();
					}
				}
			} );
		}

		@Override
		public void internalWorked( double work ) {
			throw new UnsupportedOperationException( "Matthias wants to see when this is called" );
		}

		@Override
		public boolean isCanceled() {
			return cancelled;
		}

		@Override
		public void setCanceled( boolean cancelled ) {
			this.cancelled = cancelled;
		}

		@Override
		public void setTaskName( final String name ) {
			asyncExec( new Runnable() {

				@Override
				public void run() {
					if( !widget.isDisposed() ) {
						setFeedbackText( name );
					}
				}
			} );
		}

		@Override
		public void subTask( String name ) {
		}

		@Override
		public void worked( final int work ) {
			asyncExec( new Runnable() {

				@Override
				public void run() {
					if( !widget.isDisposed() ) {
						widget.worked( work );
					}
				}
			} );
		}
	}
	private final Control parent;
	private Shell shell;
	private final boolean cancellable;

	/**
	 * Creates a new instance that blocks the given control by displaying a transparent shell with
	 * a progress report over it.<br/>
	 * This progress report is not cancellable.
	 * @param control
	 */
	public TransparentUIBlocker( Control control ) {
		this( control, false );
	}

	/**
	 * Creates a new instance that blocks the given control by displaying a transparent shell with
	 * a progress report over it.<br/>
	 * @param control
	 * @param cancellable True if a "cancel" button should be displayed
	 */
	public TransparentUIBlocker( Control control, boolean cancellable ) {
		parent = control;
		this.cancellable = cancellable;
	}

	@Override
	public void blockUIComponents() {
		shell = new Shell( parent.getDisplay(), SWT.ON_TOP
				| SWT.MODELESS
				| SWT.NO_FOCUS );
		shell.setAlpha( 155 );
		shell.setBackground( parent.getDisplay().getSystemColor( SWT.COLOR_BLACK ) );
		setBounds( shell );
		shell.open();
		final ControlListener boundSetter = new ControlListener() {

			@Override
			public void controlMoved( ControlEvent e ) {
				setBounds();
			}

			@Override
			public void controlResized( ControlEvent e ) {
				setBounds();
			}

			private void setBounds() {
				if( !shell.isDisposed() ) {
					TransparentUIBlocker.this.setBounds( shell );
				}
			}
		};
		parent.getShell().addControlListener( boundSetter );
		parent.addControlListener( boundSetter );
		DisposeListener disposer = new DisposeListener() {

			@Override
			public void widgetDisposed( DisposeEvent e ) {
				if (!parent.isDisposed()) {
					parent.removeControlListener(boundSetter);
					parent.getShell().removeControlListener(boundSetter);
				}
				if (!shell.isDisposed()) {
					shell.dispose();
				}
			}
		};
		parent.addDisposeListener( disposer );
		shell.addDisposeListener( disposer );
	}

	@Override
	public IProgressMonitor getMonitor() {
		Composite parent = new Composite( shell, SWT.NONE );
		parent.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, true ) );
		shell.setLayout( new GridLayout( 1, false ) );
		parent.setBackground( shell.getBackground() );

		Label label = new Label( parent, SWT.BORDER );
		label.setText( "" );
		label.setBackground( parent.getBackground() );
		label.setForeground( parent.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
		ProgressIndicator progressBar = new ProgressIndicator( parent );
		Button cancel = new Button( parent, SWT.FLAT );
		cancel.setBackground( label.getBackground() );
		cancel.setForeground( label.getForeground() );
		cancel.setText( "Cancel" );
		cancel.setVisible( cancellable );

		parent.setLayout( new GridLayout( 1, false ) );
		label.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false) );
		progressBar.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false) );
		cancel.setLayoutData( new GridData(SWT.CENTER, SWT.CENTER, false, false) );
		shell.layout();
		final ProgressMonitor result = new ProgressMonitor( progressBar, label );
		cancel.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected( SelectionEvent e ) {
				result.setCanceled( true );
			}
		});
		return result;
	}

	@Override
	public void releaseUIComponents() {
		shell.dispose();
	}

	@Override
	public boolean isDisposed() {
		return shell.isDisposed();
	}

	private void setBounds( Shell shell ) {
		shell.setBounds( calcX(),
				calcY(),
				parent.getBounds().width,
				parent.getBounds().height );
	}

	private int calcX() {
		return parent.toDisplay( 0, 0 ).x;
	}

	private int calcY() {
		return parent.toDisplay( 0, 0 ).y;
	}
}
