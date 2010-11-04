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
// Modifications:


package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.nsclient.Definition;
import org.opennms.netmgt.config.nsclient.NsclientConfig;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.poller.nsclient.NSClientAgentConfig;
import org.opennms.protocols.ip.IPv4Address;
import org.springframework.core.io.FileSystemResource;

/**
 * This class is the main repository for NSCLient configuration information used by
 * the capabilities daemon. When this class is loaded it reads the nsclient
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.netmgt.nsclient.NSClientAgentConfig NSClientAgentConfig} objects for specific
 * addresses. If an address cannot be located in the configuration then a
 * default peer instance is returned to the caller.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class NSClientPeerFactory extends PeerFactory {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
    /**
     * The singleton instance of this factory
     */
    private static NSClientPeerFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private NsclientConfig m_config;

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
    private NSClientPeerFactory(final String configFile) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(NsclientConfig.class, new FileSystemResource(configFile), CastorUtils.PRESERVE_WHITESPACE);
    }
    
    /**
     * <p>Constructor for NSClientPeerFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public NSClientPeerFactory(final InputStream stream) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(NsclientConfig.class, stream, CastorUtils.PRESERVE_WHITESPACE);
    }
    
    /**
     * <p>Constructor for NSClientPeerFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @Deprecated
    public NSClientPeerFactory(final Reader rdr) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(NsclientConfig.class, rdr, CastorUtils.PRESERVE_WHITESPACE);
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

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_CONFIG_FILE_NAME);

        LogUtils.debugf(NSClientPeerFactory.class, "init: config file path: %s", cfgFile.getPath());
        m_singleton = new NSClientPeerFactory(cfgFile.getPath());
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
     * Saves the current settings to disk
     *
     * @throws java.lang.Exception if any.
     */
    public void saveCurrent() throws Exception {
        getWriteLock().lock();
        
        try {
            optimize();
    
            // Marshall to a string first, then write the string to the file. This
            // way the original config
            // isn't lost if the XML from the marshall is hosed.
            final StringWriter stringWriter = new StringWriter();
            Marshaller.marshal(m_config, stringWriter);
            if (stringWriter.toString() != null) {
                final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_CONFIG_FILE_NAME)), "UTF-8");
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
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized NSClientPeerFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The NSClientPeerFactory has not been initialized");
        }

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.NSClientPeerFactory} object.
     */
    public static synchronized void setInstance(NSClientPeerFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
    }

    /**
     * Puts a specific IP address with associated password into
     * the currently loaded nsclient-config.xml.
     *  Perhaps with a bit of jiggery pokery this could be pulled up into PeerFactory
     *
     * @param ip a {@link java.net.InetAddress} object.
     * @param password a {@link java.lang.String} object.
     * @throws java.net.UnknownHostException if any.
     */
    public void define(final InetAddress ip, final String password) throws UnknownHostException {
        getWriteLock().lock();

        try {
            // Convert IP to long so that it easily compared in range elements
            int address = new IPv4Address(ip).getAddress();

            // Copy the current definitions so that elements can be added and removed
            final ArrayList<Definition> definitions = new ArrayList<Definition>(m_config.getDefinitionCollection());

            // First step: Find the first definition matching the
            // read-community or
            // create a new definition, then add the specific IP
            Definition definition = null;
            for (final Definition currentDefinition : definitions) {
                if ((currentDefinition.getPassword() != null && currentDefinition.getPassword().equals(password))
                        || (currentDefinition.getPassword() == null && m_config.getPassword() != null && m_config.getPassword().equals(password))) {
                    LogUtils.debugf(this, "define: Found existing definition with read-community %s", password);
                    definition = currentDefinition;
                    break;
                }
            }
            if (definition == null) {
                LogUtils.debugf(this, "define: Creating new definition");

                definition = new Definition();
                definition.setPassword(password);
                definitions.add(definition);
            }
            definition.addSpecific(ip.getHostAddress());

            // Second step: Find and remove any existing specific and range
            // elements with matching IP among all definitions except for the
            // definition identified in the first step
            for (final Definition currentDefinition : definitions) {
                // Ignore this definition if it was the one identified by the first step
                if (currentDefinition == definition) {
                    continue;
                }

                // Remove any specific elements that match IP
                while (currentDefinition.removeSpecific(ip.getHostAddress())) {
                    LogUtils.debugf(this, "define: Removed an existing specific element with IP %s", ip);
                }

                // Split and replace any range elements that contain IP
                final ArrayList<Range> ranges = new ArrayList<Range>(currentDefinition.getRangeCollection());
                for (final Range range : currentDefinition.getRange()) {
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
            m_config.setDefinition(definitions);
        } finally {
            getWriteLock().unlock();
        }
    }
    
    /**
     * <p>getAgentConfig</p>
     *
     * @param agentInetAddress a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.nsclient.NSClientAgentConfig} object.
     */
    public NSClientAgentConfig getAgentConfig(final InetAddress agentInetAddress) {
        getReadLock().lock();
        
        try {
            if (m_config == null) {
                return new NSClientAgentConfig(agentInetAddress);
            }
            
            final NSClientAgentConfig agentConfig = new NSClientAgentConfig(agentInetAddress);
            
            //Now set the defaults from the m_config
            setNSClientAgentConfig(agentConfig, new Definition());
    
            // Attempt to locate the node
            DEFLOOP: for (final Definition def : m_config.getDefinitionCollection()) {
                // check the specifics first
    
                for (final String saddr : def.getSpecificCollection()) {
                    try {
                        final InetAddress addr = InetAddress.getByName(saddr);
                        if (addr.equals(agentConfig.getAddress())) {
                            setNSClientAgentConfig(agentConfig, def);
                            break DEFLOOP;
                        }
                    } catch (final UnknownHostException e) {
                        LogUtils.warnf(this, e, "NSClientPeerFactory: could not convert host %s to InetAddress", saddr);
                    }
                }
    
                // check the ranges
                //
                for (final Range rng : def.getRangeCollection()) {
                    if (InetAddressUtils.isInetAddressInRange(agentConfig.getAddress().getHostAddress(), rng.getBegin(), rng.getEnd())) {
                        setNSClientAgentConfig(agentConfig, def);
                        break DEFLOOP;
                    }
                }
                
                // check the matching IP expressions
                for (final String ipMatch : def.getIpMatchCollection()) {
                    if (IPLike.matches(agentInetAddress.getHostAddress(), ipMatch)) {
                        setNSClientAgentConfig(agentConfig, def);
                        break DEFLOOP;
                    }
                }
                
            } // end DEFLOOP
    
            if (agentConfig == null) {
                setNSClientAgentConfig(agentConfig, new Definition());
            }
    
            return agentConfig;
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * Combine specific and range elements so that NSClientPeerFactory has to spend
     * less time iterating all these elements.
     * TODO This really should be pulled up into PeerFactory somehow, but I'm not sure how (given that "Definition" is different for both
     * Snmp and NSClient.  Maybe some sort of visitor methodology would work.  The basic logic should be fine as it's all IP address manipulation
     */
    private void optimize() throws UnknownHostException {
        // First pass: Remove empty definition elements
        for (final Iterator<Definition> definitionsIterator = m_config.getDefinitionCollection().iterator(); definitionsIterator.hasNext();) {
            final Definition definition = definitionsIterator.next();
            if (definition.getSpecificCount() == 0 && definition.getRangeCount() == 0) {
                LogUtils.debugf(this, "optimize: Removing empty definition element");
                definitionsIterator.remove();
            }
        }

        // Second pass: Replace single IP range elements with specific elements
        for (final Definition definition : m_config.getDefinitionCollection()) {
            for (final Iterator<Range> rangesIterator = definition.getRangeCollection().iterator(); rangesIterator.hasNext();) {
                final Range range = rangesIterator.next();
                if (range.getBegin().equals(range.getEnd())) {
                    definition.addSpecific(range.getBegin());
                    rangesIterator.remove();
                }
            }
        }

        // Third pass: Sort specific and range elements for improved XML
        // readability and then combine them into fewer elements where possible
        for (final Definition definition : m_config.getDefinitionCollection()) {
            // Sort specifics
            final TreeMap<Integer, String> specificsMap = new TreeMap<Integer, String>();
            for (final String specific : definition.getSpecificCollection()) {
                specificsMap.put(Integer.valueOf(new IPv4Address(specific).getAddress()), specific.trim());
            }

            // Sort ranges
            final TreeMap<Integer, Range> rangesMap = new TreeMap<Integer, Range>();
            for (final Range range : definition.getRangeCollection()) {
                rangesMap.put(new IPv4Address(range.getBegin()).getAddress(), range);
            }

            // Combine consecutive specifics into ranges
            Integer priorSpecific = null;
            Range addedRange = null;
            for (final Integer specific : specificsMap.keySet()) {
                if (priorSpecific == null) {
                    priorSpecific = specific;
                    continue;
                }

                final int specificInt = specific.intValue();
                final int priorSpecificInt = priorSpecific.intValue();

                if (specificInt == priorSpecificInt + 1) {
                    if (addedRange == null) {
                        addedRange = new Range();
                        addedRange.setBegin
                             (IPv4Address.addressToString(priorSpecificInt));
                        rangesMap.put(priorSpecific, addedRange);
                        specificsMap.remove(priorSpecific);
                    }

                    addedRange.setEnd(IPv4Address.addressToString(specificInt));
                    specificsMap.remove(specific);
                }
                else {
                    addedRange = null;
                }

                priorSpecific = specific;
            }

            // Move specifics to ranges
            for (final Integer specific : specificsMap.keySet()) {
                for (final Integer begin : rangesMap.keySet()) {
                    if (specific < begin - 1) {
                        continue;
                    }

                    final Range range = rangesMap.get(begin);

                    int endInt = new IPv4Address(range.getEnd()).getAddress();

                    if (specific > endInt + 1) {
                        continue;
                    }

                    if (specific >= begin && specific <= endInt) {
                        specificsMap.remove(specific);
                        break;
                    }

                    if (specific == begin - 1) {
                        rangesMap.remove(begin);
                        rangesMap.put(specific, range);
                        range.setBegin(IPv4Address.addressToString(specific));
                        specificsMap.remove(specific);
                        break;
                    }

                    if (specific == endInt + 1) {
                        range.setEnd(IPv4Address.addressToString(specific));
                        specificsMap.remove(specific);
                        break;
                    }
                }
            }

            // Combine consecutive ranges
            Range priorRange = null;
            int priorBegin = 0;
            int priorEnd = 0;
            for (final Iterator<Integer> rangesIterator = rangesMap.keySet().iterator(); rangesIterator.hasNext();) {
                final Integer begin = rangesIterator.next();

                final Range range = rangesMap.get(begin);

                final int end = new IPv4Address(range.getEnd()).getAddress();

                if (priorRange != null) {
                    if (begin - priorEnd <= 1) {
                        priorRange.setBegin(IPv4Address.addressToString(Math.min(priorBegin, begin)));
                        priorRange.setEnd(IPv4Address.addressToString(Math.max(priorEnd, end)));
                        rangesIterator.remove();
                        continue;
                    }
                }

                priorRange = range;
                priorBegin = begin;
                priorEnd = end;
            }

            // Update changes made to sorted maps
            definition.setSpecific(new ArrayList<String>(specificsMap.values()));
            definition.setRange(new ArrayList<Range>(rangesMap.values()));
        }
    }

    private void setNSClientAgentConfig(final NSClientAgentConfig agentConfig, final Definition def) {
        setCommonAttributes(agentConfig, def);
        agentConfig.setPassword(determinePassword(def));       
    }
    
    /**
     * This is a helper method to set all the common attributes in the agentConfig.
     * 
     * @param agentConfig
     * @param def
     * @param version
     */
    private void setCommonAttributes(final NSClientAgentConfig agentConfig, final Definition def) {
        agentConfig.setPort(determinePort(def));
        agentConfig.setRetries(determineRetries(def));
        agentConfig.setTimeout((int)determineTimeout(def));
    }

     /**
     * Helper method to search the nsclient configuration for the appropriate password
     * @param def
     * @return
     */
    private String determinePassword(final Definition def) {
        return (def.getPassword() == null ? (m_config.getPassword() == null ? NSClientAgentConfig.DEFAULT_PASSWORD :m_config.getPassword()) : def.getPassword());
    }

    /**
     * Helper method to search the nsclient configuration for a port
     * @param def
     * @return
     */
    private int determinePort(final Definition def) {
        return (def.getPort() == 0 ? (m_config.getPort() == 0 ? 161 : m_config.getPort()) : def.getPort());
    }

    /**
     * Helper method to search the nsclient configuration 
     * @param def
     * @return
     */
    private long determineTimeout(final Definition def) {
        return (def.getTimeout() == 0 ? (m_config.getTimeout() == 0 ? (long) NSClientAgentConfig.DEFAULT_TIMEOUT : m_config.getTimeout()) : def.getTimeout());
    }

    private int determineRetries(final Definition def) {        
        return (def.getRetry() == 0 ? (m_config.getRetry() == 0 ? NSClientAgentConfig.DEFAULT_RETRIES : m_config.getRetry()) : def.getRetry());
    }

}
