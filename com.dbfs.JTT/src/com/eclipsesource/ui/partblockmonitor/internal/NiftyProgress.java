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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;

/**
 * NiftyProgress declaration. For some tasks this class can be used as a replacement for an {@link ProgressMonitorDialog}.
 * <br/>
 * Use with <code> new NiftyProgress( myResponsiveRunner, new TransparentUIBlocker( parent ) ).run() </code>
 *<br/>
 *
 * @author Matthias Kempka (mkempka@eclipsesource.com)
 */
public class NiftyProgress  {

	private final IResponsiveRunner rr;
	private final IUIBlocker uiBlocker;

	/**
	 * @param rr A custom IResponsiveRunner implementation that does some long running task.
	 * @param uiBlocker The ui blocker. Either a {@link TransparentUIBlocker} instance or a custom {@link IUIBlocker} implementation.
	 */
	public NiftyProgress(IResponsiveRunner rr, IUIBlocker uiBlocker) {
		this.rr = rr;
		this.uiBlocker = uiBlocker;
	}

	public void run() {
		uiBlocker.blockUIComponents();
		final IProgressMonitor monitor = uiBlocker.getMonitor();
		new Thread( new Runnable() {

			@Override
			public void run() {
				try {
					rr.runOutsideUIThread( monitor );
				} catch( final Exception caught ) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							rr.handleException( caught );
						}
					} );
				} finally {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							/*Object obj = updateTaskThreadsPool.poll();
							if ((obj == null) || (updateTaskThreadsPool.size() == 1)) {
								uiBlocker.releaseUIComponents();
							}*/
						}
					} );
				}
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						rr.uiFeedbackAfterRun();
					}
				} );
			}
		} ).start();
	}
}
