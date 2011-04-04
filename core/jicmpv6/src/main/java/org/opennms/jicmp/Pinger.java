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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.jicmp.v6.V6Pinger;
import org.opennms.netmgt.icmp.PingResponseCallback;


/**
 * Main
 *
 * @author brozow
 */
public class Pinger implements org.opennms.netmgt.icmp.Pinger {

	public Long ping(InetAddress host) throws Exception {
		return ping(host, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
	}

	/**
	 * TODO: IMPLEMENT ME
	 */
	@Override
	public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception {
		return new ArrayList<Number>();
	}

	/**
	 * TODO: Add support for retries via RequestTracker
	 * TODO: Add proper timeout support
	 * TODO: Add async callback support to AbstractPinger classes
	 */
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
	 */
	@Override
	public Long ping(InetAddress host, long timeout, int retries) throws Exception {

		AbstractPinger listener = host instanceof Inet4Address ? new V4Pinger() : new V6Pinger();
		listener.start();

		long rtt = listener.ping(host, 12345, 1, timeout);

		listener.stop();
		return rtt;
	}
}
