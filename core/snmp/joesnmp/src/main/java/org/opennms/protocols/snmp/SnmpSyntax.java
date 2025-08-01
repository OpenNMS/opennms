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

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * This class defines the interface that must be implemented by all object that
 * can be passed or received to/from a SNMP agent and manager. These include
 * intergers, counters, strings, etc al.
 * 
 * The interface defines the methods for encoding and decoding buffers. It also
 * defines the methods for duplicating objects and getting the ASN.1 type.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 * 
 */
public interface SnmpSyntax {
    /**
     * Returns the ASN.1 type of the implementor object.
     */
    public byte typeId();

    /**
     * Encodes the data object in the specified buffer using the AsnEncoder
     * object
     * 
     * @param buf
     *            The buffer to write the encoded information
     * @param offset
     *            The location to start writing the encoded data
     * @param encoder
     *            The object used to encode the data
     * 
     * @return Returns the offset in buf to the byte immedantly after the last
     *         encode byte for the SnmpSyntax file
     * 
     * @exception AsnEncodingException
     *                Thrown if an encoding error occurs
     */
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException;

    /**
     * Decodes the ASN.1 buffer and sets the values in the SnmpSyntax object.
     * 
     * @param buf
     *            The encoded data buffer
     * @param offset
     *            The offset of the first valid byte
     * @param encoder
     *            The object used to decode the ASN.1 data
     * 
     * @return Returns the index to the byte of data immedantly after the last
     *         byte of encoded data.
     * 
     * @exception AsnDecodingException
     *                Thrown if an encoding error occurs
     */
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException;

    /**
     * Creates a duplicate (in memory) object of the caller. Similar to the
     * clone() method.
     * 
     */
    public SnmpSyntax duplicate();
}
