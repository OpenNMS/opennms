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
// Tab Size = 8
//

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.Ping.EchoReplyListener;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.ping.Packet;
import org.opennms.netmgt.ping.Reply;
import org.opennms.netmgt.ping.ReplyReceiver;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.protocols.icmp.IcmpSocket;
import org.opennms.netmgt.Ping;
import org.savarese.vserv.tcpip.ICMPEchoPacket;
import org.savarese.vserv.tcpip.OctetConverter;

/**
 * This class provides Capsd with the ability to check for ICMP support on new
 * interfaces as them are passed into the system. In order to minimize the
 * number of sockets and threads, this class creates a daemon thread to handle
 * all responses and a single socket for sending echo request to various hosts.
 * 
 * @author <A HREF="mailto:weave@oculan.com">Weave </a>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public final class IcmpPlugin extends AbstractPlugin {
    /**
     * The name of the protocol that is supported by this plugin
     */
    private static final String PROTOCOL_NAME = "ICMP";

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 2;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 800;

    /**
     * The filter identifier for the ping reply receiver
     */
    private static final short FILTER_ID = (short) (new java.util.Random(System.currentTimeMillis())).nextInt();

    /**
     * The sequence number for pings
     */
    private static short m_seqid = (short) 0xbeef;

    /**
     * The singular reply receiver
     */
    private static ReplyReceiver m_receiver = null; // delayed creation

    /**
     * The ICMP socket used to send/receive replies
     */
    private static IcmpSocket m_icmpSock = null; // delayed creation

    /**
     * The set used to lookup thread identifiers -- the map of long thread
     * identifiers to Packets that must be signaled. The mapped objects are
     * instances of the {@link org.opennms.netmgt.ping.Reply Reply}class.
     */
    private static Map<Integer, Boolean> m_waiting = Collections.synchronizedMap(new TreeMap<Integer, Boolean>());

    /**
     * The thread used to receive and process replies.
     */
    private static Thread m_worker = null;

    /**
     * This class is used to encapsulate a ping request. A request consist of
     * the pingable address and a signaled state.
     * 
     */
    
//    private static final class Ping {
//        /**
//         * The address being pinged
//         */
//        private final InetAddress m_addr;
//
//        /**
//         * The state of the ping
//         */
//        private boolean m_signaled;
//
//        /**
//         * Constructs a new ping object
//         */
//        Ping(InetAddress addr) {
//            m_addr = addr;
//        }
//
//        /**
//         * Returns true if signaled.
//         */
//        synchronized boolean isSignaled() {
//            return m_signaled;
//        }
//
//        /**
//         * Sets the signaled state and awakes the blocked threads.
//         */
//        synchronized void signal() {
//            m_signaled = true;
//            notifyAll();
//        }
//
//        /**
//         * Returns true if the passed address is the target of the ping.
//         */
//        boolean isTarget(InetAddress addr) {
//            return m_addr.equals(addr);
//        }
//    }

    /**
     * Constructs a new monitor.
     */
    public IcmpPlugin() throws IOException {
//        synchronized (IcmpPlugin.class) {
//            if (m_worker == null) {
//                // Create a receiver queue
//                //
//                final FifoQueueImpl q = new FifoQueueImpl();
//
//                // Open a socket
//                //
//                m_icmpSock = new IcmpSocket();
//
//                // Start the receiver
//                //
//                m_receiver = new ReplyReceiver(m_icmpSock, q, FILTER_ID);
//                m_receiver.start();
//
//                // Start the processor
//                //
//                m_worker = new Thread(new Runnable() {
//                    public void run() {
//                        for (;;) {
//                            Reply pong = null;
//                            try {
//                                pong = (Reply) q.remove();
//                            } catch (InterruptedException ex) {
//                                break;
//                            } catch (Exception ex) {
//                                ThreadCategory.getInstance(this.getClass()).error("Error processing response queue", ex);
//                            }
//
//                            Long key = new Long(pong.getPacket().getTID());
//                            Ping ping = (Ping) m_waiting.get(key);
//                            if (ping != null && ping.isTarget(pong.getAddress()))
//                                ping.signal();
//                        }
//                    }
//                }, "IcmpPlugin-Receiver");
//                m_worker.setDaemon(true);
//                m_worker.start();
//            }
//        }
    }

    private class EchoReplyListener {
    	private int tidKey;
    	EchoReplyListener(int tidKey) {
    		this.tidKey = tidKey;
    	}
    	
        public void notifyEchoReply(ICMPEchoPacket packet,
                byte[] data, int dataOffset) {
        	Category log = ThreadCategory.getInstance(this.getClass());
        	long end   = System.nanoTime();
        	long start = OctetConverter.octetsToLong(data, dataOffset);
        	double rtt = (double)(end - start) / 1e6;
        	m_waiting.put(tidKey, true);
        	log.info(packet.getICMPPacketByteLength() + 
        			" bytes: icmp_seq=" + packet.getSequenceNumber() +
        			" ttl=" + packet.getTTL() +
        			" time=" + rtt + " ms");
        }

    };
    
    /**
	 * Builds a datagram compatable with the ping ReplyReceiver class.
	 */
// private synchronized static DatagramPacket getDatagram(InetAddress addr, long
// tid) {
//        Packet iPkt = new Packet(tid);
//        iPkt.setIdentity(FILTER_ID);
//        iPkt.setSequenceId(m_seqid++);
//        iPkt.computeChecksum();
//
//        byte[] data = iPkt.toBytes();
//        return new DatagramPacket(data, data.length, addr, 0);
//    }

    /**
     * This method is used to ping a remote host to test for ICMP support. If
     * the remote host responds within the specified period, defined by retries
     * and timeouts, then a value of true is returned to the caller.
     * 
     * @param ipv4Addr
     *            The address to poll.
     * @param retries
     *            The number of times to retry
     * @param timeout
     *            The time to wait between each retry.
     * 
     * @return True if the host is reachable and responsed with an echo reply.
     * 
     */
    private boolean isPingable(final InetAddress ipv4Addr, int retries, long timeout) {
        Category log = ThreadCategory.getInstance(this.getClass());

        final String hostname = ipv4Addr.getCanonicalHostName();
        final String hostaddr = ipv4Addr.getHostAddress();
        final int count = retries + 1;
        boolean pingable = false;

        // Find an appropritate thread id
        Integer tidKey = null;
        int tid = Thread.currentThread().hashCode();
        synchronized (m_waiting) {
            while (m_waiting.containsKey(tidKey = new Integer(tid)))
                ++tid;
        }

        Ping ping = null;
        try {
        	ping = new Ping(tidKey.intValue());
        	m_waiting.put(tidKey, false);
        } catch (IOException ioE) {
        	log.info("isPingable: failed to send to address " + ipv4Addr, ioE);
        }

        EchoReplyListener erl = new EchoReplyListener(tidKey);

        log.info("PING " + hostname + " (" + hostaddr + ") " +
                ping.getRequestDataLength() + "(" +
                ping.getRequestPacketLength() + ") bytes of data");
        
        for (int attempts = 0; attempts <= retries && !(boolean)m_waiting.get(tidKey); ++attempts) {
        	try {
        		ping.sendEchoRequest(ipv4Addr);
        	} catch (IOException ioE) {
        		log.info("isPingable: Failed to send to address " + ipv4Addr, ioE);
        		break;
        	}
    //    	synchronized (ping) {
				try {
					ping.receiveEchoReply(ipv4Addr);
				} catch (Exception e) {
					Thread.currentThread().interrupt();
					break;
				}
		//	}
        }

        return (boolean)m_waiting.get(tidKey);
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     * 
     * @return The protocol name for this plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * 
     * @param address
     *            The address to check for support.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address) {
        return isPingable(address, DEFAULT_RETRY, DEFAULT_TIMEOUT);
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * 
     * @param address
     *            The address to check for support.
     * @param qualifiers
     *            The map where qualification are set by the plugin.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address, Map qualifiers) {
        int retries = DEFAULT_RETRY;
        int timeout = DEFAULT_TIMEOUT;

        if (qualifiers != null) {
            retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
        }

        return isPingable(address, retries, timeout);
    }
}
