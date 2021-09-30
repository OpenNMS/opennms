/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.Base64;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SnmpTrapHelper.
 * <p>Inspired by <code>org.opennms.netmgt.scriptd.helper.SnmpTrapHelper</code> from <code>opennms-services</code>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpTrapHelper.class);

    /** The sysUpTimeOID, which should be the first varbind in a V2 trap. */
    private static final String SNMP_SYSUPTIME_OID = ".1.3.6.1.2.1.1.3.0";

    /** The snmpTrapOID, which should be the second varbind in a V2 trap. */
    private static final String SNMP_TRAP_OID = ".1.3.6.1.6.3.1.1.4.1.0";

    /** OID prefix for generic SNMP traps. */
    private static final String SNMP_TRAPS = ".1.3.6.1.6.3.1.1.5";

    /** The SNMP generic value for an enterprise-specific trap. */
    private static final int ENTERPRISE_SPECIFIC = 6;

    /** Map of factories for generating different types of SNMP variable binding content. */
    private Map<String, Object> m_factoryMap;

    /**
     * Constructs a new SNMPTrapHelper.
     */
    public SnmpTrapHelper() {
        // create and populate the factory map
        m_factoryMap = new HashMap<String, Object>();
        m_factoryMap.put(EventConstants.TYPE_SNMP_OCTET_STRING, new SnmpOctetStringFactory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_INT32, new SnmpInt32Factory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_NULL, new SnmpNullFactory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, new SnmpObjectIdFactory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_IPADDRESS, new SnmpIPAddressFactory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_TIMETICKS, new SnmpTimeTicksFactory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_COUNTER32, new SnmpCounter32Factory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_GAUGE32, new SnmpGauge32Factory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_OPAQUE, new SnmpOpaqueFactory());
        m_factoryMap.put(EventConstants.TYPE_SNMP_COUNTER64, new SnmpCounter64Factory());
    }

    /**
     * Common interface for all variable binding factories.
     */
    private interface VarBindFactory {
        /**
         * Adds a varbind.
         *
         * @param trap the trap
         * @param name the name
         * @param encoding the encoding
         * @param value the value
         * @throws SnmpTrapException the SNMP trap helper exception
         */
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException;
    }

    /**
     * Variable binding factory for SnmpOctetString.
     */
    private static class SnmpOctetStringFactory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            byte[] contents;
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                contents = value.getBytes();
            } else if (EventConstants.XML_ENCODING_BASE64.equals(encoding)) {
                contents = Base64.decodeBase64(value.toCharArray());
            } else if (EventConstants.XML_ENCODING_MAC_ADDRESS.equals(encoding)) {
                contents = InetAddressUtils.macAddressStringToBytes(value);
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpOctetString");
            }
            trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getOctetString(contents));
        }
    }

    /**
     * Variable binding factory for SnmpInt32.
     */
    private static class SnmpInt32Factory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getInt32(Integer.parseInt(value)));
                }
                catch (NumberFormatException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpInt32");
                }
                catch (NullPointerException e) {
                    throw new SnmpTrapException("Value is null for SnmpInt32");
                }
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpInt32");
            }
        }
    }

    /**
     * Variable binding factory for SnmpNull.
     */
    private static class SnmpNullFactory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) {
            trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getNull());
        }
    }

    /**
     * Variable binding factory for SnmpObjectId.
     */
    private static class SnmpObjectIdFactory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(value)));
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpObjectId");
            }
        }
    }

    /**
     * Variable binding factory for SnmpIPAddress.
     */
    private static class SnmpIPAddressFactory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                final InetAddress addr = InetAddressUtils.addr(value);
                if (addr == null) {
                    throw new SnmpTrapException("Value " + value + "is invalid, or host unknown for SnmpIPAddress");
                }
                trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getIpAddress(addr));
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpIPAddress");
            }
        }
    }

    /**
     * Variable binding factory for SnmpTimeTicks.
     */
    private static class SnmpTimeTicksFactory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getTimeTicks(Long.parseLong(value)));
                }
                catch (NumberFormatException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpTimeTicks");
                }
                catch (IllegalArgumentException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpTimeTicks");
                }
                catch (NullPointerException e) {
                    throw new SnmpTrapException("Value is null for SnmpTimeTicks");
                }
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpTimeTicks");
            }
        }
    }

    /**
     * Variable binding factory for SnmpCounter32.
     */
    private static class SnmpCounter32Factory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getCounter32(Long.parseLong(value)));
                }
                catch (NumberFormatException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpCounter32");
                }
                catch (IllegalArgumentException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpCounter32");
                }
                catch (NullPointerException e) {
                    throw new SnmpTrapException("Value is null for SnmpCounter32");
                }
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpCounter32");
            }
        }
    }

    /**
     * Variable binding factory for SnmpGauge32.
     */
    private static class SnmpGauge32Factory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getGauge32(Long.parseLong(value)));
                }
                catch (NumberFormatException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpGauge32");
                }
                catch (IllegalArgumentException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpGauge32");
                }
                catch (NullPointerException e) {
                    throw new SnmpTrapException("Value is null for SnmpGauge32");
                }
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpGauge32");
            }
        }
    }

    /**
     * Variable binding factory for SnmpOpaque.
     */
    private static class SnmpOpaqueFactory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_BASE64.equals(encoding)) {
                trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getOpaque(Base64.decodeBase64(value.toCharArray())));
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpOpaque");
            }
        }
    }

    /**
     * Variable binding factory for SnmpCounter64.
     */
    private static class SnmpCounter64Factory implements VarBindFactory {
        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapHelper.VarBindFactory#addVarBind(org.opennms.netmgt.snmp.SnmpTrapBuilder, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapException {
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getCounter64(new BigInteger(value)));
                }
                catch (IllegalArgumentException e) {
                    throw new SnmpTrapException("Value " + value + "is invalid for SnmpCounter64");
                }
                catch (NullPointerException e) {
                    throw new SnmpTrapException("Value is null for SnmpCounter64");
                }
            } else {
                throw new SnmpTrapException("Encoding " + encoding + "is invalid for SnmpCounter64");
            }
        }
    }

    /**
     * Create a new variable binding and add it to the specified SNMP V1 trap.
     * The value encoding is assumed to be XML_ENCODING_TEXT.
     *
     * @param trap The trap to which the variable binding should be added.
     * @param name The name (a.k.a. "id") of the variable binding to be created
     * @param type The type of variable binding to be created
     * @param value The variable binding value
     * @throws SnmpTrapException if the variable binding cannot be added to the trap for any reason.
     */
    private void addVarBinding(SnmpTrapBuilder trap, String name, String type, String value) throws SnmpTrapException {
        addVarBinding(trap, name, type, EventConstants.XML_ENCODING_TEXT, value);
    }

    /**
     * Create a new variable binding and add it to the specified SNMP V1 trap.
     *
     * @param trap The trap to which the variable binding should be added.
     * @param name The name (a.k.a. "id") of the variable binding to be created
     * @param type The type of variable binding to be created
     * @param encoding Describes the way in which the value content has been encoded
     * @param value The variable binding value
     * @throws SnmpTrapException if the variable binding cannot be added to the trap for any reason.
     */
    private void addVarBinding(SnmpTrapBuilder trap, String name, String type, String encoding, String value) throws SnmpTrapException {
        if (name == null) {
            throw new SnmpTrapException("Name is null");
        }
        VarBindFactory factory = (VarBindFactory) m_factoryMap.get(type);
        if (factory == null) {
            throw new SnmpTrapException("Type " + type + " is invalid or not implemented");
        }
        factory.addVarBind(trap, name, encoding, value);
    }

    /**
     * Adds the parameters.
     *
     * @param builder the trap builder object
     * @param trapConfig the trap configuration object
     * @throws SnmpTrapException the SNMP trap exception
     */
    private void addParameters(SnmpTrapBuilder builder, SnmpTrapConfig trapConfig) throws SnmpTrapException {
        int i = 0;
        for (Parm parm : trapConfig.getParameters()) {
            try {
                Value value = parm.getValue();
                addVarBinding(builder, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
            } catch (SnmpTrapException e) {
                throw new SnmpTrapException(e.getMessage() + " in event parm[" + i + "]");
            } finally {
                i++;
            }
        }
    }

    /**
     * Create an SNMP V1 trap based on the content of the specified trap configuration, and send it to the appropriate destination.
     * 
     * @param trapConfig The trap configuration mapping object
     * @throws SnmpTrapException if any.
     */
    private void forwardV1Trap(SnmpTrapConfig trapConfig) throws SnmpTrapException {
        SnmpV1TrapBuilder trap = SnmpUtils.getV1TrapBuilder();
        trap.setEnterprise(SnmpObjId.get(trapConfig.getEnterpriseId()));
        trap.setAgentAddress(trapConfig.getHostAddress());
        if (trapConfig.hasGeneric()) {
            trap.setGeneric(trapConfig.getGeneric());
        }
        if (trapConfig.hasSpecific()) {
            trap.setSpecific(trapConfig.getSpecific());
        }
        trap.setTimeStamp(System.currentTimeMillis() / 1000);
        addParameters(trap, trapConfig);
        try {
            SnmpAgentConfig config = getAgentConfig(trapConfig);
            trap.send(config.getAddress().getHostAddress(), config.getPort(), config.getReadCommunity());
        } catch (Throwable e) {
            throw new SnmpTrapException("Failed to send trap "+e.getMessage(), e);
        }
    }

    /**
     * Populates a trap builder for v2 or v3.
     *
     * @param builder the trap builder object
     * @param trapConfig the trap configuration object
     * @throws SnmpTrapException the SNMP trap exception
     */
    private void populateTrapBuilder(SnmpTrapBuilder builder, SnmpTrapConfig trapConfig) throws SnmpTrapException {
        addVarBinding(builder, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, Long.toString(System.currentTimeMillis()/1000));
        String oid;
        if (trapConfig.getGeneric() == ENTERPRISE_SPECIFIC) {
            oid = trapConfig.getEnterpriseId() + "." + trapConfig.getSpecific();
        } else {
            oid = SNMP_TRAPS + '.' + (trapConfig.getGeneric() + 1);
        }
        addVarBinding(builder, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, oid);
        addParameters(builder, trapConfig);
    }

    /**
     * Create an SNMP V2 trap based on the content of the specified trap configuration, and send it to the appropriate destination.
     *
     * @param trapConfig The trap configuration mapping object
     * @throws SnmpTrapException if any.
     */
    private void forwardV2Trap(SnmpTrapConfig trapConfig) throws SnmpTrapException {
        SnmpTrapBuilder trap = SnmpUtils.getV2TrapBuilder();
        populateTrapBuilder(trap, trapConfig);
        try {
            SnmpAgentConfig config = getAgentConfig(trapConfig);
            trap.send(config.getAddress().getHostAddress(), config.getPort(), config.getReadCommunity());
        } catch (Throwable e) {
            throw new SnmpTrapException("Failed to send trap "+e.getMessage(), e);
        }
    }

    /**
     * Create an SNMP V2 inform based on the content of the specified trap configuration, and send it to the appropriate destination.
     *
     * @param trapConfig The trap configuration mapping object
     * @throws SnmpTrapException if any.
     */
    private void forwardV2Inform(SnmpTrapConfig trapConfig) throws SnmpTrapException {
        SnmpV2TrapBuilder trap = SnmpUtils.getV2InformBuilder();
        populateTrapBuilder(trap, trapConfig);
        try {
            SnmpAgentConfig config = getAgentConfig(trapConfig);
            trap.sendInform(config.getAddress().getHostName(), config.getPort(), config.getTimeout(), config.getRetries(), config.getReadCommunity());
        } catch (Throwable e) {
            throw new SnmpTrapException("Failed to send trap "+e.getMessage(), e);
        }
    }

    /**
     * Create an SNMP V3 trap based on the content of the specified trap configuration, and send it to the appropriate destination.
     *
     * @param trapConfig The trap configuration mapping object
     * @throws SnmpTrapException if any.
     */
    private void forwardV3Trap(SnmpTrapConfig trapConfig) throws SnmpTrapException {
        SnmpV3TrapBuilder trap = SnmpUtils.getV3TrapBuilder();
        populateTrapBuilder(trap, trapConfig);
        try {
            SnmpAgentConfig config = getAgentConfig(trapConfig);
            trap.send(config.getAddress().getHostAddress(), config.getPort(), config.getSecurityLevel(), config.getSecurityName(), config.getAuthPassPhrase(), config.getAuthProtocol(), config.getPrivPassPhrase(), config.getPrivProtocol());
        } catch (Throwable e) {
            throw new SnmpTrapException("Failed to send trap "+e.getMessage(), e);
        }
    }

    /**
     * Create an SNMP V3 inform based on the content of the specified trap configuration, and send it to the appropriate destination.
     *
     * @param trapConfig The trap configuration mapping object
     * @throws SnmpTrapException if any.
     */
    private void forwardV3Inform(SnmpTrapConfig trapConfig) throws SnmpTrapException {
        SnmpV3TrapBuilder trap = SnmpUtils.getV3InformBuilder();
        populateTrapBuilder(trap, trapConfig);
        try {
            SnmpAgentConfig config = getAgentConfig(trapConfig);
            trap.sendInform(config.getAddress().getHostAddress(), config.getPort(), config.getTimeout(), config.getTimeout(), config.getSecurityLevel(), config.getSecurityName(), config.getAuthPassPhrase(), config.getAuthProtocol(), config.getPrivPassPhrase(), config.getPrivProtocol());
        } catch (Throwable e) {
            throw new SnmpTrapException("Failed to send trap "+e.getMessage(), e);
        }
    }

    /**
     * Gets the SNMP agent configuration.
     *
     * @param trapConfig The trap configuration mapping object
     * @return the SNMP agent configuration
     * @throws SnmpTrapException if any.
     */
    // TODO Compare the estimated size with a maximum value:
    //      If the estimated is lower than the maximum in X percentage, log a warning.
    //      Otherwise, log an error and throw an exception
    private SnmpAgentConfig getAgentConfig(SnmpTrapConfig trapConfig) throws SnmpTrapException {
        SnmpAgentConfig agentConfig = trapConfig.getAgentConfig();
        if (trapConfig.getVersion().intValue() != agentConfig.getVersion()) {
            throw new SnmpTrapException("SNMP Version mismatch for " + trapConfig);
        }
        int estimatedSize = getEstimatedPacketSize(trapConfig, agentConfig);
        LOG.info("Sending SNMP{} using {}. The estimated packet size is {} bytes", trapConfig.getVersion().stringValue(), trapConfig, estimatedSize);
        return agentConfig;
    }

    /**
     * Gets the estimated packet size.
     *
     * @param trapConfig the trap configuration object
     * @param agentConfig the agent configuration object
     * @return the estimated packet size
     */
    public int getEstimatedPacketSize(SnmpTrapConfig trapConfig, SnmpAgentConfig agentConfig) {
        // Calculating the preamble overhead
        int preamble = 0;
        if (trapConfig.getVersion().isV1()) {
            preamble = 42;
        }
        if (trapConfig.getVersion().isV2()) {
            preamble = 43;
        }
        if (trapConfig.getVersion().isV3()) {
            preamble = 41;
        }
        // Calculating the version overhead
        int version = 3;
        // Calculating the community overhead
        int community = 0;
        if (trapConfig.getVersion().isV1() || trapConfig.getVersion().isV2()) {
            community = agentConfig.getReadCommunity().length() + 2;
        }
        // Calculating the basic packet overhead
        int overhead = 0;
        if (trapConfig.getVersion().isV1()) {
            overhead = trapConfig.getEnterpriseId().length() + 1;
            overhead += trapConfig.getHostAddress().getHostAddress().length() + 2;
            overhead += 13; // GenericType(3) + Specific-Type(4) + Timestamp(6)
        } else {
            overhead = 21; // Basic varbind overhead (2) + sysUpTime Varbind (19)
            overhead += trapConfig.getEnterpriseId().length() + 14; // Trap-OID Varbind 
            if (trapConfig.getVersion().isV3()) {
                switch (agentConfig.getSecurityLevel()) {
                case 1: overhead += 92; break;
                case 2: overhead += 120; break;
                case 3: overhead += 130; break;
                }
                overhead += agentConfig.getSecurityName().length() + 2;
            }
        }
        // Calculating additional varbinds overhead
        int varbinds = 0;
        for (Parm p : trapConfig.getParameters()) {
            varbinds += p.getParmName().length() + p.getValue().getContent().length() + 4;
        }
        // Calculating the estimated packet size;
        return preamble + version + community + overhead + varbinds;
    }

    /**
     * Create an SNMP trap based on the content of the specified trap configuration, and send it to the appropriate destination.
     *
     * @param trapConfig The trap configuration mapping object
     * @throws SnmpTrapException if any.
     */
    public void forwardTrap(SnmpTrapConfig trapConfig) throws SnmpTrapException {
        if (!trapConfig.isValid()) {
            throw new SnmpTrapException("The current configuration is not valid: " + trapConfig);
        }
        SnmpVersion version = trapConfig.getVersion();
        switch (version) {
        case V1: {
            forwardV1Trap(trapConfig); break;
        }
        case V2c: {
            forwardV2Trap(trapConfig); break;
        }
        case V3: {
            forwardV3Trap(trapConfig); break;
        }
        case V2_INFORM: {
            forwardV2Inform(trapConfig); break;
        }
        case V3_INFORM: {
            forwardV3Inform(trapConfig); break;
        }
        default:
            throw new SnmpTrapException("Invalid SNMP version " + version);
        }
    }

}
