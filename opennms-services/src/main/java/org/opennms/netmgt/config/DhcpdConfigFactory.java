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
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.dhcpd.DhcpdConfiguration;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * DHCP client deamon from the dhcpd-configuration xml file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class DhcpdConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static DhcpdConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private DhcpdConfiguration m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private DhcpdConfigFactory(File configFile) throws IOException, MarshalException, ValidationException {
        Reader cfgIn = new FileReader(configFile);
        m_config = (DhcpdConfiguration) Unmarshaller.unmarshal(DhcpdConfiguration.class, cfgIn);
        cfgIn.close();

    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.DHCPD_CONFIG_FILE_NAME);
        m_singleton = new DhcpdConfigFactory(configFile);

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized DhcpdConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * Return the TCP port on which the DHCP client daemon is to listen for
     * incoming client connections.
     *
     * @return the DHCP client daemon port.
     */
    public synchronized int getPort() {
        return m_config.getPort();
    }

    /**
     * Return the MAC address to be used in outgoing DHCP packets generated
     * by the DHCP client daemon.
     *
     * @return string mac address
     */
    public synchronized String getMacAddress() {
        return m_config.getMacAddress();
    }

    /**
     * Return the IP address to be used as the relay
     * address in outgoing DHCP packets generated
     * by the DHCP client daemon.
     *
     * @return string ip address
     */
    public synchronized String getMyIpAddress() {
        return m_config.getMyIpAddress();
    }


    /**
     * Return the IP address to be used
     * in outgoing DHCP REQUEST packets generated
     * by the DHCP client daemon.
     *
     * @return string ip address
     */
    public synchronized String getRequestIpAddress() {
        return m_config.getRequestIpAddress();
    }

    /**
     * Return the string value of the extended mode option
     * for the DHCP client daemon.
     *
     * @return string extended mode
     */
    public synchronized String getExtendedMode() {
        return m_config.getExtendedMode();
    }
}
