//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.notifd;

import java.util.List;
import java.util.SortedMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
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
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 * @version $Id: $
 */
public class DefaultQueueHandler implements NotifdQueueHandler {
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
    public void setQueueID(String queueID) {
        m_queueID = queueID;
    }

    /** {@inheritDoc} */
    public synchronized void setNoticeQueue(NoticeQueue noticeQueue) {
        m_noticeQueue = noticeQueue;
    }

    /** {@inheritDoc} */
    public void setInterval(String interval) {
        m_interval = TimeConverter.convertToMillis(interval);
    }

    /**
     * The main worker of the fiber. This method is executed by the encapsulated
     * thread to read commands from the execution queue and to execute those
     * commands. If the thread is interrupted or the status changes to
     * <code>STOP_PENDING</code> then the method will return as quickly as
     * possible.
     */
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
                    } catch (InterruptedException ex) {
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
                } catch (InterruptedException ex) {
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
    public void processQueue() {
        Category log = ThreadCategory.getInstance(getClass());

        if (m_noticeQueue != null) {
            synchronized(m_noticeQueue) {
                try {
                    Long now = new Long(System.currentTimeMillis());
                    SortedMap<Long, List<NotificationTask>> readyNotices = m_noticeQueue.headMap(now);
        
                    for (List<NotificationTask> list : readyNotices.values()) {
                        for (NotificationTask task : list) {
                            startTask(task);
                        }
                    }
                    readyNotices.clear();
        
                    log.debug("current state of tree: ");
                    log.debug("\n" + m_noticeQueue);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    
                }
            }
        }
    }

	private void startTask(NotificationTask task) {
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
    public synchronized void start() {
        m_status = STARTING;

        Thread thread = new Thread(this, m_queueID);
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
    public synchronized int getStatus() {
        return m_status;
    }
}
