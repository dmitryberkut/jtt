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

/** 
 * 
 * IResponsiveRunner declaration. Defines a class that contains a long running method and methods for 
 * synchronizing back into the UI thread. Clients do not need to define threads or Runnables to operate within
 * or without the UI thread.<br/>
 * The method {@link #runOutsideUIThread(IProgressMonitor)} is called
 * outside the UI thread. A progress monitor is provided for that method. 
 * <br/>
 * If an exception is thrown, it is forwarded to the method {{@link #handleException(Exception)}. 
 * <br/>
 * In any case, the method {@link #uiFeedbackAfterRun()} is called after {@link #runOutsideUIThread(IProgressMonitor)} and {@link #handleException(Exception)} inside the UI thread.
 *
 * @author Matthias Kempka (mkempka@eclipsesource.com)
 */
public interface IResponsiveRunner {

  /**
   * Called within the UI thread. This method is called if an error occurs
   * during execute before {@link #uiFeedbackAfterRun()} is called. Widgets
   * should be checked for disposal before accessing them.
   * 
   * @param caught
   */
    public abstract void handleException( final Exception caught );
  
    /**
     * called after {@link #runOutsideUIThread(IProgressMonitor)} from within the UI Thread. 
     * Widgets should be checked for disposal before accessing them.
     */
    public abstract void uiFeedbackAfterRun();
  
    /**
     * called from without the UI Thread
     */
    public abstract void runOutsideUIThread( final IProgressMonitor monitor ) throws Exception;
}
