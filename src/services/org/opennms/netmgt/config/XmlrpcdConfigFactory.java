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
// 2004 Jan 13: Added this XML RPC Daemon
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
package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.xmlrpcd.SubscribedEvent;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcServer;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcdConfiguration;

/**
 * <p>This is the singleton class used to load the configuration for
 * the OpenNMS xmlrpcd service from the xmlrpcd-configuration.xml.</p>
 *
 * <p><strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods</p>
 *
 * @author <a href="mailto:jamesz@blast.com">James Zuo</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public final class XmlrpcdConfigFactory
{
	/**
	 * The singleton instance of this factory
	 */
	private static XmlrpcdConfigFactory	m_singleton = null;

	/**
	 * This member is set to true if the configuration file
	 * has been loaded.
	 */
	private static boolean			m_loaded = false;

	/**
	 * The config class loaded from the config file
	 */
	private XmlrpcdConfiguration		m_config;

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
	private XmlrpcdConfigFactory(String configFile)
		throws 	IOException,
			MarshalException, 
			ValidationException
	{
		InputStream cfgIn = new FileInputStream(configFile);

		m_config = (XmlrpcdConfiguration) Unmarshaller.unmarshal( XmlrpcdConfiguration.class, 
                                                                          new InputStreamReader(cfgIn));
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

		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.XMLRPCD_CONFIG_FILE_NAME); 

		ThreadCategory.getInstance(XmlrpcdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());
		
		m_singleton = new XmlrpcdConfigFactory(cfgFile.getPath());

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
	public static synchronized XmlrpcdConfigFactory getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/** 
	 * <p>Return the xmlrpcd configuration object</p>
	 */
	public synchronized XmlrpcdConfiguration getConfiguration()
	{
		return m_config;
	}

	/**
	 * <p>This method is used to determine if an event of the named 
         * represented is subscribed by the external XMLRPC server.</p>
	 *
	 * @param uei	The uei to test against the subscribed events in 
         *              the configuration file.
	 *
	 * @return	True if named uei is subscribed in the subscribed 
         *              event collection of the configuration file.
	 */
	public synchronized boolean eventSubscribed(String uei)
	{
		Category log = ThreadCategory.getInstance(this.getClass());
                
                Enumeration eventEnum = m_config.getSubscription().enumerateSubscribedEvent();
                boolean isSubscribed = false;
                
                while(eventEnum.hasMoreElements())
                {
                        SubscribedEvent sEvent = (SubscribedEvent)eventEnum.nextElement();
                        if ((sEvent.getUei()).equals(uei))
                        {
                                isSubscribed = true;
		                
                                if (log.isDebugEnabled())
			                log.debug("eventSubscribed: Event " + uei + " is subscribed.");
                                break;
                        }
                }

                return isSubscribed;
	}
	
        /**
         * Retrieves configured list of subscribed event uei.
         *
         * @return an enumeration of subscribed event ueis.
         */
        public synchronized Enumeration getEventEnumeration()
        {
                return m_config.getSubscription().enumerateSubscribedEvent();
        }
        
        /**
         * Retrieves configured list of external xmlrpc servers.
         *
         * @return an enumeration of xmlrpc servers.
         */
        public synchronized Enumeration getXmlrpcServerEnumeration()
        {
                return m_config.getExternalServers().enumerateXmlrpcServer();
        }
        
        /**
         * Retrieves configured list of external xmlrpc servers.
         *
         * @return an array of xmlrpc servers.
         */
        public synchronized XmlrpcServer[] getXmlrpcServer()
        {
                return m_config.getExternalServers().getXmlrpcServer();
        }
        
        /**
         * Retrieves configured retry number for xmlrpc communication.
         *
         * @return the retry number.
         */
        public synchronized int getRetries()
        {
                return  m_config.getExternalServers().getRetries();
        }
        
        /**
         * Retrieves configured elapse time between retries for xmlrpc communication.
         *
         * @return the elapse time.
         */
        public synchronized int getElapseTime()
        {
                return  m_config.getExternalServers().getElapseTime();
        }
        /**
         * Retrieves the max event queue size from configuration.
         *
         * @return the max size of the xmlrpcd event queue.
         */
        public synchronized int getMaxQueueSize()
        {
                return  m_config.getMaxEventQueueSize();
        }
}
