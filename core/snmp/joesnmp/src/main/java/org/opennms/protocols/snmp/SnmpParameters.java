/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.snmp;

import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.BerEncoder;

/**
 * The SnmpParameters class is used to define the parameters for an SnmpSession.
 * The parameters include the read/write community strings. The protocol version
 * and the ASN.1 encoder used to encode/decode transmissions.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
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
     * 
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
     * 
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
     * 
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
     * 
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
     * 
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
     * 
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
     * 
     * @see java.lang.String#getBytes()
     */
    public void setReadCommunity(String rd) {
        m_readCommunity = rd;
    }

    /**
     * Retreives the current write community string set in the parameters.
     * 
     * @return The write community string.
     * 
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
     * 
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
     * 
     * @see SnmpSMI#SNMPV1
     * @see SnmpSMI#SNMPV2
     * 
     */
    public void setVersion(int ver) {
        m_version = ver;
    }

    /**
     * Retreives the current ASN.1 encoder object.
     * 
     * @return The current AsnEncoder
     * 
     */
    public AsnEncoder getEncoder() {
        return m_encoder;
    }

    /**
     * Sets the ASN.1 encoder.
     * 
     * @param encoder
     *            The new encoder to use.
     * 
     */
    public void setEncoder(AsnEncoder encoder) {
        m_encoder = encoder;
    }

    /**
     * Used to get a newly created duplicate of the current object.
     * 
     * @return A newly created duplicate
     * 
     */
    @Override
    public Object clone() {
        return new SnmpParameters(this);
    }
}
