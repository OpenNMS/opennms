/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.jna;

import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_RETRIES;
import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_TIMEOUT;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.opennms.netmgt.icmp.ParallelPingResponseCallback;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.SinglePingResponseCallback;
import org.opennms.protocols.rt.IDBasedRequestLocator;
import org.opennms.protocols.rt.RequestTracker;


/**
 * Main
 *
 * @author brozow
 */
public class JnaPinger implements Pinger {
    
    private final int m_pingerId = (int) (Math.random() * Short.MAX_VALUE);

    private RequestTracker<JnaPingRequest, JnaPingReply> m_pingTracker;
    private JnaIcmpMessenger m_messenger;

	/**
	 * Initializes this singleton
	 * @throws Exception 
	 */
	public synchronized void initialize() throws Exception {
		if (m_pingTracker != null) return;
		m_messenger = new JnaIcmpMessenger(m_pingerId);
        m_pingTracker = new RequestTracker<JnaPingRequest, JnaPingReply>("JNA-ICMP-"+m_pingerId, m_messenger, new IDBasedRequestLocator<JnaPingRequestId, JnaPingRequest, JnaPingReply>());
		m_pingTracker.start();
	}

	/**
	 * <p>ping</p>
	 *
	 * @param host a {@link java.net.InetAddress} object.
	 * @param timeout a long.
	 * @param retries a int.
	 * @param sequenceId a short.
	 * @param cb a {@link org.opennms.netmgt.ping.PingResponseCallback} object.
	 * @throws java.lang.Exception if any.
	 */
	public void ping(InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb) throws Exception {
		initialize();
		m_pingTracker.sendRequest(new JnaPingRequest(host, m_pingerId, sequenceId, timeout, retries, cb));
	}

	/**
	 * This method is used to ping a remote host to test for ICMP support. If
	 * the remote host responds within the specified period, defined by retries
	 * and timeouts, then the response time is returned.
	 *
	 * @param host
	 *            The address to poll.
	 * @param timeout
	 *            The time to wait between each retry.
	 * @param retries
	 *            The number of times to retry
	 * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
	 * @throws InterruptedException if any.
	 * @throws IOException if any.
	 * @throws java.lang.Exception if any.
	 */
	public Number ping(InetAddress host, long timeout, int retries) throws Exception {
		SinglePingResponseCallback cb = new SinglePingResponseCallback(host);
		ping(host, timeout, retries, 1, cb);
		cb.waitFor();
        cb.rethrowError();
		return cb.getResponseTime();
	}

	/**
	 * Ping a remote host, using the default number of retries and timeouts.
	 *
	 * @param host the host to ping
	 * @return the round-trip time of the packet
	 * @throws IOException if any.
	 * @throws InterruptedException if any.
	 * @throws java.lang.Exception if any.
	 */
	public Number ping(InetAddress host) throws Exception {
		return ping(host, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
	}

	/**
	 * <p>parallelPing</p>
	 *
	 * @param host a {@link java.net.InetAddress} object.
	 * @param count a int.
	 * @param timeout a long.
	 * @param pingInterval a long.
	 * @return a {@link java.util.List} object.
	 * @throws java.lang.Exception if any.
	 */
	public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception {
		initialize();
		ParallelPingResponseCallback cb = new ParallelPingResponseCallback(count);

		if (timeout == 0) {
			timeout = DEFAULT_TIMEOUT;
		}

		long threadId = JnaPingRequest.getNextTID();
		for (int seqNum = 0; seqNum < count; seqNum++) {
		    JnaPingRequest request = new JnaPingRequest(host, m_pingerId, seqNum, threadId, timeout, 0, cb);
			m_pingTracker.sendRequest(request);
			Thread.sleep(pingInterval);
		}

		cb.waitFor();
		return cb.getResponseTimes();
	}

}
