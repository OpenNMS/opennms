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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.vulnscand;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;

/**
 * This class implements a simple scheduler to ensure that Vulnscand rescans
 * occurs at the expected intervals.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
final class Scheduler implements Runnable, PausableFiber {
    /**
     * The prefix for the fiber name.
     */
    private static final String FIBER_NAME = "Vulnscand Scheduler";

    /**
     * The status for this fiber.
     */
    private int m_status;

    /**
     * The worker thread that executes this instance.
     */
    private Thread m_worker;

    /**
     * List of NessusScanConfiguration objects representing the IP addresses
     * that will be scheduled.
     */
    private LinkedHashMap<Object, ScheduleTrigger> m_triggers;

    /**
     * The configured initial sleep (in milliseconds) prior to scheduling
     * rescans
     */
    private long m_initialSleep;

    /**
     * The rescan queue where new NessusScan objects are enqueued for execution.
     */
    private FifoQueue m_scheduledScanQ;

    /**
     * Constructs a new instance of the scheduler.
     * @param vulnscand TODO
     * 
     */
    Scheduler(FifoQueue rescanQ) throws SQLException {
        m_scheduledScanQ = rescanQ;

        m_status = START_PENDING;
        m_worker = null;

        m_triggers = new LinkedHashMap<Object, ScheduleTrigger>();

    }

	void schedule(Object key, ScheduleTrigger trigger) {
		synchronized (getSchedulerLock()) {
			m_triggers.put(key, trigger);
		}
	}
	
	private void unschedule(Object key) {
        synchronized (getSchedulerLock()) {
        	ScheduleTrigger addressInfo = m_triggers.remove(key);
        	log().debug("unscheduleAddress: removing node " + addressInfo + " from the scheduler.");
        }
	}

	ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

    /**
     * Removes the specified node from the known node list.
     * 
     * @param address
     *            Address of interface to be removed.
     */
    void unscheduleAddress(InetAddress address) {
    	unschedule(address);
    }

    public static InetAddress toInetAddress(long address) throws UnknownHostException {
        StringBuffer buf = new StringBuffer();
        buf.append((int) ((address >>> 24) & 0xff)).append('.');
        buf.append((int) ((address >>> 16) & 0xff)).append('.');
        buf.append((int) ((address >>> 8) & 0xff)).append('.');
        buf.append((int) (address & 0xff));
        return InetAddress.getByName(buf.toString());
    }

    /**
     * Starts the fiber.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is already running.
     */
    public synchronized void start() {
        if (m_worker != null)
            throw new IllegalStateException("The fiber has already run or is running");

        m_worker = new Thread(this, getName());
        m_worker.start();
        m_status = STARTING;

        log().debug("Scheduler.start: scheduler started");
    }

    /**
     * Stops the fiber. If the fiber has never been run then an exception is
     * generated.
     * 
     * @throws java.lang.IllegalStateException
     *             Throws if the fiber has never been started.
     */
    public synchronized void stop() {
        if (m_worker == null)
            throw new IllegalStateException("The fiber has never been started");

        m_status = STOP_PENDING;
        m_worker.interrupt();

        log().debug("Scheduler.stop: scheduler stopped");
    }

    /**
     * Pauses the scheduler if it is current running. If the fiber has not been
     * run or has already stopped then an exception is generated.
     * 
     * @throws java.lang.IllegalStateException
     *             Throws if the operation could not be completed due to the
     *             fiber's state.
     */
    public synchronized void pause() {
        if (m_worker == null)
            throw new IllegalStateException("The fiber has never been started");

        if (m_status == STOPPED || m_status == STOP_PENDING)
            throw new IllegalStateException("The fiber is not running or a stop is pending");

        if (m_status == PAUSED)
            return;

        m_status = PAUSE_PENDING;
        notifyAll();
    }

    /**
     * Resumes the scheduler if it has been paused. If the fiber has not been
     * run or has already stopped then an exception is generated.
     * 
     * @throws java.lang.IllegalStateException
     *             Throws if the operation could not be completed due to the
     *             fiber's state.
     */
    public synchronized void resume() {
        if (m_worker == null)
            throw new IllegalStateException("The fiber has never been started");

        if (m_status == STOPPED || m_status == STOP_PENDING)
            throw new IllegalStateException("The fiber is not running or a stop is pending");

        if (m_status == RUNNING)
            return;

        m_status = RESUME_PENDING;
        notifyAll();
    }

    /**
     * Returns the current of this fiber.
     * 
     * @return The current status.
     */
    public synchronized int getStatus() {
        if (m_worker != null && m_worker.isAlive() == false)
            m_status = STOPPED;
        return m_status;
    }

    /**
     * Returns the name of this fiber.
     * 
     */
    public String getName() {
        return FIBER_NAME;
    }

    /**
     * The main method of the scheduler. This method is responsible for checking
     * the runnable queues for ready objects and then enqueuing them into the
     * thread pool for execution.
     * 
     */
    public void run() {
        synchronized (this) {
            m_status = RUNNING;
        }
        log().debug("Scheduler.run: scheduler running");

        // Loop until a fatal exception occurs or until
        // the thread is interrupted.
        //
        boolean firstPass = true;
        while (true) {
            // Status check
            //
            synchronized (this) {
                if (m_status != RUNNING && m_status != PAUSED && m_status != PAUSE_PENDING && m_status != RESUME_PENDING) {
                    log().debug("Scheduler.run: status = " + m_status + ", time to exit");
                    break;
                }
            }

            // If this is the first pass we want to pause momentarily
            // This allows the rest of the background processes to come
            // up and stabilize before we start generating events from rescans.
            //
            if (firstPass) {
                firstPass = false;
                synchronized (this) {
                    try {
                        log().debug("Scheduler.run: initial sleep configured for " + getInitialSleep() + "ms...sleeping...");
                        wait(getInitialSleep());
                    } catch (InterruptedException ex) {
                        log().debug("Scheduler.run: interrupted exception during initial sleep...exiting.");
                        break; // exit for loop
                    }
                }
            }

            // iterate over the known node list, add any
            // nodes ready for rescan to the rescan queue
            // for processing.
            //
            int added = 0;

            synchronized (getSchedulerLock()) {

                log().debug("Scheduler.run: iterating over known nodes list to schedule...");
                for (ScheduleTrigger addressInfo : getTriggerList()) {
					
                    log().debug("Scheduler.run: working on "  + addressInfo );
                    // Don't schedule if already scheduled
                    if (addressInfo.isScheduled())
                        continue;

                    // Don't schedule if its not time for rescan yet
                    if (!addressInfo.isTimeForRescan())
                        continue;

                    // Must be time for a rescan!
                    //
                    try {
                        addressInfo.setScheduled(true); // Mark node as
                                                        // scheduled

                        // Create a new NessusScan object
                        // and add it to the rescan queue for execution
                        //
                        log().debug("Scheduler.run: adding node " + addressInfo + " to the rescan queue.");

                        m_scheduledScanQ.add(addressInfo.getJob());
                        added++;
                    } catch (InterruptedException ex) {
                        log().info("Scheduler.schedule: failed to add new node to rescan queue", ex);
                        throw new UndeclaredThrowableException(ex);
                    } catch (FifoQueueException ex) {
                        log().info("Scheduler.schedule: failed to add new node to rescan queue", ex);
                        throw new UndeclaredThrowableException(ex);
                    }
                }
            }

            // Wait for 60 seconds if there were no nodes
            // added to the rescan queue during this loop,
            // otherwise just start over.
            //
            synchronized (this) {
                if (added == 0) {
                    try {
                        wait(60000);
                    } catch (InterruptedException ex) {
                        break; // exit for loop
                    }
                }
            }
        } // end while(true)

        log().debug("Scheduler.run: scheduler exiting, state = STOPPED");
        synchronized (this) {
            m_status = STOPPED;
        }
    } // end run

	private Object getSchedulerLock() {
		return m_triggers;
	}
	
	private Collection<ScheduleTrigger> getTriggerList() {
		return m_triggers.values();
	}

	void setInitialSleep(long initialSleep) {
		m_initialSleep = initialSleep;
	}

	long getInitialSleep() {
		return m_initialSleep;
	}
}
