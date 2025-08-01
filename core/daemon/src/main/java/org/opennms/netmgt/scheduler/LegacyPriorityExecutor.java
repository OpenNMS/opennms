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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.fiber.PausableFiber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyPriorityExecutor implements PausableFiber {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyPriorityExecutor.class);

    private final String m_parent;
    private final ExecutorService priorityJobPoolExecutor;
    private final PriorityBlockingQueue<PriorityReadyRunnable> priorityQueue;
    private final ExecutorService m_worker = Executors.newSingleThreadExecutor();
    private volatile int m_status;
    public LegacyPriorityExecutor(String parent, Integer poolSize, Integer queueSize) {
        m_parent=parent;
        m_status = START_PENDING;
        priorityJobPoolExecutor = Executors.newFixedThreadPool(poolSize, new LogPreservingThreadFactory(parent, poolSize));
        priorityQueue = new PriorityBlockingQueue<>(
                queueSize,
                Comparator.comparing(PriorityReadyRunnable::getPriority));
    }

    public synchronized void addPriorityReadyRunnable(PriorityReadyRunnable job) {
        priorityQueue.add(job);
        LOG.info("addPriorityReadyRunnable: Added {}, total in queue: {}", job.getInfo(), priorityQueue.size());
    }

    @Override
    public synchronized void pause() {
        if (m_status == PAUSED) {
            return;
        }
        m_status=PAUSE_PENDING;
        notifyAll();
    }

    @Override
    public synchronized void resume() {
        if (m_status == RUNNING) {
            return;
        }
        m_status=RESUME_PENDING;
        notifyAll();
    }

    @Override
    public void start() {
        m_worker.execute(() -> {
            synchronized (this) {
                m_status = RUNNING;
            }
            LOG.info("run: Priority Executor {} running", m_parent);

            while (true) {
                try {
                    LOG.debug("Taking");
                    PriorityReadyRunnable executable = priorityQueue.take();
                    LOG.debug("Taked: {}", executable.getInfo());
                    while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                        if (m_status == PAUSE_PENDING) {
                            LOG.info("run: pausing.");
                        }
                        m_status = PAUSED;
                        synchronized (this) {
                                wait();
                        }
                    }
                    if (m_status == STOP_PENDING) {
                        LOG.info("run: status = {}, time to exit", m_status);
                        break;
                    }
                    if (m_status == RESUME_PENDING) {
                        LOG.info("run: resuming.");
                        m_status = RUNNING;
                    }
                    if (executable.isReady()) {
                        priorityJobPoolExecutor.execute(executable);
                        LOG.debug("run: added to priority job executor thread pool: {}", executable.getInfo());
                        if (priorityJobPoolExecutor instanceof ThreadPoolExecutor) {
                            ThreadPoolExecutor e = (ThreadPoolExecutor) priorityJobPoolExecutor;
                            String ratio = String.format("%.3f",e.getTaskCount() > 0 ? BigDecimal.valueOf(e.getCompletedTaskCount()).divide(BigDecimal.valueOf(e.getTaskCount()), 2, RoundingMode.DOWN) : 0) ;
                            LOG.debug("thread pool statistics: activeCount={}, taskCount={}, completedTaskCount={}, completedRatio={}, poolSize={}",
                                    e.getActiveCount(), e.getTaskCount(), e.getCompletedTaskCount(), ratio, e.getPoolSize());
                        }
                    } else {
                        executable.setPriority(executable.getPriority() + 1);
                        addPriorityReadyRunnable(executable);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

    }

    @Override
    public synchronized void stop() {
        if (m_status == STOP_PENDING) {
            return;
        }
        m_status=STOP_PENDING;
        priorityJobPoolExecutor.shutdown();
        m_worker.shutdown();
    }

    @Override
    public synchronized int getStatus() {
        if (m_worker.isShutdown()) {
            m_status=STOPPED;
        }
        return m_status;
    }


    @Override
    public String getName() {
        return  priorityJobPoolExecutor.toString();
    }

}
