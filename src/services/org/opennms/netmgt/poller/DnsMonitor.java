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
//
package org.opennms.netmgt.poller;

import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.io.IOException;
import java.io.InterruptedIOException;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.NoRouteToHostException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.protocols.dns.DNSAddressRequest;
import org.opennms.protocols.dns.DNSAddressRR;
import org.opennms.protocols.dns.DNSInputStream;

/**
 * <P>This class is designed to be used by the service poller
 * framework to test the availability of the DNS service on 
 * remote interfaces. The class implements the ServiceMonitor
 * interface that allows it to be used along with other
 * plug-ins by the service poller framework.</P>
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class DnsMonitor
	extends IPv4Monitor
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
	 * <P>Sends a DNSAddressRequest to the name server.</P>
	 *
	 * @param request	The address request to send to the name server.
	 * @param socket	The datagram socket the request is sent on.
	 * @param nameServer	The nameserver address for the packet destination.
	 * @param port		The port number to send the address.
	 *
	 * @exception java.io.IOException	Thrown if an error occurs while
	 *	sending the datagram packet.
	 */
	private void sendRequest(DNSAddressRequest 	request, 
				 DatagramSocket 	socket, 
				 InetAddress 		nameServer,
				 int			port) 
		throws IOException 
	{
		byte[] data = request.buildRequest();
		DatagramPacket packet = new DatagramPacket(data, 
							   data.length, 
							   nameServer, 
							   port);
		socket.send(packet);
	}

	/**
	 * <P>Receives the data packet and retrieves 
	 * the address from the packet.</P> 
	 *
	 * @param request	the DNSAddressRequest whose response is to be got
	 * @param socket	the socket on which the response  is recieved
	 *
	 * @exception java.io.IOException  Thrown if response is not decoded
	 *	as expected.
	 */
	private void getResponse(DNSAddressRequest request, DatagramSocket socket) 
		throws IOException 
	{
		byte[] buffer = new byte[512];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		request.verifyResponse(packet.getData(), packet.getLength());
	}
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
	public int poll(NetworkInterface iface, Map parameters) 
	{
		//
		// Get interface address from NetworkInterface
		//
		if (iface.getType() != iface.TYPE_IPV4)
			throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

		// get the log
		//
		Category log = ThreadCategory.getInstance(getClass());

		// get the parameters
		//
		int retry = getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
		int port  = getKeyedInteger(parameters, "port", DEFAULT_PORT);
		int timeout = getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);

		// Host to lookup?
		//
		String lookup = getKeyedString(parameters, "lookup", null);
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
		try 
		{
			socket = new DatagramSocket();
			socket.setSoTimeout(timeout); // will force the InterruptedIOException

			for (int attempts=0; attempts <= retry && serviceStatus != SERVICE_AVAILABLE; attempts++)
			{
				try 
				{
					sendRequest(request, socket, ipv4Addr, port);
					getResponse(request, socket);
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
	
		//
		// return the status of the service
		//
		return serviceStatus;
	}

}
