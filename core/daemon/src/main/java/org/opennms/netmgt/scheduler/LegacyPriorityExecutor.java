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
package org.opennms.netmgt.scheduler;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.fiber.PausableFiber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyPriorityExecutor implements PausableFiber {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyPriorityExecutor.class);

    private static final long BASE_DELAY_MS = 1000;
    private static final long MAX_DELAY_MS = 60000;

    private final String m_parent;
    private final ExecutorService priorityJobPoolExecutor;
    private final DelayQueue<PriorityReadyRunnable> priorityQueue;
    private final ExecutorService m_worker = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("LegacyPriorityExecutor-Worker-", 0).factory());
    // Guards m_status with a lock rather than a monitor so that the virtual thread running the
    // worker loop parks on await() instead of pinning its carrier while paused.
    private final ReentrantLock m_lock = new ReentrantLock();
    private final Condition m_statusChanged = m_lock.newCondition();
    private volatile int m_status;
    public LegacyPriorityExecutor(String parent, Integer poolSize, Integer queueSize) {
        m_parent=parent;
        m_status = START_PENDING;
        priorityJobPoolExecutor = Executors.newFixedThreadPool(poolSize, new LogPreservingThreadFactory(parent, poolSize));
        priorityQueue = new DelayQueue<>();
    }

    public void addPriorityReadyRunnable(PriorityReadyRunnable job) {
        m_lock.lock();
        try {
            priorityQueue.add(job);
            if (LOG.isInfoEnabled()) {
                LOG.info("addPriorityReadyRunnable: Added {}, total in queue: {}", job.getInfo(), priorityQueue.size());
            }
        } finally {
            m_lock.unlock();
        }
    }

    @Override
    public void pause() {
        m_lock.lock();
        try {
            if (m_status == PAUSED) {
                return;
            }
            m_status=PAUSE_PENDING;
            m_statusChanged.signalAll();
        } finally {
            m_lock.unlock();
        }
    }

    @Override
    public void resume() {
        m_lock.lock();
        try {
            if (m_status == RUNNING) {
                return;
            }
            m_status=RESUME_PENDING;
            m_statusChanged.signalAll();
        } finally {
            m_lock.unlock();
        }
    }

    @Override
    public void start() {
        m_worker.execute(() -> {
            m_lock.lock();
            try {
                m_status = RUNNING;
            } finally {
                m_lock.unlock();
            }
            LOG.info("run: Priority Executor {} running", m_parent);

            boolean keepRunning = true;
            while (keepRunning) {
                try {
                    PriorityReadyRunnable executable;
                    while (true) {
                        executable = priorityQueue.poll(200, TimeUnit.MILLISECONDS);
                        if (executable != null) {
                            break;
                        }
                        // Drain can leave the dispatcher blocked on an empty queue; still honor pause.
                        m_lock.lock();
                        try {
                            if (m_status == PAUSE_PENDING) {
                                LOG.info("run: pausing.");
                                m_status = PAUSED;
                                m_statusChanged.signalAll();
                            }
                            while (m_status == PAUSED) {
                                m_statusChanged.await();
                            }
                            if (m_status == STOP_PENDING) {
                                LOG.info("run: status = {}, time to exit", m_status);
                                keepRunning = false;
                                break;
                            }
                            if (m_status == RESUME_PENDING) {
                                LOG.info("run: resuming.");
                                m_status = RUNNING;
                            }
                        } finally {
                            m_lock.unlock();
                        }
                    }
                    if (!keepRunning) {
                        break;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Taked: {}", executable.getInfo());
                    }
                    while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                        if (m_status == PAUSE_PENDING) {
                            LOG.info("run: pausing.");
                        }
                        m_status = PAUSED;
                        m_lock.lock();
                        try {
                            m_statusChanged.await();
                        } finally {
                            m_lock.unlock();
                        }
                    }
                    if (m_status == STOP_PENDING) {
                        LOG.info("run: status = {}, time to exit", m_status);
                        keepRunning = false;
                        break;
                    }
                    if (m_status == RESUME_PENDING) {
                        LOG.info("run: resuming.");
                        m_status = RUNNING;
                    }
                    if (executable.isReady()) {
                        executable.setPriority(0);
                        executable.setDelayUntil(0);
                        priorityJobPoolExecutor.execute(executable);
                    } else {
                        executable.setPriority(executable.getPriority() + 1);
                        executable.applyBackoffDelay(BASE_DELAY_MS, MAX_DELAY_MS);
                        addPriorityReadyRunnable(executable);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

    }

    @Override
    public void stop() {
        m_lock.lock();
        try {
            if (m_status == STOP_PENDING) {
                return;
            }
            m_status=STOP_PENDING;
            priorityJobPoolExecutor.shutdown();
            m_worker.shutdown();
        } finally {
            m_lock.unlock();
        }
    }

    @Override
    public int getStatus() {
        m_lock.lock();
        try {
            if (m_worker.isShutdown()) {
                m_status=STOPPED;
            }
            return m_status;
        } finally {
            m_lock.unlock();
        }
    }


    @Override
    public String getName() {
        return  priorityJobPoolExecutor.toString();
    }

}
