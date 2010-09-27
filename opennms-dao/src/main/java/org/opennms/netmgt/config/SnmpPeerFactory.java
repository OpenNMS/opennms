//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2005 Mar 08: Added saveCurrent, optimize, and define methods.
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.dao.castor.AbstractCastorConfigDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.protocols.ip.IPv4Address;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * This class is the main respository for SNMP configuration information used
 * by the capabilities daemon. When this class is loaded it reads the snmp
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.netmgt.snmp.SnmpAgentConfig SnmpAgentConfig} objects for
 * specific addresses. If an address cannot be located in the configuration
 * then a default peer instance is returned to the caller. <strong>Note:
 * </strong>Users of this class should make sure the <em>init()</em> is called
 * before calling any other method to ensure the config is loaded before
 * accessing other convenience methods.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public class SnmpPeerFactory extends AbstractCastorConfigDao<SnmpConfig, SnmpConfig> implements SnmpAgentConfigFactory {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();

    private final Lock m_readLock = m_globalLock.readLock();

    private final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    private CategoryDao m_categoryDao = null;

    private NodeDao m_nodeDao = null;

    private static final int VERSION_UNSPECIFIED = -1;

    public SnmpPeerFactory() {
        super(SnmpConfig.class, "snmp");
    }

    /**
     * <p>
     * Constructor for SnmpPeerFactory.
     * </p>
     * 
     * @param resource
     *            a {@link org.springframework.core.io.Resource} object.
     */
    public SnmpPeerFactory(final Resource resource) {
        this();
        setConfigResource(resource);
    }

    public Lock getReadLock() {
        return m_readLock;
    }

    public Lock getWriteLock() {
        return m_writeLock;
    }

    /** {@inheritDoc} */
    @Override
    protected String createLoadedLogMessage(final SnmpConfig config, final long diffTime) {
        return "Loaded " + getDescription() + " with " + config.getDefinitionCount() + " definitions in " + diffTime + "ms";
    }

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws DataAccessException {
        /**
         * It sucks to duplicate this first test from AbstractCastorConfigDao,
         * but we need to do so to ensure we don't get an NPE while
         * initializing programmaticStoreConfigResource (if needed).
         */
        Assert.state(getConfigResource() != null, "property configResource must be set and be non-null");

        try {
            LogUtils.debugf(this, "config resource = %s", getConfigResource().getFile().getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.afterPropertiesSet();
    }

    public SnmpConfig getConfig() {
        getReadLock().lock();
        try {
            return getContainer().getObject();
        } finally {
            getReadLock().unlock();
        }
    }

    public SnmpConfig translateConfig(final SnmpConfig config) {
        return config;
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
     * @throws java.io.IOException
     *             if any.
     * @throws org.exolab.castor.xml.MarshalException
     *             if any.
     * @throws org.exolab.castor.xml.ValidationException
     *             if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME);
        m_singleton = new SnmpPeerFactory(new FileSystemResource(cfgFile));
        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be
     *                read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException
     *             if any.
     * @throws org.exolab.castor.xml.MarshalException
     *             if any.
     * @throws org.exolab.castor.xml.ValidationException
     *             if any.
     */
    public static void reload() throws IOException, MarshalException, ValidationException {
        init();
        getInstance().update();
    }

    public void update() throws IOException, MarshalException, ValidationException {
        getReadLock().lock();
        try {
            getContainer().reload();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Saves the current settings to disk
     * 
     * @throws java.io.IOException
     *             if any.
     * @throws org.exolab.castor.xml.MarshalException
     *             if any.
     * @throws org.exolab.castor.xml.ValidationException
     *             if any.
     */
    public void saveCurrent() throws IOException, MarshalException, ValidationException {
        getWriteLock().lock();
        
        try {
            // Marshal to a string first, then write the string to the file.
            // This way the original configuration isn't lost if the XML from the
            // marshal is hosed.
            StringWriter stringWriter = new StringWriter();
            Marshaller.marshal(getConfig(), stringWriter);

            String xmlString = stringWriter.toString();
            if (xmlString != null) {
                saveXML(xmlString);
            }
        } finally {
            getWriteLock().unlock();
        }
        
        update();
    }

    protected synchronized void saveXML(final String xmlString) throws IOException, MarshalException, ValidationException {
        getWriteLock().lock();

        try {
            File cfgFile = getConfigResource().getFile();
            LogUtils.debugf(this, "cfgFile = %s", cfgFile.getCanonicalPath());
            LogUtils.debugf(this, "config = %s", xmlString);
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();
            LogUtils.debugf(this, "finished writing");
        } finally {
            getWriteLock().unlock();
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
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }

    /**
     * <p>
     * setInstance
     * </p>
     * 
     * @param singleton
     *            a {@link org.opennms.netmgt.config.SnmpPeerFactory} object.
     */
    public static synchronized void setInstance(final SnmpPeerFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
    }

    public synchronized void setCategoryDao(final CategoryDao dao) {
        m_categoryDao = dao;
    }
    
    /**
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml. Note, this *is* used, in the
     * JSPs, but Eclipse is being silly.
     */
    @SuppressWarnings("unused")
    private void define(final InetAddress ip, final String community) throws UnknownHostException {
        getReadLock().lock();

        try {
            // Convert IP to long so that it easily compared in range elements
            final int address = new IPv4Address(ip).getAddress();
            final SnmpConfig config = getConfig();
    
            // Copy the current definitions so that elements can be added and
            // removed
            final ArrayList<Definition> definitions = new ArrayList<Definition>(config.getDefinitionCollection());
    
            // First step: Find the first definition matching the read-community
            // or
            // create a new definition, then add the specific IP
            Definition definition = null;
            for (final Definition currentDefinition : definitions) {
                if ((currentDefinition.getReadCommunity() != null && currentDefinition.getReadCommunity().equals(community))
                        || (currentDefinition.getReadCommunity() == null && config.getReadCommunity() != null && config.getReadCommunity().equals(community))) {
                    LogUtils.debugf(this, "define: Found existing definition with read-community %s", community);
                    definition = currentDefinition;
                    break;
                }
            }
            if (definition == null) {
                LogUtils.debugf(this, "define: Creating new definition");
    
                definition = new Definition();
                definition.setReadCommunity(community);
                definitions.add(definition);
            }
            definition.addSpecific(ip.getHostAddress());
    
            // Second step: Find and remove any existing specific and range
            // elements with matching IP among all definitions except for the
            // definition identified in the first step
            for (final Definition currentDefinition : definitions) {
                // Ignore this definition if it was the one identified by the
                // first step
                if (currentDefinition == definition)
                    continue;
    
                // Remove any specific elements that match IP
                while (currentDefinition.removeSpecific(ip.getHostAddress())) {
                    LogUtils.debugf(this, "define: Removed an existing specific element with IP %s", ip);
                }
    
                // Split and replace any range elements that contain IP
                final ArrayList<Range> ranges = new ArrayList<Range>(currentDefinition.getRangeCollection());
                final Range[] rangesArray = currentDefinition.getRange();
                for (final Range range : rangesArray) {
                    final int begin = new IPv4Address(range.getBegin()).getAddress();
                    final int end = new IPv4Address(range.getEnd()).getAddress();
                    if (address >= begin && address <= end) {
                        LogUtils.debugf(this, "define: Splitting range element with begin %s and end %s", range.getBegin(), range.getEnd());
    
                        if (begin == end) {
                            ranges.remove(range);
                            continue;
                        }
    
                        if (address == begin) {
                            range.setBegin(IPv4Address.addressToString(address + 1));
                            continue;
                        }
    
                        if (address == end) {
                            range.setEnd(IPv4Address.addressToString(address - 1));
                            continue;
                        }
    
                        final Range head = new Range();
                        head.setBegin(range.getBegin());
                        head.setEnd(IPv4Address.addressToString(address - 1));
    
                        final Range tail = new Range();
                        tail.setBegin(IPv4Address.addressToString(address + 1));
                        tail.setEnd(range.getEnd());
    
                        ranges.remove(range);
                        ranges.add(head);
                        ranges.add(tail);
                    }
                }
                currentDefinition.setRange(ranges);
            }
    
            // Store the altered list of definitions
            getConfig().setDefinition(definitions);
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress) {
        return getAgentConfig(agentAddress, VERSION_UNSPECIFIED);
    }

    private SnmpAgentConfig getAgentConfig(final InetAddress agentInetAddress, final int requestedSnmpVersion) {
        getReadLock().lock();

        try {
            final SnmpConfig config = getConfig();
    
            if (config == null) {
                final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
                if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
                    agentConfig.setVersion(SnmpAgentConfig.DEFAULT_VERSION);
                } else {
                    agentConfig.setVersion(requestedSnmpVersion);
                }
                return agentConfig;
            }
    
            final SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
    
            // Now set the defaults from the m_config
            initializeSnmpAgentConfig(agentConfig, new Definition(), requestedSnmpVersion);
    
            // Attempt to locate the node
            DEFLOOP: for (final Definition def : config.getDefinitionCollection()) {
                // check the specifics first
                //
                for (final String saddr : def.getSpecificCollection()) {
                    try {
                        final InetAddress addr = InetAddress.getByName(saddr);
                        if (addr.equals(agentConfig.getAddress())) {
                            initializeSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                            break DEFLOOP;
                        }
                    } catch (final UnknownHostException e) {
                        LogUtils.warnf(this, e, "SnmpPeerFactory: could not convert host %s to InetAddress", saddr);
                    }
                }
    
                // check the ranges
                //
                final long lhost = InetAddressUtils.toIpAddrLong(agentConfig.getAddress());
                for (final Range rng : def.getRangeCollection()) {
                    try {
                        final InetAddress begin = InetAddress.getByName(rng.getBegin());
                        final InetAddress end = InetAddress.getByName(rng.getEnd());
    
                        final long start = InetAddressUtils.toIpAddrLong(begin);
                        final long stop = InetAddressUtils.toIpAddrLong(end);
    
                        if (start <= lhost && lhost <= stop) {
                            initializeSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                            break DEFLOOP;
                        }
                    } catch (final UnknownHostException e) {
                        LogUtils.warnf(this, e, "SnmpPeerFactory: could not convert host(s) %s - %s to InetAddrss", rng.getBegin(), rng.getEnd());
                    }
                }
    
                // check the matching ip expressions
                for (final String ipMatch : def.getIpMatchCollection()) {
                    if (ipMatch.startsWith("~")) {
                        if (agentInetAddress.getHostAddress().matches(ipMatch.substring(1))) {
                            initializeSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                            break DEFLOOP;
                        }
                    } else if (ipMatch.startsWith("catinc")) {
                        final String matchMe = ipMatch.replaceFirst("^catinc", "");
                        final OnmsCategory cat = m_categoryDao.findByName(matchMe);
                        if (cat != null) {
                        }
                    } else if (IPLike.matches(agentInetAddress.getHostAddress(), ipMatch)) {
                        initializeSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                }
    
            } // end DEFLOOP
    
            if (agentConfig == null) {
                initializeSnmpAgentConfig(agentConfig, new Definition(), requestedSnmpVersion);
            }
    
            return agentConfig;
        } finally {
            getReadLock().unlock();
        }
    }

    private void initializeSnmpAgentConfig(final SnmpAgentConfig agentConfig, final Definition def, final int requestedSnmpVersion) {
        setCommonAttributes(agentConfig, def, determineVersion(def, requestedSnmpVersion));

        agentConfig.setSecurityLevel(determineSecurityLevel(def));
        agentConfig.setSecurityName(determineSecurityName(def));
        agentConfig.setAuthProtocol(determineAuthProtocol(def));
        agentConfig.setAuthPassPhrase(determineAuthPassPhrase(def));
        agentConfig.setPrivPassPhrase(determinePrivPassPhrase(def));
        agentConfig.setPrivProtocol(determinePrivProtocol(def));
        agentConfig.setReadCommunity(determineReadCommunity(def));
        agentConfig.setWriteCommunity(determineWriteCommunity(def));
    }

    /**
     * This is a helper method to set all the common attributes in the
     * agentConfig.
     * 
     * @param agentConfig
     * @param def
     * @param version
     */
    private void setCommonAttributes(final SnmpAgentConfig agentConfig, final Definition def, final int version) {
        agentConfig.setVersion(version);
        agentConfig.setPort(determinePort(def));
        agentConfig.setRetries(determineRetries(def));
        agentConfig.setTimeout((int) determineTimeout(def));
        agentConfig.setMaxRequestSize(determineMaxRequestSize(def));
        agentConfig.setMaxVarsPerPdu(determineMaxVarsPerPdu(def));
        agentConfig.setMaxRepetitions(determineMaxRepetitions(def));
        InetAddress proxyHost = determineProxyHost(def);

        if (proxyHost != null) {
            agentConfig.setProxyFor(agentConfig.getAddress());
            agentConfig.setAddress(determineProxyHost(def));
        }
    }

    private int determineMaxRepetitions(final Definition def) {
        final SnmpConfig config = getConfig();
        return (!def.hasMaxRepetitions() ? (!config.hasMaxRepetitions() ? SnmpAgentConfig.DEFAULT_MAX_REPETITIONS : config.getMaxRepetitions()) : def.getMaxRepetitions());
    }

    private InetAddress determineProxyHost(final Definition def) {
        final SnmpConfig config = getConfig();
        InetAddress inetAddr = null;
        final String address = def.getProxyHost() == null ? (config.getProxyHost() == null ? null : config.getProxyHost()) : def.getProxyHost();
        if (address != null) {
            try {
                inetAddr = InetAddress.getByName(address);
            } catch (final UnknownHostException e) {
                LogUtils.errorf(this, e, "determineProxyHost: Problem converting proxy host string to InetAddress");
            }
        }
        return inetAddr;
    }

    private int determineMaxVarsPerPdu(final Definition def) {
        final SnmpConfig config = getConfig();
        return (!def.hasMaxVarsPerPdu() ? (!config.hasMaxVarsPerPdu() ? SnmpAgentConfig.DEFAULT_MAX_VARS_PER_PDU : config.getMaxVarsPerPdu()) : def.getMaxVarsPerPdu());
    }

    /**
     * Helper method to search the snmp-config for the appropriate read
     * community string.
     * 
     * @param def
     * @return
     */
    private String determineReadCommunity(final Definition def) {
        final SnmpConfig config = getConfig();
        return (def.getReadCommunity() == null ? (config.getReadCommunity() == null ? SnmpAgentConfig.DEFAULT_READ_COMMUNITY : config.getReadCommunity()) : def.getReadCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate write
     * community string.
     * 
     * @param def
     * @return
     */
    private String determineWriteCommunity(final Definition def) {
        final SnmpConfig config = getConfig();
        return (def.getWriteCommunity() == null ? (config.getWriteCommunity() == null ? SnmpAgentConfig.DEFAULT_WRITE_COMMUNITY : config.getWriteCommunity())
            : def.getWriteCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate maximum
     * request size. The default is the minimum necessary for a request.
     * 
     * @param def
     * @return
     */
    private int determineMaxRequestSize(final Definition def) {
        final SnmpConfig config = getConfig();
        return (!def.hasMaxRequestSize() ? (!config.hasMaxRequestSize() ? SnmpAgentConfig.DEFAULT_MAX_REQUEST_SIZE : config.getMaxRequestSize()) : def.getMaxRequestSize());
    }

    /**
     * Helper method to find a security name to use in the snmp-config. If v3
     * has been specified and one can't be found, then a default is used for
     * this is a required option for v3 operations.
     * 
     * @param def
     * @return
     */
    private String determineSecurityName(final Definition def) {
        final SnmpConfig config = getConfig();
        String securityName = (def.getSecurityName() == null ? config.getSecurityName() : def.getSecurityName());
        if (securityName == null) {
            securityName = SnmpAgentConfig.DEFAULT_SECURITY_NAME;
        }
        return securityName;
    }

    /**
     * Helper method to find a security name to use in the snmp-config. If v3
     * has been specified and one can't be found, then a default is used for
     * this is a required option for v3 operations.
     * 
     * @param def
     * @return
     */
    private String determineAuthProtocol(final Definition def) {
        final SnmpConfig config = getConfig();
        String authProtocol = (def.getAuthProtocol() == null ? config.getAuthProtocol() : def.getAuthProtocol());
        if (authProtocol == null) {
            authProtocol = SnmpAgentConfig.DEFAULT_AUTH_PROTOCOL;
        }
        return authProtocol;
    }

    /**
     * Helper method to find a authentication passphrase to use from the
     * snmp-config. If v3 has been specified and one can't be found, then a
     * default is used for this is a required option for v3 operations.
     * 
     * @param def
     * @return
     */
    private String determineAuthPassPhrase(final Definition def) {
        final SnmpConfig config = getConfig();
        String authPassPhrase = (def.getAuthPassphrase() == null ? config.getAuthPassphrase() : def.getAuthPassphrase());
        if (authPassPhrase == null) {
            authPassPhrase = SnmpAgentConfig.DEFAULT_AUTH_PASS_PHRASE;
        }
        return authPassPhrase;
    }

    /**
     * Helper method to find a privacy passphrase to use from the snmp-config.
     * If v3 has been specified and one can't be found, then a default is used
     * for this is a required option for v3 operations.
     * 
     * @param def
     * @return
     */
    private String determinePrivPassPhrase(final Definition def) {
        final SnmpConfig config = getConfig();
        String privPassPhrase = (def.getPrivacyPassphrase() == null ? config.getPrivacyPassphrase() : def.getPrivacyPassphrase());
        if (privPassPhrase == null) {
            privPassPhrase = SnmpAgentConfig.DEFAULT_PRIV_PASS_PHRASE;
        }
        return privPassPhrase;
    }

    /**
     * Helper method to find a privacy protocol to use from the snmp-config.
     * If v3 has been specified and one can't be found, then a default is used
     * for this is a required option for v3 operations.
     * 
     * @param def
     * @return
     */
    private String determinePrivProtocol(final Definition def) {
        final SnmpConfig config = getConfig();
        String authPrivProtocol = (def.getPrivacyProtocol() == null ? config.getPrivacyProtocol() : def.getPrivacyProtocol());
        if (authPrivProtocol == null) {
            authPrivProtocol = SnmpAgentConfig.DEFAULT_PRIV_PROTOCOL;
        }
        return authPrivProtocol;
    }

    /**
     * Helper method to set the security level in v3 operations. The default
     * is noAuthNoPriv if there is no authentication passphrase. From there,
     * if there is a privacy passphrase supplied, then the security level is
     * set to authPriv else it falls out to authNoPriv. There are only these 3
     * possible security levels. default
     * 
     * @param def
     * @return
     */
    private int determineSecurityLevel(final Definition def) {
        final SnmpConfig config = getConfig();

        // use the def security level first
        if (def.hasSecurityLevel()) {
            return def.getSecurityLevel();
        }

        // use a configured default security level next
        if (config.hasSecurityLevel()) {
            return config.getSecurityLevel();
        }

        // if no security level configuration exists use
        int securityLevel = SnmpAgentConfig.NOAUTH_NOPRIV;

        final String authPassPhrase = (def.getAuthPassphrase() == null ? config.getAuthPassphrase() : def.getAuthPassphrase());
        final String privPassPhrase = (def.getPrivacyPassphrase() == null ? config.getPrivacyPassphrase() : def.getPrivacyPassphrase());

        if (authPassPhrase == null) {
            securityLevel = SnmpAgentConfig.NOAUTH_NOPRIV;
        } else {
            if (privPassPhrase == null) {
                securityLevel = SnmpAgentConfig.AUTH_NOPRIV;
            } else {
                securityLevel = SnmpAgentConfig.AUTH_PRIV;
            }
        }

        return securityLevel;
    }

    /**
     * Helper method to search the snmp-config for a port
     * 
     * @param def
     * @return
     */
    private int determinePort(final Definition def) {
        final SnmpConfig config = getConfig();
        return (def.getPort() == 0 ? (config.getPort() == 0 ? 161 : config.getPort()) : def.getPort());
    }

    /**
     * Helper method to search the snmp-config
     * 
     * @param def
     * @return
     */
    private long determineTimeout(final Definition def) {
        final SnmpConfig config = getConfig();
        return (def.getTimeout() == 0 ? (config.getTimeout() == 0 ? (long) SnmpAgentConfig.DEFAULT_TIMEOUT : config.getTimeout()) : def.getTimeout());
    }

    private int determineRetries(final Definition def) {
        final SnmpConfig config = getConfig();
        return (def.getRetry() == 0 ? (config.getRetry() == 0 ? SnmpAgentConfig.DEFAULT_RETRIES : config.getRetry()) : def.getRetry());
    }

    /**
     * This method determines the configured SNMP version. the order of
     * operations is: 1st: return a valid requested version 2nd: return a
     * valid version defined in a definition within the snmp-config 3rd:
     * return a valid version in the snmp-config 4th: return the default
     * version
     * 
     * @param def
     * @param requestedSnmpVersion
     * @return
     */
    private int determineVersion(final Definition def, final int requestedSnmpVersion) {
        final SnmpConfig config = getConfig();

        int version = SnmpAgentConfig.VERSION1;

        String cfgVersion = "v1";
        if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
            if (def.getVersion() == null) {
                if (config.getVersion() == null) {
                    return version;
                } else {
                    cfgVersion = config.getVersion();
                }
            } else {
                cfgVersion = def.getVersion();
            }
        } else {
            return requestedSnmpVersion;
        }

        if (cfgVersion.equals("v1")) {
            version = SnmpAgentConfig.VERSION1;
        } else if (cfgVersion.equals("v2c")) {
            version = SnmpAgentConfig.VERSION2C;
        } else if (cfgVersion.equals("v3")) {
            version = SnmpAgentConfig.VERSION3;
        }

        return version;
    }

    /**
     * <p>
     * getSnmpConfig
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
     */
    public static SnmpConfig getSnmpConfig() {
        return SnmpPeerFactory.getInstance().getConfig();
    }

    /**
     * Enhancement: Allows specific or ranges to be merged into snmp
     * configuration with many other attributes. Uses new classes the wrap
     * Castor generated code to help with merging, comparing, and optimizing
     * definitions. Thanks for your initial work on this Gerald. Puts a
     * specific IP address with associated read-community string into the
     * currently loaded snmp-config.xml.
     * 
     * @param info
     *            a {@link org.opennms.netmgt.config.SnmpEventInfo} object.
     */
    public void define(final SnmpEventInfo info) {
        getReadLock().lock();
        try {
            final SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());
            LogUtils.debugf(this, "%s before = %s", marshallConfig(mgr.getConfig()));
            mgr.mergeIntoConfig(info.createDef());
            LogUtils.debugf(this, "%s after = %s", marshallConfig(mgr.getConfig()));
        } finally {
            getReadLock().unlock();
        }
    }

    public String marshallConfig() {
        return marshallConfig(getConfig());
    }
    
    /**
     * Creates a string containing the XML of the current SnmpConfig
     * 
     * @return Marshalled SnmpConfig
     */
    public String marshallConfig(final SnmpConfig config) {
        LogUtils.debugf(this, "marshalling config %s", config);

        String marshalledConfig = null;
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            Marshaller.marshal(config, writer);
            marshalledConfig = writer.toString();
        } catch (final MarshalException e) {
            LogUtils.errorf(SnmpPeerFactory.class, e, "marshallConfig: Error marshalling configuration");
        } catch (final ValidationException e) {
            LogUtils.errorf(SnmpPeerFactory.class, e, "marshallConfig: Error validating configuration");
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return marshalledConfig;
    }

}
