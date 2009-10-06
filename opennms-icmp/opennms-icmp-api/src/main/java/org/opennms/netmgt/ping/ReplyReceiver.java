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
// 2007 Jun 23: Use Java 5 generics, format code. - dj@opennms.org
// 2003 Mar 05: Cleaned up some ICMP related code.
// 2002 Nov 13: Added response time stats for ICMP requests.
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
// ReplyReceiver.java,v 1.1.1.1 2001/11/11 17:34:37 ben Exp
//

package org.opennms.netmgt.ping;

import java.net.DatagramPacket;

import org.apache.log4j.Category;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

/**
 * <p>
 * This class is designed to be a single point of reciept for all ICMP messages
 * received by an {@link org.opennms.protocols.icmp.IcmpSocketIcmpSocket}
 * instance. The class implements the
 * {@link org.opennms.core.fiber.PausableFiber PausableFiber}interface as a
 * means to control the operation of the receiver.
 * </p>
 * 
 * <p>
 * Once the receiver is started it will process all recieved datagrams and
 * filter them based upon their ICMP code and the filter identifier used to
 * construct the reciever. All ICMP messages, except for Echo Replies, are
 * discared by the reciever. In addition, only those echo replies that have
 * their identifier set to the passed filter identifier are also discarded.
 * </p>
 * 
 * <p>
 * Received datagrams that pass the requirement of the receiver are added to the
 * reply queue for processing by the application. Only instances of the
 * {@link PingReply Reply}class are added to the queue for processing.
 * </p>
 * 
 * @author <A HREF="sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public final class ReplyReceiver implements PausableFiber, Runnable {
    /**
     * The name of this instance, always starts the same and the filterID is
     * appended to the name.
     */
    private static final String NAME = "ICMPReceiver";

    /**
     * The queue to write the received replies
     */
    private FifoQueue<PingReply> m_replyQ;

    /**
     * The connection to the icmp daemon.
     */
    private IcmpSocket m_portal;

    /**
     * The filter to look for
     */
    private short m_filterID;

    /**
     * The paused flag
     */
    private volatile boolean m_paused;

    /**
     * The name of this instance.
     */
    private String m_name;

    /**
     * The underlying thread doing the work.
     */
    private Thread m_worker;

    /**
     * The thread's status
     */
    private int m_status;

    /**
     * The default constructor is marked private to prevent it's used. The
     * constructor always throws an UnsupportedOperationException.
     * 
     * @exception java.lang.UnsupportedOperationException
     *                Always thrown.
     */
    @SuppressWarnings("unused")
    private ReplyReceiver() throws java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("invalid constructor invocation");
    }

    /**
     * <p>
     * Processes the received datagram and adds a new {@link PingReply Reply}
     * instance to the reply queue. The recieved packet must pass the following
     * criteria:
     * </p>
     * 
     * <ul>
     * <li>ICMP Type == Echo Reply</li>
     * <li>ICMP Identity == Filter ID</li>
     * <li>ICMP Length =={@link ICMPEchoPacket#getNetworkSize Packet.getNetworkSize()}
     * </li>
     * </ul>
     * 
     * @param pkt
     *            The datagram to process.
     * 
     * @throws java.lang.InterruptedException
     *             Thrown if the thread is interrupted.
     * @throws org.opennms.core.fiber.FifoQueueException
     *             Thrown if a queue exception occurs adding a new reply.
     * 
     */
    protected void process(DatagramPacket pkt) throws InterruptedException, FifoQueueException {
        boolean doIt = false;
        synchronized (this) {
            doIt = m_paused;
        }

        if (!doIt) {
            PingReply reply = null;
            try {
                reply = PingReply.create(pkt); // create a reply
            } catch (IllegalArgumentException iaE) {
                // Throw by Reply.create if the packet
                // is not of type Packet
                //
                // Discard
                return;
            } catch (IndexOutOfBoundsException iooB) {
                // Throw by Reply.create if the packet
                // is not the correct length
                //
                // Discard
                return;
            }

            // Test the match criteria
            if (reply.isEchoReply() && reply.getIdentity() == m_filterID) {
                m_replyQ.add(reply);
                // The following is a useless log that really, really, messes up capsd.log. Commenting it out.
                //    if (log().isDebugEnabled()) {
                //        log().debug("process: received matching echo reply from host " + pkt.getAddress().getHostAddress());
                //    }
            }
        }
    }

    /**
     * <p>
     * Constructs a ping reciever thread that reads datagrams from the
     * connection and adds them to the queue. As each datagram is received and
     * processed by the receiver, replies matching the criteria are added to the
     * queue. Each reply must be of type ICMP Echo Reply, its identity must
     * match the filterID, and its length must be equal to the
     * {@link ICMPEchoPacket ping packet's}length.
     * </p>
     * 
     * @param portal
     *            The ICMP socket
     * @param replyQ
     *            The reply queue for matching messages.
     * @param filterID
     *            The ICMP Identity for matching.
     * 
     */
    public ReplyReceiver(IcmpSocket portal, FifoQueue<PingReply> replyQ, short filterID) {
        m_portal = portal;
        m_replyQ = replyQ;
        m_filterID = filterID;
        m_paused = false;
        m_name = NAME + (filterID < 0 ? filterID + 0x10000 : filterID);
        m_worker = null;
        m_status = START_PENDING;
    }

    /**
     * Starts the ICMP receiver. Once started the receiver reads new messages
     * from the ICMP socket and processes them. Packets that match the proper
     * criteria are added to the queue. If the receiver is already started then
     * an exception is thrown.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the receiver has already been started.
     * 
     */
    public final synchronized void start() {
        if (m_worker != null) {
            throw new IllegalStateException("The Fiber is already running or has run");
        }

        m_status = STARTING;
        m_worker = new Thread(this, m_name);
        m_worker.setDaemon(true);
        m_worker.start();
    }

    /**
     * Stops the current receiver. If the receiver was never started then an
     * exception is thrown.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the receiver was never started.
     * 
     */
    public final synchronized void stop() {
        if (m_worker == null) {
            throw new IllegalStateException("The Fiber has not been started");
        }

        if (m_worker.isAlive()) {
            if (m_status != STOPPED) {
                m_status = STOP_PENDING;
            }
            m_worker.interrupt();
        } else {
            m_status = STOPPED;
        }
    }

    /**
     * Pauses the reciever. While the receiver is pauses the it still reads and
     * processes new ICMP datagrams. However, all datagrams are discarded while
     * in a paused state regardless of matching criteria. Messages are still
     * read since the operating system will continue to deliver them.
     * 
     */
    public final synchronized void pause() {
        if (m_worker == null || !m_worker.isAlive()) {
            throw new IllegalStateException("The fiber is not running");
        }
        m_paused = true;
    }

    /**
     * Resumes the recipt and processing of ICMP messages.
     * 
     */
    public final synchronized void resume() {
        if (m_worker == null || !m_worker.isAlive()) {
            throw new IllegalStateException("The fiber is not running");
        }
        m_paused = false;
    }

    /**
     * Returns the name of this fiber.
     * 
     * @return The fiber's name.
     */
    public final String getName() {
        return m_name;
    }

    /**
     * Returns the status of the fiber.
     * 
     * @return The fiber's status.
     */
    public final synchronized int getStatus() {
        if (m_status == RUNNING && m_paused) {
            return PAUSED;
        }

        return m_status;
    }

    /**
     * The run() method does the actual work of reading messages from the daemon
     * and placing those messages in the appropriate queue for use by other
     * threads.
     * 
     */
    public final void run() {
        synchronized (this) {
            m_status = RUNNING;
        }

        try {
            for (;;) {
                synchronized (this) {
                    if (m_status == STOP_PENDING) {
                        break;
                    }
                }
                process(m_portal.receive());
            }
        } catch (Exception e) {
            if (log().isDebugEnabled()) {
                log().debug("run: an exception occured processing the datagram, thread exiting");
            }
            return;
        } finally {
            synchronized (this) {
                m_status = STOPPED;
            }
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}