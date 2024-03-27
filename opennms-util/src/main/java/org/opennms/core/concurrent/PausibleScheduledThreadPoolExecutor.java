/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.concurrent;

import java.util.concurrent.ThreadFactory;
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

    public PausibleScheduledThreadPoolExecutor(final int corePoolSize) {
        super(corePoolSize);
    }
    
    public PausibleScheduledThreadPoolExecutor(final int corePoolSize, final ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
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
