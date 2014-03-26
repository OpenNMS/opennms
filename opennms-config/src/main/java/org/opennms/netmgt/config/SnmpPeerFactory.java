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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.AddressSnmpConfigVisitor;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

/**
 * This class is the main repository for SNMP configuration information used by
 * the capabilities daemon. When this class is loaded it reads the snmp
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.netmgt.snmp.SnmpAgentConfig SnmpAgentConfig} objects for specific
 * addresses. If an address cannot be located in the configuration then a
 * default peer instance is returned to the caller.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner</a>
 */
public class SnmpPeerFactory implements SnmpAgentConfigFactory {
    public static final Logger LOG = LoggerFactory.getLogger(SnmpPeerFactory.class);
    private static final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private static final Lock m_readLock = m_globalLock.readLock();
    private static final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    public static SnmpConfig m_config;

    private static File m_configFile;

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
    private SnmpPeerFactory(final File configFile) throws IOException {
        this(new FileSystemResource(configFile));
    }

    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     */
    public SnmpPeerFactory(final Resource resource) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_config = JaxbUtils.unmarshal(SnmpConfig.class, resource);
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     * @deprecated Use code for InputStream instead to avoid character set issues
     */
    public SnmpPeerFactory(final Reader rdr) throws IOException {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_config = JaxbUtils.unmarshal(SnmpConfig.class, rdr);
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * A constructor that takes a config string for use mostly in tests
     */
    public SnmpPeerFactory(final String configString) throws IOException {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_config = JaxbUtils.unmarshal(SnmpConfig.class, configString);
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     */
    public SnmpPeerFactory(final InputStream stream) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_config = JaxbUtils.unmarshal(SnmpConfig.class, new InputSource(stream), null);
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    public static Lock getReadLock() {
        return m_readLock;
    }

    public static Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException
     *             if any.
     */
    public static void init() throws IOException {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            if (m_loaded) {
                // init already called - return
                // to reload, reload() will need to be called
                return;
            }

            final File cfgFile = getFile();
            LOG.debug("init: config file path: {}", cfgFile.getPath());
            m_singleton = new SnmpPeerFactory(cfgFile);
            m_loaded = true;
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * Saves the current settings to disk
     *
     * @throws java.io.IOException if any.
     */
    public static void saveCurrent() throws IOException {
        saveToFile(getFile());
    }

    public static void saveToFile(final File file)
            throws UnsupportedEncodingException, FileNotFoundException,
            IOException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        final String marshalledConfig = marshallConfig();

        FileOutputStream out = null;
        Writer fileWriter = null;
        SnmpPeerFactory.getWriteLock().lock();
        try {
            if (marshalledConfig != null) {
                out = new FileOutputStream(file);
                fileWriter = new OutputStreamWriter(out, "UTF-8");
                fileWriter.write(marshalledConfig);
                fileWriter.flush();
                fileWriter.close();
            }
        } finally {
            IOUtils.closeQuietly(fileWriter);
            IOUtils.closeQuietly(out);
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }


    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static SnmpPeerFactory getInstance() {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (!m_loaded) {
                throw new IllegalStateException("The factory has not been initialized");
            }

            return m_singleton;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

    /**
     * <p>setFile</p>
     *
     * @param configFile a {@link java.io.File} object.
     */
    public static void setFile(final File configFile) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            final File oldFile = m_configFile;
            m_configFile = configFile;

            // if the file changed then we need to reload the config
            if (oldFile == null || m_configFile == null || !oldFile.equals(m_configFile)) {
                m_singleton = null;
                m_loaded = false;
            }
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /**
     * <p>getFile</p>
     *
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public static File getFile() throws IOException {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (m_configFile == null) {
                setFile(ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME));
            }
            return m_configFile;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.SnmpPeerFactory} object.
     */
    public static void setInstance(final SnmpPeerFactory singleton) {
        SnmpPeerFactory.getWriteLock().lock();
        try {
            m_singleton = singleton;
            m_loaded = true;
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress) {
        return getAgentConfig(agentAddress, SnmpAgentConfig.VERSION_UNSPECIFIED);
    }

    private SnmpAgentConfig getAgentConfig(final InetAddress agentInetAddress, final int requestedSnmpVersion) {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (m_config == null) {
                final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
                if (requestedSnmpVersion == SnmpAgentConfig.VERSION_UNSPECIFIED) {
                    agentConfig.setVersion(SnmpAgentConfig.DEFAULT_VERSION);
                } else {
                    agentConfig.setVersion(requestedSnmpVersion);
                }

                return agentConfig;
            }

            final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);

            // Now set the defaults from the m_config
            setSnmpAgentConfig(agentConfig, new Definition(), requestedSnmpVersion);

            final AddressSnmpConfigVisitor visitor = new AddressSnmpConfigVisitor(agentInetAddress);
            m_config.visit(visitor);
            final Definition matchingDef = visitor.getDefinition();
            if (matchingDef != null) {
                setSnmpAgentConfig(agentConfig, matchingDef, requestedSnmpVersion);
            }
            return agentConfig;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

    private void setSnmpAgentConfig(final SnmpAgentConfig agentConfig, final Definition def, final int requestedSnmpVersion) {
        int version = getVersionCode(def, m_config, requestedSnmpVersion);

        setCommonAttributes(agentConfig, def, version);
        agentConfig.setSecurityLevel(def.getSecurityLevel());
        agentConfig.setSecurityName(def.getSecurityName());
        agentConfig.setAuthProtocol(def.getAuthProtocol());
        agentConfig.setAuthPassPhrase(def.getAuthPassphrase());
        agentConfig.setPrivPassPhrase(def.getPrivacyPassphrase());
        agentConfig.setPrivProtocol(def.getPrivacyProtocol());
        agentConfig.setReadCommunity(def.getReadCommunity());
        agentConfig.setWriteCommunity(def.getWriteCommunity());
        agentConfig.setContextName(def.getContextName());
        agentConfig.setEngineId(def.getEngineId());
        agentConfig.setContextEngineId(def.getContextEngineId());
        agentConfig.setEnterpriseId(def.getEnterpriseId());
    }

    /**
     * This is a helper method to set all the common attributes in the agentConfig.
     * 
     * @param agentConfig
     * @param def
     * @param version
     */
    private void setCommonAttributes(final SnmpAgentConfig agentConfig, final Definition def, final int version) {
        agentConfig.setVersion(version);
        agentConfig.setPort(def.getPort());
        agentConfig.setRetries(def.getRetry());
        agentConfig.setTimeout((int)def.getTimeout());
        agentConfig.setMaxRequestSize(def.getMaxRequestSize());
        agentConfig.setMaxVarsPerPdu(def.getMaxVarsPerPdu());
        agentConfig.setMaxRepetitions(def.getMaxRepetitions());
        InetAddress proxyHost = InetAddressUtils.addr(def.getProxyHost());

        if (proxyHost != null) {
            agentConfig.setProxyFor(agentConfig.getAddress());
            agentConfig.setAddress(proxyHost);
        }
    }

    public int getVersionCode(final Definition def, final SnmpConfig config, final int requestedSnmpVersion) {
        if (requestedSnmpVersion == SnmpAgentConfig.VERSION_UNSPECIFIED) {
            if (def.getVersion() == null) {
                if (config.getVersion() == null) {
                    return SnmpAgentConfig.DEFAULT_VERSION;
                } else {
                    return SnmpConfiguration.stringToVersion(config.getVersion());
                }
            } else {
                return SnmpConfiguration.stringToVersion(def.getVersion());
            }
        } else {
            return requestedSnmpVersion;
        }
    }

    /**
     * <p>getSnmpConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
     */
    public static SnmpConfig getSnmpConfig() {
        SnmpPeerFactory.getReadLock().lock();
        try {
            return m_config;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

    /**
     * Enhancement: Allows specific or ranges to be merged into SNMP
     * configuration with many other attributes. Uses new classes to help with
     * merging, comparing, and optimizing definitions. Puts a specific IP
     * address with associated read-community string into the currently loaded
     * snmp-config.xml.
     * 
     * @param info
     *            a {@link org.opennms.netmgt.config.SnmpEventInfo} object.
     */
    public void define(final SnmpEventInfo info) {
        getWriteLock().lock();
        try {
            final SnmpConfigManager mgr = new SnmpConfigManager(m_config);
            mgr.mergeIntoConfig(info.createDef());
        } finally {
            getWriteLock().unlock();
        }
    }


    /**
     * Creates a string containing the XML of the current SnmpConfig
     *
     * @return Marshalled SnmpConfig
     */
    public static String marshallConfig() {
        SnmpPeerFactory.getReadLock().lock();
        try {
            String marshalledConfig = null;
            StringWriter writer = null;
            try {
                writer = new StringWriter();
                JaxbUtils.marshal(m_config, writer);
                marshalledConfig = writer.toString();
            } finally {
                IOUtils.closeQuietly(writer);
            }
            return marshalledConfig;
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

}
