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

import java.io.Serializable;

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * Implements the SNMP Null object as defined by the SNMPv1 and SNMPv2 SMI. The
 * object has no data or length, but is encoded with a specific header. Often
 * used as the value portion of an SnmpVarBind when sending a GET, GETNEXT, etc
 * to a remote agent.
 * 
 * This class manages no internal data, but is derived to provide the proper
 * encoding and handling of SNMP Null datatypes.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class SnmpNull extends Object implements SnmpSyntax, Cloneable, Serializable {
    /**
     * Used to allow the serialization format to evolve.
     * 
     */
    static final long serialVersionUID = 441279481529521581L;

    /**
     * Defines the ASN.1 value for the SnmpNull class.
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_NULL;

    /**
     * Constructs a default SnmpNull class
     * 
     */
    public SnmpNull() {
        // do nothing
    }

    /**
     * Copy constructor. Provided so that if the class is extended or should at
     * some point manage internal data, the data could be meaningfully copied
     * from the passed object.
     * 
     * @param second
     *            The class object to set data from.
     * 
     */
    public SnmpNull(SnmpNull second) {
        // do nothing
    }

    /**
     * Used to retreive the ASN.1 type for this object.
     * 
     * @return The ASN.1 value for the SnmpNull
     * 
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Used to encode the null value into an ASN.1 buffer. The passed encoder
     * defines the method for encoding the data.
     * 
     * @param buf
     *            The location to write the encoded data
     * @param offset
     *            The start of the encoded buffer.
     * @param encoder
     *            The ASN.1 encoder object
     * 
     * @return The byte immediantly after the last encoded byte.
     * 
     */
    @Override
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        return encoder.buildNull(buf, offset, typeId());
    }

    /**
     * Used to decode the null value from the ASN.1 buffer. The passed encoder
     * is used to decode the ASN.1 information.
     * 
     * @param buf
     *            The encoded ASN.1 data
     * @param offset
     *            The offset of the first byte of data
     * @param encoder
     *            The ASN.1 decoder object.
     * 
     * @return The byte immediantly after the last decoded byte of information.
     * 
     */
    @Override
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseNull(buf, offset);

        if (((Byte) rVals[1]).byteValue() != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        return ((Integer) rVals[0]).intValue();
    }

    /**
     * Used to get a duplicate of the current object so that it can be modified
     * without affecting the creating object.
     * 
     * @return A duplicate of the current object.
     * 
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpNull(this);
    }

    /**
     * Used to get a duplicate of the current object so that it can be modified
     * without affecting the creating object.
     * 
     * @return A duplicate of the current object.
     * 
     */
    @Override
    public Object clone() {
        return new SnmpNull(this);
    }

    /**
     * Returns a string representation of the SNMP Null object
     * 
     */
    @Override
    public String toString() {
        return "";
    }
}
