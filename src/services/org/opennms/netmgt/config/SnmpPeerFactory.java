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
import org.opennms.netmgt.config.capsd.Definition;
import org.opennms.netmgt.config.capsd.Range;
import org.opennms.netmgt.config.capsd.SnmpConfig;
import org.opennms.protocols.ip.IPv4Address;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

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

    private static final int VERSION_UNSPECIFIED = -1;

    private static final int DEFAULT_MAX_REQUEST_SIZE = 464;

    private static final int DEFAULT_VERSION = SnmpConstants.version1;

    private static final OID DEFAULT_AUTH_PROTOCOL = AuthMD5.ID;

    private static final OID DEFAULT_PRIV_PROTOCOL = PrivDES.ID;

    private USM m_usm;

    private static final String DEFAULT_SECURITY_NAME = "opennmsUser";

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
        addSecurityModels();

    }
    
    public SnmpPeerFactory(Reader rdr) throws IOException, MarshalException, ValidationException {
        m_config = (SnmpConfig) Unmarshaller.unmarshal(SnmpConfig.class, rdr);
        addSecurityModels();
    }

    private void addSecurityModels() {
        
        Definition def = new Definition();
        
        if (versionString2Int(m_config.getVersion()) == SnmpConstants.version3) {
            def.setSecurityName(m_config.getSecurityName());
            def.setAuthPassphrase(m_config.getAuthPassphrase());
            def.setAuthProtocol(m_config.getAuthProtocol());
            def.setPrivacyPassphrase(m_config.getPrivacyPassphrase());
            def.setPrivacyProtocol(m_config.getPrivacyProtocol());
            initSecurityModels(def);
        }
        
        Enumeration edef = m_config.enumerateDefinition();
        while (edef.hasMoreElements()) {
            def = (Definition) edef.nextElement();
            
            if (versionString2Int(def.getVersion()) == SnmpConstants.version3) {
                initSecurityModels(def);
            }
        }
    }

    /**
     * This method will construct the v3 security models in the SNMP4J library.
     * @param def
     */
    private void initSecurityModels(Definition def) {
        OID authProtocol;
        OID privProtocol;
        OctetString authPassphrase;
        OctetString privPassphrase;
        OctetString securityName;
        securityName = null;
        authPassphrase = null;
        authProtocol = null;
        privPassphrase = null;
        privProtocol = null;
        
        if (def.getSecurityName() != null) {
            
            //Work on Authorization
            securityName = createOctetString(def.getSecurityName());
            if (def.getAuthPassphrase() != null) {
                authPassphrase = createOctetString(def.getAuthPassphrase());
                authProtocol = DEFAULT_AUTH_PROTOCOL;
                if (def.getAuthProtocol() != null) {
                    if (def.getAuthProtocol().equals("MD5")) {
                        authProtocol = AuthMD5.ID;
                    } else if (def.getAuthProtocol().equals("SHA")) {
                        authProtocol = AuthSHA.ID;
                    } else {
                        throw new IllegalArgumentException("Authentication protocol unsupported: " + def.getAuthProtocol());
                    }
                }
            }
            
            //Work on Privacy
            if (def.getPrivacyPassphrase() != null) {
                privPassphrase = createOctetString(def.getPrivacyPassphrase());
                privProtocol = DEFAULT_PRIV_PROTOCOL;
                if (def.getPrivacyProtocol() != null) {
                    if (def.getPrivacyProtocol().equals("DES")) {
                        privProtocol = PrivDES.ID;
                    } else if ((def.getPrivacyProtocol().equals("AES128")) || (def.getPrivacyProtocol().equals("AES"))) {
                        privProtocol = PrivAES128.ID;
                    } else if (def.getPrivacyProtocol().equals("AES192")) {
                        privProtocol = PrivAES192.ID;
                    } else if (def.getPrivacyProtocol().equals("AES256")) {
                        privProtocol = PrivAES256.ID;
                    } else {
                        throw new IllegalArgumentException("Privacy protocol " + def.getPrivacyProtocol() + " not supported");
                    }
                }
            }
            
            if (m_usm == null) {
                m_usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
                SecurityModels.getInstance().addSecurityModel(m_usm);
            }
            UsmUser user = new UsmUser(securityName, authProtocol, authPassphrase, privProtocol, privPassphrase);
            m_usm.addUser(securityName, user);
        }
    }
    
    private static OctetString createOctetString(String s) {
        OctetString octetString;
        if (s.startsWith("0x")) {
            octetString = OctetString.fromHexString(s.substring(2), ':');
        } else {
            octetString = new OctetString(s);
        }
        return octetString;
    }


    private int versionString2Int(String strVersion) {

        int version = DEFAULT_VERSION;

        if (strVersion.equals("v3")) {
            version = SnmpConstants.version3;
        } else if (strVersion.equals("v2c")) { 
            version = SnmpConstants.version2c;
        } else if (strVersion.equals("v1")) {
            version = SnmpConstants.version1;
        }
        
        return version;
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
    
    public Target getTarget(InetAddress inetAddress, int requestedSnmpVersion) {
        
        String transportAddress = null;
        //String transportAddress = inetAddress.getHostAddress() + "/" + DEFAULT_PORT;
        Address targetAddress = null;
        //Address targetAddress = new UdpAddress(transportAddress);
        
        if (m_config == null) {
            Target target = null;
            if (requestedSnmpVersion == SnmpConstants.version3) {
                target = new UserTarget();
                target.setVersion(SnmpConstants.version3);
            } else if (requestedSnmpVersion == SnmpConstants.version1){
                target = new CommunityTarget();
                target.setVersion(SnmpConstants.version1);
            } else if (requestedSnmpVersion == SnmpConstants.version2c) {
                target = new CommunityTarget();
                target.setVersion(SnmpConstants.version2c);
            }
            
            return target;
        }

        Target target = null;

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
                    if (addr.equals(inetAddress)) {
                        target = createTarget(addr, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host " + saddr + " to InetAddress", e);
                }
            }

            // check the ranges
            //
            long lhost = toLong(inetAddress);
            Enumeration erange = def.enumerateRange();
            while (erange.hasMoreElements()) {
                Range rng = (Range) erange.nextElement();
                try {
                    InetAddress begin = InetAddress.getByName(rng.getBegin());
                    InetAddress end = InetAddress.getByName(rng.getEnd());

                    long start = toLong(begin);
                    long stop = toLong(end);

                    if (start <= lhost && lhost <= stop) {
                        target = createTarget(inetAddress, def, requestedSnmpVersion);
                        break DEFLOOP;
                    }
                } catch (UnknownHostException e) {
                    Category log = ThreadCategory.getInstance(getClass());
                    log.warn("SnmpPeerFactory: could not convert host(s) " + rng.getBegin() + " - " + rng.getEnd() + " to InetAddress", e);
                }
            }
        } // end DEFLOOP

        if (target == null) {

            Definition def = new Definition();
            target = createTarget(inetAddress, def, requestedSnmpVersion);
        }

        return target;

    }
        
    private Target createTarget(InetAddress addr, Definition def, int requestedSnmpVersion) {
        
        int version = determineVersion(def, requestedSnmpVersion);
        
        if (version == SnmpConstants.version3) {
            UserTarget target = new UserTarget();
            setCommonAttributes(target, def, version, addr);
            target.setSecurityLevel(determineSercurityLevel(def));
            target.setSecurityName(determineSecurityName(def));
            return target;
        } else {
            CommunityTarget target = new CommunityTarget();
            setCommonAttributes(target, def, version, addr);
            target.setCommunity(determineCommunity(def));
            return target;
        }

    }
    
    private void setCommonAttributes(Target target, Definition def, int version, InetAddress addr) {
        target.setVersion(version);
        target.setRetries(determineRetries(def));
        target.setTimeout(determineTimeout(def));
        target.setAddress(determineAddress(def, addr));
        target.setMaxSizeRequestPDU(determineMaxRequestSize(def));
    }

    private OctetString determineCommunity(Definition def) {
        return new OctetString((def.getReadCommunity() == null ? (m_config.getReadCommunity() == null ? "public" :m_config.getReadCommunity()) : def.getReadCommunity()));
    }

    private int determineMaxRequestSize(Definition def) {
        return (def.getMaxRequestSize() == 0 ? (m_config.getMaxRequestSize() == 0 ? DEFAULT_MAX_REQUEST_SIZE : m_config.getMaxRequestSize()) : DEFAULT_MAX_REQUEST_SIZE);
    }

    private OctetString determineSecurityName(Definition def) {
        String securityName = (def.getSecurityName() == null ? m_config.getSecurityName() : def.getSecurityName() );
        if (securityName == null) {
            securityName = DEFAULT_SECURITY_NAME;
        }
        return new OctetString(securityName);
    }

    private int determineSercurityLevel(Definition def) {

        int securityLevel = SecurityLevel.NOAUTH_NOPRIV;

        String authPassPhrase = (def.getAuthPassphrase() == null ? m_config.getAuthPassphrase() : def.getAuthPassphrase());
        String privPassPhrase = (def.getPrivacyPassphrase() == null ? m_config.getPrivacyPassphrase() : def.getPrivacyPassphrase());
        
        if (authPassPhrase == null) {
            securityLevel = SecurityLevel.NOAUTH_NOPRIV;
        } else {
            if (privPassPhrase == null) {
                securityLevel = SecurityLevel.AUTH_NOPRIV;
            } else {
                securityLevel = SecurityLevel.AUTH_PRIV;
            }
        }
        
        return securityLevel;
    }

    private Address determineAddress(Definition def, InetAddress addr) {
        String transportAddress = addr.getHostAddress();
        int port = determinePort(def);
        transportAddress += "/" + port;
        Address targetAddress = new UdpAddress(transportAddress);
        return targetAddress;
    }

    /**
     * Helper method to search the snmp-config for a port
     * @param def
     * @return
     */
    private int determinePort(Definition def) {
        int port = 161;
        return (def.getPort() == 0 ? (m_config.getPort() == 0 ? port : m_config.getPort()) : port);
    }

    /**
     * Helper method to search the snmp-config 
     * @param def
     * @return
     */
    private long determineTimeout(Definition def) {
        long timeout = 3;
        return (long)(def.getTimeout() == 0 ? (m_config.getTimeout() == 0 ? timeout : m_config.getTimeout()) : timeout);
    }

    private int determineRetries(Definition def) {        
        int retries = 3;
        return (def.getRetry() == 0 ? (m_config.getRetry() == 0 ? retries : m_config.getRetry()) : retries);
    }

    /**
     * This method determines the appropriate value for an SNMP4J target.  If
     * the order of operations is:
     * 1st: return a valid requested version
     * 2nd: return a valid version defined in a definition within the snmp-config
     * 3rd: return a valid version in the snmp-config
     * 4th: return the default version
     * @param def
     * @param requestedSnmpVersion
     * @return
     */
    private int determineVersion(Definition def, int requestedSnmpVersion) {
        
        int version = SnmpConstants.version1;
        
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
            version = SnmpConstants.version1;
        } else if (cfgVersion.equals("v2c")) {
            version = SnmpConstants.version2c;
        } else if (cfgVersion.equals("v3")) {
            version = SnmpConstants.version3;
        }
        
        return version;
    }

    /**
     * Helper method.  Calls overloaded partner with the version unspecified
     * value.  In effect this sets the target to the default version if one isn't
     * found in the snmp-config or in a definition within the snmp-config.
     * @param inetAddress
     * @return
     */
    public Target getTarget(InetAddress inetAddress) {
        return getTarget(inetAddress, VERSION_UNSPECIFIED);
    }
        
}
