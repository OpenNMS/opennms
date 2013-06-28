/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd;

import java.util.List;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.TimeConverter;

/**
 * This class is used as a thread for executing notices for events that are
 * discovered by the notice daemon. The notices are read from an scheduler queue
 * and the processes are created by the fiber. Each created process is added to
 * garbage collection list that is periodically polled and culled based upon the
 * status of the process or how long the process is run. If the process has run
 * long than allocated it is terminated during collection.
 *
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 */
public class DefaultQueueHandler implements NotifdQueueHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultQueueHandler.class);
    /**
     * The input queue of runnable commands.
     */
    private NoticeQueue m_noticeQueue;

    /**
     * The name of this Fiber
     */
    private String m_queueID;

    /**
     * How long to sleep between processing more notices
     */
    private long m_interval;

    /**
     * The status of this fiber.
     */
    private int m_status;

    /**
     * <p>Constructor for DefaultQueueHandler.</p>
     */
    public DefaultQueueHandler() {
        m_status = START_PENDING;
    }

    /** {@inheritDoc} */
    @Override
    public void setQueueID(final String queueID) {
        m_queueID = queueID;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setNoticeQueue(final NoticeQueue noticeQueue) {
        m_noticeQueue = noticeQueue;
    }

    /** {@inheritDoc} */
    @Override
    public void setInterval(final String interval) {
        m_interval = TimeConverter.convertToMillis(interval);
    }

    /**
     * The main worker of the fiber. This method is executed by the encapsulated
     * thread to read commands from the execution queue and to execute those
     * commands. If the thread is interrupted or the status changes to
     * <code>STOP_PENDING</code> then the method will return as quickly as
     * possible.
     */
    @Override
    public void run() {
        synchronized (this) {
            m_status = RUNNING;
        }

        for (;;) {
            synchronized (this) {
                // if stopped or stop pending then break out
                if (m_status == STOP_PENDING || m_status == STOPPED) {
                    break;
                }

                // if paused or pause pending then block
                while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                    m_status = PAUSED;
                    try {
                        wait();
                    } catch (final InterruptedException ex) {
                        // exit
                        break;
                    }
                }

                // if resume pending then change to running
                if (m_status == RESUME_PENDING) {
                    m_status = RUNNING;
                }
            }

            processQueue();

            synchronized (this) {
                // wait for the next iteration
                try {
                    wait(m_interval);
                } catch (final InterruptedException ex) {
                    // exit
                    break;
                }
            }

        } // end infinite loop

        synchronized (this) {
            m_status = STOPPED;
        }

    } // end run

    /**
     * <p>processQueue</p>
     */
    @Override
    public void processQueue() {
        if (m_noticeQueue != null) {
            synchronized(m_noticeQueue) {
                try {
                	final Long now = System.currentTimeMillis();
                	final SortedMap<Long, List<NotificationTask>> readyNotices = m_noticeQueue.headMap(now);
        
                    for (final List<NotificationTask> list : readyNotices.values()) {
                        for (final NotificationTask task : list) {
                            startTask(task);
                        }
                    }
                    readyNotices.clear();
        
                    if (m_noticeQueue != null && m_noticeQueue.size() > 0) {
			LOG.debug("current state of tree: {}", m_noticeQueue);
                    }
                } catch (final Throwable e) {
                    LOG.error("failed to start notification task", e);
                    
                }
            }
        }
    }

	private void startTask(final NotificationTask task) {
		if (!task.isStarted())
			task.start();
	}

    /**
     * Starts the fiber. If the fiber has already been run or is currently
     * running then an exception is generated. The status of the fiber is
     * updated to <code>STARTING</code> and will transition to <code>
     * RUNNING</code>
     * when the fiber finishes initializing and begins processing the
     * encapsulated queue.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    @Override
    public synchronized void start() {
        m_status = STARTING;

        final Thread thread = new Thread(this, this.getClass().getSimpleName() + "-" + m_queueID);
        thread.start();
    }

    /**
     * Stops a currently running fiber. If the fiber has already been stopped
     * then the command is silently ignored. If the fiber was never started then
     * an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber was never started.
     */
    @Override
    public synchronized void stop() {
        if (m_status != STOPPED)
            m_status = STOP_PENDING;

        notifyAll();
    }

    /**
     * Pauses a currently running fiber. If the fiber was not in a running or
     * resuming state then the command is silently discarded. If the fiber is
     * not running or has terminated then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    @Override
    public synchronized void pause() {
        if (m_status == RUNNING || m_status == RESUME_PENDING) {
            m_status = PAUSE_PENDING;
            notifyAll();
        }
    }

    /**
     * Resumes the fiber if it is paused. If the fiber was not in a paused or
     * pause pending state then the request is discarded. If the fiber has not
     * been started or has already stopped then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    @Override
    public synchronized void resume() {
        if (m_status == PAUSED || m_status == PAUSE_PENDING) {
            m_status = RESUME_PENDING;
            notifyAll();
        }
    }

    /**
     * Returns the name of this fiber.
     *
     * @return The name of the fiber.
     */
    @Override
    public String getName() {
        return m_queueID;
    }

    /**
     * Returns the current status of the pausable fiber.
     *
     * @return The current status of the fiber.
     * @see org.opennms.core.fiber.PausableFiber
     * @see org.opennms.core.fiber.Fiber
     */
    @Override
    public synchronized int getStatus() {
        return m_status;
    }
}
