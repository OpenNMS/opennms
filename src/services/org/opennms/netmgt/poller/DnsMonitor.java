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
// 2003 Jul 18: Enabled retries for monitors.
// 2003 Jun 11: Added a "catch" for RRD update errors. Bug #748.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 29: Added response times to certain monitors.
// 2002 Nov 12: Display DNS response time data in webUI.
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
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.protocols.dns.DNSAddressRequest;

/**
 * <P>This class is designed to be used by the service poller
 * framework to test the availability of the DNS service on 
 * remote interfaces. The class implements the ServiceMonitor
 * interface that allows it to be used along with other
 * plug-ins by the service poller framework.</P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class DnsMonitor
	extends IPv4LatencyMonitor
{
	/** 
	 * Default DNS port.
	 */
	private static final int DEFAULT_PORT 		= 53;

	/** 
	 * Default retries.
	 */
	private static final int DEFAULT_RETRY 		= 0;

	/** 
	 * Default timeout.  Specifies how long (in milliseconds) to block waiting
	 * for data from the monitored interface.
	 */
	private static final int DEFAULT_TIMEOUT 	= 5000; 

	/**
	 * <P>Poll the specified address for DNS service availability.</P>
	 *
	 * <P>During the poll an DNS address request query packet is generated
	 * for hostname 'localhost'.  The query is sent via UDP socket to the
	 * interface at the specified port (by default UDP port 53).  If a 
	 * response is received, it is parsed and validated.  If the DNS lookup
	 * was successful the service status is set to SERVICE_AVAILABLE and the
	 * method returns.</P>
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
		//
		// Get interface address from NetworkInterface
		//
		if (iface.getType() != NetworkInterface.TYPE_IPV4)
			throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

		// get the log
		//
		Category log = ThreadCategory.getInstance(getClass());

		// get the parameters
		//
		int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
		int port  = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);
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


		// Host to lookup?
		//
		String lookup = ParameterMap.getKeyedString(parameters, "lookup", null);
		if(lookup == null || lookup.length() == 0)
		{
			// Get hostname of local machine for future DNS lookups
			//
			try 
			{
				lookup = InetAddress.getLocalHost().getHostName();
			}
			catch(UnknownHostException ukE)
			{
				// Recast the exception as a Service Monitor Exception
				//
				ukE.fillInStackTrace();
				throw new UndeclaredThrowableException(ukE);
			}
		}
		
		// get the address and DNS address request
		//
		InetAddress ipv4Addr = (InetAddress)iface.getAddress();
		DNSAddressRequest request = new DNSAddressRequest(lookup);

		int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
		DatagramSocket socket = null;
		long responseTime = -1;
		try 
		{
			socket = new DatagramSocket();
			socket.setSoTimeout(timeout); // will force the InterruptedIOException

			for (int attempts=0; attempts <= retry && serviceStatus != SERVICE_AVAILABLE; attempts++)
			{
				try 
				{
					// Send DNS request
					//
					byte[] data = request.buildRequest();
					DatagramPacket outgoing = new DatagramPacket(data, 
										   data.length, 
										   ipv4Addr, 
										   port);
					long sentTime = System.currentTimeMillis();
					socket.send(outgoing);
					
					// Get DNS Response
					//
					byte[] buffer = new byte[512];
					DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
					socket.receive(incoming);
					responseTime = System.currentTimeMillis() - sentTime;
					
					// Validate DNS Response
					// IOException thrown if packet does not decode as expected.
					request.verifyResponse(incoming.getData(), incoming.getLength());
					
					if (log.isDebugEnabled())
						log.debug("poll: valid DNS request received, responseTime= " + responseTime + "ms");
					serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
				} 
				catch (InterruptedIOException ex) 
				{
					// Ignore, no response received.
				}
			}
		} 
		catch(NoRouteToHostException e)
		{
			e.fillInStackTrace();
			log.debug("No route to host exception for address: " + ipv4Addr, e);
		}
		catch(ConnectException e)
		{
		        //Connection refused. Continue to retry.
                        //
                        e.fillInStackTrace();
			log.debug("Connection exception for address: " + ipv4Addr, e);
		}
		catch (IOException ex) 
		{
			ex.fillInStackTrace();
			log.info("IOException while polling address: " + ipv4Addr, ex);
		}
		finally
		{
			if(socket != null)
				socket.close();
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
					this.updateRRD(rrdPath, ipv4Addr, dsName, responseTime, pkg);
                        	}
                        	catch(RuntimeException rex)
                        	{
                                	log.debug("There was a problem writing the RRD:" + rex);
                        	}
			}
		}
		
		// 
		//
		// return the status of the service
		//
		return serviceStatus;
	}

}
