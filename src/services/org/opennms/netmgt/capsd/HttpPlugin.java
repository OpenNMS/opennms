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
//
package org.opennms.netmgt.capsd;

import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ConnectException;
import java.net.NoRouteToHostException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * <P>This class is designed to be used by the capabilities
 * daemon to test for the existance of an HTTP server on 
 * remote interfaces. The class implements the Plugin
 * interface that allows it to be used along with other
 * plugins by the daemon.
 *
 * This plugin generates a HTTP GET request and checks the
 * return code returned by the remote host to determine if
 * it supports the protocol.  
 * 
 * The remote host's response will be deemed valid if the return code 
 * falls in the 100 to 599 range (inclusive).
 * 
 * This is based on the following information from RFC 1945 (HTTP 1.0)
 *	HTTP 1.0 GET return codes:
 *		1xx: Informational - Not used, future use
 *		2xx: Success
 *		3xx: Redirection
 *		4xx: Client error
 *		5xx: Server error</P>
 *
 * This plugin generates a HTTP GET request and checks the
 * return code returned by the remote host to determine if
 * it supports the protocol.
 *
 * The remote host's response will be deemed valid if the return code
 * falls in the 100 to 599 range (inclusive).
 *
 * This is based on the following information from RFC 1945 (HTTP 1.0)
 *    HTTP 1.0 GET return codes:
 *            1xx: Informational - Not used, future use
 *            2xx: Success
 *            3xx: Redirection
 *            4xx: Client error
 *            5xx: Server error</P>
 *
 * This plugin generates a HTTP GET request and checks the
 * return code returned by the remote host to determine if
 * it supports the protocol.
 *
 * The remote host's response will be deemed valid if the return code
 * falls in the 100 to 599 range (inclusive).
 *
 * This is based on the following information from RFC 1945 (HTTP 1.0)
 *    HTTP 1.0 GET return codes:
 *            1xx: Informational - Not used, future use
 *            2xx: Success
 *            3xx: Redirection
 *            4xx: Client error
 *            5xx: Server error</P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@opennms.org">Weaver</A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 */
public class HttpPlugin
	extends AbstractPlugin
{
	protected  String	PROTOCOL_NAME	= "HTTP";
	
	/**
	 * Boolean indicating whether to check for a return code
	 */
	protected  boolean CHECK_RETURN_CODE = true;
	
	/**
	 * The query to send to the HTTP server
	 */
	protected String QUERY_STRING = "GET / HTTP/1.0\r\n\r\n";
	
	/**
	 * A string to look for in the response from the server
	 */
	protected String RESPONSE_STRING = "HTTP/";
	
	/**
	 * <P>The default ports on which the host is checked to see if 
	 * it supports HTTP.</P>
	 */
	private static final int[]	DEFAULT_PORTS	= { 80, 8080, 8888 };

	/**
	 * Default number of retries for HTTP requests.
	 */
	private final static int	DEFAULT_RETRY	= 0;
	
	/**
	 * Default timeout (in milliseconds) for HTTP requests.
	 */
	private final static int	DEFAULT_TIMEOUT	= 5000; // in milliseconds
	
	/**
	 * <P>Test to see if the passed host-port pair is the 
	 * endpoint for an HTTP server. If there is an HTTP server
	 * at that destination then a value of true is returned
	 * from the method. Otherwise a false value is returned 
	 * to the caller.</P>
	 *
	 * @param host	The remote host to connect to.
	 * @param port	The remote port to connect to.
	 *
	 * @return True if server supports HTTP on the specified 
	 *	port, false otherwise
	 */
	private boolean isServer(InetAddress host, int port, int retries, int timeout)
	{
		Category log = ThreadCategory.getInstance(getClass());

		boolean isAServer = false;
		
		for (int attempts=0; attempts <= retries && !isAServer; attempts++)
		{
			log.debug(getClass().getName()+".isServer: attempt " + attempts + " to connect " + host.getHostAddress() + ":" + port + ", timeout=" + timeout);

			Socket  portal    = null; 
			try
			{
				// create a connected socket
				//
				portal = new Socket(host, port);
				portal.setSoTimeout(timeout);	

				BufferedReader lineRdr = new BufferedReader(new InputStreamReader(portal.getInputStream()));
				
				portal.getOutputStream().write(QUERY_STRING.getBytes());
				String line = null;
				StringBuffer response = new StringBuffer();
				while( (line=lineRdr.readLine())!=null)
				{
					response.append(line).append(System.getProperty("line.separator"));
				}
				
				if(response.toString() != null && response.toString().indexOf(RESPONSE_STRING)>-1)
				{
					if (CHECK_RETURN_CODE)
					{
						StringTokenizer t = new StringTokenizer(response.toString());
						t.nextToken();
						int rVal = Integer.parseInt(t.nextToken());
						if(rVal >= 99 && rVal <= 600)
							isAServer = true;
					}
					else
					{
						isAServer = true;
					}
				}
			}
			catch(NumberFormatException e)
			{
				log.debug(getClass().getName()+": failed to parse response code from host " + host.getHostAddress(),  e);
			}
			catch(ConnectException e)
			{
				// Connection refused!!  No need to perform retries.
				//
				log.debug(getClass().getName()+": connection refused to " + host.getHostAddress() + ":" + port);
				break;
			}
			catch(NoRouteToHostException e)
			{
				// No route to host!! No need to perform retries.
				e.fillInStackTrace();

				log.warn(getClass().getName()+": No route to host " + host.getHostAddress(), e);
				throw new UndeclaredThrowableException(e);
			}
			catch(InterruptedIOException e)
			{
				// Timed out
				log.debug(getClass().getName()+": connection request timed out.");
			}
			catch(IOException e)
			{
				log.warn(getClass().getName()+": An undeclared I/O exception occured contacting host " + host.getHostAddress(), e);
			}
			catch(Throwable t)
			{
				log.warn(getClass().getName()+": An undeclared throwable exception caught contacting host " + host.getHostAddress(), t);
			}
			finally
			{
				try
				{
					if(portal != null)
						portal.close();
				}
				catch(IOException e) { }
			}
		}

		return isAServer;
	}

	/**
	 * Returns the name of the protocol that this plugin
	 * checks on the target system for support.
	 *
	 * @return The protocol name for this plugin.
	 */
	public String getProtocolName()
	{
		return PROTOCOL_NAME;
	}

	/**
	 * Returns true if the protocol defined by this
	 * plugin is supported. If the protocol is not 
	 * supported then a false value is returned to the 
	 * caller.
	 *
	 * @param address	The address to check for support.
	 *
	 * @return True if the protocol is supported by the address.
	 */
	public boolean isProtocolSupported(InetAddress address)
	{
		for(int i = 0; i < DEFAULT_PORTS.length; i++)
		{
			if(isServer(address, DEFAULT_PORTS[i], DEFAULT_RETRY, DEFAULT_TIMEOUT))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the protocol defined by this
	 * plugin is supported. If the protocol is not 
	 * supported then a false value is returned to the 
	 * caller. The qualifier map passed to the method is
	 * used by the plugin to return additional information
	 * by key-name. These key-value pairs can be added to 
	 * service events if needed.
	 *
	 * @param address	The address to check for support.
	 * @param qualiier	The map where qualification are set
	 *			by the plugin.
	 *
	 * @return True if the protocol is supported by the address.
	 */
	public boolean isProtocolSupported(InetAddress address, Map qualifiers)
	{
		int retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
		int timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
		int[] ports = ParameterMap.getKeyedIntegerArray(qualifiers, "ports", DEFAULT_PORTS);

		for(int i = 0; i < ports.length; i++)
		{
			if(isServer(address, ports[i], retries, timeout))
			{
				qualifiers.put("port", new Integer(ports[i]));
				return true;
			}
		}

		return false;
	}
}


