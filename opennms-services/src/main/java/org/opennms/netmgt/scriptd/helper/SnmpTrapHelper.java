/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.scriptd.helper;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.opennms.core.utils.Base64;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.datablock.EventUtil;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;

/**
 * This "helper" class provides a convenience interface for generating and
 * forwarding SNMP traps. This class was created in order to make it easier to
 * write simple scripts to generate traps based on events or to forward traps,
 * using scripting languages that are able to access Java classes (such as
 * BeanShell).
 *
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
public class SnmpTrapHelper {

    /**
     * The sysUpTimeOID, which should be the first varbind in a V2 trap
     */
    private static final String SNMP_SYSUPTIME_OID = ".1.3.6.1.2.1.1.3.0";

    /**
     * The snmpTrapOID, which should be the second varbind in a V2 trap
     */
    private static final String SNMP_TRAP_OID = ".1.3.6.1.6.3.1.1.4.1.0";

    /**
     * The snmpTrapAddress, which may occur in a V2 trap
     */
    private static final String SNMP_TRAP_ADDRESS_OID = ".1.3.6.1.6.3.18.1.3.0";

    /**
     * The snmpTrapCommunity, which may occur in a V2 trap
     */
    private static final String SNMP_TRAP_COMMUNITY_OID = ".1.3.6.1.6.3.18.1.4.0";

    /**
     * The SNMP trap enterprise OID, which if present in a V2 trap is the last
     * varbind
     */
    private static final String SNMP_TRAP_ENTERPRISE_OID = ".1.3.6.1.6.3.1.1.4.3.0";

    /**
     * OID prefix for generic SNMP traps
     */
    private static final String SNMP_TRAPS = ".1.3.6.1.6.3.1.1.5";

    /**
     * The SNMP generic value for an enterprise-specific trap
     */
    private static final int ENTERPRISE_SPECIFIC = 6;

    /**
     * Map of factories for generating different types of SNMP variable binding
     * content
     */
    private HashMap<String, Object> m_factoryMap;

    /**
     * Constructs a new SNMPTrapHelper.
     */
    public SnmpTrapHelper() {

        // create the trap session

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
     * Stops the SnmpTrapHelper. If there is a valid SnmpTrapSession, that trap
     * session is stopped.
     */
    public void stop() {
    }

    /**
     * Common interface for all variabe binding factories
     */
    private interface VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value is assumed to have been encoded with the specified encoding
         * (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64).
         * @param trap TODO
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException;
    }

    /**
     * Variable binding factory for SnmpOctetString
     */
    private class SnmpOctetStringFactory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpOctetString. The value is assumed to
         * have been encoded with the specified encoding (i.e.
         * XML_ENCODING_TEXT, XML_ENCODING_BASE64, or XML_ENCODING_MAC_ADDRESS).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, XML_ENCODING_BASE64, or XML_ENCODING_MAC_ADDRESS)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            byte[] contents;
            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                contents = value.getBytes();
            } else if (EventConstants.XML_ENCODING_BASE64.equals(encoding)) {
                contents = Base64.decodeBase64(value.toCharArray());
            } else if (EventConstants.XML_ENCODING_MAC_ADDRESS.equals(encoding)) {
                contents = InetAddressUtils.macAddressStringToBytes(value);
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpOctetString");
            }
            trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getOctetString(contents));
        }
    }

    /**
     * Variable binding factory for SnmpInt32
     */
    private class SnmpInt32Factory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpInt32. The value is assumed to have
         * been encoded with the specified encoding (only XML_ENCODING_TEXT is
         * supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getInt32(Integer.parseInt(value)));
                }

                catch (NumberFormatException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpInt32");
                }

                catch (NullPointerException e) {
                    throw new SnmpTrapHelperException("Value is null for SnmpInt32");
                }
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpInt32");
            }
        }
    }

    /**
     * Variable binding factory for SnmpNull
     */
    private class SnmpNullFactory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpNull.The value and encoding
         * parameters are ignored.
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            This parameter value is ignored.
         * @param value
         *            This parameter value is ignored.
         * 
         * @return The newly-created variable binding
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) {
            trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getNull());
        }
    }

    /**
     * Variable binding factory for SnmpObjectId
     */
    private class SnmpObjectIdFactory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpObjectId. The value is assumed to
         * have been encoded with the specified encoding (only XML_ENCODING_TEXT
         * is supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(value)));
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpObjectId");
            }
        }
    }

    /**
     * Variable binding factory for SnmpIPAddress
     */
    private class SnmpIPAddressFactory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpIPAddress. The value is assumed to
         * have been encoded with the specified encoding (only XML_ENCODING_TEXT
         * is supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                final InetAddress addr = InetAddressUtils.addr(value);
                if (addr == null) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid, or host unknown for SnmpIPAddress");
                }
				trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getIpAddress(addr));
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpIPAddress");
            }
        }
    }

    /**
     * Variable binding factory for SnmpTimeTicks
     */
    private class SnmpTimeTicksFactory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpTimeTicks. The value is assumed to
         * have been encoded with the specified encoding (only XML_ENCODING_TEXT
         * is supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getTimeTicks(Long.parseLong(value)));
                }
                
                catch (NumberFormatException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpTimeTicks");
                }

                catch (IllegalArgumentException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpTimeTicks");
                }

                catch (NullPointerException e) {
                    throw new SnmpTrapHelperException("Value is null for SnmpTimeTicks");
                }
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpTimeTicks");
            }
        }
    }

    /**
     * Variable binding factory for SnmpCounter32
     */
    private class SnmpCounter32Factory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpCounter32. The value is assumed to
         * have been encoded with the specified encoding (only XML_ENCODING_TEXT
         * is supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getCounter32(Long.parseLong(value)));
                }
                
                catch (NumberFormatException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpCounter32");
                }

                catch (IllegalArgumentException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpCounter32");
                }

                catch (NullPointerException e) {
                    throw new SnmpTrapHelperException("Value is null for SnmpCounter32");
                }
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpCounter32");
            }
        }
    }

    /**
     * Variable binding factory for SnmpGauge32
     */
    private class SnmpGauge32Factory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpGauge32. The value is assumed to have
         * been encoded with the specified encoding (only XML_ENCODING_TEXT is
         * supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getGauge32(Long.parseLong(value)));
                }
                
                catch (NumberFormatException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpGauge32");
                }

                catch (IllegalArgumentException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpGauge32");
                }

                catch (NullPointerException e) {
                    throw new SnmpTrapHelperException("Value is null for SnmpGauge32");
                }
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpGauge32");
            }
        }
    }

    /**
     * Variable binding factory for SnmpOpaque
     */
    private class SnmpOpaqueFactory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpOpaque. The value is assumed to have
         * been encoded with the specified encoding (only XML_ENCODING_BASE64 is
         * supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_BASE64.equals(encoding)) {
                trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getOpaque(Base64.decodeBase64(value.toCharArray())));
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpOpaque");
            }
        }
    }

    /**
     * Variable binding factory for SnmpCounter64
     */
    private class SnmpCounter64Factory implements VarBindFactory {
        /**
         * Constructs a new SnmpVarBind with the specified name and value. The
         * value will be encoded as an SnmpCounter64. The value is assumed to
         * have been encoded with the specified encoding (only XML_ENCODING_TEXT
         * is supported).
         * @param name
         *            The name (a.k.a. "id") of the variable binding to be
         *            created
         * @param encoding
         *            Describes the way in which the value content has been
         *            encoded (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
         * @param value
         *            The variable binding value
         * 
         * @return The newly-created variable binding
         * @exception Throws
         *                SnmpTrapHelperException if the variable binding cannot
         *                be created for any reason (e.g. encoding not
         *                supported, invalid value, etc.).
         */
        @Override
        public void addVarBind(SnmpTrapBuilder trap, String name, String encoding, String value) throws SnmpTrapHelperException {

            if (EventConstants.XML_ENCODING_TEXT.equals(encoding)) {
                try {
                    trap.addVarBind(SnmpObjId.get(name), SnmpUtils.getValueFactory().getCounter64(new BigInteger(value)));
                }

                catch (IllegalArgumentException e) {
                    throw new SnmpTrapHelperException("Value " + value + "is invalid for SnmpCounter64");
                }

                catch (NullPointerException e) {
                    throw new SnmpTrapHelperException("Value is null for SnmpCounter64");
                }
            } else {
                throw new SnmpTrapHelperException("Encoding " + encoding + "is invalid for SnmpCounter64");
            }
        }
    }

    /**
     * Create an SNMP V1 trap with the specified enterprise IS, agent address,
     * generic ID, specific ID, and time stamp.
     *
     * @param entId
     *            The enterprise ID for the trap.
     * @param agentAddr
     *            The agent address for the trap.
     * @param generic
     *            The generic ID for the trap.
     * @param specific
     *            The specific ID for the trap.
     * @param timeStamp
     *            The time stamp for the trap.
     * @return The newly-created trap.
     * @throws java.net.UnknownHostException if any.
     */
    public SnmpV1TrapBuilder createV1Trap(String entId, String agentAddr, int generic, int specific, long timeStamp) throws UnknownHostException {

        SnmpV1TrapBuilder trap = SnmpUtils.getV1TrapBuilder();
        trap.setEnterprise(SnmpObjId.get(entId));
        trap.setAgentAddress(InetAddressUtils.addr(agentAddr));
        trap.setGeneric(generic);
        trap.setSpecific(specific);
        trap.setTimeStamp(timeStamp);

        return trap;
    }

    /**
     * Create an SNMP V2 trap with the specified trap object ID, and sysUpTime
     * value.
     *
     * @param trapOid
     *            The trap object id.
     * @param sysUpTime
     *            The system up time.
     * @return The newly-created trap.
     * @exception Throws
     *                SnmpTrapHelperException if the trap cannot be created for
     *                any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public SnmpTrapBuilder createV2Trap(String trapOid, String sysUpTime) throws SnmpTrapHelperException {

        SnmpTrapBuilder packet = SnmpUtils.getV2TrapBuilder();

        addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, sysUpTime);
        addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, trapOid);

        return packet;
    }

    /**
     * Create an SNMP V2 inform with the specified trap object ID, and sysUpTime
     * value.
     *
     * @param trapOid
     *            The trap object id.
     * @param sysUpTime
     *            The system up time.
     * @return The newly-created trap.
     * @exception Throws
     *                SnmpTrapHelperException if the trap cannot be created for
     *                any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public SnmpV2TrapBuilder createV2Inform(String trapOid, String sysUpTime) throws SnmpTrapHelperException {

        SnmpV2TrapBuilder packet = SnmpUtils.getV2InformBuilder();

        addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, sysUpTime);
        addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, trapOid);

        return packet;
    }

    /**
     * Create an SNMP V3 trap with the specified trap object ID, and sysUpTime
     * value.
     *
     * @param trapOid
     *            The trap object id.
     * @param sysUpTime
     *            The system up time.
     * @return The newly-created trap.
     * @exception Throws
     *                SnmpTrapHelperException if the trap cannot be created for
     *                any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public SnmpV3TrapBuilder createV3Trap(String trapOid, String sysUpTime) throws SnmpTrapHelperException {

        SnmpV3TrapBuilder packet = SnmpUtils.getV3TrapBuilder();

        addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, sysUpTime);
        addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, trapOid);

        return packet;
    }

    /**
     * Create an SNMP V3 trap with the specified trap object ID, and sysUpTime
     * value.
     *
     * @param trapOid
     *            The trap object id.
     * @param sysUpTime
     *            The system up time.
     * @return The newly-created trap.
     * @exception Throws
     *                SnmpTrapHelperException if the trap cannot be created for
     *                any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public SnmpV3TrapBuilder createV3Inform(String trapOid, String sysUpTime) throws SnmpTrapHelperException {

        SnmpV3TrapBuilder packet = SnmpUtils.getV3InformBuilder();

        addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, sysUpTime);
        addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, trapOid);

        return packet;
    }

    /**
     * 
     * This helper method helps SNMP trap daemon
     * administrator to set up authentication
     * An snmpv3 trap is sent using the sender 
     * EngineID that needs to be known 
     * over remote trap receivers
     * 
     * @return The local engine ID
     * 
     */
    public String getLocalEngineID() {
    	return "0x"+SnmpUtils.getLocalEngineID();
    }
    /**
     * Create a new variable binding and add it to the specified SNMP V1 trap.
     * The value encoding is assumed to be XML_ENCODING_TEXT.
     *
     * @param trap
     *            The trap to which the variable binding should be added.
     * @param name
     *            The name (a.k.a. "id") of the variable binding to be created
     * @param type
     *            The type of variable binding to be created
     * @param value
     *            The variable binding value
     * @exception Throws
     *                SnmpTrapHelperException if the variable binding cannot be
     *                added to the trap for any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public void addVarBinding(SnmpTrapBuilder trap, String name, String type, String value) throws SnmpTrapHelperException {
        addVarBinding(trap, name, type, EventConstants.XML_ENCODING_TEXT, value);
    }

    /**
     * Create a new variable binding and add it to the specified SNMP V1 trap.
     *
     * @param trap
     *            The trap to which the variable binding should be added.
     * @param name
     *            The name (a.k.a. "id") of the variable binding to be created
     * @param type
     *            The type of variable binding to be created
     * @param encoding
     *            Describes the way in which the value content has been encoded
     *            (i.e. XML_ENCODING_TEXT, or XML_ENCODING_BASE64)
     * @param value
     *            The variable binding value
     * @exception Throws
     *                SnmpTrapHelperException if the variable binding cannot be
     *                added to the trap for any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public void addVarBinding(SnmpTrapBuilder trap, String name, String type, String encoding, String value) throws SnmpTrapHelperException {

        if (name == null) {
            throw new SnmpTrapHelperException("Name is null");
        }

        VarBindFactory factory = (VarBindFactory) m_factoryMap.get(type);

        if (factory == null) {
            throw new SnmpTrapHelperException("Type " + type + " is invalid or not implemented");
        }

        factory.addVarBind(trap, name, encoding, value);
    }

    /**
     * Create an SNMP V1 trap, based on the content of the specified event, and
     * forward the trap to the specified address and port. It is assumed that
     * the specified event represents an SNMP V1 or V2 trap that was received by
     * OpenNMS (TrapD).
     *
     * @param event
     *            The event upon which the trap content should be based
     * @param destAddr
     *            The address to which the trap should be forwarded
     * @param destPort
     *            The port to which the trap should be forwarded
     * @exception Throws
     *                SnmpTrapHelperException if the variable binding cannot be
     *                added to the trap for any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public void forwardV1Trap(Event event, String destAddr, int destPort) throws SnmpTrapHelperException {
        // the event must correspond to an SNMP trap

        Snmp snmpInfo = event.getSnmp();

        if (snmpInfo == null) {
            throw new SnmpTrapHelperException("Cannot forward an event with no SNMP info: " + event.getUei());
        }
        

        // check the version of the original trap

        String version = snmpInfo.getVersion();

        SnmpV1TrapBuilder trap = SnmpUtils.getV1TrapBuilder();

        if ("v1".equals(version)) {

            trap.setEnterprise(SnmpObjId.get(snmpInfo.getId()));

            InetAddress agentAddress;
            agentAddress = InetAddressUtils.addr(event.getSnmphost());
            if (agentAddress == null) {
                throw new SnmpTrapHelperException("Invalid ip address.");
            }

            trap.setAgentAddress(agentAddress);

            if (snmpInfo.hasGeneric()) {
            	trap.setGeneric(snmpInfo.getGeneric());
            }

            if (snmpInfo.hasSpecific()) {
            	trap.setSpecific(snmpInfo.getSpecific());
            }

            trap.setTimeStamp(snmpInfo.getTimeStamp());

            // varbinds

            int i = 0;
            for (Parm parm : event.getParmCollection()) {
                try {
                    Value value = parm.getValue();
                    addVarBinding(trap, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
                } catch (SnmpTrapHelperException e) {
                    throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
                } finally {
                    i++;
                }
            }
        } else if ("v2".equals(version)) {

            // converting V2 trap to V1 (see RFC2576)

            trap.setEnterprise(SnmpObjId.get(snmpInfo.getId()));

            String addr = null;

            for (Parm parm : event.getParmCollection()) {
                if (SNMP_TRAP_ADDRESS_OID.equals(parm.getParmName())) {
                    addr = parm.getValue().getContent();
                    break;
                }
            }

            if (addr == null) {
                addr = "0.0.0.0";
            }
            
            InetAddress agentAddress;
            agentAddress = InetAddressUtils.addr(addr);
            if (agentAddress == null) {
                throw new SnmpTrapHelperException("Invalid ip address.");
            }

            trap.setAgentAddress(agentAddress);

            trap.setGeneric(snmpInfo.getGeneric());

            trap.setSpecific(snmpInfo.getSpecific());

            trap.setTimeStamp(snmpInfo.getTimeStamp());

            // varbinds

            int i = 0;
            for (Parm parm : event.getParmCollection()) {
                Value value = parm.getValue();

                // omit any parms with type=Counter64

                if (!(EventConstants.TYPE_SNMP_COUNTER64.equals(value.getType()))) {

                    try {
                        addVarBinding(trap, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
                    }

                    catch (SnmpTrapHelperException e) {
                        throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
                    }
                }

                i++;
            }

        } else {
            throw new SnmpTrapHelperException("Invalid SNMP version: " + version);
        }

        // send the trap
        
        sendTrap(destAddr, destPort, snmpInfo.getCommunity(), trap);
    }

    private void sendTrap(String destAddr, int destPort, String community, SnmpTrapBuilder trap) throws SnmpTrapHelperException {
        try {
            trap.send(destAddr, destPort, community);
        } catch (Throwable e) {
            throw new SnmpTrapHelperException("Failed to send trap "+e.getMessage(), e);
        }
    }

    /**
     * Create an SNMP V2 trap, based on the content of the specified event, and
     * forward the trap to the specified address and port. It is assumed that
     * the specified event represents an SNMP V1 or V2 trap that was received by
     * OpenNMS (TrapD).
     *
     * @param event
     *            The event upon which the trap content should be based
     * @param destAddr
     *            The address to which the trap should be forwarded
     * @param destPort
     *            The port to which the trap should be forwarded
     * @exception Throws
     *                SnmpTrapHelperException if the variable binding cannot be
     *                added to the trap for any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public void forwardV2Trap(Event event, String destAddr, int destPort) throws SnmpTrapHelperException {

        // the event must correspond to an SNMP trap

        Snmp snmpInfo = event.getSnmp();

        if (snmpInfo == null) {
            throw new SnmpTrapHelperException("Cannot forward an event with no SNMP info: " + event.getUei());
        }

        // check the version of the original trap

        String version = snmpInfo.getVersion();

        SnmpTrapBuilder packet = SnmpUtils.getV2TrapBuilder();

        if ("v1".equals(version)) {

            // converting V1 trap to V2 (see RFC2576)

            addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, Long.toString(snmpInfo.getTimeStamp()));

            String oid;

            if (snmpInfo.getGeneric() == ENTERPRISE_SPECIFIC && snmpInfo.hasSpecific()) {
                oid = snmpInfo.getId() + ".0." + snmpInfo.getSpecific();
            } else {
                oid = SNMP_TRAPS + '.' + (snmpInfo.getGeneric() + 1);
            }

            addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, oid);

            // add the V1 var bindings

            boolean addrPresent = false;
            boolean communityPresent = false;
            boolean enterprisePresent = false;

            int i = 0;
            for (Parm parm : event.getParmCollection()) {
                Value value = parm.getValue();

                try {
                    addVarBinding(packet, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
                }

                catch (SnmpTrapHelperException e) {
                    throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
                }

                if (SNMP_TRAP_ADDRESS_OID.equals(parm.getParmName())) {
                    addrPresent = true;
                } else if (SNMP_TRAP_COMMUNITY_OID.equals(parm.getParmName())) {
                    communityPresent = true;
                } else if (SNMP_TRAP_ENTERPRISE_OID.equals(parm.getParmName())) {
                    enterprisePresent = true;
                }
                i++;
            }

            if (!addrPresent) {
                addVarBinding(packet, SNMP_TRAP_ADDRESS_OID, EventConstants.TYPE_SNMP_IPADDRESS, event.getSnmphost());
            }

            if (!communityPresent) {
                addVarBinding(packet, SNMP_TRAP_COMMUNITY_OID, EventConstants.TYPE_SNMP_OCTET_STRING, snmpInfo.getCommunity());
            }

            if (!enterprisePresent) {
                addVarBinding(packet, SNMP_TRAP_ENTERPRISE_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, snmpInfo.getId());
            }
        } else if ("v2".equals(version)) {

            addVarBinding(packet, SNMP_SYSUPTIME_OID, EventConstants.TYPE_SNMP_TIMETICKS, Long.toString(snmpInfo.getTimeStamp()));
            
            String oid;

            if (snmpInfo.getGeneric() == ENTERPRISE_SPECIFIC) {
                oid = snmpInfo.getId() + "." + snmpInfo.getSpecific();
            } else {
                oid = SNMP_TRAPS + '.' + (snmpInfo.getGeneric() + 1);
            }

            addVarBinding(packet, SNMP_TRAP_OID, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER, oid);

            int i = 0;
            for (Parm parm : event.getParmCollection()) {
                Value value = parm.getValue();

                try {
                    addVarBinding(packet, parm.getParmName(), value.getType(), value.getEncoding(), value.getContent());
                }

                catch (SnmpTrapHelperException e) {
                    throw new SnmpTrapHelperException(e.getMessage() + " in event parm[" + i + "]");
                }

                i++;
            }
        } else {
            throw new SnmpTrapHelperException("Invalid SNMP version: " + version);
        }

        // send the trap

        sendTrap(destAddr, destPort, snmpInfo.getCommunity(), packet);
    }

    /**
     * Create an SNMP trap, based on the content of the specified event, and
     * forward the trap to the specified address and port. It is assumed that
     * the specified event represents an SNMP V1 or V2 trap that was received by
     * OpenNMS (TrapD). The type of trap to be created depends on the type of
     * the original trap (i.e. if the original trap was an SNMP V1 trap, an SNMP
     * V1 trap will be created; if the original trap was an SNMP V2 trap, an
     * SNMP V2 trap will be created).
     *
     * @param event
     *            The event upon which the trap content should be based
     * @param destAddr
     *            The address to which the trap should be forwarded
     * @param destPort
     *            The port to which the trap should be forwarded
     * @exception Throws
     *                SnmpTrapHelperException if the variable binding cannot be
     *                added to the trap for any reason.
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     */
    public void forwardTrap(Event event, String destAddr, int destPort) throws SnmpTrapHelperException {

        Snmp snmpInfo = event.getSnmp();

        if (snmpInfo == null) {
            throw new SnmpTrapHelperException("Cannot forward an event with no SNMP info: " + event.getUei());
        }

        String version = snmpInfo.getVersion();

        if ("v1".equals(version)) {
            forwardV1Trap(event, destAddr, destPort);
        } else if ("v2".equals(version)) {
            forwardV2Trap(event, destAddr, destPort);
        } else {
            throw new SnmpTrapHelperException("Invalid SNMP version: " + version);
        }
    }
    
    /**
     * Create an SNMP trap, based on the content of an event derived from a
     * TL1 autonomous message received by Tl1d, and forward the trap to the
     * specified address and port. The type of trap created depends on the value
     * of the "trapVersion" parameter. The "community" parameter determines the
     * SNMP community string of the resulting trap, defaulting to "public".
     *
     * @param event
     *            The event upon which the trap content should be based
     * @param destAddr
     *            The address to which the trap should be sent
     * @param destPort
     *            The port to which the trap should be sent
     * @param trapVersion
     *            The SNMP version ("v1" or "v2c") of the trap
     * @param community
     *            The SNMP community string for the trap (defaults to "public")
     * @throws org.opennms.netmgt.scriptd.helper.SnmpTrapHelperException if any.
     * @throws java.net.UnknownHostException if any.
     * @exception Throws
     *            SnmpTrapHelperException if the event is not of the appropriate type.
     * @exception Throws
     *            UnknownHostException if agent-addr resolution fails for the case of an SNMPv1 trap
     * @exception Throws
     *            SnmpTrapHelperException if the event is not of the appropriate type.
     * @exception Throws
     *            UnknownHostException if agent-addr resolution fails for the case of an SNMPv1 trap
     */
    public void sendTL1AutonomousMsgTrap(Event event, String destAddr, int destPort, String trapVersion, String community) throws SnmpTrapHelperException, UnknownHostException {
        
        // Check first thing that the event is of the right type.
        if (! org.opennms.netmgt.EventConstants.TL1_AUTONOMOUS_MESSAGE_UEI.equals(event.getUei())) {
            throw new SnmpTrapHelperException("The event must have a UEI of " + org.opennms.netmgt.EventConstants.TL1_AUTONOMOUS_MESSAGE_UEI);
        }
        
        // Create a TrapBuilder and bootstrap it according to trapVersion
        SnmpTrapBuilder trapBuilder = null;
        // What to do about timestamp? Hard-wiring to zero for now.
        long trapTimeStamp = 0;
        
        final String iface = event.getInterface();
		if ("v1".equalsIgnoreCase(trapVersion)) {
            trapBuilder = createV1Trap(".1.3.6.1.4.1.5813.1",   // OPENNMS-MIB::openNMS-traps
            	                       iface,
                                       ENTERPRISE_SPECIFIC,
                                       2,                       // OPENNMS-MIB::openNMS-tl1AutonomousMessageTrap
                                       trapTimeStamp);
        } else if ("v2c".equalsIgnoreCase(trapVersion)) {
            trapBuilder = createV2Trap(".1.3.6.1.4.1.5813.1.0.2",   // OPENNMS-MIB::openNMS-tl1AutonomousMessageTrap
                                       Long.toString(trapTimeStamp));
        } else {
            throw new SnmpTrapHelperException("The trap SNMP version must be either v1 or v2c");
        }
        
        // Add all the MIB-specified varbinds
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.1.8.0", // OPENNMS-MIB::openNMS-event-nodeid 
                      EventConstants.TYPE_SNMP_OCTET_STRING, Long.toString(event.getNodeid()));
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.1.9.0", // OPENNMS-MIB::openNMS-event-time
                      EventConstants.TYPE_SNMP_OCTET_STRING, event.getTime());
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.1.10.0", // OPENNMS-MIB::openNMS-event-host
                      EventConstants.TYPE_SNMP_OCTET_STRING, event.getHost());
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.1.11.0", // OPENNMS-MIB::openNMS-event-interface
                      EventConstants.TYPE_SNMP_OCTET_STRING, iface);
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.1.13.0", // OPENNMS-MIB::openNMS-event-service
                      EventConstants.TYPE_SNMP_OCTET_STRING, event.getService());
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.1.18.0", // OPENNMS-MIB::openNMS-event-severity
                      EventConstants.TYPE_SNMP_OCTET_STRING, event.getSeverity());
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.2.1.1.0", // OPENNMS-MIB::tl1amRawMessage
                      EventConstants.TYPE_SNMP_OCTET_STRING, EventUtil.expandParms("%parm[raw-message]%", event));
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.2.1.2.0", // OPENNMS-MIB::tl1amAlarmCode
                      EventConstants.TYPE_SNMP_OCTET_STRING, EventUtil.expandParms("%parm[alarm-code]%", event));
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.2.1.3.0", // OPENNMS-MIB::tl1amAutonomousTag
                      EventConstants.TYPE_SNMP_OCTET_STRING, EventUtil.expandParms("%parm[atag]%", event));
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.2.1.4.0", // OPENNMS-MIB::tl1amVerb
                      EventConstants.TYPE_SNMP_OCTET_STRING, EventUtil.expandParms("%parm[verb]%", event));
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.2.1.5.0", // OPENNMS-MIB::tl1amAutoBlock
                      EventConstants.TYPE_SNMP_OCTET_STRING, EventUtil.expandParms("%parm[autoblock]%", event));
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.2.1.6.0", // OPENNMS-MIB::tl1amAID
                      EventConstants.TYPE_SNMP_OCTET_STRING, EventUtil.expandParms("%parm[aid]%", event));
        addVarBinding(trapBuilder, ".1.3.6.1.4.1.5813.20.2.1.7.0", // OPENNMS-MIB::tl1amAdditionalParams
                      EventConstants.TYPE_SNMP_OCTET_STRING, EventUtil.expandParms("%parm[additionalParams]%", event));
        
        // Finally, send the trap!
        this.sendTrap(destAddr, destPort, community, trapBuilder);
    }
}
