/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.springframework.core.io.FileSystemResource;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Trapd from the trapd-configuration.xml.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class TrapdConfigFactory implements TrapdConfig {
    /**
     * The singleton instance of this factory
     */
    private static TrapdConfig m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private TrapdConfiguration m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     */
    private TrapdConfigFactory(String configFile) throws IOException {
        m_config = JaxbUtils.unmarshal(TrapdConfiguration.class, new FileSystemResource(configFile));
    }
    
    /**
     * <p>Constructor for TrapdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws IOException 
     */
    public TrapdConfigFactory(InputStream stream) throws IOException {
        try(final Reader reader = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(TrapdConfiguration.class, reader);
        }
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
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.TRAPD_CONFIG_FILE_NAME);

        m_singleton = new TrapdConfigFactory(cfgFile.getPath());

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
    public static synchronized TrapdConfig getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param config a {@link org.opennms.netmgt.config.TrapdConfig} object.
     */
    public static synchronized void setInstance(TrapdConfig config) {
        m_singleton = config;
        m_loaded = true;
    }

    @Override
    public synchronized String getSnmpTrapAddress() {
    	return m_config.getSnmpTrapAddress();
    }

    /**
     * Return the port on which SNMP traps should be received.
     *
     * @return the port on which SNMP traps should be received
     */
    @Override
    public synchronized int getSnmpTrapPort() {
        return m_config.getSnmpTrapPort();
    }

    /**
     * Return whether or not a newSuspect event should be sent when a trap is
     * received from an unknown IP address.
     *
     * @return whether to generate newSuspect events on traps.
     */
    @Override
    public synchronized boolean getNewSuspectOnTrap() {
        return m_config.getNewSuspectOnTrap();
    }

    @Override
    public synchronized List<SnmpV3User> getSnmpV3Users() {
        List<SnmpV3User> snmpUsers = new ArrayList<>();
        for (Snmpv3User user : m_config.getSnmpv3UserCollection()) {
            snmpUsers.add(new SnmpV3User(
                    user.getEngineId(),
                    user.getSecurityName(),
                    user.getAuthProtocol(),
                    user.getAuthPassphrase(),
                    user.getPrivacyProtocol(),
                    user.getPrivacyPassphrase(),
                    user.getSecurityLevel()));
        }
        return snmpUsers;
    }

    @Override
    public boolean isIncludeRawMessage() {
        return m_config.isIncludeRawMessage();
    }

    @Override
    public int getNumThreads() {
        if (m_config.getThreads() <= 0) {
            return Runtime.getRuntime().availableProcessors() * 2;
        }
        return m_config.getThreads();
    }

    @Override
    public int getQueueSize() {
        return m_config.getQueueSize();
    }

    @Override
    public int getBatchSize() {
        return m_config.getBatchSize();
    }

    @Override
    public int getBatchIntervalMs() {
        return m_config.getBatchInterval();
    }

    @Override
    public boolean shouldUseAddressFromVarbind() {
        return m_config.shouldUseAddressFromVarbind();
    }

    @Override
    public void update(TrapdConfig config) {
        m_config.setSnmpTrapAddress(config.getSnmpTrapAddress());
        m_config.setSnmpTrapPort(config.getSnmpTrapPort());
        m_config.setNewSuspectOnTrap(config.getNewSuspectOnTrap());
        m_config.setQueueSize(config.getQueueSize());
        m_config.setBatchSize(config.getBatchSize());
        m_config.setBatchInterval(config.getBatchIntervalMs());
        m_config.setThreads(config.getNumThreads());
        m_config.setIncludeRawMessage(config.isIncludeRawMessage());

        final List<Snmpv3User> snmpv3Users = config.getSnmpV3Users().stream().map(u -> {
            Snmpv3User newUser = new Snmpv3User();
            newUser.setEngineId(u.getEngineId());
            newUser.setSecurityName(u.getSecurityName());
            newUser.setSecurityLevel(u.getSecurityLevel());
            newUser.setAuthProtocol(u.getAuthProtocol());
            newUser.setAuthPassphrase(u.getAuthPassPhrase());
            newUser.setPrivacyProtocol(u.getPrivProtocol());
            newUser.setPrivacyPassphrase(u.getPrivPassPhrase());
            return newUser;
        }).collect(Collectors.toList());
        m_config.setSnmpv3User(snmpv3Users);
    }

    public TrapdConfiguration getConfig() {
        return m_config;
    }
}
