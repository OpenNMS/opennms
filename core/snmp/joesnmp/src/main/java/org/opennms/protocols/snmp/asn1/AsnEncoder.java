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

package org.opennms.protocols.snmp.asn1;

import java.math.BigInteger;

/**
 * The AsnEncoder interface defines the contract that objects for
 * encoding/decoding ASN.1 SNMP values must fulfill. The encoder must be able to
 * encode and decode integers (unsigned and signed), object identifier, strings,
 * and null values. To support the SNMPv2 the AsnEncoder class must also support
 * encoding/decoding 64-bit integers. Currently the AsnEncoder interface only
 * supports SNMPv1 types.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 */
public interface AsnEncoder {
    /**
     * 
     * The buildLength() method is used to encode an ASN.1 length into the
     * specified byte buffer. The encoding used is dependant on the implementor
     * of the interface.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnLength
     *            The length to be encoded.
     * 
     * @return Returns the new offset for the next encoding routine. If the
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildLength(byte[] buf, int startOffset, int asnLength) throws AsnEncodingException;

    /**
     * 
     * The parseLength() method is used to decode an ASN.1 length from the
     * specified buffer. The encoding used is depandant on the implemetor of the
     * interface.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset and the
     *         decoded length. The first object is an Integer object and
     *         contains the new offset for the next object in buf. The second
     *         object is an Integer and contains the actual decoded length.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseLength(byte[] buf, int startOffset) throws AsnDecodingException;

    /**
     * 
     * The buildHeader() method is used to encode an ASN.1 header into the
     * specified byte buffer. The encoding used is dependant on the implementor
     * of the interface.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnType
     *            The ASN.1 type to place in the buffer
     * @param asnLength
     *            The length to be encoded.
     * 
     * @return Returns the new offset for the next encoding routine. If
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildHeader(byte[] buf, int startOffset, byte asnType, int asnLength) throws AsnEncodingException;

    /**
     * 
     * The parseHeader() method is used to decode an ASN.1 header from the
     * specified buffer. The encoding used is depandant on the implemetor of the
     * interface.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset, ASN.1 type,
     *         and decoded length. The first object is an Integer object and
     *         contains the new offset for the next object in buf. The second
     *         object is a Byte object that represents the decoded ASN.1 Type.
     *         The third object is an Integer and contains the actual decoded
     *         length.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseHeader(byte[] buf, int startOffset) throws AsnDecodingException;

    /**
     * 
     * The buildInteger32() method is used to encode an ASN.1 32-bit signed
     * integer into the specified byte buffer. The encoding used is dependant on
     * the implementor of the interface.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnType
     *            The ASN.1 type to place in the buffer
     * @param asnInt32
     *            The 32-bit signed integer to encode.
     * 
     * @return Returns the new offset for the next encoding routine. If
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildInteger32(byte[] buf, int startOffset, byte asnType, int asnInt32) throws AsnEncodingException;

    /**
     * 
     * The parseInteger32() method is used to decode an ASN.1 32-bit signed
     * integer from the specified buffer. The encoding used is depandant on the
     * implemetor of the interface.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset, ASN.1 type,
     *         and value. The first object is an Integer object and contains the
     *         new offset for the next object in buf. The second object is a
     *         Byte object that represents the decoded ASN.1 Type. The third
     *         object is an Integer and contains the actual decoded value.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseInteger32(byte[] buf, int startOffset) throws AsnDecodingException;

    /**
     * 
     * The buildUInteger32() method is used to encode an ASN.1 32-bit unsigned
     * integer into the specified byte buffer. The encoding used is dependant on
     * the implementor of the interface.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnType
     *            The ASN.1 type to place in the buffer
     * @param asnUInt32
     *            The 32-bit unsigned integer to encode.
     * 
     * @return Returns the new offset for the next encoding routine. If
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildUInteger32(byte[] buf, int startOffset, byte asnType, long asnUInt32) throws AsnEncodingException;

    /**
     * 
     * The parseUInteger32() method is used to decode an ASN.1 32-bit unsigned
     * integer from the specified buffer. The encoding used is depandant on the
     * implemetor of the interface.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset, ASN.1 type,
     *         and value. The first object is an Integer object and contains the
     *         new offset for the next object in buf. The second object is a
     *         Byte object that represents the decoded ASN.1 Type. The third
     *         object is a Long object and contains the actual decoded value.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseUInteger32(byte[] buf, int startOffset) throws AsnDecodingException;

    /**
     * 
     * The buildUInteger64() method is used to encode an ASN.1 64-bit unsigned
     * integer into the specified byte buffer. The encoding used is dependant on
     * the implementor of the interface.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnType
     *            The ASN.1 type to place in the buffer
     * @param asnUInt64
     *            The 64-bit unsigned integer to encode.
     * 
     * @return Returns the new offset for the next encoding routine. If
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildUInteger64(byte[] buf, int startOffset, byte asnType, BigInteger asnUInt64) throws AsnEncodingException;

    /**
     * 
     * The parseUInteger64() method is used to decode an ASN.1 64-bit unsigned
     * integer from the specified buffer. The encoding used is depandant on the
     * implemetor of the interface.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset, ASN.1 type,
     *         and value. The first object is an Integer object and contains the
     *         new offset for the next object in buf. The second object is a
     *         Byte object that represents the decoded ASN.1 Type. The third
     *         object is a BigInteger object and contains the actual decoded
     *         value.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseUInteger64(byte[] buf, int startOffset) throws AsnDecodingException;

    /**
     * 
     * The buildNull() method is used to encode an ASN.1 NULL value into the
     * specified byte buffer. The encoding used is dependant on the implementor
     * of the interface.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnType
     *            The ASN.1 type to place in the buffer
     * 
     * @return Returns the new offset for the next encoding routine. If
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildNull(byte[] buf, int startOffset, byte asnType) throws AsnEncodingException;

    /**
     * 
     * The parseNull() method is used to decode an ASN.1 Null value from the
     * specified buffer. The encoding used is depandant on the implemetor of the
     * interface. Since there is no "null" value only the new offset and ASN.1
     * type are returned.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset and the
     *         ASN.1 type. The first object is an Integer object and contains
     *         the new offset for the next object in buf. The second object is a
     *         Byte object that represents the decoded ASN.1 Type.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseNull(byte[] buf, int startOffset) throws AsnDecodingException;

    /**
     * 
     * The buildString() method is used to encode an ASN.1 string value into the
     * specified byte buffer.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnType
     *            The ASN.1 type to place in the buffer
     * @param opaque
     *            An array of bytes to encode into the string.
     * 
     * @return Returns the new offset for the next encoding routine. If
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildString(byte[] buf, int startOffset, byte asnType, byte[] opaque) throws AsnEncodingException;

    /**
     * 
     * The parseString() method is used to decode an ASN.1 opaque string from
     * the specified buffer. The encoding used is depandant on the implemetor of
     * the interface.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset and ASN.1
     *         type, and byte array. The first object is an Integer object and
     *         contains the new offset for the next object in buf. The second
     *         object is a Byte object that represents the decoded ASN.1 Type.
     *         The third object is an array of primitive bytes.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseString(byte[] buf, int startOffset) throws AsnDecodingException;

    /**
     * 
     * The buildObjectId() method is used to encode an ASN.1 object id value
     * into the specified byte buffer.
     * 
     * @param buf
     *            The output buffer of encoded bytes.
     * @param startOffset
     *            The offset from the start of the buffer where the method
     *            should start writing the encoded data.
     * @param asnType
     *            The ASN.1 type to place in the buffer
     * @param oids
     *            An array of integers to encode.
     * 
     * @return Returns the new offset for the next encoding routine. If
     *         startOffset is subtracted from the return value then the length
     *         of the encoded data can be determined.
     * 
     * @exception AsnEncodingException
     *                Thrown if an error occurs encoding the datatype.
     * 
     */
    public int buildObjectId(byte[] buf, int startOffset, byte asnType, int[] oids) throws AsnEncodingException;

    /**
     * 
     * The parseObjectId() method is used to decode an ASN.1 Object Identifer
     * from the specified buffer. The encoding used is depandant on the
     * implemetor of the interface.
     * 
     * @param buf
     *            The input buffer
     * @param startOffset
     *            The offset to start decoding in the buffer
     * 
     * @return Returns an Object array that contains the new offset and ASN.1
     *         type, and ObjectId array. The first object is an Integer object
     *         and contains the new offset for the next object in buf. The
     *         second object is a Byte object that represents the decoded ASN.1
     *         Type. The third object is an array of primitive integers.
     * 
     * @exception AsnDecodingException
     *                Thrown if an error occurs decoding the buffer.
     */
    public Object[] parseObjectId(byte[] buf, int startOffset) throws AsnDecodingException;
}
