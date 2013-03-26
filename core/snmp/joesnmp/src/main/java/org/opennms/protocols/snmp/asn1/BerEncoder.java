/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.snmp.asn1;

import java.math.BigInteger;

/**
 * The BerEncoder class is used to implement the AsnEncoder interface for the
 * Basic Encoding Rules (BER). The encoding rules are used to encode and decode
 * SNMP values using BER.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class BerEncoder implements AsnEncoder {
    /**
     * Defines the ASN.1 long length marker for the Basic Encoding Rule (BER)
     */
    private static final byte LONG_LENGTH = (byte) 0x80;

    /**
     * Defines the "high bit" that is the sign extension bit for a 8-bit signed
     * value.
     */
    private static final byte HIGH_BIT = (byte) 0x80;

    /**
     * Defines the BER extension "value" that is used to mark an extension type.
     */
    private static final byte EXTENSION_ID = (byte) 0x1F;

    /**
     * Defines the BER constructor id
     */
    private static final byte CONSTRUCTOR = (byte) 0x20;

    /**
     * Converts a primitive byte to a primitive long using "unsigned" logic.
     * 
     * @param b
     *            The 8-bit value to convert
     * @return Returns the 32-bit converted value
     * 
     */
    protected static int byteToInt(byte b) {
        return (b < 0) ? 256 + (int) b : (int) b;
    }

    /**
     * Converts a primitive byte to a primitive long. The byte is converted
     * using "unsigned" logic
     * 
     * @param b
     *            The 8-bit value to convert
     * @return Returns the 64-bit converted value
     * 
     */
    protected static long byteToLong(byte b) {
        return (b < 0) ? 256 + (long) b : (long) b;
    }

    /**
     * Used to determine if the ASN.1 type is a constructor.
     * 
     * @param b
     *            The ASN.1 type
     * 
     * @return True if the ASN.1 type is a constructor, otherwise a false is
     *         returned.
     * 
     */
    protected static boolean isConstructor(byte b) {
        return ((b & CONSTRUCTOR) == CONSTRUCTOR);
    }

    /**
     * Used to test if the ASN.1 type is an extension.
     * 
     * @param b
     *            The ASN.1 type.
     * 
     * @return True if the ASN.1 type is an extension. False if the ASN.1 type
     *         is not an extension.
     * 
     */
    protected static boolean isExtensionId(byte b) {
        return ((b & EXTENSION_ID) == EXTENSION_ID);
    }

    /**
     * Used to copy data from one buffer to another. The method has the
     * flexability to allow the caller to specify an offset in each buffer and
     * the total number of bytes to copy
     * 
     * @param src
     *            The source buffer
     * @param srcOff
     *            The offset of the first byte in the source buffer
     * @param dest
     *            The destination buffer
     * @param destOff
     *            The offset of the first byte in the destination buffer
     * @param bytesToCopy
     *            The number of bytes to copy
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                Thrown if there is insufficent space in either array to
     *                copy the data.
     * 
     */
    protected static void copy(byte[] src, int srcOff, byte[] dest, int destOff, int bytesToCopy) throws ArrayIndexOutOfBoundsException {
        if ((dest.length - destOff) < bytesToCopy || (src.length - srcOff) < bytesToCopy)
            throw new ArrayIndexOutOfBoundsException("Destination or source buffer is insufficent");

        for (int x = bytesToCopy - 1; x >= 0; x--) {
            dest[destOff + x] = src[srcOff + x];
        }
    }

    /**
     * Used to copy data from one buffer to another. The method has the
     * flexability to allow the caller to specify an offset in each buffer and
     * the total number of integers to copy
     * 
     * @param src
     *            The source buffer
     * @param srcOff
     *            The offset of the first integer in the source buffer
     * @param dest
     *            The destination buffer
     * @param destOff
     *            The offset of the first integer in the destination buffer
     * @param intsToCopy
     *            The number of integer elements to copy
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                Thrown if there is insufficent space in either array to
     *                copy the data.
     * 
     */
    protected static void copy(int[] src, int srcOff, int[] dest, int destOff, int intsToCopy) throws ArrayIndexOutOfBoundsException {
        if ((dest.length - destOff) < intsToCopy || (src.length - srcOff) < intsToCopy)
            throw new ArrayIndexOutOfBoundsException("Destination or source buffer is insufficent");

        for (int x = intsToCopy - 1; x >= 0; x--) {
            dest[destOff + x] = src[srcOff + x];
        }
    }

    /**
     * Rotates a give buffer area marked by begin, pivot, and end. The pivot
     * marks the point where the bytes between [pivot..end) are moved to the
     * position marked by begin. The bytes between [begin..pivot) are shifted
     * such that begin is at [begin+(end-pivot)].
     * 
     * @param buf
     *            The buffer containing the data to rotate
     * @param begin
     *            The start of the rotation
     * @param pivot
     *            The pivot point for the rotation
     * @param end
     *            The end of the rotational buffer
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                Thrown if an access exception occurs
     * 
     */
    protected static void rotate(byte[] buf, int begin, int pivot, int end) throws ArrayIndexOutOfBoundsException {
        int dist = end - pivot;
        byte[] hold = new byte[dist];

        copy(buf, // source
             pivot, // source offset
             hold, // destination
             0, // destination offset
             dist); // length

        //
        // shift from end of buffer to front. This
        // way we do not have to worry about data
        // corruption
        //
        for (int x = (pivot - begin) - 1; x >= 0; x--) {
            buf[begin + dist + x] = buf[begin + x]; // SHIFT!
        }

        copy(hold, // source
             0, // source offset
             buf, // destination
             begin, // destination offset
             dist); // length
    }

    /**
     * Default constructor for the BER Encoder.
     * 
     */
    public BerEncoder() {
        // default class constructor.
        // Does nothing
    }

    /**
     * 
     * The buildLength() method is used to encode an ASN.1 length into the
     * specified byte buffer. The method is defined in the AsnEncoder interface.
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
     */
    public int buildLength(byte[] buf, int startOffset, int asnLength) throws AsnEncodingException {
        if (asnLength <= 0x7f) {
            // 
            // check the buffer length
            //
            if ((buf.length - startOffset) < 1)
                throw new AsnEncodingException("Buffer overflow error");

            buf[startOffset++] = (byte) (asnLength & 0x7f);
        } else if (asnLength <= 0xff) {
            //
            // check the buffer length
            //
            if ((buf.length - startOffset) < 2)
                throw new AsnEncodingException("Buffer overflow error");

            buf[startOffset++] = (byte) (0x01 | LONG_LENGTH);
            buf[startOffset++] = (byte) (asnLength & 0xff);
        } else // 0xff < asnLength <= 0xffff
        {
            //
            // check the buffer length
            //
            if ((buf.length - startOffset) < 3)
                throw new AsnEncodingException("Buffer overflow error");

            buf[startOffset++] = (byte) (0x02 | LONG_LENGTH);
            buf[startOffset++] = (byte) ((asnLength >>> 8) & 0xff);
            buf[startOffset++] = (byte) (asnLength & 0xff);
        }
        return startOffset;
    }

    /**
     * 
     * The parseLength() method is used to decode an ASN.1 length from the
     * specified buffer. The method is defined to support the AsnEncoder
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
    public Object[] parseLength(byte[] buf, int startOffset) throws AsnDecodingException {
        //
        // check the buffer length
        //
        if ((buf.length - startOffset) < 1)
            throw new AsnDecodingException("Buffer underflow error");

        //
        // 1) Integer w/new offset value
        // 2) Integer w/recovered length
        //
        Object[] retVals = new Object[2];

        //
        // get the first byte and check it for
        // the long length field
        //
        byte numBytes = buf[startOffset++];
        if ((numBytes & LONG_LENGTH) == 0) {
            //
            // short definiate length encoding
            //
            numBytes = (byte) (numBytes & ~LONG_LENGTH);
            retVals[1] = Integer.valueOf(byteToInt(numBytes));
        } else {
            //
            // Long length encoding
            //
            numBytes = (byte) (numBytes & ~LONG_LENGTH);
            if (numBytes == 1) {
                if ((buf.length - startOffset) < 1)
                    throw new AsnDecodingException("Buffer underflow error");

                retVals[1] = Integer.valueOf(byteToInt(buf[startOffset++]));
            } else if (numBytes == 2) {
                if ((buf.length - startOffset) < 2)
                    throw new AsnDecodingException("Buffer underflow error");

                int val = byteToInt(buf[startOffset++]) << 8 | byteToInt(buf[startOffset++]);
                retVals[1] = Integer.valueOf(val);
            } else {
                throw new AsnDecodingException("Invalid ASN.1 length");
            }
        }

        //
        // create the offset object
        //
        retVals[0] = Integer.valueOf(startOffset);

        return retVals;
    }

    /**
     * 
     * The buildHeader() method is used to encode an ASN.1 header into the
     * specified byte buffer. The method is defined to support the AsnEncoder
     * interface. This method is dependant on the buildLength() method.
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
    public int buildHeader(byte[] buf, int startOffset, byte asnType, int asnLength) throws AsnEncodingException {
        if ((buf.length - startOffset) < 1)
            throw new AsnEncodingException("Buffer overflow error");

        buf[startOffset++] = asnType;
        return buildLength(buf, startOffset, asnLength);
    }

    /**
     * 
     * The parseHeader() method is used to decode an ASN.1 header from the
     * specified buffer. The method is defined to support the AsnEncoder
     * interface. The method also calls the parseLength() method.
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
    public Object[] parseHeader(byte[] buf, int startOffset) throws AsnDecodingException {
        if ((buf.length - startOffset) < 1)
            throw new AsnDecodingException("Insufficent buffer length");

        //
        // get the ASN.1 Type
        //
        byte asnType = buf[startOffset++];
        if (isExtensionId(asnType))
            throw new AsnDecodingException("Buffer underflow error");

        //
        // get the length
        //
        Object[] lenVals = parseLength(buf, startOffset);

        //
        // create the return values
        // 1) offset for next object
        // 2) ASN.1 type
        // 3) ASN.1 length
        //
        Object[] rVals = new Object[3];
        rVals[0] = lenVals[0];
        rVals[1] = new Byte(asnType);
        rVals[2] = lenVals[1];

        //
        // return the results
        //
        return rVals;
    }

    /**
     * 
     * The buildInteger32() method is used to encode an ASN.1 32-bit signed
     * integer into the specified byte buffer.
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
    public int buildInteger32(byte[] buf, int startOffset, byte asnType, int asnInt32) throws AsnEncodingException {
        //
        // Store a copy of the value to mask off the
        // unnecessary bits. There should not be any
        // sequence of 9 consecutive 1's or 0 bits
        //
        int mask = 0xff800000; // negative :)
        int intSz = 4; // int == 32-bits == 4 bytes in java

        while (((asnInt32 & mask) == 0 || (asnInt32 & mask) == mask) && intSz > 1) {
            --intSz;
            asnInt32 = (asnInt32 << 8);
        }

        //
        // build the header
        //
        startOffset = buildHeader(buf, startOffset, asnType, intSz);

        //
        // verify the buffer length
        //
        if ((buf.length - startOffset) < intSz)
            throw new AsnEncodingException("Insufficent buffer size");

        //
        // mask off and store the values
        //
        mask = 0xff000000;
        while (intSz-- > 0) {
            byte b = (byte) ((asnInt32 & mask) >>> 24);
            buf[startOffset++] = b;
            asnInt32 = (asnInt32 << 8);
        }

        //
        // return the result
        //
        return startOffset;
    }

    /**
     * 
     * The parseInteger32() method is used to decode an ASN.1 32-bit signed
     * integer from the specified buffer.
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
    public Object[] parseInteger32(byte[] buf, int startOffset) throws AsnDecodingException {
        //
        // parse the header first
        //
        Object[] hdrVals = parseHeader(buf, startOffset);

        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();

        //
        // check for sufficent data
        //
        if ((buf.length - startOffset) < asnLength)
            throw new AsnDecodingException("Buffer underflow error");

        //
        // check to see that we can actually decode
        // the value (must fit in integer == 32-bits)
        //
        if (asnLength > 4)
            throw new AsnDecodingException("Integer too large: cannot decode");

        //
        // check for negativity!
        //
        int asnValue = 0;
        if ((buf[startOffset] & HIGH_BIT) == HIGH_BIT)
            asnValue = -1;

        //
        // extract the information from the buffer
        //
        while (asnLength-- > 0) {
            asnValue = (asnValue << 8) | byteToInt(buf[startOffset++]);
        }

        //
        // return the data!
        //
        Object[] rVals = new Object[3];
        rVals[0] = Integer.valueOf(startOffset);
        rVals[1] = asnType; // java.lang.Byte()
        rVals[2] = Integer.valueOf(asnValue);

        return rVals;
    }

    /**
     * 
     * The buildUInteger32() method is used to encode an ASN.1 32-bit unsigned
     * integer into the specified byte buffer.
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
    public int buildUInteger32(byte[] buf, int startOffset, byte asnType, long asnUInt32) throws AsnEncodingException {
        //
        // NOTE: the value is a 'long' which is 64 bits long, but we only use
        // the lower order 32-bits! If the number is greater than 2^32 - 1
        // the upper order bits will be lost!

        //
        // Store a copy of the value to mask off the
        // unnecessary bits. There should not be any
        // sequence of 9 consecutive 1's or 0 bits
        //
        long mask = 0xff800000L;
        int intSz = 4; // int == 32-bits == 4 bytes in java
        boolean bAddNullByte = false;

        //
        // check to see if an additional (zero) byte is needed
        // Since an Integer is a signed 32-bit quantity, if the
        // passed long is greater than Integer.MAX_VALUE (2^31-1)
        // then it must have the high-bit (2^32) set!
        //
        // effectively checking to see if (asnUInt32 & 0x80000000L) != 0
        //
        if (asnUInt32 > (long) (Integer.MAX_VALUE)) {
            bAddNullByte = true;
            intSz++;
        }

        //
        // check for and remove any sequence of 9 consecutive zeros
        // from the head of the number
        //
        // NOTE: 10/9/00 Weave - This use to also mask off any set
        // of 9 consecutive 1's as well, but that didn't make
        // any sense because it's unsigned. What if you wanted
        // to send 0xffffffffL, masking off the ones would be
        // incorrect cause for unsigned there isn't going to
        // be a sign extension.
        //
        while ((asnUInt32 & mask) == 0 && intSz > 1) {
            --intSz;
            asnUInt32 = (asnUInt32 << 8);
        }

        //
        // build the header
        //
        startOffset = buildHeader(buf, startOffset, asnType, intSz);

        //
        // verify the buffer length
        //
        if ((buf.length - startOffset) < intSz)
            throw new AsnEncodingException("Buffer overflow error");

        //
        // Add the null byte if necessary
        //
        if (bAddNullByte) {
            buf[startOffset++] = (byte) 0;
            --intSz;
        }

        //
        // mask off and store the values
        //
        mask = 0xff000000L;
        while (intSz-- > 0) {
            byte b = (byte) ((asnUInt32 & mask) >>> 24);
            buf[startOffset++] = b;
            asnUInt32 = (asnUInt32 << 8);
        }

        //
        // return the result
        //
        return startOffset;
    }

    /**
     * 
     * The parseUInteger32() method is used to decode an ASN.1 32-bit unsigned
     * integer from the specified buffer.
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
    public Object[] parseUInteger32(byte[] buf, int startOffset) throws AsnDecodingException {
        //
        // parse the header first
        //
        Object[] hdrVals = parseHeader(buf, startOffset);

        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();

        //
        // check for sufficent data
        //
        if ((buf.length - startOffset) < asnLength)
            throw new AsnDecodingException("Buffer underflow error");

        //
        // check to see that we can actually decode
        // the value (must fit in integer == 32-bits)
        //
        if (asnLength > 5)
            throw new AsnDecodingException("Integer too large: cannot decode");

        //
        // check for negativity!
        //
        long asnValue = 0;
        if ((buf[startOffset] & HIGH_BIT) == HIGH_BIT)
            asnValue = -1;

        //
        // extract the information from the buffer
        //
        while (asnLength-- > 0) {
            asnValue = (asnValue << 8) | byteToLong(buf[startOffset++]);
        }

        //
        // remember this is a 32-bit number, mask off all but
        // the last 32-bits!
        //
        asnValue = (asnValue & 0xffffffffL);

        //
        // return the data!
        //
        Object[] rVals = new Object[3];
        rVals[0] = Integer.valueOf(startOffset);
        rVals[1] = asnType;
        rVals[2] = Long.valueOf(asnValue);

        return rVals;
    }

    /**
     * 
     * The buildUInteger64() method is used to encode an ASN.1 64-bit unsigned
     * integer into the specified byte buffer.
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
    public int buildUInteger64(byte[] buf, int startOffset, byte asnType, BigInteger asnUInt64) throws AsnEncodingException {
        //
        // compute the number of bits required and the
        // integer size required to represent it minimally
        //
        byte[] bytes = asnUInt64.toByteArray(); // returns 2 complement minimum
                                                // representation + sign bit!

        //
        // build the header
        //
        startOffset = buildHeader(buf, startOffset, asnType, bytes.length);

        //
        // verify the buffer length
        //
        if ((buf.length - startOffset) < bytes.length)
            throw new AsnEncodingException("Buffer overflow error");

        for (int i = 0; i < bytes.length; ++i)
            buf[startOffset++] = bytes[i];

        //
        // return the result
        //
        return startOffset;
    }

    /**
     * 
     * The parseUInteger64() method is used to decode an ASN.1 64-bit unsigned
     * integer from the specified buffer.
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
    public Object[] parseUInteger64(byte[] buf, int startOffset) throws AsnDecodingException {
        //
        // parse the header first
        //
        Object[] hdrVals = parseHeader(buf, startOffset);

        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();

        //
        // check for sufficent data
        //
        if ((buf.length - startOffset) < asnLength)
            throw new AsnDecodingException("Buffer underflow error");

        //
        // check to see that we can actually decode
        // the value (must fit in integer == 64-bits)
        //
        if (asnLength > 9)
            throw new AsnDecodingException("Integer too large: cannot decode");

        byte[] asnBuf = new byte[asnLength];
        for (int i = 0; i < asnLength; ++i)
            asnBuf[i] = buf[startOffset++];

        BigInteger asnValue = new BigInteger(asnBuf);

        //
        // return the data!
        //
        Object[] rVals = new Object[3];
        rVals[0] = Integer.valueOf(startOffset);
        rVals[1] = asnType;
        rVals[2] = asnValue;

        return rVals;
    }

    /**
     * 
     * The buildNull() method is used to encode an ASN.1 NULL value into the
     * specified byte buffer.
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
    public int buildNull(byte[] buf, int startOffset, byte asnType) throws AsnEncodingException {
        return buildHeader(buf, startOffset, asnType, 0);
    }

    /**
     * 
     * The parseNull() method is used to decode an ASN.1 Null value from the
     * specified buffer. Since there is no "null" value only the new offset and
     * ASN.1 type are returned.
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
    public Object[] parseNull(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);

        //
        // Verify the ASN.1 length == 0
        //
        if (((Integer) hdrVals[2]).intValue() != 0)
            throw new AsnDecodingException("Malformed ASN.1 Type");

        Object[] rVals = new Object[2];
        rVals[0] = hdrVals[0];
        rVals[1] = hdrVals[1];

        return rVals;
    }

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
    public int buildString(byte[] buf, int startOffset, byte asnType, byte[] opaque) throws AsnEncodingException {
        //
        // get the length of the data
        //
        int asnLength = opaque.length;

        //
        // build the header
        //
        startOffset = buildHeader(buf, startOffset, asnType, asnLength);

        //
        // check the data length verses the remaining buffer
        // and then copy the data
        //
        if ((buf.length - startOffset) < opaque.length)
            throw new AsnEncodingException("Insufficent buffer length");

        try {
            copy(opaque, // source
                 0, // source offset
                 buf, // destination
                 startOffset, // destination offset
                 opaque.length); // bytes to copy
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new AsnEncodingException("Buffer overflow error");
        }

        //
        // return the new offset
        //
        return startOffset + opaque.length;
    }

    /**
     * 
     * The parseString() method is used to decode an ASN.1 opaque string from
     * the specified buffer.
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
    public Object[] parseString(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);

        //
        // get the header values
        //
        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = ((Byte) hdrVals[1]);
        int asnLength = ((Integer) hdrVals[2]).intValue();

        //
        // verify that there is enough data to decode
        //
        if ((buf.length - startOffset) < asnLength)
            throw new AsnDecodingException("Insufficent buffer length");

        //
        // copy the data
        //
        byte[] opaque = new byte[asnLength];
        try {
            copy(buf, // source buffer
                 startOffset, // source offset
                 opaque, // destination buffer
                 0, // destination offset
                 asnLength); // number of items to copy
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new AsnDecodingException("Buffer underflow exception");
        }

        //
        // fix the return values
        //
        Object[] rVals = new Object[3];
        rVals[0] = Integer.valueOf(startOffset + asnLength);
        rVals[1] = asnType;
        rVals[2] = opaque;

        return rVals;
    }

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
    public int buildObjectId(byte[] buf, int startOffset, byte asnType, int[] oids) throws AsnEncodingException {
        if ((buf.length - startOffset) < 1)
            throw new AsnEncodingException("Buffer overflow error");

        int[] toEncode = oids;
        int begin = startOffset; // used for rotate!

        //
        // silently create an oid = ".0.0" for arrays
        // less than 2 in length
        //
        if (oids.length < 2) {
            toEncode = new int[2];
            toEncode[0] = 0;
            toEncode[1] = 0;
        }

        //
        // verify that it is a valid object id!
        //
        if (toEncode[0] < 0 || toEncode[0] > 2)
            throw new AsnEncodingException("Invalid Object Identifier");

        if (toEncode[1] < 0 || toEncode[1] > 40)
            throw new AsnEncodingException("Invalid Object Identifier");

        //
        // add the first oid!
        //
        buf[startOffset++] = (byte) (toEncode[0] * 40 + toEncode[1]);
        int oidNdx = 2;

        //
        // encode the remainder
        //
        while (oidNdx < toEncode.length) {
            //
            // get the next object id
            //
            int oid = toEncode[oidNdx++];

            //
            // encode it
            //
            if (oid >= 0 && oid < 127) {
                if ((buf.length - startOffset) < 1)
                    throw new AsnEncodingException("Buffer overflow error");

                buf[startOffset++] = (byte) oid;
            } else // oid >= 127
            {
                int mask = 0, bits = 0; // avoids compiler whining!
                int tmask = 0, tbits = 0; // even if it may be right ;)

                //
                // figure out the number of bits required
                //
                tmask = 0x7f;
                tbits = 0;
                while (tmask != 0) {
                    if ((oid & tmask) != 0) {
                        mask = tmask;
                        bits = tbits;
                    }
                    tmask <<= 7;
                    tbits += 7;
                }

                while (mask != 0x7f) {
                    if ((buf.length - startOffset) < 1)
                        throw new AsnEncodingException("Buffer overflow error");

                    buf[startOffset++] = (byte) (((oid & mask) >>> bits) | HIGH_BIT);

                    mask = (mask >>> 7);
                    bits -= 7;

                    //
                    // fix an off-shift mask (4 bits --> 7 bits)
                    //
                    if (mask == 0x01e00000)
                        mask = 0x0fe00000;
                }

                //
                // add the last byte
                //
                if ((buf.length - startOffset) < 1)
                    throw new AsnEncodingException("Insufficent buffer space");

                buf[startOffset++] = (byte) (oid & mask);

            } // end else
        } // end while oids!

        //
        // mark the "pivot" of the rotation
        //
        int pivot = startOffset;

        //
        // encode the length
        //
        int asnLength = pivot - begin;
        int end = buildHeader(buf, pivot, asnType, asnLength);

        //
        // rotate the bytes around
        //
        try {
            rotate(buf, begin, pivot, end);
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new AsnEncodingException("Insufficent buffer space");
        }

        return end;
    }

    /**
     * 
     * The parseObjectId() method is used to decode an ASN.1 Object Identifer
     * from the specified buffer.
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
    public Object[] parseObjectId(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);

        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();

        //
        // check for sufficent data
        //
        if ((buf.length - startOffset) < asnLength)
            throw new AsnDecodingException("Buffer underflow error");

        //
        // if the length is zero then
        // silently create a ".0.0" object
        // id and return it!
        //
        if (asnLength == 0) {
            int[] ids = new int[2];
            ids[0] = ids[1] = 0;

            Object[] rVals = new Object[3];
            rVals[0] = Integer.valueOf(startOffset);
            rVals[1] = asnType;
            rVals[2] = ids;

            return rVals;
        }

        //
        // build a large buffer for the moment.
        // definately may need to srink the buffer
        // Use asnLength + 1 since the first byte
        // encode's two object ids
        //
        int idsOff = 0;
        int[] ids = new int[asnLength + 1];

        //
        // decode the first byte
        //
        {
            --asnLength;
            int oid = byteToInt(buf[startOffset++]);
            ids[idsOff++] = oid / 40;
            ids[idsOff++] = oid % 40;
        }

        //
        // decode the rest of the identifiers
        //
        while (asnLength > 0) {
            int oid = 0;
            boolean done = false;
            do {
                --asnLength;
                byte b = buf[startOffset++];
                oid = (oid << 7) | (int) (b & 0x7f);

                if ((b & HIGH_BIT) == 0)
                    done = true;
            } while (!done);
            ids[idsOff++] = oid;
        }

        //
        // now perpare the return value
        //
        int[] retOids;
        if (idsOff == ids.length) {
            retOids = ids;
        } else {
            retOids = new int[idsOff];
            copy(ids, // source
                 0, // source offset
                 retOids, // destination
                 0, // destination offset
                 idsOff); // number of items to copy
        }

        //
        // build the return objects
        //
        Object[] rVals = new Object[3];
        rVals[0] = Integer.valueOf(startOffset);
        rVals[1] = asnType;
        rVals[2] = retOids;

        return rVals;
    }

} // end class!
