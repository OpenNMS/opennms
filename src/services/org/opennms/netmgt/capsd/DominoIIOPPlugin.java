//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
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
import java.io.FileNotFoundException;

import java.nio.channels.SocketChannel;
import org.opennms.netmgt.utils.SocketChannelUtil;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ConnectException;
import java.net.NoRouteToHostException;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * <P>This class is designed to be used by the capabilities
 * daemon to test for the existance of an IIOP on a Domino server on 
 * remote interfaces. The class implements the Plugin
 * interface that allows it to be used along with other
 * plugins by the daemon.</P>
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason</A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS</A>
 *
 *
 */
public final class DominoIIOPPlugin 
	extends AbstractPlugin
{	
	/**
	 * The protocol supported by the plugin
	 */
	private final static String 	PROTOCOL_NAME	= "DominoIIOP";

	/**
	 * Default number of retries for TCP requests
	 */
	private final static int	DEFAULT_RETRY	= 0;
	
	/**
	 * Default timeout (in milliseconds) for TCP requests
	 */
	private final static int	DEFAULT_TIMEOUT	= 5000; // in milliseconds
	
	/** 
	 * Default port.
	 */
	private static final int DEFAULT_PORT = 63148;
	
	/**
	 * Default port of where to find the IOR via HTTP
	 */
	private static final int DEFAULT_IORPORT = 80;
	
	/**
	 * <P>Test to see if the passed host-port pair is the 
	 * endpoint for a Domino IIOP server. If there is a IIOP server
	 * at that destination then a value of true is returned
	 * from the method. Otherwise a false value is returned 
	 * to the caller.  In order to return true the remote host
	 * must generate a banner line which contains the text from
	 * the bannerMatch argument.</P>
	 *
	 * @param host	The remote host to connect to.
	 * @param port 	The remote port on the host.
	 * @param iorPort The remote port in which to find the IOR via HTTP.
	 *
	 * @return True if a connection is established with the host and the banner line
	 *		contains the bannerMatch text.
	 */
	private boolean isServer(InetAddress host, int port, int retries, int timeout, int iorPort)
	{
		Category log = ThreadCategory.getInstance(getClass());

		boolean isAServer = false;
		for (int attempts=0; attempts <= retries && !isAServer; attempts++)
		{
			// Lets first try to the the IOR via HTTP, if we can't get that then any other process that can
			// do it the right way won't be able to connect anyway
			//
			try {
				String IOR = retrieveIORText(host.getHostAddress(), iorPort);
			} 
			catch(FileNotFoundException e)
			{
                                // This is an expected exception
                                //
                                isAServer = false;
			}
			catch (Exception e)
			{
				if(log.isDebugEnabled())
					log.debug("DominoIIOPMonitor: failed to get the corba IOR from " + host.getHostAddress(), e);
				isAServer = false;
				break;
			}
			
                        SocketChannel sChannel = null;
			try
			{
				//
				// create a connected socket
				//
                                sChannel = SocketChannelUtil.getConnectedSocketChannel(host, port, timeout);
                                if (sChannel == null)
                                {
                                        log.debug("DominoIIOPPlugin: did not connect to host within timeout: " + timeout +" attempt: " + attempts);
                                        continue;
                                }
                                log.debug("DominoIIOPPlugin: connected to host: " + host + " on port: " + port);
	
				isAServer = true;
			}
			catch(ConnectException e)
			{
				// Connection refused!!  No need to perform retries.
				//
				log.debug("DominoIIOPMonitor: Connection refused to " + host.getHostAddress() + ":" + port);
				isAServer = false;
				break;
			}
			catch(NoRouteToHostException e)
			{
				// No Route to host!!!
				//
				e.fillInStackTrace();
				log.info("DominoIIOPMonitor: Could not connect to host " + host.getHostAddress() + ", no route to host", e);
				isAServer = false;
				throw new UndeclaredThrowableException(e);
			}
			catch(InterruptedIOException e)
			{
				// This is an expected exception
				//
				isAServer = false;
			}
			catch(IOException e)
			{
				log.info("DominoIIOPMonitor: An expected I/O exception occured connecting to host " + host.getHostAddress() + " on port " + port, e);
				isAServer = false;
			}
			catch(Throwable t)
			{
				isAServer = false;
				log.warn("DominoIIOPMonitor: An undeclared throwable exception was caught connecting to host " + host.getHostAddress()
					 + " on port " + port, t);
			}
			finally
			{
				try
				{
					if(sChannel != null)
						sChannel.close();
				}
				catch(IOException e) { }
			}
		}

		//
		// return the success/failure of this
		// attempt to contact an IIOP server.
		//
		return isAServer;
	}
	
	/**
	 * Method used to retrieve the IOR string from the Domino server.
	 * @param host, the host name which has the IOR
	 * @param port, the port to find the IOR via HTTP
	 */
	private String retrieveIORText(String host, int port)
		throws IOException
	{
	       String IOR = "";
	       java.net.URL u = new java.net.URL("http://" + host + ":" + port + "/diiop_ior.txt");
	       java.io.InputStream is = u.openStream();
	       java.io.BufferedReader dis = new java.io.BufferedReader(new java.io.InputStreamReader(is));
	       boolean done = false;
	       while (!done)
	       {
		       String line = dis.readLine();
		       if (line == null)
		       {
			       // end of stream
			       done = true;
		       }
		       else
		       {
			       IOR += line;
			       if (IOR.startsWith("IOR:"))
			       {
				       // the IOR does not span a line, so we're done
				       done = true;
			       }
		       }
	       }
	       dis.close();
	       
	       if (!IOR.startsWith("IOR:"))
		       throw new IOException("Invalid IOR: " + IOR);
	       
	       return IOR;
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
	 *
	 * @throws java.lang.UnsupportedOperationException This is always
	 * 	thrown by this plugin.
	 */
	public boolean isProtocolSupported(InetAddress address)
	{
		throw new UnsupportedOperationException("Undirected TCP checking not supported");
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
		int retries = DEFAULT_RETRY;
		int timeout = DEFAULT_TIMEOUT;
		int port    = DEFAULT_PORT;
		int iorPort = DEFAULT_IORPORT;

		if(qualifiers != null)
		{
			retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
			timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
			port    = ParameterMap.getKeyedInteger(qualifiers, "port", DEFAULT_PORT);
			iorPort = ParameterMap.getKeyedInteger(qualifiers, "ior-port", DEFAULT_IORPORT);
		}

		
		try
		{
			boolean result = isServer(address, port, retries, timeout, iorPort);
			
			return result;
		}
		catch(Exception e)
		{
			throw new java.lang.reflect.UndeclaredThrowableException(e);
		}
	}
}
