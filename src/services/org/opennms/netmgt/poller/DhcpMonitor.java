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
// 2002 Nov 14: Used non-blocking I/O socket channel classes.
// 2002 Nov 12: Added web based reports for DHCP monitor.
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
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dhcpd.Dhcpd;
import org.opennms.netmgt.utils.ParameterMap;

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
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class DhcpMonitor
	extends IPv4LatencyMonitor
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
	public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) 
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

		// Get interface address from NetworkInterface
		//
		InetAddress ipv4Addr = (InetAddress)iface.getAddress();

		if(log.isDebugEnabled())
			log.debug("DhcpMonitor.poll: address: " + ipv4Addr + " timeout: " + timeout + " retry: " + retry);
		
		int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
		long responseTime = -1;
		try
		{
			// Dhcpd.isServer() returns the response time in milliseconds
			// if the remote box is a DHCP server or -1 if the remote
			// box is NOT a DHCP server.
			// 
			responseTime = Dhcpd.isServer(ipv4Addr, (long)timeout, retry);
			if (responseTime >= 0)
			{
				serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
			}
		}
		catch(IOException ioE)
		{
			ioE.fillInStackTrace();
			log.warn("DhcpMonitor.poll: An I/O exception occured during DHCP discovery", ioE);
		}

		// Store response time if available
		//
		if (serviceStatus == ServiceMonitor.SERVICE_AVAILABLE)
		{
			// Store response time in RRD
			if (responseTime >= 0 && rrdPath != null)
			{
                        	try
                                {
					this.updateRRD(m_rrdInterface, rrdPath, ipv4Addr, dsName, responseTime, pkg);
                                }
                                catch(RuntimeException rex)
                                {
                                	log.debug("There was a problem writing the RRD:" + rex);
                                }
			}
		}
		
		//
		// return the status of the service
		//
		return serviceStatus;
	}
}

