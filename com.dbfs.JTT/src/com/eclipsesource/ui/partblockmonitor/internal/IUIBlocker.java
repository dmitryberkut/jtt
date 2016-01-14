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
 * The interface defining a class that disables UI components during a long running operation. 
 * 
 * @author Matthias Kempka (mkempka@eclipsesource.com)
 */
public interface IUIBlocker {

  /**
   * Disable UI components so that a user can not interact with them.
   */
  public void blockUIComponents();

  /**
   * Release the UI components that where blocked in {@link #blockUIComponents()}.
   */
  public void releaseUIComponents();

  /**
   * Return a progress monitor that may have it's representation on the blocked ui components.
   * This method is called after {@link #blockUIComponents()} was called. 
   * It is guaranteed that {@link IProgressMonitor#done()} is called on the return value before {@link #releaseUIComponents()} 
   * is called.
   * 
   * @return
   */
  public IProgressMonitor getMonitor();

  /**
   * Returns whether the underlying widgets where disposed during the operation
   * @return
   */
  public boolean isDisposed();

}
