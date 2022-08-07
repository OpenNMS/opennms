/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd.common;

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

    public int getPriorityQueueSize() {
        synchronized (priorityQueue) {
            return priorityQueue.size();
        }
    }

    public void addPriorityReadyRunnable(PriorityReadyRunnable job) {
        synchronized (priorityQueue) {
            priorityQueue.add(job);
            LOG.info("addPriorityReadyRunnable: Added {}, total in queue: {}", job.getInfo(), priorityQueue.size());
        }
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
    public synchronized void start() {
        m_worker.execute(() -> {
            synchronized (this) {
                m_status = RUNNING;
            }
            LOG.info("run: Priority Executor {} running", m_parent);

            while (true) {
                if (m_status == STOP_PENDING) {
                    LOG.debug("run: status = {}, time to exit", m_status);
                    break;
                }
                while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                    if (m_status == PAUSE_PENDING) {
                        LOG.debug("run: pausing.");
                    }
                    m_status = PAUSED;
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (InterruptedException ex) {
                        // exit
                        break;
                    }
                }

                // if resume pending then change to running

                if (m_status == RESUME_PENDING) {
                    LOG.debug("run: resuming.");

                    m_status = RUNNING;
                }
                try {
                        PriorityReadyRunnable executable = priorityQueue.take();
                        if (executable.isReady()) {
                            priorityJobPoolExecutor.execute(executable);
                        } else {
                            executable.setPriority(executable.getPriority() + 1);
                            addPriorityReadyRunnable(executable);
                        }
                        if (priorityJobPoolExecutor instanceof ThreadPoolExecutor) {
                            ThreadPoolExecutor e = (ThreadPoolExecutor) priorityJobPoolExecutor;
                            String ratio = String.format("%.3f",e.getTaskCount() > 0 ? BigDecimal.valueOf(e.getCompletedTaskCount()).divide(BigDecimal.valueOf(e.getTaskCount()), 2, RoundingMode.DOWN) : 0) ;
                            LOG.debug("thread pool statistics: activeCount={}, taskCount={}, completedTaskCount={}, completedRatio={}, poolSize={}",
                                    e.getActiveCount(), e.getTaskCount(), e.getCompletedTaskCount(), ratio, e.getPoolSize());
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
