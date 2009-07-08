/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.core.concurrent;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PausibleScheduledThreadPoolExecutor
 *
 * @author brozow
 */
public class PausibleScheduledThreadPoolExecutor extends
        ScheduledThreadPoolExecutor {
    
    private AtomicBoolean isPaused = new AtomicBoolean(false);
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();

    public PausibleScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

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
  
    public void pause() {
      pauseLock.lock();
      try {
        isPaused.set(true);
      } finally {
        pauseLock.unlock();
      }
    }
  
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
