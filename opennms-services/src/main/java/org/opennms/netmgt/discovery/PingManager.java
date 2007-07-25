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

package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;

import org.apache.log4j.Category;
import org.opennms.core.concurrent.QuantumSemaphore;
import org.opennms.core.fiber.ExtendedStatusFiber;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ping.Reply;
import org.opennms.netmgt.ping.ReplyReceiver;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

final class PingManager implements Runnable, PausableFiber {
    private static final long TID_CONST_KEY = 0x0110deadbeef0000L;

    private Pinger[] m_pingers;

    private QuantumSemaphore m_semaphore;

    private IcmpSocket m_socket;

    private FifoQueue m_replyQ;

    private FifoQueue m_discoveredQ;

    private ReplyReceiver m_icmpReceiver;

    private int m_status;

    private short m_filterId;

    private Thread m_worker;

    static final class Pinger implements Runnable, PausableFiber, ExtendedStatusFiber {
        /**
         * The semaphore that each poller must acquire before they can send a
         * packet
         */
        private final QuantumSemaphore m_semaphore;

        /**
         * The socket used to send datagrams.
         */
        private final IcmpSocket m_socket;

        /**
         * The queue of inbound addresses to poll.
         */
        private final FifoQueue m_addressQ;

        /**
         * The value stored in the icmp identifier field.
         */
        private final short m_icmpId;

        /**
         * The identifier for the fiber. This is stored in the ping packet.
         */
        private final long m_fiberId;

        /**
         * The normal fiber's status.
         */
        private int m_status;

        /**
         * The extended status.
         */
        private int m_xstatus;

        /**
         * The current target address.
         */
        private InetAddress m_target;

        /**
         * True if the <code>signal</code> method is invoked. Used to indicate
         * a responsive address.
         */
        private boolean m_signaled;

        /**
         * The thread that is executing the <code>run</code> method on behalf
         * of the fiber.
         */
        private Thread m_worker;

        /**
         * This is the constant value that represents an idle pinger thread.
         */
        final static int IDLE = 0;

        /**
         * This is the constant value that represents a polling pinger thread.
         */
        final static int POLLING = 1;

        /**
         * <p>
         * This method does the actual polling of the encapsulated address using
         * the timeout and retry values. The polling will continue until either
         * all the retries are exhausted or the pinger is signaled that the
         * address responded.
         * </p>
         * 
         * <p>
         * <strong>NOTICE: The instance lock must be heald prior to calling this
         * method. </strong> This method uses the
         * {@link java.lang.Object#wait wait}method and lock ownership is
         * required to invoke the <code>wait</code> method.
         * </p>
         * 
         * @param addr
         *            The address and information for polling.
         * 
         * @throws java.io.IOException
         *             Thrown if an error occurs sending the ICMP information.
         * @throws java.lang.InterruptedException
         *             Thrown if the thread is interrupted.
         */
        private/* synchronized */boolean poll(IPPollAddress addr) throws IOException, InterruptedException {
            Category log = ThreadCategory.getInstance(getClass());

            for (int tries = 0; !m_signaled && tries <= addr.getRetries(); tries++) {

                // build a packet
                //
                ICMPEchoPacket pingPkt = new ICMPEchoPacket(m_fiberId);
                pingPkt.setIdentity(m_icmpId);
                pingPkt.computeChecksum();

                // convert it to
                byte[] buf = pingPkt.toBytes();
                DatagramPacket sendPkt = new DatagramPacket(buf, buf.length, addr.getAddress(), 0);
                buf = null;
                pingPkt = null;

                // Aquire a right to send
                //
                boolean x = m_semaphore.acquire();

                long expire = System.currentTimeMillis() + addr.getTimeout();
                while (m_status != STOP_PENDING && !m_signaled) {
                    long wtime = expire - System.currentTimeMillis();
                    if (wtime <= 0) {
                        break;
		    }

                    if (log.isDebugEnabled()) {
                        log.debug("poll: try#: " + tries + " - sending ping to address " + addr.getAddress().getHostAddress());
		    }

                    m_socket.send(sendPkt);
                    this.wait(wtime);
                }
            }

            return m_signaled;
        }

        /**
         * Constructs a new instance of a pinging fiber.
         * 
         * @param socket
         *            The ICMP socket
         * @param semaphore
         *            The control semaphore
         * @param addrQ
         *            The input address queue.
         * @param filterId
         *            The ICMP id for filtering datagrams
         * @param tid
         *            The Thread ID stored in the Packet.
         * 
         */
        Pinger(IcmpSocket socket, QuantumSemaphore semaphore, FifoQueue addrQ,
	       short filterId, long tid) {
            m_socket = socket;
            m_semaphore = semaphore;
            m_addressQ = addrQ;
            m_icmpId = filterId;
            m_fiberId = tid;

            m_status = START_PENDING;
            m_xstatus = IDLE;
            m_signaled = false;
            m_target = null;
            m_worker = null;
        }

        /**
         * Starts the current fiber. If the fiber has already been started,
         * regardless of it's current state, then an IllegalStateException is
         * thrown.
         * 
         * @throws java.lang.IllegalStateException
         *             Thrown if the fiber has already been started.
         * 
         */
        public synchronized void start() {
            if (m_worker != null) {
                throw new IllegalStateException("The fiber is running or has already run");
	    }

            m_worker = new Thread(this, getName());
            m_worker.start();
            m_status = STARTING;
        }

        /**
         * Attempts to stop the current polling cycle as quickly as possbile. If
         * the fiber has never been started then an
         * <code>IllegalStateExceptio</code> is generated.
         * 
         * @throws java.lang.IllegalStateException
         *             Thrown if the fiber has never been started.
         */
        public synchronized void stop() {
            if (m_worker == null) {
                throw new IllegalStateException("The fiber has never run");
	    }

            m_status = STOP_PENDING;
            m_worker.interrupt();
            notifyAll();
        }

        /**
         * <p>
         * Pauses the current fiber. This does not take effect immediently, but
         * after the current polling cycle has completed.
         * </p>
         * 
         * <p>
         * To determine if the fiber has finished its current polling cycle
         * check for a status equal to <code>PAUSED</code> and the extended
         * status equal to <code>IDLE</code>.
         * </p>
         * 
         */
        public synchronized void pause() {
            if (m_worker == null || m_worker.isAlive() == false) {
                throw new IllegalStateException("The fiber is not running");
	    }

            m_status = PAUSED;
            notifyAll();
        }

        /**
         * Resumes a currently paused fiber.
         */
        public synchronized void resume() {
            if (m_worker == null || m_worker.isAlive() == false) {
                throw new IllegalStateException("The fiber is not running");
	    }

            m_status = RUNNING;
            notifyAll();
        }

        /**
         * Returns the name of the fiber.
         * 
         * @return The name of the Fiber.
         */
        public String getName() {
            return "PingManager.Pinger-" + (m_fiberId ^ TID_CONST_KEY);
        }

        /**
         * Returns the current status of the pinging thread.
         * 
         * @return The status of the Fiber.
         */
        public synchronized int getStatus() {
            if (m_worker != null && !m_worker.isAlive()) {
                m_status = STOPPED;
	    }

            return m_status;
        }

        /**
         * Returns the extends status of the thread. This will always be one of
         * two values: POLLING or IDLE.
         * 
         * @return The current polling status.
         * 
         */
        public synchronized int getExtendedStatus() {
            return m_xstatus;
        }

        /**
         * <p>
         * The main method that does the work for the pinging thread. This
         * method reads {@link IPPollAddress IPPollAddress}instances from the
         * input queue and then polls the target using the information.
         * </p>
         * 
         * <p>
         * While a poll is in process the extended status of the fiber will
         * return {@link #POLLING POLLING}otherwise it should be
         * {@link #IDLE IDLE}.
         * </p>
         * 
         * <p>
         * If an error occurs then the thread will exit and set the status to
         * {@link org.opennms.core.fiber.Fiber#STOPPED STOPPED}.
         * </p>
         * 
         */
        public void run() {
            Category log = ThreadCategory.getInstance(getClass());
            synchronized (this) {
                m_status = RUNNING;
                m_xstatus = IDLE;
            }

            try {
                for (;;) {
                    synchronized (this) {
                        while (m_status == PAUSED) {
                            if (log.isDebugEnabled()) {
                                log.debug("run: fiber paused, waiting");
			    }

                            wait();

                            if (log.isDebugEnabled()) {
                                log.debug("run: fiber wait is over");
			    }

                            continue;
                        }

                        if (m_status != RUNNING) {
                            break;
			}
                    }

                    IPPollAddress addr = (IPPollAddress) m_addressQ.remove();
                    boolean isKnown = false;
                    if (addr != null) {
                        isKnown = DiscoveredIPMgr.isDiscoveredOrExcluded(addr.getAddress());
                    }
                    if (addr != null && !isKnown) {
                        synchronized (this) {
                            m_xstatus = POLLING;
                            m_target = addr.getAddress();
                            m_signaled = false;

                            if (log.isDebugEnabled()) {
                                log.debug("run: starting poll");
			    }

                            // MUST HAVE LOCK TO CALL POLL!
                            //
                            try {
                                poll(addr);

                                if (log.isDebugEnabled()) {
                                    log.debug("run: poll completed");
				}
                            } catch (NoRouteToHostException ex) {
                                log.warn("Check discovery configuration, cannot poll broadcast addresses (addr = " + m_target + ")");
                                if (log.isDebugEnabled()) {
                                    log.debug("run: poll cancelled, invalid address " + m_target, ex);
				}
                            }

                            m_target = null;
                            m_signaled = false;
                            m_xstatus = IDLE;
                        }
                    }
                }
            } catch (Throwable t) {
                if (m_status != STOP_PENDING) {
                    log.fatal("run: Error in ping thread, exiting", t);
		} else {
                    log.info("run: pinging thread exiting", t);
		}
                return;
            } finally {
                m_status = STOPPED;
                if (log.isDebugEnabled()) {
                    log.debug("run: status set to stopped");
		}
            }
        }

        /**
         * Returns true if this thread is currently polling the passed IP
         * Address.
         * 
         * @param addr
         *            The address to check.
         * 
         * @return True if the thread is currently targeting the passed address.
         */
        public synchronized boolean isPinging(InetAddress addr) {
            if (addr == null) {
                return false;
	    }
            return addr.equals(m_target);
        }

        /**
         * Signals the instance that it's current target has responded to an
         * echo check.
         */
        public synchronized void signal() {
            m_signaled = true;
            notifyAll();
        }

        /**
         * Returns the identifier that is placed in the ping
         * {@link org.opennms.protocols.icmp.ICMPEchoPacket packet}.
         * 
         * @return The thread id for the packet.
         */
        public long getId() {
            return m_fiberId;
        }

    } // end class Pinger

    /**
     * Constructs a new instance of a ping manager. Once the manager is
     * constructed it must be started with a call to the method
     * <code>start</code>.
     * 
     * @param addressQ
     *            The input address queue.
     * @param discoveredQ
     *            The output queue of responding addresses.
     * @param threads
     *            The number of pinging fibers to create.
     * @param pktsPerSecond
     *            The maximum packets per second that can be generated.
     * 
     * @throws java.io.IOException
     *             Thrown if the ICMP socket cannot be constructed.
     * 
     */
    protected PingManager(FifoQueue addressQ, FifoQueue discoveredQ,
			  short filterId, int threads, int pktsPerSecond)
	throws IOException {
        Category log = ThreadCategory.getInstance(getClass());

        if (threads < 1) {
	    log.error("The number of PingManager threads must be greater than zero");
            throw new IllegalArgumentException("The number of threads must be greater than zero");
	}

        if (pktsPerSecond < 1) {
	    log.error("The number of PingManager packets per second must be greater than zero");
            throw new IllegalArgumentException("The number of packets per second must be greater than zero");
	}

        // Save the filter id for thread nameing
        //
        m_filterId = filterId;

        // Open a new raw ICMP socket
        //
	try {
	    m_socket = new IcmpSocket();
	} catch (NoClassDefFoundError e) {
	    log.error("NoClassDefFoundError while creating an IcmpSocket.  " +
		      "Most likely failed to load libjicmp.so.", e);
	    throw e;
	} catch (Throwable t) {
	    log.error("Throwable received while creating an IcmpSocket", t);
	    throw new UndeclaredThrowableException(t, t.getMessage());
	}

        // Create the timed semaphore
        //
        m_semaphore = new QuantumSemaphore((long) pktsPerSecond, 1000L);

        // Allocate the queue for reading back requests
        //
        m_replyQ = new FifoQueueImpl();

        // Save the output queue
        //
        m_discoveredQ = discoveredQ;

        // Allocate the pingers
        //
        m_pingers = new Pinger[threads];
        for (int x = 0; x < m_pingers.length; x++) {
            m_pingers[x] = new Pinger(m_socket, m_semaphore, addressQ, filterId, TID_CONST_KEY | (long) x);
        }

        m_icmpReceiver = new ReplyReceiver(m_socket, m_replyQ, filterId);
    }

    /**
     * Starts the ping manager.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber has previously been started.
     * 
     */
    public synchronized void start() {
        if (m_worker != null) {
            throw new IllegalStateException("The fiber is already running or has already run");
	}

        m_status = STARTING;
        m_worker = new Thread(this, getName());
        m_worker.setDaemon(true);
        m_worker.start();

        m_icmpReceiver.start();
        for (int x = 0; x < m_pingers.length; x++) {
            m_pingers[x].start();
	}
    }

    /**
     * Stops the currently running ping manager. If the manager has never been
     * run then an exception is generated.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber has never been started.
     * 
     */
    public synchronized void stop() {
        // stop the sub processes
        //
        for (int x = 0; x < m_pingers.length; x++) {
            try {
                m_pingers[x].stop();
            } catch (Exception e) {
            }
        }
        try {
            m_icmpReceiver.stop();
        } catch (Exception e) {
        }

        // stop the worker
        //
        if (m_worker == null) {
            throw new IllegalStateException("The fiber was never started");
	}

        if (m_worker.isAlive()) {
            if (m_status != STOPPED) {
                m_status = STOP_PENDING;
	    }

            m_worker.interrupt();
        } else if (m_status != STOPPED) {
            m_status = STOPPED;
	}

        m_socket.close();
    }

    /**
     * Pauses the currently running manager. If the manager is already paused or
     * is stopping, then the call is silently ignored. If the fiber is not
     * currently running, either due to it being stopped or having never been
     * started, then an exception is generated.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is not running.
     */
    public synchronized void pause() {
        if (m_worker == null || m_worker.isAlive() == false) {
            throw new IllegalStateException("The fiber is not running");
	}

        if (m_status == RUNNING) {
            for (int x = 0; x < m_pingers.length; x++) {
                m_pingers[x].pause();
	    }

            m_icmpReceiver.pause();
            m_status = PAUSED;
        }
    }

    /**
     * Resumes a currently paused manager. If the manager is not running, either
     * by never being started or it has stopped then an exception is generated.
     * If the manager is not currently paused then the manager silently discards
     * the request.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the manager is not currently active.
     * 
     */
    public synchronized void resume() {
        if (m_worker == null || m_worker.isAlive() == false) {
            throw new IllegalStateException("The fiber is not running");
	}

        if (m_status == PAUSED) {
            m_icmpReceiver.resume();
            for (int x = 0; x < m_pingers.length; x++) {
                m_pingers[x].resume();
	    }
            m_status = RUNNING;
        }
        notifyAll();
    }

    /**
     * Returns the current manager status.
     * 
     * @return The manager's status
     * 
     */
    public synchronized int getStatus() {
        if (m_worker != null && !m_worker.isAlive()) {
            m_status = STOPPED;
	}

        return m_status;
    }

    /**
     * Returns the name of this fiber.
     * 
     * @return The name of the fiber.
     */
    public String getName() {
        return "PingManager-" + m_filterId;
    }

    /**
     * This is the main method that receives the ICMP replies from the ICMP
     * recever and processes them. If the replies match the expected format then
     * each reply is added to the output queue for discovered elements.
     * 
     */
    public void run() {
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Starting the PingManager");

        synchronized (this) {
            m_status = RUNNING;
        }

        try {
            for (;;) {
                try {
                    // Check our status
                    //
                    synchronized (this) {
                        if (m_status == PAUSED) {
                            wait();
                            continue;
                        } else if (m_status == STOP_PENDING) {
                            break;
                        }
                    }

                    // Read the next reply
                    //
                    Reply r = (Reply) m_replyQ.remove();
                    if (r != null && r.isEchoReply() && r.getIdentity() == m_filterId && (r.getPacket().getTID() & TID_CONST_KEY) == TID_CONST_KEY) {
                        // Check to make sure it's being polled
                        // Add if to the discovered queue if necessary
                        //
                        boolean doAdd = false;
                        int ndx = (int) (r.getPacket().getTID() & (~TID_CONST_KEY));
                        if (0 <= ndx && ndx < m_pingers.length) {
                            synchronized (m_pingers[ndx]) {
                                if (m_pingers[ndx].isPinging(r.getAddress())) {
                                    m_pingers[ndx].signal();
                                    doAdd = true;
                                }
                            }
                        }

                        if (doAdd) {
                            m_discoveredQ.add(r);
			}
                    }
                } catch (Exception e) {
                    log.error("Unexpected Exception occurred in the PingManager.", e);
                }

            } // end infinate for loop
        } catch (Exception e) {
            log.error("Unexpected Exception occurred in the PingManager.", e);
        } finally {
            synchronized (this) {
                m_status = STOPPED;
            }
        }
    }

}
