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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.*;
import java.util.*;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

// castor classes generated from dhcpd-configuration.xsd
import org.opennms.netmgt.config.dhcpd.*;

import org.opennms.netmgt.ConfigFileConstants;

/**
 * <p>This is the singleton class used to load the configuration for
 * the OpenNMS DHCP client deamon from the dhcpd-configuration.xml.</p>
 *
 * <p><strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods</p>
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public final class DhcpdConfigFactory
{
	/**
	 * The singleton instance of this factory
	 */
	private static DhcpdConfigFactory	m_singleton=null;

	/**
	 * The config class loaded from the config file
	 */
	private DhcpdConfiguration		m_config;

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
	private DhcpdConfigFactory(String configFile)
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		InputStream cfgIn = new FileInputStream(configFile);

		m_config = (DhcpdConfiguration) Unmarshaller.unmarshal(DhcpdConfiguration.class, new InputStreamReader(cfgIn));
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

		String homeDir = System.getProperty("opennms.home");
		if(homeDir == null)
			throw new IOException("Unable to get required property \'opennms.home\'");
		

		// Form the complete filename for the config file
		String configFile;
		if (homeDir.endsWith(File.separator))
		{
			configFile = homeDir + "etc" + File.separator + ConfigFileConstants.DHCPD_CONFIG_FILE_NAME;
		}
		else if (homeDir.endsWith("/"))
			// here because the user can still use the '/'
			// delimited name for the directory on the windows platform
		{
			configFile = homeDir + "etc/" + ConfigFileConstants.DHCPD_CONFIG_FILE_NAME;
		}
		else
		{
			configFile = homeDir + File.separator + "etc" + File.separator + ConfigFileConstants.DHCPD_CONFIG_FILE_NAME;
		}

		m_singleton = new DhcpdConfigFactory(configFile);

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
	public static synchronized DhcpdConfigFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/**
	 * <p>Return the TCP port on which the DHCP client daemon is to listen
	 * for incoming client connections.</p>
	 *
	 * @return the DHCP client daemon port. 
	 */
	public synchronized int getPort()
	{
		return m_config.getPort();
	}

	/**
	 * <p>Return the MAC address to be used in all DHCP DISCOVER packets generated by
     	 * the DHCP client daemon.</p> 
	 *
	 * @return string indicating if timeout is to be set on the socket
	 */
	public synchronized String getMacAddress()
	{
		return m_config.getMacAddress();
	}
}
