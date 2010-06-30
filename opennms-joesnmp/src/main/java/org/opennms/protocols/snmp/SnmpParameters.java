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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// SnmpParameters.java,v 1.1.1.1 2001/11/11 17:27:22 ben Exp
//
//

package org.opennms.protocols.snmp;

import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.BerEncoder;

/**
 * The SnmpParameters class is used to define the parameters for an SnmpSession.
 * The parameters include the read/write community strings. The protocol version
 * and the ASN.1 encoder used to encode/decode transmissions.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 */
public class SnmpParameters extends Object implements Cloneable {
    /**
     * The read community string.
     */
    private String m_readCommunity;

    /**
     * The write community string
     */
    private String m_writeCommunity;

    /**
     * The SNMP protocol version used for communication. The possible values are
     * defined in the SnmpSMI
     * 
     * @see SnmpSMI#SNMPV1
     * @see SnmpSMI#SNMPV2
     */
    private int m_version;

    /**
     * The AsnEncoder object used by the session to encode and decode
     * information.
     * 
     * @see org.opennms.protocols.snmp.asn1.BerEncoder
     */
    private AsnEncoder m_encoder;

    /**
     * The default read-only community string
     */
    public static final String defaultCommunity = "public";

    /**
     * The AsnEncoder used by default.
     */
    public static final AsnEncoder defaultEncoder = new BerEncoder();

    /**
     * The SNMP protocol version used by default.
     */
    public static final int defaultVersion = SnmpSMI.SNMPV1;

    /**
     * The default class constructor. Constructs the object with the publicly
     * available default values. By default the write community string is left
     * null.
     *
     * @see #defaultCommunity
     * @see #defaultEncoder
     * @see #defaultVersion
     * @see #defaultCommunity
     * @see #defaultEncoder
     * @see #defaultVersion
     * @see #defaultCommunity
     * @see #defaultEncoder
     * @see #defaultVersion
     */
    public SnmpParameters() {
        m_readCommunity = defaultCommunity;
        m_writeCommunity = null;
        m_version = defaultVersion;
        m_encoder = defaultEncoder;
    }

    /**
     * Constructs a copy of the parameters defined in the object second.
     *
     * @param second
     *            The object to copy into self.
     */
    public SnmpParameters(SnmpParameters second) {
        m_readCommunity = second.m_readCommunity;
        m_writeCommunity = second.m_writeCommunity;
        m_version = second.m_version;
        m_encoder = second.m_encoder;
    }

    /**
     * Constructs a default object with the specified SNMP protocol version.
     *
     * @param version
     *            The SNMP protocol version.
     */
    public SnmpParameters(int version) {
        this();
        m_version = version;
    }

    /**
     * Constructs a default object with the specified read-only community
     * string.
     *
     * @param read
     *            The read-only community string.
     */
    public SnmpParameters(String read) {
        this();
        m_readCommunity = read;
    }

    /**
     * Constructs an object with the specified read-only and write-only
     * community strings.
     *
     * @param read
     *            The read-only community string.
     * @param write
     *            The write-only community string.
     */
    public SnmpParameters(String read, String write) {
        this();
        m_readCommunity = read;
        m_writeCommunity = write;
    }

    /**
     * Retreives the current read community string from the parameters.
     *
     * @return The read community string.
     */
    public String getReadCommunity() {
        return m_readCommunity;
    }

    /**
     * Used to set the parameters read community string.
     *
     * NOTE: The community string is covnerted to a set of 8-bit characters by
     * the String.getBytes() method.
     *
     * @param rd
     *            The new read community string.
     * @see java.lang.String#getBytes()
     */
    public void setReadCommunity(String rd) {
        m_readCommunity = rd;
    }

    /**
     * Retreives the current write community string set in the parameters.
     *
     * @return The write community string.
     */
    public String getWriteCommunity() {
        return m_writeCommunity;
    }

    /**
     * Used to set the parameters write community string. The write community
     * string is only used by SNMP SET packet.
     *
     * NOTE: The community string is covnerted to a set of 8-bit characters by
     * the String.getBytes() method.
     *
     * @param wr
     *            The new write community string.
     * @see java.lang.String#getBytes()
     */
    public void setWriteCommunity(String wr) {
        m_writeCommunity = wr;
    }

    /**
     * Returns the current SNMP version defined by the parameters.
     *
     * @return The current protocol version.
     */
    public int getVersion() {
        return m_version;
    }

    /**
     * Use to set the SNMP protocol version. The version should be one of the
     * constants within the SnmpSMI.
     *
     * @param ver
     *            The SNMP version protocol to use.
     * @see SnmpSMI#SNMPV1
     * @see SnmpSMI#SNMPV2
     * @see SnmpSMI#SNMPV1
     * @see SnmpSMI#SNMPV2
     */
    public void setVersion(int ver) {
        m_version = ver;
    }

    /**
     * Retreives the current ASN.1 encoder object.
     *
     * @return The current AsnEncoder
     */
    public AsnEncoder getEncoder() {
        return m_encoder;
    }

    /**
     * Sets the ASN.1 encoder.
     *
     * @param encoder
     *            The new encoder to use.
     */
    public void setEncoder(AsnEncoder encoder) {
        m_encoder = encoder;
    }

    /**
     * Used to get a newly created duplicate of the current object.
     *
     * @return A newly created duplicate
     */
    public Object clone() {
        return new SnmpParameters(this);
    }
}
