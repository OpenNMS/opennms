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
import java.net.UnknownHostException;
import java.net.NoRouteToHostException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import com.novell.ldap.*;

/**
 * <P>This class is designed to be used by the capabilities
 * daemon to test for the existance of an LDAP server on 
 * remote interfaces. The class implements the CapsdPlugin
 * interface that allows it to be used along with other
 * plugins by the daemon.</P>
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="mailto:weave@opennms.org">Brian Weaver</A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 */
public final class LdapPlugin
	extends AbstractPlugin
{
	private static final String	PROTOCOL_NAME	= "LDAP";
	
	/**
	 * <P>The default ports on which the host is checked to see if 
	 * it supports LDAP.</P>
	 */
	private static final int[]	DEFAULT_PORTS	= { LDAPConnection.DEFAULT_PORT  };

	/**
	 * Default number of retries for HTTP requests.
	 */
	private final static int	DEFAULT_RETRY	= 0;
	
	/**
	 * Default timeout (in milliseconds) for HTTP requests.
	 */
	private final static int	DEFAULT_TIMEOUT	= 5000; // in milliseconds
	
        /**
         * A class to add a timeout to the socket that the LDAP code uses to access an
         * LDAP server
         */
        private class TimeoutLDAPSocket implements LDAPSocketFactory
        {
                private int m_timeout;
                
                public TimeoutLDAPSocket(int timeout)
                {
                        m_timeout = timeout;
                }
                
                public Socket makeSocket(String host, int port)
                        throws IOException, UnknownHostException
                {
                        Socket socket = new Socket(host, port);
                        socket.setSoTimeout(m_timeout);
                        return socket;
                }
        }
        
	/**
	 * <P>Test to see if the passed host-port pair is the 
	 * endpoint for an LDAP server. If there is an LDAP server
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
		
                //first just try a connection to the box via socket. Just in case there is
                //a no way to route to the address, don't iterate through the retries, as a
                //NoRouteToHost exception will only be thrown after about 5 minutes, thus tying
                //up the thread
                Socket portal = null;
                try
                {
                        portal = new Socket(host.getHostAddress(), port);
			portal.setSoTimeout(timeout);
                        portal.close();
                        
                        //now go ahead and attempt to determin if LDAP is on this host
                        for (int attempts=0; attempts <= retries && !isAServer; attempts++)
                        {
                                log.debug("LDAPPlugin.isServer: attempt " + attempts + " to connect host " + host.getHostAddress());
                                
                                try {
                                        LDAPConnection lc = new LDAPConnection(new TimeoutLDAPSocket(timeout));
                                        lc.connect(host.getHostAddress(),port);
                                        isAServer = true;
                                } catch (LDAPException e)
                                {
                                        isAServer = false;
                                }
                        }
                }
                catch(ConnectException e)
		{
			// Connection refused!!  No need to perform retries.
			//
			e.fillInStackTrace();
			log.debug(getClass().getName()+": connection refused to host " + host.getHostAddress() , e);
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
			// ignore totally, we expect to get this
			//
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

		return isAServer;
	}

	/**
	 * This method is used to lookup a specific key in 
	 * the map. If the mapped value is a string is is converted
	 * to an interger and the original string value is replaced
	 * in the map. The converted value is returned to the caller.
	 * If the value cannot be converted then the default value is
	 * used.
	 *
	 * @return The int array value associated with the key.
	 */
	final static int[] getKeyedIntegerArray(Map map, String key, int[] defValue)
	{
		int[] result = defValue;
		Object oValue = map.get(key);

		if(oValue != null && oValue instanceof String)
		{
			List list = new ArrayList(8);
			StringTokenizer ntoks = new StringTokenizer(oValue.toString(), ":,; ");
			while(ntoks.hasMoreTokens())
			{
				String p = ntoks.nextToken();
				try
				{
					int v = Integer.parseInt(p);
					list.add(new Integer(v));
				}
				catch(NumberFormatException ne)
				{
					ThreadCategory.getInstance(HttpPlugin.class).info("getKeyedIntegerArray: Failed to convert token " + p + " for key " + key);
				}
			}
			result = new int[list.size()];
			Iterator i = list.iterator();
			int ndx = 0;
			while(i.hasNext())
				result[ndx++] = ((Integer)i.next()).intValue();
			
			map.put(key, result);
		} 
		else if(oValue != null)
		{
			result = ((int[])oValue);
		}
		return result;
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
		int retries = getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
		int timeout = getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
		int[] ports = getKeyedIntegerArray(qualifiers, "ports", DEFAULT_PORTS);

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


