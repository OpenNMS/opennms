//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Jun 11: Added a "catch" for RRD update errors. Bug #748.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response times to certain monitors.
// 2002 Nov 13: Added ICMP response time measurements.
// 2002 Nov 12: Added response time graphs to webUI.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ping.Packet;
import org.opennms.netmgt.ping.Reply;
import org.opennms.netmgt.ping.ReplyReceiver;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.protocols.icmp.IcmpSocket;

/**
 * <P>This class is designed to be used by the service poller
 * framework to test the availability of the ICMP service on 
 * remote interfaces. The class implements the ServiceMonitor
 * interface that allows it to be used along with other
 * plug-ins by the service poller framework.</P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class IcmpMonitor
	extends IPv4LatencyMonitor
{
	/** 
	 * Default retries.
	 */
	private static final int DEFAULT_RETRY = 2;

	/** 
	 * Default timeout.  Specifies how long (in milliseconds) to block waiting
	 * for data from the monitored interface.
	 */
	private static final int DEFAULT_TIMEOUT = 800;

	/**
	 * The filter identifier for the ping reply receiver
	 */
	private static final short FILTER_ID	= (short)(new java.util.Random(System.currentTimeMillis())).nextInt();

	/**
	 * The sequence number for pings
	 */
	private static short 		m_seqid = (short)0xbabe;

	/**
	 * The singular reply receiver
	 */
	private static ReplyReceiver	m_receiver = null; // delayed creation

	/**
	 * The ICMP socket used to send/receive replies
	 */
	private static IcmpSocket	m_icmpSock = null; // delayed creation

	/**
	 * The set used to lookup thread identifiers
	 * The map of long thread identifiers to Packets
	 * that must be signaled. The mapped objects are instances 
	 * of the {@link org.opennms.netmgt.ping.Reply Reply} class.
	 */
	private static Map		m_waiting = Collections.synchronizedMap(new TreeMap());

	/**
	 * The thread used to receive and process replies.
	 */
	private static Thread		m_worker = null;

	/**
	 * This class is used to encapsulate a ping request.
	 * A request consist of the pingable address and
	 * a signaled state.
	 *
	 */
	private static final class Ping
	{
		/**
		 * The address being pinged
		 */
		private final InetAddress m_addr;

		/**
		 * The state of the ping
		 */
		private boolean		m_signaled;

		/**
		 * The ping packet (contains sent/received time stamps)
		 */
		private Packet 		m_packet;
		
		/**
		 * Constructs a new ping object
		 */
		Ping(InetAddress addr) 
		{
			m_addr = addr;
		}

		/**
		 * Returns true if signaled.
		 */
		synchronized boolean isSignaled()
		{
			return m_signaled;
		}

		/**
		 * Sets the signaled state and awakes
		 * the blocked threads.
		 */
		synchronized void signal()
		{
			m_signaled = true;
			notifyAll();
		}

		/**
		 * Returns true if the passed address
		 * is the target of the ping.
		 */
		boolean isTarget(InetAddress addr)
		{
			return m_addr.equals(addr);
		}
		
		void setPacket(Packet packet)
		{
			m_packet = packet;
		}
		
		Packet getPacket()
		{
			return m_packet;
		}
	}

	/**
	 * Construts a new monitor.
	 */
	IcmpMonitor()
		throws IOException
	{
		synchronized(IcmpMonitor.class)
		{
			if(m_worker == null)
			{
				// Create a receiver queue
				//
				final FifoQueueImpl q = new FifoQueueImpl();

				// Open a socket
				//
				m_icmpSock = new IcmpSocket();

				// Start the receiver
				//
				m_receiver = new ReplyReceiver(m_icmpSock, q, FILTER_ID);
				m_receiver.start();

				// Start the processor
				//
				m_worker = new Thread(new Runnable() {
					public void run()
					{
						for(;;)
						{
							Reply pong = null;
							try
							{
								pong = (Reply)q.remove();
							}
							catch(InterruptedException ex)
							{
								break;
							}
							catch(Exception ex)
							{
								ThreadCategory.getInstance(this.getClass()).error("Error processing response queue", ex);
							}

							Long key = new Long(pong.getPacket().getTID());
							Ping ping = (Ping)m_waiting.get(key);
							if(ping != null && ping.isTarget(pong.getAddress()))
							{
								// Save reference to packet so that the
								// poll() method of the IcmpMonitor will
								// have access to sent/received time stamps
								// to calculate round trip time
								ping.setPacket(pong.getPacket());
								ping.signal();
							}
						}
					}
				}, "IcmpMonitor-Receiver" );
				m_worker.setDaemon(true);
				m_worker.start();
			}
		}
	}

	/**
	 * Builds a datagram compatable with the ping ReplyReceiver
	 * class.
	 */
	private synchronized static DatagramPacket getDatagram(InetAddress addr, long tid)
	{
		Packet iPkt = new Packet(tid);
		iPkt.setIdentity(FILTER_ID);
		iPkt.setSequenceId(m_seqid++);

		byte[] data = iPkt.toBytes();
		return new DatagramPacket(data, data.length, addr, 0);
	}
	
	/**
	 * <P>Poll the specified address for ICMP service availability.</P>
	 *
	 * <P>The ICMP service monitor relies on Discovery for the actual
	 * generation of IMCP 'ping' requests.  A JSDT session with two
	 * channels (send/recv) is utilized for passing poll requests and
	 * receiving poll replies from discovery.  All exchanges are
	 * SOAP/XML compliant.</P>
	 *
	 * @param iface		The network interface to test the service on.
	 * @param parameters	The package parameters (timeout, retry, etc...) to be 
	 *  used for this poll.
	 *
	 * @return The availibility of the interface and if a transition event
	 * 	should be supressed.
	 *
	 */
	public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) 
	{
		// Get interface address from NetworkInterface
		//
		if (iface.getType() != iface.TYPE_IPV4)
			throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

		Category log = ThreadCategory.getInstance(this.getClass());

		// get parameters
		//
		int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
		int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
		String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
                String dsName = ParameterMap.getKeyedString(parameters, "ds-name", null);

		if (rrdPath == null)
		{
			log.info("poll: RRD repository not specified in parameters, latency data will not be stored.");
		}
                if (dsName == null)
                {
                        dsName = DS_NAME;
                }
		
		// Find an appropritate thread id
		//
		Long tidKey = null;
		long tid = (long)Thread.currentThread().hashCode();
		synchronized(m_waiting)
		{
			while(m_waiting.containsKey(tidKey = new Long(tid)))
				++tid;
		}


		InetAddress ipv4Addr = (InetAddress)iface.getAddress();
		DatagramPacket pkt   = getDatagram(ipv4Addr, tid);
		Ping reply           = new Ping(ipv4Addr);
		m_waiting.put(tidKey, reply);

		int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
		for(int attempts = 0; attempts <= retry && !reply.isSignaled(); ++attempts)
		{
			// Send the datagram and wait
			//
			synchronized(reply)
			{
				try
				{
					m_icmpSock.send(pkt);
				}
				catch(IOException ioE)
				{
					log.info("Failed to send to address " + ipv4Addr, ioE);
					break;
				}
				catch(Throwable t)
				{
					log.info("Undeclared throwable exception caught sending to " + ipv4Addr, t);
					break;
				}
			
				try
				{
					reply.wait(timeout);
				}
				catch(InterruptedException ex)
				{
					// interrupted so return, reset interrupt.
					//
					Thread.currentThread().interrupt();
					break;
				}
			}
		}

		m_waiting.remove(tidKey);

		if(reply.isSignaled())
		{
			serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
			
			// Determine round-trip-time for the ping packet
			Packet replyPkt = reply.getPacket();
			if (replyPkt != null)
			{
                                long rtt = replyPkt.getPingRTT();
                                log.debug("Ping round trip time for " + ipv4Addr + ": " + rtt + "us");

                                // Store round-trip-time in RRD database
                                if (rtt >= 0 && rrdPath != null)
				{
					try
					{
	                                        this.updateRRD(m_rrdInterface, rrdPath, ipv4Addr, dsName, rtt, pkg);
					}
					catch(RuntimeException rex)
					{
						log.debug("There was a problem writing the RRD:" + rex);
					}
				}
			}
		}
		
		return serviceStatus;
	}
}

