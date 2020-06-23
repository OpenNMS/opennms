/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.snmp;

import org.opennms.protocols.snmp.asn1.ASN1;

/**
 * SNMP SMI v1 &amp; v2 constants.
 * 
 * @see org.opennms.protocols.snmp.asn1.ASN1
 * 
 * @version 1.1.1.1
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public abstract class SnmpSMI {
    /**
     * Defines the positive and negative whole numbers, including zero. The
     * value is represented as a 32-bit signed integer.
     * 
     * @see org.opennms.protocols.snmp.asn1.ASN1#INTEGER
     */
    public static final byte SMI_INTEGER = (ASN1.UNIVERSAL | ASN1.INTEGER);

    /**
     * A sequence of zero or more octets, where an octet is an 8-bit quantity.
     * 
     * @see org.opennms.protocols.snmp.asn1.ASN1#OCTETSTRING
     */
    public static final byte SMI_STRING = (ASN1.UNIVERSAL | ASN1.OCTETSTRING);

    /**
     * A set of values associated with the information objects defined by the
     * standard.
     * 
     * @see org.opennms.protocols.snmp.asn1.ASN1#OBJECTID
     */
    public static final byte SMI_OBJECTID = (ASN1.UNIVERSAL | ASN1.OBJECTID);

    /**
     * A Null value. Commonly used to where there are several alternatives but
     * none of them apply.
     * 
     * @see org.opennms.protocols.snmp.asn1.ASN1#NULL
     */
    public static final byte SMI_NULL = (ASN1.UNIVERSAL | ASN1.NULL);

    /**
     * An application string is a sequence of octets defined at the application
     * level. Although the SMI does not define an Application String, it does
     * define an IP Address which is an Application String of length four.
     */
    public static final byte SMI_APPSTRING = (ASN1.APPLICATION | 0x00);

    /**
     * An IP Address is an application string of length four and is
     * indistinguishable from the SMI_APPSTRING value. The address is a 32-bit
     * quantity stored in network byte order.
     */
    public static final byte SMI_IPADDRESS = (ASN1.APPLICATION | 0x00);

    /**
     * A non-negative integer that may be incremented, but not decremented. The
     * value is a 32-bit unsigned quantity representing the range of zero to
     * 2^32-1 (4,294,967,295). When the counter reaches its maximum value it
     * wraps back to zero and starts again.
     */
    public static final byte SMI_COUNTER32 = (ASN1.APPLICATION | 0x01);

    /**
     * Represents a non-negative integer that may increase or decrease with a
     * maximum value of 2^32-1. If the maximum value is reached the gauge stays
     * latched until reset.
     */
    public static final byte SMI_GAUGE32 = (ASN1.APPLICATION | 0x02);

    /**
     * Used to represent the integers in the range of 0 to 2^32-1. This type is
     * identical to the SMI_COUNTER32 and are indistinguishable in ASN.1
     */
    public static final byte SMI_UNSIGNED32 = (ASN1.APPLICATION | 0x02); // same
                                                                            // as
                                                                            // gauge

    /**
     * This represents a non-negative integer that counts time, modulo 2^32. The
     * time is represented in hundredths (1/100th) of a second.
     */
    public static final byte SMI_TIMETICKS = (ASN1.APPLICATION | 0x03);

    /**
     * Used to support the transport of arbitrary data. The data itself is
     * encoded as an octet string, but may be in any format defined by ASN.1 or
     * another standard.
     */
    public static final byte SMI_OPAQUE = (ASN1.APPLICATION | 0x04);

    /**
     * Defines a 64-bit unsigned counter. A counter is an integer that can be
     * incremented, but cannot be decremented. A maximum value of 2^64 - 1
     * (18,446,744,073,709,551,615) can be represented. When the counter reaches
     * it's maximum it wraps back to zero and starts again.
     */
    public static final byte SMI_COUNTER64 = (ASN1.APPLICATION | 0x06); // SMIv2
                                                                        // only

    /**
     * The SNMPv2 error representing that there is No-Such-Object for a
     * particular object identifier. This error is the result of a requested
     * object identifier that does not exist in the agent's tables
     */
    public static final byte SMI_NOSUCHOBJECT = (ASN1.CONTEXT | ASN1.PRIMITIVE | 0x00);

    /**
     * The SNMPv2 error representing that there is No-Such-Instance for a
     * particular object identifier. This error is the result of a requested
     * object identifier instance does not exist in the agent's tables.
     */
    public static final byte SMI_NOSUCHINSTANCE = (ASN1.CONTEXT | ASN1.PRIMITIVE | 0x01);

    /**
     * The SNMPv2 error representing the End-Of-Mib-View. This error variable
     * will be returned by a SNMPv2 agent if the requested object identfier has
     * reached the end of the agent's mib table and there is no lexicographic
     * successor.
     */
    public static final byte SMI_ENDOFMIBVIEW = (ASN1.CONTEXT | ASN1.PRIMITIVE | 0x02);

    /**
     * The value for a SNMP V1 protocol session
     */
    public static final int SNMPV1 = 0;

    /**
     * The value for a SNMP V2 protocol session.
     */
    public static final int SNMPV2 = 1;

    public static String getVersionString(int version) {
        return ((version == SNMPV1) ? "SNMPv1" : "SNMPv2");
    }

        public static int toInt(SnmpSyntax result, int deflt) {
            if (result == null)
                return deflt;
            
            try {
                return Integer.parseInt(result.toString());
            } catch (NumberFormatException e) {
                return deflt;
            }
            
        }
}
