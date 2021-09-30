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

import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_SECURITY_LEVEL;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_SECURITY_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.IOUtils;
import org.opennms.core.spring.FileReloadCallback;
import org.opennms.core.spring.FileReloadContainer;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.AddressSnmpConfigVisitor;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
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

    private static final int VERSION_UNSPECIFIED = -1;

    private static File s_configFile;

    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory s_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static AtomicBoolean s_loaded = new AtomicBoolean(false);

    private final ReadWriteUpdateLock m_globalLock = new ReentrantReadWriteUpdateLock();
    private final Lock m_readLock = m_globalLock.updateLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * The config class loaded from the config file
     */
    private SnmpConfig m_config;

    private FileReloadContainer<SnmpConfig> m_container;

    private FileReloadCallback<SnmpConfig> m_callback;

    /**
     * <p>Constructor for SnmpPeerFactory.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     */
    public SnmpPeerFactory(final Resource resource) {
        LOG.debug("creating new instance for resource {}: {}", resource, this);

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
            LOG.debug("No file associated with resource {}, skipping reload container initialization. Reason: ", resource, e.getMessage());
        }

        // if we fall through to here, then the file was null, or something else went wrong store the config directly
        m_config = config;
    }

    protected Lock getReadLock() {
        return m_readLock;
    }

    protected Lock getWriteLock() {
        return m_writeLock;
    }

    public static synchronized void init() throws IOException {
        if (!s_loaded.get()) {
            final File cfgFile = getFile();
            LOG.debug("init: config file path: {}", cfgFile.getPath());
            final FileSystemResource resource = new FileSystemResource(cfgFile);

            s_singleton = new SnmpPeerFactory(resource);
            s_loaded.set(true);
        }
    }

    /**
     * Load the config from the default config file and create the singleton instance of this factory.
     *
     * @exception java.io.IOException Thrown if the specified config file cannot be read
     */
    public static synchronized SnmpPeerFactory getInstance() {
        if (!s_loaded.get()) {
            try {
                init();
            } catch (final IOException e) {
                LOG.error("Failed to initialize SnmpPeerFactory instance!", e);
            }
        }
        return s_singleton;
    }

    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.SnmpPeerFactory} object.
     */
    public static synchronized void setInstance(final SnmpPeerFactory singleton) {
        LOG.debug("setting new singleton instance {}", singleton);
        s_singleton = singleton;
        s_loaded.set(true);
    }

    public static synchronized File getFile() throws IOException {
        if (s_configFile == null) {
            setFile(ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME));
        }
        return s_configFile;
    }

    /**
     * <p>setFile</p>
     *
     * @param configFile a {@link java.io.File} object.
     */
    public static synchronized void setFile(final File configFile) {
        final File oldFile = s_configFile;
        s_configFile = configFile;

        // if the file changed then we need to reload the config
        if (oldFile == null || s_configFile == null || !oldFile.equals(s_configFile)) {
            s_singleton = null;
            s_loaded.set(false);
        }
    }

    /**
     * Saves the current settings to disk
     *
     * @throws java.io.IOException if any.
     */
    public void saveCurrent() throws IOException {
        saveToFile(getFile());
    }

    public void saveToFile(final File file) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config isn't lost if the XML from the marshal is hosed.
        getWriteLock().lock();

        final String marshalledConfig = getSnmpConfigAsString();

        FileOutputStream out = null;
        Writer fileWriter = null;
        try {
            if (marshalledConfig != null) {
                out = new FileOutputStream(file);
                fileWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
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
            getWriteLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress) {
        return getAgentConfig(agentAddress, null, VERSION_UNSPECIFIED);
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress, String location) {
        return getAgentConfig(agentAddress, location, VERSION_UNSPECIFIED);
    }

    @Override
    public SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address) {
        final SnmpAgentConfig agentConfig = new SnmpAgentConfig(address);
        AddressSnmpConfigVisitor visitor = new AddressSnmpConfigVisitor(address);
        // Need to populate default snmp config.
        visitor.visitSnmpConfig(getSnmpConfig());
        snmpProfile.visit(visitor);
        Definition definition = visitor.getDefinition();
        setSnmpAgentConfig(agentConfig, definition, VERSION_UNSPECIFIED);
        // config is derived from profile
        agentConfig.setDefault(false);
        agentConfig.setProfileLabel(snmpProfile.getLabel());
        return agentConfig;
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentInetAddress, final int requestedSnmpVersion) {

        return getAgentConfig(agentInetAddress, null, requestedSnmpVersion);
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentInetAddress, String location, final int requestedSnmpVersion) {
        getReadLock().lock();
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

            // Set the values from best matching definition
            final AddressSnmpConfigVisitor visitor = new AddressSnmpConfigVisitor(agentInetAddress, location);
            getSnmpConfig().visit(visitor);
            final Definition matchingDef = visitor.getDefinition();
            // Is agent config matching specific definition or coming from default config
            if(!visitor.isMatchingDefaultConfig()) {
               agentConfig.setDefault(false);
            }
            if (matchingDef != null) {
                setSnmpAgentConfig(agentConfig, matchingDef, requestedSnmpVersion);
            }
            return agentConfig;
        } finally {
            getReadLock().unlock();
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
        agentConfig.setProfileLabel(def.getProfileLabel());
    }

    private void setCommonAttributes(final SnmpAgentConfig agentConfig, final Definition def, final int version) {
        agentConfig.setVersion(version);
        agentConfig.setPort(def.getPort());
        agentConfig.setRetries(def.getRetry());
        agentConfig.setTimeout((int)def.getTimeout());
        agentConfig.setMaxRequestSize(def.getMaxRequestSize());
        agentConfig.setMaxVarsPerPdu(def.getMaxVarsPerPdu());
        agentConfig.setMaxRepetitions(def.getMaxRepetitions());
        agentConfig.setTTL(def.getTTL());
        InetAddress proxyHost = InetAddressUtils.addr(def.getProxyHost());

        if (proxyHost != null) {
            agentConfig.setProxyFor(agentConfig.getAddress());
            agentConfig.setAddress(proxyHost);
        }
    }

    private void setDefinitionFromAgentConfig(Definition definition, SnmpAgentConfig snmpAgentConfig) {

        definition.setVersion(SnmpConfiguration.versionToString(snmpAgentConfig.getVersion()));
        definition.setPort(snmpAgentConfig.getPort());
        definition.setRetry(snmpAgentConfig.getRetries());
        definition.setTimeout(snmpAgentConfig.getTimeout());
        definition.setMaxRequestSize(snmpAgentConfig.getMaxRequestSize());
        definition.setMaxVarsPerPdu(snmpAgentConfig.getMaxVarsPerPdu());
        definition.setMaxRepetitions(snmpAgentConfig.getMaxRepetitions());
        definition.setTTL(snmpAgentConfig.getTTL());
        if (snmpAgentConfig.getProxyFor() != null) {
            definition.addSpecific(snmpAgentConfig.getProxyFor().getHostAddress());
            definition.setProxyHost(snmpAgentConfig.getAddress().getHostAddress());
        } else {
            definition.addSpecific(snmpAgentConfig.getAddress().getHostAddress());
        }
        if (DEFAULT_SECURITY_LEVEL != snmpAgentConfig.getSecurityLevel()) {
            definition.setSecurityLevel(snmpAgentConfig.getSecurityLevel());
        }
        if (!DEFAULT_SECURITY_NAME.equals(snmpAgentConfig.getSecurityName())) {
            definition.setSecurityName(snmpAgentConfig.getSecurityName());
        }
        definition.setAuthProtocol(snmpAgentConfig.getAuthProtocol());
        definition.setAuthPassphrase(snmpAgentConfig.getAuthPassPhrase());
        definition.setPrivacyPassphrase(snmpAgentConfig.getPrivPassPhrase());
        definition.setPrivacyProtocol(snmpAgentConfig.getPrivProtocol());
        definition.setReadCommunity(snmpAgentConfig.getReadCommunity());
        definition.setWriteCommunity(snmpAgentConfig.getWriteCommunity());
        definition.setContextName(snmpAgentConfig.getContextName());
        definition.setEngineId(snmpAgentConfig.getEngineId());
        definition.setContextEngineId(snmpAgentConfig.getContextEngineId());
        definition.setEnterpriseId(snmpAgentConfig.getEnterpriseId());
        definition.setProfileLabel(snmpAgentConfig.getProfileLabel());
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
        getReadLock().lock();
        try {
            if (m_container == null) {
                return m_config;
            } else {
                return m_container.getObject();
            }
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Enhancement: Allows specific or ranges to be merged into SNMP configuration
     * with many other attributes.  Uses new classes the wrap JAXB-generated code to
     * help with merging, comparing, and optimizing definitions.  Thanks for your
     * initial work on this Gerald.
     *
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml.
     *
     * @param info a {@link org.opennms.netmgt.config.SnmpEventInfo} object.
     */
    public void define(final SnmpEventInfo info) {
        saveDefinition(info.createDef());
    }

    @Override
    public void saveDefinition(final Definition definition) {
        getWriteLock().lock();
        try {
            final SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
            mgr.mergeIntoConfig(definition);
        } finally {
            getWriteLock().unlock();
        }
    }

    @Override
    public boolean removeFromDefinition(InetAddress inetAddress, String location, String module) {
        boolean succeeded = false;
        getWriteLock().lock();
        try {
            // Check if there is a matching definition from the config itself instead of doing getAgentConfig.
            Definition matchingDefinition = findMatchingDefinition(inetAddress, location);
            if(matchingDefinition !=  null) {
                // Form a definition just with this IP Address.
                Definition definition = createDefinition(matchingDefinition);
                List<String> specifics = new ArrayList<>();
                specifics.add(InetAddressUtils.toIpAddrString(inetAddress));
                definition.setSpecifics(specifics);
                final SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
                succeeded = mgr.removeDefinition(definition);
            }
        } finally {
            getWriteLock().unlock();
        }
        if(succeeded) {
            try {
                saveCurrent();
                LOG.info("Removed {} at location {} from definitions by module {}", inetAddress.getHostAddress(), location, module);
            } catch (IOException e) {
                // This never should happen, we currently don't support rollback of configuration.
                LOG.error("Exception while saving current config", e);
            }
        }
        return succeeded;
    }

    private Definition findMatchingDefinition(InetAddress inetAddress, String location) {
        SnmpConfig config = getSnmpConfig();
        List<Definition> definitions = config.getDefinitions();
        return definitions.stream().filter(definition -> matchDefinition(definition, inetAddress, location)).findFirst().orElse(null);
    }

    private  static Definition createDefinition(Definition matchingDefinition) {
        Definition definition = new Definition();
        definition.setProfileLabel(matchingDefinition.getProfileLabel());
        definition.setLocation(matchingDefinition.getLocation());
        // Fill configuration
        definition.setProxyHost(matchingDefinition.getProxyHost());
        definition.setMaxVarsPerPdu(matchingDefinition.getMaxVarsPerPdu());
        definition.setMaxRepetitions(matchingDefinition.getMaxRepetitions());
        definition.setMaxRequestSize(matchingDefinition.getMaxRequestSize());

        definition.setSecurityName(matchingDefinition.getSecurityName());
        definition.setSecurityLevel(matchingDefinition.getSecurityLevel());
        definition.setAuthPassphrase(matchingDefinition.getAuthPassphrase());
        definition.setAuthProtocol(matchingDefinition.getAuthProtocol());
        definition.setEngineId(matchingDefinition.getEngineId());
        definition.setContextEngineId(matchingDefinition.getContextEngineId());
        definition.setContextName(matchingDefinition.getContextName());
        definition.setEnterpriseId(matchingDefinition.getEnterpriseId());
        definition.setPrivacyPassphrase(matchingDefinition.getPrivacyPassphrase());
        definition.setPrivacyProtocol(matchingDefinition.getPrivacyProtocol());
        definition.setVersion(matchingDefinition.getVersion());
        definition.setReadCommunity(matchingDefinition.getReadCommunity());
        definition.setWriteCommunity(matchingDefinition.getWriteCommunity());
        definition.setPort(matchingDefinition.getPort());
        definition.setTimeout(matchingDefinition.getTimeout());
        definition.setTTL(matchingDefinition.getTTL());
        definition.setRetry(matchingDefinition.getRetry());
        return definition;
    }

    private boolean matchDefinition(Definition definition, InetAddress inetAddress, String location) {
        boolean locationMatched =  LocationUtils.doesLocationsMatch(location, definition.getLocation());
        return locationMatched && matchingIpAddress(inetAddress, definition);
    }

    private static boolean matchingIpAddress(InetAddress inetAddress, Definition definition) {

         boolean matchingIpAddress = definition.getSpecifics().stream()
                 .anyMatch(saddr -> saddr.equals(inetAddress.getHostAddress()));
         if(!matchingIpAddress) {
             return definition.getRanges().stream().anyMatch(range -> matchingRanges(inetAddress, range));
         }
         return true;
    }

    private static boolean matchingRanges(InetAddress inetAddress, Range range) {
        final byte[] addr = inetAddress.getAddress();
        final byte[] begin = InetAddressUtils.toIpAddrBytes(range.getBegin());
        final byte[] end = InetAddressUtils.toIpAddrBytes(range.getEnd());

        final boolean inRange;
        final ByteArrayComparator BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();
        if (BYTE_ARRAY_COMPARATOR.compare(begin, end) <= 0) {
            inRange = InetAddressUtils.isInetAddressInRange(addr, begin, end);
        } else {
            inRange = InetAddressUtils.isInetAddressInRange(addr, end, begin);
        }
        return inRange;
    }



    @Override
    public void saveAgentConfigAsDefinition(SnmpAgentConfig snmpAgentConfig, String location, String module) {
        Definition definition = new Definition();
        //agent config always have one ip-address.
        String ipAddress = snmpAgentConfig.getAddress().getHostAddress();
        definition.setLocation(location);
        setDefinitionFromAgentConfig(definition, snmpAgentConfig);
        saveDefinition(definition);
        LOG.info("Definition saved for {} by module {}", ipAddress, module);
        try {
            saveCurrent();
        } catch (IOException e) {
            // This never should happen, we currently don't support rollback of configuration.
            LOG.error("Exception while saving current config", e);
        }
    }


    @Override
    public List<SnmpProfile> getProfiles() {
        SnmpConfig snmpConfig = getSnmpConfig();
        if (snmpConfig != null && snmpConfig.getSnmpProfiles() != null) {
            return snmpConfig.getSnmpProfiles().getSnmpProfiles();
        }
        return new ArrayList<>();
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
