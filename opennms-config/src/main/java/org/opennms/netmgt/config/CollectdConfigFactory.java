/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Collection Daemon from the collectd-configuration.xml file.
 *
 * A mapping of the configured URLs to the IP list they contain is built at
 * init() time so as to avoid numerous file reads.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo</a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 */
public class CollectdConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CollectdConfigFactory.class);
    public static final String SELECT_METHOD_MIN = "min";

    private CollectdConfig m_collectdConfig;
    private final Object m_collectdConfigMutex = new Object();

    private final String m_fileName;
    private final String m_serverName;
    private final boolean m_verifyServer;

    static {
        // Make sure that the OpennmsServerConfigFactory is initialized
        try {
            OpennmsServerConfigFactory.init();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public CollectdConfigFactory() throws IOException {
        m_fileName = ConfigFileConstants.getFile(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME).getPath();
        m_serverName = OpennmsServerConfigFactory.getInstance().getServerName();
        m_verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();

        init(new FileInputStream(m_fileName), m_serverName, m_verifyServer);
    }

    /**
     * For testing purposes only.
     * 
     * @param stream
     * @param serverName
     * @param verifyServer
     * @throws IOException
     */
    public CollectdConfigFactory(InputStream stream, String serverName, boolean verifyServer) throws IOException {
        m_fileName = null;
        m_serverName = serverName;
        m_verifyServer = verifyServer;

        init(stream, m_serverName, m_verifyServer);
    }

    /**
     * <p>Constructor for CollectdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param localServer a {@link java.lang.String} object.
     * @param verifyServer a boolean.
     * @throws IOException 
     */
    private void init(final InputStream stream, final String localServer, boolean verifyServer) throws IOException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(stream);
            CollectdConfiguration config = JaxbUtils.unmarshal(CollectdConfiguration.class, isr);
            synchronized (m_collectdConfigMutex) {
                m_collectdConfig = new CollectdConfig(config, localServer, verifyServer);
            }
        } finally {
            IOUtils.closeQuietly(isr);
        }
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public void reload() throws IOException {
        init(new FileInputStream(m_fileName), m_serverName, m_verifyServer);
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException if any.
     */
    public void saveCurrent() throws IOException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME);

        CollectdConfiguration config = null;
        synchronized (m_collectdConfigMutex) {
            config = m_collectdConfig.getConfig();
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(cfgFile);
            JaxbUtils.marshal(config, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }

        reload();
    }

    /**
     * <p>getCollectdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.CollectdConfig} object.
     */
    public CollectdConfig getCollectdConfig() {
        synchronized (m_collectdConfigMutex) {
            return m_collectdConfig;
        }
    }

    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.CollectdPackage} object.
     */
    public CollectdPackage getPackage(String name) {
        synchronized (m_collectdConfigMutex) {
            return m_collectdConfig.getPackage(name);
        }
    }

    /**
     * Returns true if collection package exists
     *
     * @param name
     *            The package name to check
     * @return True if the package exists
     */
    public boolean packageExists(String name) {
        synchronized (m_collectdConfigMutex) {
            return m_collectdConfig.getPackage(name) != null;
        }
    }

    /**
     * Returns true if collection domain exists
     *
     * @param name
     *            The domain name to check
     * @return True if the domain exists
     */
    public boolean domainExists(String name) {
        synchronized (m_collectdConfigMutex) {
            return m_collectdConfig.domainExists(name);
        }
    }

    /**
     * Returns true if the specified interface is included by at least one
     * package which has the specified service and that service is enabled (set
     * to "on").
     *
     * @deprecated This function should take normal model objects instead of bare IP addresses
     * and service names. Use {@link CollectdConfig#isServiceCollectionEnabled(OnmsIpInterface, String)}
     * instead.
     *
     * @param ipAddr
     *            IP address of the interface to lookup
     * @param svcName
     *            The service name to lookup
     * @return true if Collectd config contains a package which includes the
     *         specified interface and has the specified service enabled.
     */
    public boolean isServiceCollectionEnabled(final String ipAddr, final String svcName) {
        synchronized (m_collectdConfigMutex) {
            return m_collectdConfig.isServiceCollectionEnabled(ipAddr, svcName);
        }
    }
}
