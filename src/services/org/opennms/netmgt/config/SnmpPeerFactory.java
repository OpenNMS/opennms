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
import java.io.Reader;
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
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.protocols.ip.IPv4Address;

/**
 * This class is the main respository for SNMP configuration information used by
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
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
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

    private static final int VERSION_UNSPECIFIED = -1;

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
    
    public SnmpPeerFactory(Reader rdr) throws IOException, MarshalException, ValidationException {
        m_config = (SnmpConfig) Unmarshaller.unmarshal(SnmpConfig.class, rdr);
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
                String specific = ((String) specificsIterator.next()).trim();
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
            definition.setSpecificCollection(new ArrayList(specificsMap.values()));
            definition.setRangeCollection(new ArrayList(rangesMap.values()));
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
    
    public static synchronized void setInstance(SnmpPeerFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
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
    
    public synchronized SnmpAgentConfig getAgentConfig(InetAddress agentAddress) {
        return getAgentConfig(agentAddress, VERSION_UNSPECIFIED);
    }
    
    public synchronized SnmpAgentConfig getAgentConfig(InetAddress agentInetAddress, int requestedSnmpVersion) {

        if (m_config == null) {
            SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
            if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
                agentConfig.setVersion(SnmpAgentConfig.DEFAULT_VERSION);
            } else {
                agentConfig.setVersion(requestedSnmpVersion);
            }
            
            return agentConfig;
        }
        
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(agentInetAddress);
        
        //Now set the defaults from the m_config
        setSnmpAgentConfig(agentConfig, new Definition(), requestedSnmpVersion);

        // Attempt to locate the node
        //
        Enumeration edef = m_config.enumerateDefinition();
        DEFLOOP: while (edef.hasMoreElements()) {
            Definition def = (Definition) edef.nextElement();

            // check the specifics first
            //
            Enumeration espec = def.enumerateSpecific();
            while (espec.hasMoreElements()) {
                String saddr = ((String) espec.nextElement()).trim();
                try {
                    InetAddress addr = InetAddress.getByName(saddr);
                    if (addr.equals(agentConfig.getAddress())) {
                        setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host " + saddr + " to InetAddress", e);
                }
            }

            // check the ranges
            //
            long lhost = toLong(agentConfig.getAddress());
            Enumeration erange = def.enumerateRange();
            while (erange.hasMoreElements()) {
                Range rng = (Range) erange.nextElement();
                try {
                    InetAddress begin = InetAddress.getByName(rng.getBegin());
                    InetAddress end = InetAddress.getByName(rng.getEnd());

                    long start = toLong(begin);
                    long stop = toLong(end);

                    if (start <= lhost && lhost <= stop) {
                        setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host(s) " + rng.getBegin() + " - " + rng.getEnd() + " to InetAddress", e);
                }
            }
            
            // check the matching ip expressions
            //
            Enumeration eMatch = def.enumerateIpMatch();
            while (eMatch.hasMoreElements()) {
                String ipMatch = (String)eMatch.nextElement();
                if (verifyIpMatch(agentInetAddress.getHostAddress(), ipMatch)) {
                    setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
                    break DEFLOOP;
                }
            }
            
        } // end DEFLOOP

        if (agentConfig == null) {

            Definition def = new Definition();
            setSnmpAgentConfig(agentConfig, def, requestedSnmpVersion);
        }

        return agentConfig;

    }
    
    public static boolean verifyIpMatch(String hostAddress, String ipMatch) {
        
        String hostOctets[] = hostAddress.split("\\.", 0);
        String matchOctets[] = ipMatch.split("\\.", 0);
        for (int i = 0; i < 4; i++) {
            if (!matchNumericListOrRange(hostOctets[i], matchOctets[i]))
                return false;
        }
        return true;
    }
    
    /**
    * Use this method to match ranges, lists, and specific number strings
    * such as:
    * "200-300" or "200,300,501-700"
    * "*" matches any
    * This method is commonly used for matching IP octets or ports
    * 
    * @param value
    * @param patterns
    * @return
    */
    public static boolean matchNumericListOrRange(String value, String patterns) {
        
        String patternList[] = patterns.split(",", 0);
        for (int i = 0; i < patternList.length; i++) {
            if (matchRange(value, patternList[i]))
                return true;
        }
        return false;
    }

    /**
    * Helper method in support of matchNumericListOrRange
    * @param value
    * @param pattern
    * @return
    */
    public static boolean matchRange(String value, String pattern) {
        int dashCount = countChar('-', pattern);
        
        if ("*".equals(pattern))
            return true;
        else if (dashCount == 0)
            return value.equals(pattern);
        else if (dashCount > 1)
            return false;
        else if (dashCount == 1) {
            String ar[] = pattern.split("-");
            int rangeBegin = Integer.parseInt(ar[0]);
            int rangeEnd = Integer.parseInt(ar[1]);
            int ip = Integer.parseInt(value);
            return (ip >= rangeBegin && ip <= rangeEnd);
        }
        return false;
    }

    public static int countChar(char charIn, String stingIn) {
        
        int charCount = 0;
        int charIndex = 0;
        for (int i=0; i<stingIn.length(); i++) {
            charIndex = stingIn.indexOf(charIn, i);
            if (charIndex != -1) {
                charCount++;
                i = charIndex +1;
            }
        }
        return charCount;
    }

    private void setSnmpAgentConfig(SnmpAgentConfig agentConfig, Definition def, int requestedSnmpVersion) {
        
        int version = determineVersion(def, requestedSnmpVersion);
        
        setCommonAttributes(agentConfig, def, version);
        agentConfig.setSecurityLevel(determineSecurityLevel(def));
        agentConfig.setSecurityName(determineSecurityName(def));
        agentConfig.setAuthProtocol(determineAuthProtocol(def));
        agentConfig.setAuthPassPhrase(determineAuthPassPhrase(def));
        agentConfig.setPrivProtocol(determinePrivProtocol(def));
        agentConfig.setReadCommunity(determineReadCommunity(def));
        agentConfig.setWriteCommunity(determineWriteCommunity(def));
        
        //TODO: need to work on the Proxy flag.  Probalby should add a proxy host field
        //to the SnmpAgentConfig.
        
    }
    
    /**
     * This is a helper method to set all the common attributes in the agentConfig.
     * 
     * @param agentConfig
     * @param def
     * @param version
     */
    private void setCommonAttributes(SnmpAgentConfig agentConfig, Definition def, int version) {
        agentConfig.setVersion(version);
        agentConfig.setPort(determinePort(def));
        agentConfig.setRetries(determineRetries(def));
        agentConfig.setTimeout((int)determineTimeout(def));
        agentConfig.setMaxRequestSize(determineMaxRequestSize(def));
    }

    /**
     * Helper method to search the snmp-config for the appropriate read
     * community string.
     * @param def
     * @return
     */
    private String determineReadCommunity(Definition def) {
        return (def.getReadCommunity() == null ? (m_config.getReadCommunity() == null ? SnmpAgentConfig.DEFAULT_READ_COMMUNITY :m_config.getReadCommunity()) : def.getReadCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate write
     * community string.
     * @param def
     * @return
     */
    private String determineWriteCommunity(Definition def) {
        return (def.getWriteCommunity() == null ? (m_config.getWriteCommunity() == null ? SnmpAgentConfig.DEFAULT_WRITE_COMMUNITY :m_config.getWriteCommunity()) : def.getWriteCommunity());
    }

    /**
     * Helper method to search the snmp-config for the appropriate maximum
     * request size.  The default is the minimum necessary for a request.
     * @param def
     * @return
     */
    private int determineMaxRequestSize(Definition def) {
        return (def.getMaxRequestSize() == 0 ? (m_config.getMaxRequestSize() == 0 ? SnmpAgentConfig.DEFAULT_MAX_REQUEST_SIZE : m_config.getMaxRequestSize()) : def.getMaxRequestSize());
    }

    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineSecurityName(Definition def) {
        String securityName = (def.getSecurityName() == null ? m_config.getSecurityName() : def.getSecurityName() );
        if (securityName == null) {
            securityName = SnmpAgentConfig.DEFAULT_SECURITY_NAME;
        }
        return securityName;
    }

    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineAuthProtocol(Definition def) {
        String authProtocol = (def.getAuthProtocol() == null ? m_config.getAuthProtocol() : def.getAuthProtocol());
        if (authProtocol == null) {
            authProtocol = SnmpAgentConfig.DEFAULT_AUTH_PROTOCOL;
        }
        return authProtocol;
    }
    
    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determineAuthPassPhrase(Definition def) {
        String authPassPhrase = (def.getAuthPassphrase() == null ? m_config.getAuthPassphrase() : def.getAuthPassphrase());
        if (authPassPhrase == null) {
            authPassPhrase = SnmpAgentConfig.DEFAULT_AUTH_PASS_PHRASE;
        }
        return authPassPhrase;
    }

    /**
     * Helper method to find a security name to use in the snmp-config.  If v3 has
     * been specified and one can't be found, then a default is used for this
     * is a required option for v3 operations.
     * @param def
     * @return
     */
    private String determinePrivProtocol(Definition def) {
        String authPrivProtocol = (def.getPrivacyProtocol() == null ? m_config.getPrivacyProtocol() : def.getPrivacyProtocol());
        if (authPrivProtocol == null) {
            authPrivProtocol = SnmpAgentConfig.DEFAULT_PRIV_PROTOCOL;
        }
        return authPrivProtocol;
    }

    /**
     * Helper method to set the security level in v3 operations.  The default is
     * noAuthNoPriv if there is no authentication passphrase.  From there, if
     * there is a privacy passphrase supplied, then the security level is set to
     * authPriv else it falls out to authNoPriv.  There are only these 3 possible
     * security levels.
     * default 
     * @param def
     * @return
     */
    private int determineSecurityLevel(Definition def) {

        int securityLevel = SnmpAgentConfig.NOAUTH_NOPRIV;

        String authPassPhrase = (def.getAuthPassphrase() == null ? m_config.getAuthPassphrase() : def.getAuthPassphrase());
        String privPassPhrase = (def.getPrivacyPassphrase() == null ? m_config.getPrivacyPassphrase() : def.getPrivacyPassphrase());
        
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
     * @param def
     * @return
     */
    private int determinePort(Definition def) {
        int port = 161;
        return (def.getPort() == 0 ? (m_config.getPort() == 0 ? port : m_config.getPort()) : def.getPort());
    }

    /**
     * Helper method to search the snmp-config 
     * @param def
     * @return
     */
    private long determineTimeout(Definition def) {
        long timeout = SnmpAgentConfig.DEFAULT_TIMEOUT;
        return (long)(def.getTimeout() == 0 ? (m_config.getTimeout() == 0 ? timeout : m_config.getTimeout()) : def.getTimeout());
    }

    private int determineRetries(Definition def) {        
        int retries = SnmpAgentConfig.DEFAULT_RETRIES;
        return (def.getRetry() == 0 ? (m_config.getRetry() == 0 ? retries : m_config.getRetry()) : def.getRetry());
    }

    /**
     * This method determines the configured SNMP version.
     * the order of operations is:
     * 1st: return a valid requested version
     * 2nd: return a valid version defined in a definition within the snmp-config
     * 3rd: return a valid version in the snmp-config
     * 4th: return the default version
     * 
     * @param def
     * @param requestedSnmpVersion
     * @return
     */
    private int determineVersion(Definition def, int requestedSnmpVersion) {
        
        int version = SnmpAgentConfig.VERSION1;
        
        String cfgVersion = "v1";
        if (requestedSnmpVersion == VERSION_UNSPECIFIED) {
            if (def.getVersion() == null) {
                if (m_config.getVersion() == null) {
                    return version;
                } else {
                    cfgVersion = m_config.getVersion();
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

    public static SnmpConfig getSnmpConfig() {
        return m_config;
    }

    public static synchronized void setSnmpConfig(SnmpConfig m_config) {
        SnmpPeerFactory.m_config = m_config;
    }

}
