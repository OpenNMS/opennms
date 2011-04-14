/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.jicmp;

import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_RETRIES;
import static org.opennms.netmgt.icmp.PingConstants.DEFAULT_TIMEOUT;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.opennms.jicmp.jna.NativeDatagramSocket;
import org.opennms.jicmp.v6.V6Pinger;
import org.opennms.netmgt.icmp.ParallelPingResponseCallback;
import org.opennms.netmgt.icmp.PingReply;
import org.opennms.netmgt.icmp.PingRequest;
import org.opennms.netmgt.icmp.PingRequestId;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.SinglePingResponseCallback;
import org.opennms.protocols.rt.IDBasedRequestLocator;
import org.opennms.protocols.rt.RequestTracker;


/**
 * Main
 *
 * @author brozow
 */
public class JnaPinger implements org.opennms.netmgt.icmp.Pinger {

	private final V4Pinger v4Pinger;
	private final V6Pinger v6Pinger;

	private RequestTracker<PingRequest<NativeDatagramSocket>, PingReply> s_pingTracker;

	public JnaPinger() throws Exception {
		v4Pinger = new V4Pinger();
		v6Pinger = new V6Pinger();
	}

	/**
	 * Initializes this singleton
	 * @throws Exception 
	 */
	public synchronized void initialize() throws Exception {
		if (s_pingTracker != null) return;
		v4Pinger.start();
		v6Pinger.start();
		s_pingTracker = new RequestTracker<PingRequest<NativeDatagramSocket>, PingReply>("ICMP", new PingMessenger(v4Pinger, v6Pinger), new IDBasedRequestLocator<PingRequestId, PingRequest<NativeDatagramSocket>, PingReply>());
		s_pingTracker.start();
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
		s_pingTracker.sendRequest(new JnaPingRequest(v4Pinger, v6Pinger, host, sequenceId, timeout, retries, cb));
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
	public Long ping(InetAddress host, long timeout, int retries) throws Exception {
		SinglePingResponseCallback cb = new SinglePingResponseCallback(host);
		ping(host, timeout, retries, 1, cb);
		cb.waitFor();
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
	public Long ping(InetAddress host) throws Exception {
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

		for (int i = 0; i < count; i++) {
			PingRequest<NativeDatagramSocket> request = new JnaPingRequest(v4Pinger, v6Pinger, host, i, timeout, 0, cb);
			s_pingTracker.sendRequest(request);
			Thread.sleep(pingInterval);
		}

		cb.waitFor();
		return cb.getResponseTimes();
	}

	/*
	/**
	 * TODO: Add support for retries via RequestTracker
	 * TODO: Add proper timeout support
	 * TODO: Add async callback support to AbstractPinger classes
	 * /
	@Override
	public void ping(InetAddress host, long timeout, int retries, short sequenceId, PingResponseCallback cb) throws Exception {
		AbstractPinger listener = host instanceof Inet4Address ? new V4Pinger() : new V6Pinger();
		listener.start();

		long rtt = listener.ping(host, sequenceId, 1, timeout);

		listener.stop();
	}

	/**
	 * TODO: Add support for retries via RequestTracker
	 * TODO: Add proper timeout support
	 * /
	@Override
	public Long ping(InetAddress host, long timeout, int retries) throws Exception {

		AbstractPinger listener = host instanceof Inet4Address ? new V4Pinger() : new V6Pinger();
		listener.start();

		long rtt = listener.ping(host, 12345, 1, timeout);

		listener.stop();
		return rtt;
	}
	 */
}
