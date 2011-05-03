/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.icmp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * <p>Pinger class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface Pinger {
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
    public void ping(InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb) throws Exception;

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
    public Number ping(InetAddress host, long timeout, int retries) throws Exception;
    

	/**
	 * Ping a remote host, using the default number of retries and timeouts.
	 *
	 * @param host the host to ping
	 * @return the round-trip time of the packet in microseconds or null if no reply
	 * @throws IOException if any.
	 * @throws InterruptedException if any.
	 * @throws java.lang.Exception if any.
	 */
	public Number ping(InetAddress host) throws Exception;

	/**
	 * <p>parallelPing</p>
	 *
	 * @param host a {@link java.net.InetAddress} object.
	 * @param count a int.
	 * @param timeout a long.
	 * @param pingInterval a long.
	 * @return a {@link java.util.List} object represening the round trip time in microseconds of each packet
	 * @throws java.lang.Exception if any.
	 */
	public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception;
}
