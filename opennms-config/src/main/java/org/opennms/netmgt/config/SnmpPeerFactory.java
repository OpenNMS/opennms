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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.IOUtils;
import org.opennms.core.spring.FileReloadCallback;
import org.opennms.core.spring.FileReloadContainer;
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

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;

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
    private static final Logger LOG = LoggerFactory.getLogger(SnmpPeerFactory.class);
    private static final ReadWriteUpdateLock m_globalLock = new ReentrantReadWriteUpdateLock();
    private static final Lock m_readLock = m_globalLock.updateLock();
    private static final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory m_singleton = null;

    private static File m_configFile;

    /**
     * The config class loaded from the config file
     */
    private SnmpConfig m_config;

    private static FileReloadContainer<SnmpConfig> m_container;
    private static FileReloadCallback<SnmpConfig> m_callback;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static volatile boolean m_loaded = false;

    private static final int VERSION_UNSPECIFIED = -1;

    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     */
    public SnmpPeerFactory(final Resource resource) {
        SnmpPeerFactory.getWriteLock().lock();

        try {
            final SnmpConfig config = JaxbUtils.unmarshal(SnmpConfig.class, resource);

            try {
                final File file = resource.getFile();
                if (file != null) {
                    m_callback = new FileReloadCallback<SnmpConfig>() {
                        @Override
                        public SnmpConfig reload(final SnmpConfig object, final Resource resource) throws IOException {
                            return JaxbUtils.unmarshal(SnmpConfig.class, resource);
                        }

                    };
                    m_container = new FileReloadContainer<SnmpConfig>(config, resource, m_callback);
                    return;
                }
            } catch (final IOException e) {
                LOG.debug("No file associated with resource {}, skipping reload container initialization.", resource);
            }

            // if we fall through to here, then the file was null, or something else went wrong store the config directly
            m_config = config;
        } finally {
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    protected static Lock getReadLock() {
        return m_readLock;
    }

    protected static Lock getWriteLock() {
        return m_writeLock;
    }

    public static synchronized void init() throws IOException {
        if (!m_loaded) {
            final File cfgFile = getFile();
            LOG.debug("init: config file path: {}", cfgFile.getPath());
            final FileSystemResource resource = new FileSystemResource(cfgFile);

            m_singleton = new SnmpPeerFactory(resource);
            m_loaded = true;
        }
    }

    /**
     * Load the config from the default config file and create the singleton instance of this factory.
     *
     * @exception java.io.IOException Thrown if the specified config file cannot be read
     */
    public static synchronized SnmpPeerFactory getInstance() {
        if (!m_loaded) {
            try {
                init();
            } catch (final IOException e) {
                LOG.error("Failed to initialize SnmpPeerFactory instance!", e);
            }
        }
        return m_singleton;
    }

    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.SnmpPeerFactory} object.
     */
    public static synchronized void setInstance(final SnmpPeerFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
    }

    public static synchronized File getFile() throws IOException {
        if (m_configFile == null) {
            setFile(ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME));
        }
        return m_configFile;
    }

    /**
     * <p>setFile</p>
     *
     * @param configFile a {@link java.io.File} object.
     */
    public static synchronized void setFile(final File configFile) {
        final File oldFile = m_configFile;
        m_configFile = configFile;

        // if the file changed then we need to reload the config
        if (oldFile == null || m_configFile == null || !oldFile.equals(m_configFile)) {
            m_singleton = null;
            m_loaded = false;
        }
    }

    /**
     * Saves the current settings to disk
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void saveCurrent() throws IOException {
        saveToFile(getFile());
    }

    public void saveToFile(final File file) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config isn't lost if the XML from the marshal is hosed.
        SnmpPeerFactory.getWriteLock().lock();

        final String marshalledConfig = getSnmpConfigAsString();

        FileOutputStream out = null;
        Writer fileWriter = null;
        try {
            if (marshalledConfig != null) {
                out = new FileOutputStream(file);
                fileWriter = new OutputStreamWriter(out, "UTF-8");
                fileWriter.write(marshalledConfig);
                fileWriter.flush();
                fileWriter.close();
                if (m_container != null) {
                    m_container.reload();
                }
            }
        } finally {
            IOUtils.closeQuietly(fileWriter);
            IOUtils.closeQuietly(out);
            SnmpPeerFactory.getWriteLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress) {
        return getAgentConfig(agentAddress, VERSION_UNSPECIFIED);
    }

    private SnmpAgentConfig getAgentConfig(final InetAddress agentInetAddress, final int requestedSnmpVersion) {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (getSnmpConfig() == null) {
                final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
                if (requestedSnmpVersion == SnmpAgentConfig.VERSION_UNSPECIFIED) {
                    agentConfig.setVersion(SnmpAgentConfig.DEFAULT_VERSION);
                } else {
                    agentConfig.setVersion(requestedSnmpVersion);
                }

                return agentConfig;
            }

            final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);

            // Now set the defaults from the getSnmpConfig()
            setSnmpAgentConfig(agentConfig, new Definition(), requestedSnmpVersion);

            final AddressSnmpConfigVisitor visitor = new AddressSnmpConfigVisitor(agentInetAddress);
            getSnmpConfig().visit(visitor);
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
        int version = getVersionCode(def, getSnmpConfig(), requestedSnmpVersion);

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
    public SnmpConfig getSnmpConfig() {
        SnmpPeerFactory.getReadLock().lock();
        try {
            if (m_container == null) {
                return m_config;
            } else {
                return m_container.getObject();
            }
        } finally {
            SnmpPeerFactory.getReadLock().unlock();
        }
    }

    /**
     * Enhancement: Allows specific or ranges to be merged into SNMP configuration
     * with many other attributes.  Uses new classes the wrap Castor-generated code to
     * help with merging, comparing, and optimizing definitions.  Thanks for your
     * initial work on this Gerald.
     *
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml.
     *
     * @param info a {@link org.opennms.netmgt.config.SnmpEventInfo} object.
     */
    public void define(final SnmpEventInfo info) {
        getWriteLock().lock();
        try {
            final SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
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
    public String getSnmpConfigAsString() {
        String marshalledConfig = null;
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            JaxbUtils.marshal(getSnmpConfig(), writer);
            marshalledConfig = writer.toString();
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return marshalledConfig;
    }
}
