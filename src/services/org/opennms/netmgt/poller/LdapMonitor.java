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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.nio.channels.SocketChannel;
import org.opennms.netmgt.utils.SocketChannelUtil;

import java.net.*;

import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;

import com.novell.ldap.*;

import org.opennms.netmgt.utils.ParameterMap;

/**
 * <P>This class is designed to be used by the service poller
 * framework to test the availability of a generic LDAP service on 
 * remote interfaces. The class implements the ServiceMonitor
 * interface that allows it to be used along with other
 * plug-ins by the service poller framework.</P>
 *
 * @author <A HREF="jason@opennms.org">Jason</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class LdapMonitor
	extends IPv4Monitor
{	
	/** 
	 * Default retries.
	 */
	private static final int DEFAULT_RETRY = 1;

	/** 
	 * Default timeout. Specifies how long (in milliseconds) to block waiting
	 * for data from the monitored interface.
	 */
	private static final int DEFAULT_TIMEOUT 	= 3000; // 3 second timeout on read()
	
	/**
	 * Default search base for an LDAP search
	 */
	private static final String DEFAULT_BASE = "base";
	
	/**
	 * Default search filter for an LDAP search
	 */
	private static final String DEFAULT_FILTER = "(objectclass=*)";
	
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
	 * <P>Poll the specified address for service availability</P>
	 *
	 * <P>During the poll an attempt is made to connect the service
	 *
	 * Provided that the interface's response is valid we set the
	 * service status to SERVICE_AVAILABLE and return.</P>
	 *
	 * @param iface		The network interface to test the service on.
	 * @param parameters	The package parameters (timeout, retry, etc...) to be 
	 *  used for this poll.
	 *
	 * @return The availibility of the interface and if a transition event
	 * 	should be supressed.
	 *
	 * @throws java.lang.RuntimeException Thrown if the interface experiences errors
	 * 	during the poll.
	 */
	public int poll(NetworkInterface iface, Map parameters) 
	{
		Category log = ThreadCategory.getInstance(getClass());
		int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
		
		// get the parameters
		//
		int ldapVersion = ParameterMap.getKeyedInteger(parameters, "version", LDAPConnection.LDAP_V3);
		int ldapPort = ParameterMap.getKeyedInteger(parameters, "port", LDAPConnection.DEFAULT_PORT);
		int retries = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
		int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
		String searchBase = ParameterMap.getKeyedString(parameters, "searchbase", DEFAULT_BASE);
		String searchFilter = ParameterMap.getKeyedString(parameters, "searchfilter", DEFAULT_FILTER);
		
		String password = (String)parameters.get("password");
		String ldapDn   = (String)parameters.get("dn");
		
		Object addressObject = iface.getAddress();
		String address = null;
		if (addressObject instanceof InetAddress)
			address = ((InetAddress)addressObject).getHostAddress();
		else if (addressObject instanceof String)
			address = (String)addressObject;
		
		//first just try a connection to the box via socket. Just in case there is
		//a no way to route to the address, don't iterate through the retries, as a
		//NoRouteToHost exception will only be thrown after about 5 minutes, thus tying
		//up the thread
                SocketChannel sChannel = null;
		try
		{
                        sChannel = SocketChannelUtil.getConnectedSocketChannel((InetAddress)iface.getAddress(), ldapPort, timeout);
                        if (sChannel == null)
                        {
                                log.debug("LdapMonitor: did not connect to host within timeout: " + timeout);
                        }
                        log.debug("LdapMonitor: connected to host: " + address + " on port: " + ldapPort);

			// We're connected, so upgrade status to unresponsive
			serviceStatus = SERVICE_UNRESPONSIVE;
                        if(sChannel != null)
                                sChannel.close();
			
			//lets detect the service
			LDAPConnection lc = new LDAPConnection(new TimeoutLDAPSocket(timeout));
			
			for (int attempts = 1; attempts <= retries && serviceStatus != ServiceMonitor.SERVICE_AVAILABLE; attempts++)
			{
				log.debug("polling LDAP on " + address + ", attempt " + attempts + " of " + (retries==0 ? "1" : retries+""));
				
				//connect to the ldap server
				try {
					lc.connect(address,ldapPort);
					log.debug("connected to LDAP server " + address + " on port " + ldapPort);
				} catch (LDAPException e)
				{
					log.debug("could not connect to LDAP server " + address + " on port " + ldapPort);
					continue;
				}
				
				//bind if possible
				if (ldapDn != null && password != null)
				{
					try {
						lc.bind(ldapVersion, ldapDn, password);
						log.debug("bound to LDAP server version " + ldapVersion + " with distinguished name " + ldapDn);
					} catch (LDAPException e)
					{
						try {
							lc.disconnect();
						} catch(LDAPException ex)
						{ log.debug(ex); }
						
						log.debug("could not bind to LDAP server version " + ldapVersion + " with distinguished name " + ldapDn);
						continue;
					}
				}
				
				//do a quick search and see if any results come back
				boolean attributeOnly = true;
				String attrs[] = {LDAPConnection.NO_ATTRS};
				int searchScope = LDAPConnection.SCOPE_ONE;
				
				log.debug("running search " + searchFilter + " from " + searchBase);
				LDAPSearchResults results = null;
				
				try {
					results = lc.search(searchBase, searchScope, searchFilter, attrs, attributeOnly);
					
					if (results !=null && results.hasMoreElements())
					{
						log.debug("search yielded results");
						serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
					}
					else
					{
						log.debug("no results found from search");
						serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
					}
				} catch(LDAPException e)
				{
					try {
						lc.disconnect();
					} catch(LDAPException ex)
					{ log.debug(ex); }
					
					log.debug("could not perform search " + searchFilter + " from " + searchBase);
					continue;
				}
				
				try {
					lc.disconnect();
					log.debug("disconected from LDAP server "+ address + " on port ");
				} catch(LDAPException e)
				{ log.debug(e); }
			}
		}
                catch(InterruptedException e)
                {
                        // Ignore
                        e.fillInStackTrace();
                        if(log.isDebugEnabled())
                                log.debug("LdapMonitor: Interrupted exception for address: " + address, e);
                }
		catch(ConnectException e)
		{
			// Connection refused!!  No need to perform retries.
			//
			e.fillInStackTrace();
			log.debug(getClass().getName()+": connection refused to host " + address , e);
		}
		catch(NoRouteToHostException e)
		{
			// No route to host!! No need to perform retries.
			e.fillInStackTrace();
			log.warn(getClass().getName()+": No route to host " + address, e);
		}
		catch(InterruptedIOException e)
		{
			// ignore totally, we expect to get this
			//
		}
		catch(Throwable t)
		{
			log.warn(getClass().getName()+": An undeclared throwable exception caught contacting host " + address, t);
		}
		
		return serviceStatus;
	}
}
