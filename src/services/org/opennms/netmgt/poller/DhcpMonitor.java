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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
//
// Tab Size = 8
//
package org.opennms.netmgt.poller;

import java.lang.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.dhcpd.Dhcpd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * <P>This class is designed to be used by the service poller
 * framework to test the availability of the DHCP service on 
 * remote interfaces as defined by RFC 2131.</P>
 * 
 * <P>This class relies on the DHCP API provided by JDHCP v1.1.1
 * (please refer to
 * <A HREF="http://www.dhcp.org/javadhcp">http://www.dhcp.org/javadhcp</A>).
 * </P>
 *
 * <P>The class implements the ServiceMonitor interface that allows 
 * it to be used along with other plug-ins by the service poller 
 * framework.</P>
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class DhcpMonitor
	extends IPv4Monitor
{
	/** 
	 * Default retries.
	 */
	private static final int DEFAULT_RETRY 		= 0;

	/** 
	 * Default timeout. Specifies how long (in milliseconds) to block waiting
	 * for data from the monitored interface.
	 */
	private static final int DEFAULT_TIMEOUT 	= 3000; // 3 second timeout on read()

	/**
	 * <P>Poll the specified address for DHCP service availability</P>
	 *
	 * @param iface		The network interface to test the service on.
	 * @param parameters	The package parameters (timeout, retry, etc...) to be 
	 *  used for this poll.
	 *
	 * @return The availability of the interface and if a transition event
	 * 	should be supressed.
	 *
	 */
	public int poll(NetworkInterface iface, Map parameters) 
	{
		// Get interface address from NetworkInterface
		//
		if (iface.getType() != iface.TYPE_IPV4)
			throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

		// Process parameters
		//
		Category log = ThreadCategory.getInstance(getClass());

		// Retries
		//
		int retry = getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
		int timeout = getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);

		// Get interface address from NetworkInterface
		//
		InetAddress ipv4Addr = (InetAddress)iface.getAddress();

		if(log.isDebugEnabled())
			log.debug("DhcpMonitor.poll: address: " + ipv4Addr + " timeout: " + timeout + " retry: " + retry);
		
		int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
			try
			{
				if(Dhcpd.isServer(ipv4Addr, (long)timeout, retry))
					serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
			}
			catch(IOException ioE)
			{
				ioE.fillInStackTrace();
				log.warn("DhcpMonitor.poll: An I/O exception occured during DHCP discovery", ioE);
		}

		//
		// return the status of the service
		//
		return serviceStatus;
	}
}

