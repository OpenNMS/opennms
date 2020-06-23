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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.ami.AmiAgentConfig;
import org.opennms.netmgt.config.ami.AmiConfig;
import org.opennms.netmgt.config.ami.Definition;
import org.opennms.netmgt.config.ami.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the main repository for AMI configuration information used by
 * the capabilities daemon. When this class is loaded it reads the AMI
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.protocols.ami.AmiAgentConfig AmiAgentConfig} objects for specific
 * addresses. If an address cannot be located in the configuration then a
 * default peer instance is returned to the caller.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public class AmiPeerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AmiPeerFactory.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
    /**
     * The singleton instance of this factory
     */
    private static AmiPeerFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private static AmiConfig m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     *
     * @param configFile the path to the config file to load in.
     */
    private AmiPeerFactory(final String configFile) throws IOException {
        super();
        m_config = JaxbUtils.unmarshal(AmiConfig.class, new File(configFile));
    }

    public Lock getReadLock() {
        return m_readLock;
    }
    
    public Lock getWriteLock() {
        return m_writeLock;
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

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.AMI_CONFIG_FILE_NAME);
        LOG.debug("init: config file path: {}", cfgFile.getPath());
        m_singleton = new AmiPeerFactory(cfgFile.getPath());
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
    public static synchronized AmiPeerFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The AmiPeerFactory has not been initialized");

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.AmiPeerFactory} object.
     */
    public static synchronized void setInstance(final AmiPeerFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
    }

    /**
     * <p>setAmiConfig</p>
     *
     * @param m_config a {@link org.opennms.netmgt.config.ami.AmiConfig} object.
     */
    public static synchronized void setAmiConfig(AmiConfig m_config) {
        AmiPeerFactory.m_config = m_config;
    }

    /**
     * Saves the current settings to disk
     *
     * @throws java.lang.Exception if saving settings to disk fails.
     */
    public void saveCurrent() throws Exception {
        getWriteLock().lock();
        
        try {
            optimize();
    
            // Marshal to a string first, then write the string to the file. This
            // way the original config
            // isn't lost if the XML from the marshal is hosed.
            final StringWriter stringWriter = new StringWriter();
            JaxbUtils.marshal(m_config, stringWriter);
            if (stringWriter.toString() != null) {
                final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.AMI_CONFIG_FILE_NAME)), StandardCharsets.UTF_8);
                fileWriter.write(stringWriter.toString());
                fileWriter.flush();
                fileWriter.close();
            }
    
            reload();
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Combine specific and range elements so that AMIPeerFactory has to spend
     * less time iterating all these elements.
     * TODO This really should be pulled up into PeerFactory somehow, but I'm not sure how (given that "Definition" is different for both
     * SNMP and AMI.  Maybe some sort of visitor methodology would work.  The basic logic should be fine as it's all IP address manipulation
     *
     * @throws UnknownHostException
     */
    void optimize() throws UnknownHostException {
        getWriteLock().lock();
        
        try {
            // First pass: Remove empty definition elements
            for (final Iterator<Definition> definitionsIterator = m_config.getDefinitions().iterator(); definitionsIterator.hasNext();) {
                final Definition definition = definitionsIterator.next();
                if (definition.getSpecifics().size() == 0
                    && definition.getRanges().size() == 0) {
                    LOG.debug("optimize: Removing empty definition element");
                    definitionsIterator.remove();
                }
            }
    
            // Second pass: Replace single IP range elements with specific elements
            for (Definition definition : m_config.getDefinitions()) {
                for (Iterator<Range> rangesIterator = definition.getRanges().iterator(); rangesIterator.hasNext();) {
                    Range range = rangesIterator.next();
                    if (range.getBegin().equals(range.getEnd())) {
                        definition.addSpecific(range.getBegin());
                        rangesIterator.remove();
                    }
                }
            }
    
            // Third pass: Sort specific and range elements for improved XML
            // readability and then combine them into fewer elements where possible
            for (final Definition definition : m_config.getDefinitions()) {
                // Sort specifics
                final TreeMap<InetAddress,String> specificsMap = new TreeMap<InetAddress,String>(new InetAddressComparator());
                for (final String specific : definition.getSpecifics()) {
                    specificsMap.put(InetAddressUtils.getInetAddress(specific), specific.trim());
                }
    
                // Sort ranges
                final TreeMap<InetAddress,Range> rangesMap = new TreeMap<InetAddress,Range>(new InetAddressComparator());
                for (final Range range : definition.getRanges()) {
                    rangesMap.put(InetAddressUtils.getInetAddress(range.getBegin()), range);
                }
    
                // Combine consecutive specifics into ranges
                InetAddress priorSpecific = null;
                Range addedRange = null;
                for (final InetAddress specific : specificsMap.keySet()) {
                    if (priorSpecific == null) {
                        priorSpecific = specific;
                        continue;
                    }
    
                    if (BigInteger.ONE.equals(InetAddressUtils.difference(specific, priorSpecific)) &&
                            InetAddressUtils.inSameScope(specific, priorSpecific)) {
                        if (addedRange == null) {
                            addedRange = new Range();
                            addedRange.setBegin(InetAddressUtils.toIpAddrString(priorSpecific));
                            rangesMap.put(priorSpecific, addedRange);
                            specificsMap.remove(priorSpecific);
                        }
    
                        addedRange.setEnd(InetAddressUtils.toIpAddrString(specific));
                        specificsMap.remove(specific);
                    }
                    else {
                        addedRange = null;
                    }
    
                    priorSpecific = specific;
                }
    
                // Move specifics to ranges
                for (final InetAddress specific : new ArrayList<InetAddress>(specificsMap.keySet())) {
                    for (final InetAddress begin : new ArrayList<InetAddress>(rangesMap.keySet())) {
                        
                        if (!InetAddressUtils.inSameScope(begin, specific)) {
                            continue;
                        }
    
                        if (InetAddressUtils.toInteger(begin).subtract(BigInteger.ONE).compareTo(InetAddressUtils.toInteger(specific)) > 0) {
                            continue;
                        }
    
                        final Range range = rangesMap.get(begin);
    
                        final InetAddress end = InetAddressUtils.getInetAddress(range.getEnd());
    
                        if (InetAddressUtils.toInteger(end).add(BigInteger.ONE).compareTo(InetAddressUtils.toInteger(specific)) < 0) {
                            continue;
                        }
    
                        if (
                            InetAddressUtils.toInteger(specific).compareTo(InetAddressUtils.toInteger(begin)) >= 0 &&
                            InetAddressUtils.toInteger(specific).compareTo(InetAddressUtils.toInteger(end)) <= 0
                        ) {
                            specificsMap.remove(specific);
                            break;
                        }
    
                        if (InetAddressUtils.toInteger(begin).subtract(BigInteger.ONE).equals(InetAddressUtils.toInteger(specific))) {
                            rangesMap.remove(begin);
                            rangesMap.put(specific, range);
                            range.setBegin(InetAddressUtils.toIpAddrString(specific));
                            specificsMap.remove(specific);
                            break;
                        }
    
                        if (InetAddressUtils.toInteger(end).add(BigInteger.ONE).equals(InetAddressUtils.toInteger(specific))) {
                            range.setEnd(InetAddressUtils.toIpAddrString(specific));
                            specificsMap.remove(specific);
                            break;
                        }
                    }
                }
    
                // Combine consecutive ranges
                Range priorRange = null;
                InetAddress priorBegin = null;
                InetAddress priorEnd = null;
                for (final Iterator<InetAddress> rangesIterator = rangesMap.keySet().iterator(); rangesIterator.hasNext();) {
                    final InetAddress beginAddress = rangesIterator.next();
                    final Range range = rangesMap.get(beginAddress);
                    final InetAddress endAddress = InetAddressUtils.getInetAddress(range.getEnd());
    
                    if (priorRange != null) {
                        if (InetAddressUtils.inSameScope(beginAddress, priorEnd) && InetAddressUtils.difference(beginAddress, priorEnd).compareTo(BigInteger.ONE) <= 0) {
                            priorBegin = new InetAddressComparator().compare(priorBegin, beginAddress) < 0 ? priorBegin : beginAddress;
                            priorRange.setBegin(InetAddressUtils.toIpAddrString(priorBegin));
                            priorEnd = new InetAddressComparator().compare(priorEnd, endAddress) > 0 ? priorEnd : endAddress;
                            priorRange.setEnd(InetAddressUtils.toIpAddrString(priorEnd));
    
                            rangesIterator.remove();
                            continue;
                        }
                    }
    
                    priorRange = range;
                    priorBegin = beginAddress;
                    priorEnd = endAddress;
                }
    
                // Update changes made to sorted maps
                definition.setSpecifics(new ArrayList<>(specificsMap.values()));
                definition.setRanges(new ArrayList<>(rangesMap.values()));
            }
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * <p>getAgentConfig</p>
     *
     * @param agentInetAddress a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.protocols.ami.AmiAgentConfig} object.
     */
    public AmiAgentConfig getAgentConfig(final InetAddress agentInetAddress) {
        getReadLock().lock();
        
        try {
            if (m_config == null) return new AmiAgentConfig(agentInetAddress);
            
            final AmiAgentConfig agentConfig = new AmiAgentConfig(agentInetAddress);
            
            //Now set the defaults from the m_config
            setAmiAgentConfig(agentConfig, new Definition());
    
            // Attempt to locate the node
            DEFLOOP: for (final Definition def : m_config.getDefinitions()) {
                // check the specifics first
                for (String saddr : def.getSpecifics()) {
                    saddr = saddr.trim();
                    final InetAddress addr = InetAddressUtils.addr(saddr);
                    if (addr.equals(agentConfig.getAddress())) {
                        setAmiAgentConfig(agentConfig, def);
                        break DEFLOOP;
                    }
                }
    
                // check the ranges
                for (final Range rng : def.getRanges()) {
                    if (InetAddressUtils.isInetAddressInRange(InetAddressUtils.str(agentConfig.getAddress().orElse(null)), rng.getBegin(), rng.getEnd())) {
                        setAmiAgentConfig(agentConfig, def );
                        break DEFLOOP;
                    }
                }
                
                // check the matching IP expressions
                for (final String ipMatch : def.getIpMatches()) {
                    if (IPLike.matches(InetAddressUtils.str(agentInetAddress), ipMatch)) {
                        setAmiAgentConfig(agentConfig, def);
                        break DEFLOOP;
                    }
                }
                
            } // end DEFLOOP
    
            if (agentConfig == null) setAmiAgentConfig(agentConfig, new Definition());
    
            return agentConfig;
        } finally {
            getReadLock().unlock();
        }
    }
    
    private void setAmiAgentConfig(final AmiAgentConfig agentConfig, final Definition def) {
        setCommonAttributes(agentConfig, def);
        agentConfig.setPassword(determinePassword(def));
    }
    
    /**
     * This is a helper method to set all the common attributes in the agentConfig.
     * 
     * @param agentConfig
     * @param def
     */
    private void setCommonAttributes(final AmiAgentConfig agentConfig, final Definition def) {
        agentConfig.setRetries(determineRetries(def));
        agentConfig.setTimeout((int)determineTimeout(def));
        agentConfig.setUsername(determineUsername(def));
        agentConfig.setPassword(determinePassword(def));
    }

    /**
     * Helper method to search the ami-config for the appropriate username
     * @param def
     * @return a string containing the username. will return the default if none is set.
     */
    private String determineUsername(final Definition def) {
        return def.getUsername().orElse(m_config.getUsername().orElse(AmiAgentConfig.DEFAULT_USERNAME));
    }

     /**
     * Helper method to search the ami-config for the appropriate password
     * @param def
     * @return a string containing the password. will return the default if none is set.
     */
    private String determinePassword(final Definition def) {
        return def.getPassword().orElse(m_config.getPassword().orElse(AmiAgentConfig.DEFAULT_PASSWORD));
    }

    /**
     * Helper method to search the ami-config 
     * @param def
     * @return a long containing the timeout, AmiAgentConfig.DEFAULT_TIMEOUT if not specified.
     */
    private long determineTimeout(final Definition def) {
        if (def.getTimeout().isPresent() && def.getTimeout().get() != 0) {
            return def.getTimeout().get();
        } else if (m_config.getTimeout() != null && m_config.getTimeout() != 0) {
            return m_config.getTimeout();
        }
        return (long) AmiAgentConfig.DEFAULT_TIMEOUT;
    }

    private int determineRetries(final Definition def) {   
        if (def.getRetry().isPresent() && def.getRetry().get() != 0) {
            return def.getRetry().get();
        } else if (m_config.getRetry() != 0) {
            return m_config.getRetry();
        }
        return AmiAgentConfig.DEFAULT_RETRIES;
    }

    /**
     * <p>getAmiConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.ami.AmiConfig} object.
     */
    public static AmiConfig getAmiConfig() {
        return m_config;
    }
}
