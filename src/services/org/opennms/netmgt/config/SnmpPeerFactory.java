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
package org.opennms.netmgt.config;

import java.lang.*;
import java.io.*;
import java.util.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;

import org.opennms.netmgt.ConfigFileConstants;

// castor classes generated from the snmp-config.xsd
import org.opennms.netmgt.config.capsd.*;

/**
 * This class is the main respository for SNMP configuration information
 * used by the capabilities daemon. When this class is loaded it reads
 * the snmp configuration into memory, and uses the configuration to find
 * the {@link org.opennms.protocols.snmp.SnmpPeer SnmpPeer} objects for
 * specific addresses. If an address cannot be located in the configuration
 * then a default peer instance is returned to the caller.
 *
 * <p><strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods</p>
 *
 * @author <a href="mailto:weave@opennms.org">Weave</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
public final class SnmpPeerFactory
{
	/**
	 * The singleton instance of this factory
	 */
	private static SnmpPeerFactory		m_singleton=null;

	/**
	 * The config class loaded from the config file
	 */
	private SnmpConfig			m_config;

	/**
	 * This member is set to true if the configuration file
	 * has been loaded.
	 */
	private static boolean			m_loaded=false;

	/**
	 * Private constructor
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	private SnmpPeerFactory(String configFile)
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		InputStream cfgIn = new FileInputStream(configFile);

		m_config = (SnmpConfig) Unmarshaller.unmarshal(SnmpConfig.class, new InputStreamReader(cfgIn));
		cfgIn.close();

	}

	/**
	 * Load the config from the default config file and create the 
	 * singleton instance of this factory.
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void init()
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		if (m_loaded)
		{
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}

		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME);

		ThreadCategory.getInstance(SnmpPeerFactory.class).debug("init: config file path: " + cfgFile.getPath());
		
		m_singleton = new SnmpPeerFactory(cfgFile.getPath());

		m_loaded = true;
	}

	/**
	 * Reload the config from the default config file
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read/loaded
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void reload()
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		m_singleton = null;
		m_loaded    = false;

		init();
	}

	/**
	 * <p>Return the singleton instance of this factory<p>
	 *
	 * @return The current factory instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the factory
	 * 	has not yet been initialized.
	 */
	public static synchronized SnmpPeerFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/**
	 * Converts the internet address to a long value so that
	 * it can be compared using simple opertions. The address
	 * is converted in network byte order (big endin) and 
	 * allows for comparisions like &lt;, &gt;, &lt;=,
	 * &gt;=, ==, and !=.
	 *
	 * @param addr	The address to convert to a long
	 *
	 * @return The address as a long value.
	 *
	 */
	private static long toLong(InetAddress addr)
	{
		byte[] baddr = addr.getAddress();
		long result = ((long)baddr[0] & 0xffL) << 24
			    | ((long)baddr[1] & 0xffL) << 16
			    | ((long)baddr[2] & 0xffL) << 8
			    | ((long)baddr[3] & 0xffL);

		return result;
	}

	/**
	 * this method uses the passed address and definition to construct
	 * an appropriate SNMP peer object for use by an SnmpSession. 
	 *
	 * @param addr	The address to construct the snmp peer instance.
	 * @param def	The definition containing the appropriate information.
	 *
	 * @return The SnmpPeer matching for the passed address.
	 */
	private SnmpPeer create(InetAddress addr, Definition def)
	{
	 	return create(addr, def, -1);
	}
	
	/**
	 * this method uses the passed address and definition to construct
	 * an appropriate SNMP peer object for use by an SnmpSession. 
	 *
	 * @param addr	The address to construct the snmp peer instance.
	 * @param def	The definition containing the appropriate information.
	 * @param supportedSnmpVersion	SNMP version to associate with the 
	 * 					peer object if SNMP version has
	 * 					not been explicitly configured.
	 *
	 * @return The SnmpPeer matching for the passed address.
	 */
	private SnmpPeer create(InetAddress addr, Definition def, int supportedSnmpVersion)
	{
		// Allocate a new SNMP parameters
		//
		SnmpParameters parms = new SnmpParameters();

		// get the version information, if any
		//
		// If version information is provided it will be used...
		// if not then the passed supportedSnmpVersion variable
		// will be used to set the peer's SNMP version.
		//
		if(def.getVersion() != null)
		{
			if(def.getVersion().equals("v1"))
				parms.setVersion(SnmpSMI.SNMPV1);
			else if(def.getVersion().equals("v2c"))
				parms.setVersion(SnmpSMI.SNMPV2);
		}
		else
		{
			// Verify valid SNMP version provided
			if (supportedSnmpVersion == SnmpSMI.SNMPV1 || 
				 supportedSnmpVersion == SnmpSMI.SNMPV2)
				parms.setVersion(supportedSnmpVersion);
			else
				parms.setVersion(SnmpSMI.SNMPV1);
		} 

		// setup the read community
		//
		if(def.getReadCommunity() != null)
		{
			parms.setReadCommunity(def.getReadCommunity());
		}
		else if(m_config.getReadCommunity() != null)
		{
			parms.setReadCommunity(m_config.getReadCommunity());
		}

		// setup the write community
		//
		if(def.getWriteCommunity() != null)
		{
			parms.setWriteCommunity(def.getWriteCommunity());
		}
		else if(m_config.getWriteCommunity() != null)
		{
			parms.setWriteCommunity(m_config.getWriteCommunity());
		}

		// Allocate a peer for this address
		// and set the parameters
		//
		SnmpPeer peer = new SnmpPeer(addr);
		peer.setParameters(parms);

		// setup the retries
		//
		if(def.hasRetry())
		{
			peer.setRetries(def.getRetry());
		}
		else if(m_config.hasRetry())
		{
			peer.setRetries(m_config.getRetry());
		}

		// setup the timeout
		//
		if(def.hasTimeout())
		{
			peer.setTimeout(def.getTimeout());
		}
		else if(m_config.hasTimeout())
		{
			peer.setTimeout(m_config.getTimeout());
		}

		// check for port changes
		//
		if(def.hasPort())
			peer.setPort(def.getPort());

		// return the peer
		//
		return peer;
	}

	/**
	 * This method is used by the Capabilities poller to lookup the
	 * SNMP peer information associated with the passed host. If 
	 * there is no specific information available then a default
	 * SnmpPeer instance is returned to the caller.
	 *
	 * @param host	The host for locating the SnmpPeer information.
	 *
	 * @return The configured SnmpPeer information.
	 *
	 */
	public synchronized SnmpPeer getPeer(InetAddress host)
	{
		return getPeer(host, -1);
	}
	
	/**
	 * This method is used by the Capabilities poller to lookup the
	 * SNMP peer information associated with the passed host. If 
	 * there is no specific information available then a default
	 * SnmpPeer instance is returned to the caller.
	 *
	 * @param host	The host for locating the SnmpPeer information.
	 * @param supportedSnmpVersion	SNMP version to associate with the 
	 * 					peer object if SNMP version has
	 * 					not been explicitly configured.
	 *
	 * @return The configured SnmpPeer information.
	 *
	 */
	public synchronized SnmpPeer getPeer(InetAddress host, int supportedSnmpVersion)
	{
		// Verify configuration information present!
		//
		if (m_config == null)
		{
			SnmpPeer peer = new SnmpPeer(host);
			
			// Verify valid SNMP version provided
			if (supportedSnmpVersion == SnmpSMI.SNMPV1 || 
				 supportedSnmpVersion == SnmpSMI.SNMPV2)
			{
				peer.getParameters().setVersion(supportedSnmpVersion);
			}
			
			return peer;
		}
		
		SnmpPeer peer = null;
		
		// Attempt to locate the node
		//
		Enumeration edef = m_config.enumerateDefinition();
		DEFLOOP: while(edef.hasMoreElements())
		{
			Definition def = (Definition)edef.nextElement();
			
			// check the specifics first
			//
			Enumeration espec = def.enumerateSpecific();
			while(espec.hasMoreElements())
			{
				String saddr = (String)espec.nextElement();
				try
				{
					InetAddress addr = InetAddress.getByName(saddr);
					if(addr.equals(host))
					{
						// get the information
						peer = create(addr, def, supportedSnmpVersion);
						break DEFLOOP;
					}
				}
				catch(UnknownHostException e)
				{
					Category log = ThreadCategory.getInstance(getClass());
					log.warn("SnmpPeerFactory: could not convert host " + saddr + " to InetAddress", e);
				}
			}
	
			// check the ranges
			//
			long lhost = toLong(host);
			Enumeration erange = def.enumerateRange();
			while(erange.hasMoreElements())
			{
				Range rng = (Range)erange.nextElement();
				try
				{
					InetAddress begin = InetAddress.getByName(rng.getBegin());
					InetAddress end   = InetAddress.getByName(rng.getEnd());
	
					long start = toLong(begin);
					long stop  = toLong(end);

					if(start <= lhost && lhost <= stop)
					{
						peer = create(host, def, supportedSnmpVersion);
						break DEFLOOP;
					}
				}
				catch(UnknownHostException e)
				{
					Category log = ThreadCategory.getInstance(getClass());
					log.warn("SnmpPeerFactory: could not convert host(s) "
						+ rng.getBegin() + " - "
						+ rng.getEnd() + " to InetAddress", e);
				}
			}
		} // end DEFLOOP

		if(peer == null)
		{
			// try defaults!
			//
			peer = new SnmpPeer(host);
			
			// Verify valid SNMP version provided
			if (supportedSnmpVersion == SnmpSMI.SNMPV1 || 
				 supportedSnmpVersion == SnmpSMI.SNMPV2)
			{
				peer.getParameters().setVersion(supportedSnmpVersion);
			}
			
			if(m_config.getReadCommunity() != null || m_config.getWriteCommunity() != null)
			{
				if(m_config.getReadCommunity() != null)
					peer.getParameters().setReadCommunity(m_config.getReadCommunity());
		
				if(m_config.getWriteCommunity() != null)
					peer.getParameters().setWriteCommunity(m_config.getWriteCommunity());
			}

			if(m_config.hasRetry())
				peer.setRetries(m_config.getRetry());

			if(m_config.hasTimeout())
				peer.setTimeout(m_config.getTimeout());
		}

		return peer;

	} // end getPeer();
}
