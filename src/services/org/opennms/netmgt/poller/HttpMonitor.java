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
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ConnectException;
import java.net.NoRouteToHostException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.rrd.Interface;

/**
 * <P>This class is designed to be used by the service poller
 * framework to test the availability of the HTTP service on 
 * remote interfaces. The class implements the ServiceMonitor
 * interface that allows it to be used along with other
 * plug-ins by the service poller framework.</P>
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 *
 */
final class HttpMonitor
	extends IPv4LatencyMonitor
{
	/** 
	 * Default HTTP ports.
	 */
	private static final int[] DEFAULT_PORTS 	= {80, 8080, 8888};

	/**
	 * Default retries.
	 */
	private static final int DEFAULT_RETRY 		= 0;

	/**
	 * Default URL to 'GET'
	 */
	private static final String DEFAULT_URL 	= "/";

	/** 
	 * Default timeout. Specifies how long (in milliseconds) to block waiting
	 * for data from the monitored interface.
	 */
	private static final int DEFAULT_TIMEOUT 	= 3000; // 3 second timeout on read()
	
	/**
	 * <P>Poll the specified address for HTTP service availability</P>
	 *
	 * <P>During the poll an attempt is made to connect on the specified
	 * port(s) (by default TCP ports 80, 8080, 8888).  If the connection
	 * request is successful, an HTTP 'GET' command is sent to the interface.
	 * The response is parsed and a return code extracted and verified.  
	 * Provided that the interface's response is valid we set the service 
	 * status to SERVICE_AVAILABLE and return.</P>
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

		Category log = ThreadCategory.getInstance(getClass());

		int retry   = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
		int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
		int[] ports = ParameterMap.getKeyedIntegerArray(parameters, "ports", DEFAULT_PORTS);
		String url  = ParameterMap.getKeyedString(parameters, "url", DEFAULT_URL);
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
		int response = ParameterMap.getKeyedInteger(parameters, "response", -1);
		String responseText = ParameterMap.getKeyedString(parameters, "response text", null);

		// Set to true if "response" property has a valid return code specified.
		//  By default response will be deemed valid if the return code 
		//  falls in the range:   99 < rc < 500
		//  This is based on the following information from RFC 1945 (HTTP 1.0)
		// 		HTTP 1.0 GET return codes:
		//		 	1xx: Informational - Not used, future use
		//			2xx: Success
		//			3xx: Redirection
		//			4xx: Client error
		//			5xx: Server error
		boolean bStrictResponse = (response > 99 && response < 600);


		// Extract the ip address
		//
		InetAddress ipv4Addr = (InetAddress)iface.getAddress();
	
		// Following a successful poll 'currentPort' will contain the port on 
		// the remote host that was successfully queried
		//
		final String cmd = "GET " + url + " HTTP/1.0\r\n\r\n";

		// Cycle through the port list
		//
		int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
		int currentPort = -1;
		long responseTime = -1;
		for (int portIndex=0; portIndex < ports.length && serviceStatus != ServiceMonitor.SERVICE_AVAILABLE; portIndex++)
		{
			currentPort = ports[portIndex];
			
			if (log.isDebugEnabled()) 
			{
				log.debug("Port = "+ currentPort + ", Address = " + ipv4Addr 
					  + ", Timeout = " + timeout + ", Retry = " + retry);
			}

			for (int attempts=0; attempts <= retry && serviceStatus != ServiceMonitor.SERVICE_AVAILABLE; attempts++)
			{
				Socket portal = null;
				try
				{
					//
					// create a connected socket
					//
					portal = new Socket(ipv4Addr, currentPort);
					// We're connected, so upgrade status to unresponsive
					serviceStatus = SERVICE_UNRESPONSIVE;
					portal.setSoTimeout(timeout);
					
					//
					// Issue HTTP 'GET' command and check the return code in the response
					//
					long sentTime = System.currentTimeMillis();
					portal.getOutputStream().write(cmd.getBytes());

					//
					// Get a buffered input stream that will read a line
					// at a time
					//
					BufferedReader lineRdr = new BufferedReader(new InputStreamReader(portal.getInputStream()));
					String line = lineRdr.readLine();
					responseTime = System.currentTimeMillis() - sentTime;
					if (line == null)
						continue;

					if(log.isDebugEnabled())
					{
						log.debug("poll: response= " + line);
						log.debug("poll: responseTime= " + responseTime + "ms");
					}

					if(line.startsWith("HTTP/"))
					{
						StringTokenizer t = new StringTokenizer(line);
						t.nextToken();

						int rVal = -1;
						try
						{
							rVal = Integer.parseInt(t.nextToken());
						}
						catch(NumberFormatException nfE)
						{
							log.info("Error converting response code from host = " + ipv4Addr + ", response = " + line);
						}

						if (bStrictResponse && rVal == response)
						{
							serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;	
						}
						else if(!bStrictResponse && rVal > 99 && rVal < 500)
						{
							serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
						}
						else
						{
							serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
						}
					}
	
					if (serviceStatus == ServiceMonitor.SERVICE_AVAILABLE &&
					    responseText != null && responseText.length() > 0)
					{
						// This loop will rip through the rest of the Response Header
						//
						do 
						{
							line = lineRdr.readLine();

						} while (line != null && line.length() != 0);
						if (line == null)
							continue;
						
						// Now lets rip through the Entity-Body (i.e., content) looking
						// for the required text.
						//
						boolean bResponseTextFound = false;
						do 
						{
							line = lineRdr.readLine();
							
							if(line != null)
							{
								int responseIndex = line.indexOf(responseText);
								if (responseIndex != -1)
									bResponseTextFound = true;
							}	

						} while (line != null && !bResponseTextFound);

						// Set the status back to failed
						//
						if (!bResponseTextFound)
							serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
					}
				}
				catch(NoRouteToHostException e)
				{
					e.fillInStackTrace();
					log.warn("No route to host exception for address " + ipv4Addr, e);
					portIndex = ports.length; // Will cause outer for(;;) to terminate
					break; 			  // Break out of inner for(;;)
				}
				catch(ConnectException e)
				{
					// Connection Refused!!  No need to perform retries for this port.
					//
					e.fillInStackTrace();
					log.debug("Connection exception for " + ipv4Addr + ":" + ports[portIndex], e);

					break; // Break out of inner for(;;)
				}
				catch(IOException e)
				{
					// Ignore
					//
					e.fillInStackTrace();
					log.debug("IOException while polling address " + ipv4Addr, e);
				}
				finally
				{
					try
					{
						// Close the socket
						if(portal != null)
							portal.close();
					}
					catch(IOException e) 
					{ 
						e.fillInStackTrace();
						log.debug("Error closing socket connection", e);
					}
				}

			} // end for (attempts)
		} // end for (ports)
		
		// Add the 'qualifier' parm to the parameter map.  This parm will
		// contain the port on which the service was found if AVAILABLE or
		// will contain a comma delimited list of the port(s) which were 
		// tried if the service is UNAVAILABLE
		//
		if (serviceStatus == ServiceMonitor.SERVICE_UNAVAILABLE)
		{
			//
			// Build port string
			//
			StringBuffer testedPorts = new StringBuffer();
			for(int i = 0; i < ports.length; i++)
			{
				if(i == 0)
					testedPorts.append(ports[0]);
				else
					testedPorts.append(',').append(ports[i]);
			}
			
			// Add to parameter map
			parameters.put("qualifier", testedPorts);
		}
		else if (serviceStatus == ServiceMonitor.SERVICE_AVAILABLE)
		{
			parameters.put("qualifier", Integer.toString(currentPort));
			
			// Store response time in RRD
			if (responseTime >= 0 && rrdPath != null)
				this.updateRRD(m_rrdInterface, rrdPath, ipv4Addr, dsName, responseTime);
		}	
		
		//
		// return the status of the service
		//
		return serviceStatus;
	}

}
