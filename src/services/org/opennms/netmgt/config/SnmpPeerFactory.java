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
//
// 2005 Mar 08: Added saveCurrent, optimize, and define methods.
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.capsd.Definition;
import org.opennms.netmgt.config.capsd.Range;
import org.opennms.netmgt.config.capsd.SnmpConfig;
import org.opennms.protocols.ip.IPv4Address;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;

/**
 * This class is the main respository for SNMP configuration information used by
 * the capabilities daemon. When this class is loaded it reads the snmp
 * configuration into memory, and uses the configuration to find the
 * {@link org.opennms.protocols.snmp.SnmpPeer SnmpPeer}objects for specific
 * addresses. If an address cannot be located in the configuration then a
 * default peer instance is returned to the caller.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="mailto:gturner@newedgenetworks.com">Gerald Turner </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
public final class SnmpPeerFactory {
    /**
     * The singleton instance of this factory
     */
    private static SnmpPeerFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private static SnmpConfig m_config;

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
    private SnmpPeerFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream cfgIn = new FileInputStream(configFile);

        m_config = (SnmpConfig) Unmarshaller.unmarshal(SnmpConfig.class, new InputStreamReader(cfgIn));
        cfgIn.close();

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
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME);

        ThreadCategory.getInstance(SnmpPeerFactory.class).debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new SnmpPeerFactory(cfgFile.getPath());

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
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Saves the current settings to disk
     */
    public static synchronized void saveCurrent() throws Exception {
        optimize();

        // Marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);
        if (stringWriter.toString() != null) {
            FileWriter fileWriter = new FileWriter(ConfigFileConstants.getFile(ConfigFileConstants.SNMP_CONF_FILE_NAME));
            fileWriter.write(stringWriter.toString());
            fileWriter.flush();
            fileWriter.close();
        }

        reload();
    }

    /**
     * Combine specific and range elements so that SnmpPeerFactory has to spend
     * less time iterating all these elements.
     */
    private static void optimize() throws UnknownHostException {
        Category log = ThreadCategory.getInstance(SnmpPeerFactory.class);

        // First pass: Remove empty definition elements
        for (Iterator definitionsIterator =
                 m_config.getDefinitionCollection().iterator();
             definitionsIterator.hasNext();) {
            Definition definition =
                (Definition) definitionsIterator.next();
            if (definition.getSpecificCount() == 0
                && definition.getRangeCount() == 0) {
                if (log.isDebugEnabled())
                    log.debug("optimize: Removing empty definition element");
                definitionsIterator.remove();
            }
        }

        // Second pass: Replace single IP range elements with specific elements
        for (Iterator definitionsIterator =
                 m_config.getDefinitionCollection().iterator();
             definitionsIterator.hasNext();) {
            Definition definition =
                (Definition) definitionsIterator.next();
            for (Iterator rangesIterator =
                     definition.getRangeCollection().iterator();
                 rangesIterator.hasNext();) {
                Range range = (Range) rangesIterator.next();
                if (range.getBegin().equals(range.getEnd())) {
                    definition.addSpecific(range.getBegin());
                    rangesIterator.remove();
                }
            }
        }

        // Third pass: Sort specific and range elements for improved XML
        // readability and then combine them into fewer elements where possible
        for (Iterator definitionsIterator =
                 m_config.getDefinitionCollection().iterator();
             definitionsIterator.hasNext();) {
            Definition definition =
                (Definition) definitionsIterator.next();

            // Sort specifics
            TreeMap specificsMap = new TreeMap();
            for (Iterator specificsIterator =
                     definition.getSpecificCollection().iterator();
                 specificsIterator.hasNext();) {
                String specific = (String) specificsIterator.next();
                specificsMap.put(new Integer(new IPv4Address(specific).getAddress()),
                                 specific);
            }

            // Sort ranges
            TreeMap rangesMap = new TreeMap();
            for (Iterator rangesIterator =
                     definition.getRangeCollection().iterator();
                 rangesIterator.hasNext();) {
                Range range = (Range) rangesIterator.next();
                rangesMap.put(new Integer(new IPv4Address(range.getBegin()).getAddress()),
                              range);
            }

            // Combine consecutive specifics into ranges
            Integer priorSpecific = null;
            Range addedRange = null;
            for (Iterator specificsIterator =
                     new ArrayList(specificsMap.keySet()).iterator();
                 specificsIterator.hasNext();) {
                Integer specific = (Integer) specificsIterator.next();

                if (priorSpecific == null) {
                    priorSpecific = specific;
                    continue;
                }

                int specificInt = specific.intValue();
                int priorSpecificInt = priorSpecific.intValue();

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
            for (Iterator specificsIterator =
                     new ArrayList(specificsMap.keySet()).iterator();
                 specificsIterator.hasNext();) {
                Integer specific = (Integer) specificsIterator.next();
                int specificInt = specific.intValue();
                for (Iterator rangesIterator =
                         new ArrayList(rangesMap.keySet()).iterator();
                     rangesIterator.hasNext();) {
                    Integer begin = (Integer) rangesIterator.next();
                    int beginInt = begin.intValue();

                    if (specificInt < beginInt - 1)
                        continue;

                    Range range = (Range) rangesMap.get(begin);

                    int endInt = new IPv4Address(range.getEnd()).getAddress();

                    if (specificInt > endInt + 1)
                        continue;

                    if (specificInt >= beginInt && specificInt <= endInt) {
                        specificsMap.remove(specific);
                        break;
                    }

                    if (specificInt == beginInt - 1) {
                        rangesMap.remove(begin);
                        rangesMap.put(specific, range);
                        range.setBegin(IPv4Address.addressToString(specificInt));
                        specificsMap.remove(specific);
                        break;
                    }

                    if (specificInt == endInt + 1) {
                        range.setEnd(IPv4Address.addressToString(specificInt));
                        specificsMap.remove(specific);
                        break;
                    }
                }
            }

            // Combine consecutive ranges
            Range priorRange = null;
            int priorBegin = 0;
            int priorEnd = 0;
            for (Iterator rangesIterator =
                     rangesMap.keySet().iterator();
                 rangesIterator.hasNext();) {
                Integer rangeKey = (Integer) rangesIterator.next();

                Range range = (Range) rangesMap.get(rangeKey);

                int begin = rangeKey.intValue();
                int end = new IPv4Address(range.getEnd()).getAddress();

                if (priorRange != null) {
                    if (begin - priorEnd <= 1) {
                        priorRange.setBegin(IPv4Address.addressToString
                                             (Math.min(priorBegin, begin)));
                        priorRange.setEnd(IPv4Address.addressToString
                                           (Math.max(priorEnd, end)));

                        rangesIterator.remove();
                        continue;
                    }
                }

                priorRange = range;
                priorBegin = begin;
                priorEnd = end;
            }

            // Update changes made to sorted maps
            definition.setSpecificCollection(specificsMap.values());
            definition.setRangeCollection(rangesMap.values());
        }
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized SnmpPeerFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * Converts the internet address to a long value so that it can be compared
     * using simple opertions. The address is converted in network byte order
     * (big endin) and allows for comparisions like &lt;, &gt;, &lt;=, &gt;=,
     * ==, and !=.
     * 
     * @param addr
     *            The address to convert to a long
     * 
     * @return The address as a long value.
     * 
     */
    private static long toLong(InetAddress addr) {
        byte[] baddr = addr.getAddress();
        long result = ((long) baddr[0] & 0xffL) << 24 | ((long) baddr[1] & 0xffL) << 16 | ((long) baddr[2] & 0xffL) << 8 | ((long) baddr[3] & 0xffL);

        return result;
    }

    /**
     * Puts a specific IP address with associated read-community string into
     * the currently loaded snmp-config.xml.
     */
    public void define(InetAddress ip, String community) throws UnknownHostException {
        Category log = ThreadCategory.getInstance(SnmpPeerFactory.class);

        // Convert IP to long so that it easily compared in range elements
        int address = new IPv4Address(ip).getAddress();

        // Copy the current definitions so that elements can be added and
        // removed
        ArrayList definitions =
            new ArrayList(m_config.getDefinitionCollection());

        // First step: Find the first definition matching the read-community or
        // create a new definition, then add the specific IP
        Definition definition = null;
        for (Iterator definitionsIterator = definitions.iterator();
             definitionsIterator.hasNext();) {
            Definition currentDefinition =
                (Definition) definitionsIterator.next();

            if ((currentDefinition.getReadCommunity() != null
                 && currentDefinition.getReadCommunity().equals(community))
                || (currentDefinition.getReadCommunity() == null
                    && m_config.getReadCommunity() != null
                    && m_config.getReadCommunity().equals(community))) {
                if (log.isDebugEnabled())
                    log.debug("define: Found existing definition "
                              + "with read-community " + community);
                definition = currentDefinition;
                break;
            }
        }
        if (definition == null) {
            if (log.isDebugEnabled())
                log.debug("define: Creating new definition");

            definition = new Definition();
            definition.setReadCommunity(community);
            definitions.add(definition);
        }
        definition.addSpecific(ip.getHostAddress());

        // Second step: Find and remove any existing specific and range
        // elements with matching IP among all definitions except for the
        // definition identified in the first step
        for (Iterator definitionsIterator = definitions.iterator();
             definitionsIterator.hasNext();) {
            Definition currentDefinition =
                (Definition) definitionsIterator.next();

            // Ignore this definition if it was the one identified by the first
            // step
            if (currentDefinition == definition)
                continue;

            // Remove any specific elements that match IP
            while (currentDefinition.removeSpecific(ip.getHostAddress())) {
                if (log.isDebugEnabled())
                    log.debug("define: Removed an existing specific "
                              + "element with IP " + ip);
            }

            // Split and replace any range elements that contain IP
            ArrayList ranges =
                new ArrayList(currentDefinition.getRangeCollection());
            Range[] rangesArray = currentDefinition.getRange();
            for (int rangesArrayIndex = 0;
                 rangesArrayIndex < rangesArray.length;
                 rangesArrayIndex++) {
                Range range = rangesArray[rangesArrayIndex];
                int begin = new IPv4Address(range.getBegin()).getAddress();
                int end = new IPv4Address(range.getEnd()).getAddress();
                if (address >= begin && address <= end) {
                    if (log.isDebugEnabled())
                        log.debug("define: Splitting range element "
                                  + "with begin " + range.getBegin() + " and "
                                  + "end " + range.getEnd());

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

                    Range head = new Range();
                    head.setBegin(range.getBegin());
                    head.setEnd(IPv4Address.addressToString(address - 1));

                    Range tail = new Range();
                    tail.setBegin(IPv4Address.addressToString(address + 1));
                    tail.setEnd(range.getEnd());

                    ranges.remove(range);
                    ranges.add(head);
                    ranges.add(tail);
                }
            }
            currentDefinition.setRangeCollection(ranges);
        }

        // Store the altered list of definitions
        m_config.setDefinitionCollection(definitions);
    }

    /**
     * this method uses the passed address and definition to construct an
     * appropriate SNMP peer object for use by an SnmpSession.
     * 
     * @param addr
     *            The address to construct the snmp peer instance.
     * @param def
     *            The definition containing the appropriate information.
     * 
     * @return The SnmpPeer matching for the passed address.
     */
    private SnmpPeer create(InetAddress addr, Definition def) {
        return create(addr, def, -1);
    }

    /**
     * this method uses the passed address and definition to construct an
     * appropriate SNMP peer object for use by an SnmpSession.
     * 
     * @param addr
     *            The address to construct the snmp peer instance.
     * @param def
     *            The definition containing the appropriate information.
     * @param supportedSnmpVersion
     *            SNMP version to associate with the peer object if SNMP version
     *            has not been explicitly configured.
     * 
     * @return The SnmpPeer matching for the passed address.
     */
    private SnmpPeer create(InetAddress addr, Definition def, int supportedSnmpVersion) {
        // Allocate a new SNMP parameters
        //
        InetAddress snmpHost = addr;
        SnmpParameters parms = new SnmpParameters();

        // get the version information, if any
        //
        // If version information is provided it will be used...
        // if not then the passed supportedSnmpVersion variable
        // will be used to set the peer's SNMP version.
        //
        if (def.getVersion() != null) {
            if (def.getVersion().equals("v1"))
                parms.setVersion(SnmpSMI.SNMPV1);
            else if (def.getVersion().equals("v2c"))
                parms.setVersion(SnmpSMI.SNMPV2);
        } else {
            // Verify valid SNMP version provided
            if (supportedSnmpVersion == SnmpSMI.SNMPV1 || supportedSnmpVersion == SnmpSMI.SNMPV2)
                parms.setVersion(supportedSnmpVersion);
            else
                parms.setVersion(SnmpSMI.SNMPV1);
        }

        // setup the read community
        //
        if (def.getReadCommunity() != null) {
            parms.setReadCommunity(def.getReadCommunity());
        } else if (m_config.getReadCommunity() != null) {
            parms.setReadCommunity(m_config.getReadCommunity());
        }

        // setup the write community
        //
        if (def.getWriteCommunity() != null) {
            parms.setWriteCommunity(def.getWriteCommunity());
        } else if (m_config.getWriteCommunity() != null) {
            parms.setWriteCommunity(m_config.getWriteCommunity());
        }

        if (def.getProxyHost() != null) {
            try {
                snmpHost = InetAddress.getByName(def.getProxyHost());
            }
            catch (UnknownHostException e) {
                Category log = ThreadCategory.getInstance(getClass());
                log.error("SnmpPeerFactory: could not convert host " + def.getProxyHost() + " to InetAddress", e);
            }
        }
        
        // Allocate a peer for this address
        // and set the parameters
        //
        SnmpPeer peer = new SnmpPeer(snmpHost);
        peer.setParameters(parms);

        // setup the retries
        //
        if (def.hasRetry()) {
            peer.setRetries(def.getRetry());
        } else if (m_config.hasRetry()) {
            peer.setRetries(m_config.getRetry());
        }

        // setup the timeout
        //
        if (def.hasTimeout()) {
            peer.setTimeout(def.getTimeout());
        } else if (m_config.hasTimeout()) {
            peer.setTimeout(m_config.getTimeout());
        }

        // check for port changes
        //
        if (def.hasPort())
            peer.setPort(def.getPort());

        // return the peer
        //
        return peer;
    }

    /**
     * This method is used by the Capabilities poller to lookup the SNMP peer
     * information associated with the passed host. If there is no specific
     * information available then a default SnmpPeer instance is returned to the
     * caller.
     * 
     * @param host
     *            The host for locating the SnmpPeer information.
     * 
     * @return The configured SnmpPeer information.
     * 
     */
    public synchronized SnmpPeer getPeer(InetAddress host) {
        return getPeer(host, -1);
    }

    /**
     * This method is used by the Capabilities poller to lookup the SNMP peer
     * information associated with the passed host. If there is no specific
     * information available then a default SnmpPeer instance is returned to the
     * caller.
     * 
     * @param host
     *            The host for locating the SnmpPeer information.
     * @param supportedSnmpVersion
     *            SNMP version to associate with the peer object if SNMP version
     *            has not been explicitly configured.
     * 
     * @return The configured SnmpPeer information.
     * 
     */
    public synchronized SnmpPeer getPeer(InetAddress host, int supportedSnmpVersion) {
        // Verify configuration information present!
        //
        if (m_config == null) {
            SnmpPeer peer = new SnmpPeer(host);

            // Verify valid SNMP version provided
            if (supportedSnmpVersion == SnmpSMI.SNMPV1 || supportedSnmpVersion == SnmpSMI.SNMPV2) {
                peer.getParameters().setVersion(supportedSnmpVersion);
            }

            return peer;
        }

        SnmpPeer peer = null;

        // Attempt to locate the node
        //
        Enumeration edef = m_config.enumerateDefinition();
        DEFLOOP: while (edef.hasMoreElements()) {
            Definition def = (Definition) edef.nextElement();

            // check the specifics first
            //
            Enumeration espec = def.enumerateSpecific();
            while (espec.hasMoreElements()) {
                String saddr = (String) espec.nextElement();
                try {
                    InetAddress addr = InetAddress.getByName(saddr);
                    if (addr.equals(host)) {
                        // get the information
                        peer = create(addr, def, supportedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host " + saddr + " to InetAddress", e);
                }
            }

            // check the ranges
            //
            long lhost = toLong(host);
            Enumeration erange = def.enumerateRange();
            while (erange.hasMoreElements()) {
                Range rng = (Range) erange.nextElement();
                try {
                    InetAddress begin = InetAddress.getByName(rng.getBegin());
                    InetAddress end = InetAddress.getByName(rng.getEnd());

                    long start = toLong(begin);
                    long stop = toLong(end);

                    if (start <= lhost && lhost <= stop) {
                        peer = create(host, def, supportedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host(s) " + rng.getBegin() + " - " + rng.getEnd() + " to InetAddress", e);
                }
            }
        } // end DEFLOOP

        if (peer == null) {
            // try defaults!
            //
            peer = new SnmpPeer(host);

            // Verify valid SNMP version provided
            if (supportedSnmpVersion == SnmpSMI.SNMPV1 || supportedSnmpVersion == SnmpSMI.SNMPV2) {
                peer.getParameters().setVersion(supportedSnmpVersion);
            }

            if (m_config.getReadCommunity() != null || m_config.getWriteCommunity() != null) {
                if (m_config.getReadCommunity() != null)
                    peer.getParameters().setReadCommunity(m_config.getReadCommunity());

                if (m_config.getWriteCommunity() != null)
                    peer.getParameters().setWriteCommunity(m_config.getWriteCommunity());
            }

            if (m_config.hasRetry())
                peer.setRetries(m_config.getRetry());

            if (m_config.hasTimeout())
                peer.setTimeout(m_config.getTimeout());
        }

        return peer;

    } // end getPeer();
}
