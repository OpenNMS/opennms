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

import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.spring.FileReloadCallback;
import org.opennms.core.spring.FileReloadContainer;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.AddressSnmpConfigVisitor;
import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_SECURITY_LEVEL;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_SECURITY_NAME;

/**
 * This class is the main repository for SNMP configuration information used by
 * the capabilities daemon. When this class is loaded it reads the snmp
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.netmgt.snmp.SnmpAgentConfig SnmpAgentConfig} objects for specific
 * addresses. If an address cannot be located in the configuration then a
 * default peer instance is returned to the caller.
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

    protected static volatile SnmpConfigDao snmpConfigDao;

    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory s_singleton = null;


    private final ReadWriteLock m_lock = new ReentrantReadWriteLock();

    /**
     * The config class loaded from the config file.
     * Note, this is repopulated from the DAO on each call to getSnmpConfig() to ensure it is always in sync with
     * the latest saved config.
     * This is used as a temporary, mutable object to hold the config when making changes, e.g. via
     * SnmpConfigManager, then can be saved to the DAO via saveCurrent().
     */
    private SnmpConfig m_config;

    private FileReloadContainer<SnmpConfig> m_container;

    private FileReloadCallback<SnmpConfig> m_callback;

    private static Scope secureCredentialsVaultScope;

    public SnmpPeerFactory() {
        LOG.debug("creating new instance: {}", this);
    }

    public SnmpPeerFactory(final Resource resource) {
        LOG.debug("creating new instance from resource: {}", this);
        setResource(resource);
    }

    private Lock getReadLock() {
        return m_lock.readLock();
    }

    private Lock getWriteLock() {
        return m_lock.writeLock();
    }

    /**
     * Initializes the singleton instance.
     * However, this does not initialize snmpConfigDao as it would create a circular dependency.
     * Calling getSnmpConfig() will perform this initialization.
     *
     * @throws IOException
     */
    public static synchronized void init() throws IOException {
        if (s_singleton == null) {
            s_singleton = new SnmpPeerFactory();
        }
    }

    public static void setSecureCredentialsVaultScope(Scope secureCredentialsVaultScope) {
        SnmpPeerFactory.secureCredentialsVaultScope = secureCredentialsVaultScope;
    }

    /**
     * Gets the singleton instance, which is created if needed.
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be read
     */
    public static synchronized SnmpPeerFactory getInstance() {
        if (s_singleton == null) {
            try {
                init();
            } catch (final IOException e) {
                LOG.error("Failed to initialize SnmpPeerFactory instance!", e);
            }
        }
        return s_singleton;
    }

    /**
     * Set the singleton instance, e.g. to a mocked instance.
     * This should only be used in unit or integration tests.
     *
     * @param singleton a {@link org.opennms.netmgt.config.SnmpPeerFactory} object.
     */
    public static synchronized void setInstance(final SnmpPeerFactory singleton) {
        LOG.debug("setting new singleton instance {}", singleton);
        s_singleton = singleton;
    }

    /**
     * Used only by unit and integration tests to construct a mock DAO object populated from a file.
     */
    public static synchronized void setFile(final File configFile) {
        setResource(new FileSystemResource(configFile));
    }

    /**
     * Used only by unit and integration tests to construct a mock DAO object populated from a file or string.
     */
    public static synchronized void setResource(final Resource resource) {
        SnmpPeerFactory.snmpConfigDao = new SnmpConfigDao() {
            SnmpConfig snmpConfig = JaxbUtils.unmarshal(SnmpConfig.class, resource);

            @Override
            public SnmpConfig getConfig() {
                return snmpConfig;
            }

            @Override
            public void updateConfig(final SnmpConfig config) {
                snmpConfig = config;
            }
        };
        s_singleton = null;
    }

    /**
     * Saves the current settings to the database.
     * This is a no-op if the configuration has not been initialized, e.g. via getSnmpConfigDao().getConfig().
     */
    @Override
    public void saveCurrent() {
        getWriteLock().lock();

        try {
            if (m_config != null) {
                getSnmpConfigDao().updateConfig(m_config);
            }
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Update and save the config, replacing any existing config.
     * Also gets the config from the store afterward, ensuring that the in-memory config is in sync with the store.
     */
    @Override
    public void setAndSaveConfig(SnmpConfig snmpConfig) throws IOException {
        getWriteLock().lock();

        try {
            getSnmpConfigDao().updateConfig(snmpConfig, true);
            // repopulate m_config from the DAO to make sure it is in sync with what is saved
            this.m_config = getSnmpConfigDao().getConfig();
        } finally {
            getWriteLock().unlock();
        }
    }

    private static synchronized Scope getSecureCredentialsScope() {
        if (secureCredentialsVaultScope == null) {
            try {
                final EntityScopeProvider entityScopeProvider = BeanUtils.getBean("daoContext", "entityScopeProvider", EntityScopeProvider.class);

                if (entityScopeProvider != null) {
                    secureCredentialsVaultScope = entityScopeProvider.getScopeForScv();
                } else {
                    LOG.warn("SnmpPeerFactory: EntityScopeProvider is null, SecureCredentialsVault not available for metadata interpolation");
                }
            } catch (FatalBeanException e) {
                LOG.warn("SnmpPeerFactory: Error retrieving EntityScopeProvider bean");
            }
        }

        return secureCredentialsVaultScope;
    }

    private static SnmpConfigDao getSnmpConfigDao() {
        if (snmpConfigDao == null) {
            synchronized (SnmpPeerFactory.class) {
                if (snmpConfigDao == null) {
                    snmpConfigDao = BeanUtils.getBean("daoContext", "snmpConfigDao", SnmpConfigDao.class);
                }
            }
        }
        return snmpConfigDao;
    }

    /**
     * {@inheritDoc}
     */
    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress, boolean metaDataInterpolation) {
        return getAgentConfig(agentAddress, null, VERSION_UNSPECIFIED, metaDataInterpolation);
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress) {
        return getAgentConfig(agentAddress, null, VERSION_UNSPECIFIED, true);
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress, String location) {
        return getAgentConfig(agentAddress, location, VERSION_UNSPECIFIED, true);
    }

    @Override
    public SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address) {
        return getAgentConfigFromProfile(snmpProfile, address, true);
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress, String location, boolean metaDataInterpolation) {
        return getAgentConfig(agentAddress, location, VERSION_UNSPECIFIED, metaDataInterpolation);
    }

    public SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address, final boolean metaDataInterpolation) {
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

        if (!metaDataInterpolation) {
            return agentConfig;
        } else {
            final Scope scope = getSecureCredentialsScope();

            if (scope != null) {
                agentConfig.setSecurityName(Interpolator.interpolate(agentConfig.getSecurityName(), scope).output);
                agentConfig.setReadCommunity(Interpolator.interpolate(agentConfig.getReadCommunity(), scope).output);
                agentConfig.setWriteCommunity(Interpolator.interpolate(agentConfig.getWriteCommunity(), scope).output);
                agentConfig.setAuthPassPhrase(Interpolator.interpolate(agentConfig.getAuthPassPhrase(), scope).output);
                agentConfig.setPrivPassPhrase(Interpolator.interpolate(agentConfig.getPrivPassPhrase(), scope).output);
            } else {
                LOG.warn("Failed metadata interpolation for SNMP profile {}/{}", snmpProfile.getLabel(), InetAddressUtils.str(address));
            }
            return agentConfig;
        }
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentInetAddress, final int requestedSnmpVersion) {
        return getAgentConfig(agentInetAddress, null, requestedSnmpVersion, true);
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress agentInetAddress, String location, final int requestedSnmpVersion, boolean metaDataInterpolation) {
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
            if (!visitor.isMatchingDefaultConfig()) {
                agentConfig.setDefault(false);
            }

            if (matchingDef != null) {
                setSnmpAgentConfig(agentConfig, matchingDef, requestedSnmpVersion);
            }

            if (!metaDataInterpolation) {
                return agentConfig;
            } else {
                final Scope scope = getSecureCredentialsScope();

                if (scope != null) {
                    agentConfig.setSecurityName(Interpolator.interpolate(agentConfig.getSecurityName(), scope).output);
                    agentConfig.setReadCommunity(Interpolator.interpolate(agentConfig.getReadCommunity(), scope).output);
                    agentConfig.setWriteCommunity(Interpolator.interpolate(agentConfig.getWriteCommunity(), scope).output);
                    agentConfig.setAuthPassPhrase(Interpolator.interpolate(agentConfig.getAuthPassPhrase(), scope).output);
                    agentConfig.setPrivPassPhrase(Interpolator.interpolate(agentConfig.getPrivPassPhrase(), scope).output);
                } else {
                    LOG.warn("Failed metadata interpolation for agent config {}/{}/{}", location, InetAddressUtils.str(agentInetAddress), requestedSnmpVersion);
                }
                return agentConfig;
            }
        } finally {
            getReadLock().unlock();
        }
    }

    private void setSnmpAgentConfig(final SnmpAgentConfig agentConfig, final Definition def, final int requestedSnmpVersion) {
        int version = getVersionCode(def, getSnmpConfig(), requestedSnmpVersion);

        setCommonAttributes(agentConfig, def, version);
        agentConfig.setSecurityLevel(def.hasSecurityLevel() ? def.getSecurityLevel() : DEFAULT_SECURITY_LEVEL);
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
        agentConfig.setTimeout((int) def.getTimeout());
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
     * Repopulate m_config from the DAO and return the reference.
     *
     * @return a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
     */
    @Override
    public SnmpConfig getSnmpConfig() {
        m_config = getSnmpConfigDao().getConfig();
        return m_config;
    }

    /**
     * Enhancement: Allows specific or ranges to be merged into SNMP configuration
     * with many other attributes.  Uses new classes the wrap JAXB-generated code to
     * help with merging, comparing, and optimizing definitions.  Thanks for your
     * initial work on this Gerald.
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml.
     *
     * @param info a {@link org.opennms.netmgt.config.SnmpEventInfo} object.
     */
    public void define(final SnmpEventInfo info) {
        saveDefinition(info.createDef(), false);
    }

    @Override
    public void saveDefinition(final Definition definition, boolean save) {
        getWriteLock().lock();

        try {
            // this mutates m_config. Can call saveCurrent() afterwards to save to DAO.
            final SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
            mgr.mergeIntoConfig(definition);

            if (save) {
                saveCurrent();
            }
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

            if (matchingDefinition != null) {
                // Form a definition just with this IP Address.
                Definition definition = createDefinition(matchingDefinition);
                List<String> specifics = new ArrayList<>();
                specifics.add(InetAddressUtils.toIpAddrString(inetAddress));
                definition.setSpecifics(specifics);

                // this mutates m_config. Can call saveCurrent() afterwards to save to DAO.
                final SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
                succeeded = mgr.removeDefinition(definition);
            }

            if (succeeded) {
                saveCurrent();
                LOG.info("Removed {} at location {} from definitions by module {}", inetAddress.getHostAddress(), location, module);
            }
        } finally {
            getWriteLock().unlock();
        }

        return succeeded;
    }

    /**
     * Removes ranges, specific IP and/or ipMatch expressions from the configuration.
     * If a given range, specific IP or ipMatch expression is not found in the config,
     * it will be ignored and the method will continue to try to remove the other items.
     * The method returns true if at least one item is removed from the config, false otherwise.
     *
     * @param ranges List of ranges to remove. Set to null or empty list to ignore.
     * @param specifics List of individual IP addresses to remove. Set to null or empty list to ignore.
     * @param ipMatches List of IP match expressions to remove. Set to null or empty list to ignore.
     * @param location  location at which this ipaddress belongs.
     * @param module    module from which the definition is getting removed.
     * @return true if at least one item is removed from the config, false otherwise.
     */
    @Override
    public boolean removeRangesFromDefinition(List<Range> ranges, List<String> specifics, List<String> ipMatches,
                                              String location, String module) {
        getWriteLock().lock();

        int rangesRemoved = 0;
        int specificsRemoved = 0;
        int ipMatchesRemoved = 0;

        // Refresh m_config from the DAO before cloning so we always operate on current state.
        // clone the current in-memory config to work with so that we can compare at the end
        // and only update if there are actual changes
        m_config = getSnmpConfigDao().getConfig();
        SnmpConfig clonedConfig = SnmpPeerFactory.cloneConfig(m_config);
        final SnmpConfigManager mgr = new SnmpConfigManager(clonedConfig);

        try {
            // Remove any specifics
            if (specifics != null) {
                for (String specific : specifics) {
                    if (Strings.isNullOrEmpty(specific)) {
                        continue;
                    }

                    final InetAddress specificInetAddr = InetAddressUtils.addr(specific);
                    Definition matchingDefinition = findMatchingDefinition(mgr.getConfig(), specificInetAddr, location);

                    if (matchingDefinition != null) {
                        // Form a definition just with this IP Address.
                        Definition definition = createDefinition(matchingDefinition);
                        List<String> specificList = new ArrayList<>();
                        specificList.add(InetAddressUtils.toIpAddrString(specificInetAddr));
                        definition.setSpecifics(specificList);

                        boolean removed = mgr.removeDefinition(definition);

                        if (removed) {
                            specificsRemoved++;
                        }
                    }
                }
            }

            if (ranges != null) {
                // Remove any ranges
                for (Range range : ranges) {
                    if (Strings.isNullOrEmpty(range.getBegin()) || Strings.isNullOrEmpty(range.getEnd())) {
                        continue;
                    }

                    final InetAddress rangeStart = InetAddressUtils.addr(range.getBegin());
                    Definition matchingDefinition = findMatchingDefinition(mgr.getConfig(), rangeStart, location);

                    if (matchingDefinition != null) {
                        // Form a definition with the range
                        Definition definition = createDefinition(matchingDefinition);
                        List<Range> rangeList = new ArrayList<>();
                        Range r = new Range(range.getBegin(), range.getEnd());
                        rangeList.add(r);
                        definition.setRanges(rangeList);

                        boolean removed = mgr.removeDefinition(definition);

                        if (removed) {
                            rangesRemoved++;
                        }
                    }
                }
            }

            // Remove any definitions that are ipMatchOnly and have the exact ip match expressions
            if (ipMatches != null && !ipMatches.isEmpty()) {
                Definition definition = new Definition();
                definition.setLocation(location);
                definition.setIpMatches(ipMatches);

                boolean removed = mgr.removeDefinition(definition);

                if (removed) {
                    ipMatchesRemoved++;
                }
            }

            // only update config here and in the DAO if it is actually changed
            if (!Objects.equals(clonedConfig, m_config)) {
                m_config = clonedConfig;
                saveCurrent();

                LOG.info("Removed {} ranges, {} specifics, {} ipMatches from definitions at location {} by module {}",
                        rangesRemoved, specificsRemoved, ipMatchesRemoved, location, module);
            } else {
                LOG.info("No matching items found to remove for location {} by module {}", location, module);
            }
        } finally {
            getWriteLock().unlock();
        }

        return rangesRemoved > 0 || specificsRemoved > 0 || ipMatchesRemoved > 0;
    }

    @Override
    public void saveProfile(final SnmpProfile profile) {
        getWriteLock().lock();

        try {
            // this mutates m_config. saveCurrent() then saves the updated m_config to DAO.
            final SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
            mgr.mergeProfileIntoConfig(profile);
            saveCurrent();
        } finally {
            getWriteLock().unlock();
        }
    }

    @Override
    public boolean removeProfile(final String label) {
        boolean succeeded = false;
        getWriteLock().lock();

        try {
            // this mutates m_config. saveCurrent() then saves the updated m_config to DAO.
            final SnmpConfigManager mgr = new SnmpConfigManager(getSnmpConfig());
            succeeded = mgr.removeProfile(label);

            if (succeeded) {
                saveCurrent();
                LOG.info("Removed profile {}", label);
            }
        } finally {
            getWriteLock().unlock();
        }

        return succeeded;
    }

    /** Find matching definitions from the SnmpConfig retrieved by the DAO. */
    private Definition findMatchingDefinition(InetAddress inetAddress, String location) {
        SnmpConfig config = getSnmpConfig();
        List<Definition> definitions = config.getDefinitions();

        return definitions.stream().filter(definition -> matchDefinition(definition, inetAddress, location)).findFirst().orElse(null);
    }

    /** Find matching definitions for the given config. */
    private Definition findMatchingDefinition(SnmpConfig config, InetAddress inetAddress, String location) {
        List<Definition> definitions = config.getDefinitions();

        return definitions.stream().filter(definition -> matchDefinition(definition, inetAddress, location)).findFirst().orElse(null);
    }

    @Override
    public void saveAgentConfigAsDefinition(SnmpAgentConfig snmpAgentConfig, String location, String module) {
        Definition definition = new Definition();

        // agent config always have one ip-address.
        String ipAddress = snmpAgentConfig.getAddress().getHostAddress();
        definition.setLocation(location);

        setDefinitionFromAgentConfig(definition, snmpAgentConfig);

        saveDefinition(definition, true);
        LOG.info("Definition saved for {} by module {}", ipAddress, module);
    }

    @Override
    public void saveDefaultOverrides(Configuration config) {
        if (config == null) {
            return;
        }

        getWriteLock().lock();

        try {
            // clone the current in-memory config so we do not override the current
            // in-memory config with invalid values if the new config fails validation
            SnmpConfig clonedConfig = SnmpPeerFactory.cloneConfig(getSnmpConfig());
            setConfigurationParams(clonedConfig, config);

            try {
                setAndSaveConfig(clonedConfig);
            } catch (Exception e) {
                LOG.error("Failed to save default overrides to config.", e);
                // m_config remains untouched
            }
        } finally {
            getWriteLock().unlock();
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
        SnmpConfig snmpConfig = getSnmpConfig();

        try {
            writer = new StringWriter();
            JaxbUtils.marshal(snmpConfig, writer);
            marshalledConfig = writer.toString();
        } finally {
            IOUtils.closeQuietly(writer);
        }

        return marshalledConfig;
    }

    public static SnmpConfig cloneConfig(SnmpConfig config) {
        StringWriter writer = null;

        try {
            writer = new StringWriter();
            JaxbUtils.marshal(config, writer);
            String marshalledConfig = writer.toString();
            return JaxbUtils.unmarshal(SnmpConfig.class, marshalledConfig);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private void setConfigurationParams(SnmpConfig snmpConfig, Configuration config) {
        snmpConfig.setPort(config.getPort());
        snmpConfig.setRetry(config.getRetry());
        snmpConfig.setTimeout(config.getTimeout());
        snmpConfig.setReadCommunity(config.getReadCommunity());
        snmpConfig.setWriteCommunity(config.getWriteCommunity());
        snmpConfig.setProxyHost(config.getProxyHost());
        snmpConfig.setVersion(config.getVersion());
        snmpConfig.setMaxVarsPerPdu(config.getMaxVarsPerPdu());
        snmpConfig.setMaxRepetitions(config.getMaxRepetitions());
        snmpConfig.setMaxRequestSize(config.getMaxRequestSize());
        snmpConfig.setSecurityName(config.getSecurityName());
        snmpConfig.setSecurityLevel(config.getSecurityLevel());
        snmpConfig.setAuthPassphrase(config.getAuthPassphrase());
        snmpConfig.setAuthProtocol(config.getAuthProtocol());
        snmpConfig.setEngineId(config.getEngineId());
        snmpConfig.setContextEngineId(config.getContextEngineId());
        snmpConfig.setContextName(config.getContextName());
        snmpConfig.setPrivacyPassphrase(config.getPrivacyPassphrase());
        snmpConfig.setPrivacyProtocol(config.getPrivacyProtocol());
        snmpConfig.setEnterpriseId(config.getEnterpriseId());
        snmpConfig.setTTL(config.getTTL());
    }

    private static Definition createDefinition(Definition matchingDefinition) {
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
        boolean locationMatched = LocationUtils.doesLocationsMatch(location, definition.getLocation());

        return locationMatched && matchingIpAddress(inetAddress, definition);
    }

    private static boolean matchingIpAddress(InetAddress inetAddress, Definition definition) {
        boolean matchingIpAddress = definition.getSpecifics().stream()
                .anyMatch(saddr -> matchingSpecific(inetAddress, saddr));

        if (!matchingIpAddress) {
            return definition.getRanges().stream().anyMatch(range -> matchingRanges(inetAddress, range));
        }

        return true;
    }

    private static boolean matchingSpecific(InetAddress inetAddress, String specific) {
        final byte[] addr = inetAddress.getAddress();
        final byte[] specificBytes;

        try {
            specificBytes = InetAddressUtils.toIpAddrBytes(specific);
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        return InetAddressUtils.areSameInetAddress(addr, specificBytes);
    }

    private static boolean matchingRanges(InetAddress inetAddress, Range range) {
        if (range == null || range.getBegin() == null || range.getEnd() == null) {
            return false;
        }

        final byte[] addr = inetAddress.getAddress();
        final byte[] begin;
        final byte[] end;

        try {
            begin = InetAddressUtils.toIpAddrBytes(range.getBegin());
            end = InetAddressUtils.toIpAddrBytes(range.getEnd());
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        final boolean inRange;
        final ByteArrayComparator BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();

        if (BYTE_ARRAY_COMPARATOR.compare(begin, end) <= 0) {
            inRange = InetAddressUtils.isInetAddressInRange(addr, begin, end);
        } else {
            inRange = InetAddressUtils.isInetAddressInRange(addr, end, begin);
        }

        return inRange;
    }
}
