/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.concurrent;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PausibleScheduledThreadPoolExecutor
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PausibleScheduledThreadPoolExecutor extends
        ScheduledThreadPoolExecutor {
    
    private AtomicBoolean isPaused = new AtomicBoolean(false);
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();

    /**
     * <p>Constructor for PausibleScheduledThreadPoolExecutor.</p>
     *
     * @param corePoolSize a int.
     */
    public PausibleScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }
    
    /**
     * <p>isPaused</p>
     *
     * @return a boolean.
     */
    public boolean isPaused() {
        return isPaused.get();
    }

    /** {@inheritDoc} */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
      super.beforeExecute(t, r);
      pauseLock.lock();
      try {
        while (isPaused.get()) unpaused.await();
      } catch(InterruptedException ie) {
        t.interrupt();
      } finally {
        pauseLock.unlock();
      }
    }
  
    /**
     * <p>pause</p>
     */
    public void pause() {
      pauseLock.lock();
      try {
        isPaused.set(true);
      } finally {
        pauseLock.unlock();
      }
    }
  
    /**
     * <p>resume</p>
     */
    public void resume() {
      pauseLock.lock();
      try {
        isPaused.set(false);
        unpaused.signalAll();
      } finally {
        pauseLock.unlock();
      }
    }




}
