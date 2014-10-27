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
