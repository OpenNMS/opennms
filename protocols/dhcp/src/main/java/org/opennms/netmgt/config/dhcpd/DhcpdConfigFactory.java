/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.dhcpd;

import java.io.File;
import java.io.IOException;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;

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
     * Default constructor (used by test as was as static methods)
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     */
    DhcpdConfigFactory(File configFile) {
    	m_config = JaxbUtils.unmarshal(DhcpdConfiguration.class, configFile);
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
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
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
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
