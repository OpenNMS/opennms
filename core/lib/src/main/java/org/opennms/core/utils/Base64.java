/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

/**
 * Performs base 64 encoding and decoding on byte arrays.
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 */
public final class Base64 {
    /**
     * <P>
     * The base64 encoding map. Using 6-bit values it is possible to map 24-bits
     * into 4 characters. If there are not sufficent amount of bits to makeup
     * six then it is padded with BASE64_PAD.
     * </P>
     */
    private static final char[] BASE64_CHARS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    // 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
    private static final byte[] BASE64_VALUES = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    /* 16 - 31 */-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    /* 32 - 47 */-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
    /* 48 - 63 */52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, 0, -1, -1,
    /* 64 - 79 */-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
    /* 80 - 95 */15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
    /* 96 - 111 */-1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    /* 112 - 127 */41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };

    /**
     * The base64 padding character
     */
    private static final char BASE64_PAD = '=';

    /**
     * <P>
     * Encodes the passed byte array using the base64 rules. The base64 encoding
     * schema is performed by grouping the bytes in to 6-bit quantities and then
     * encoding them.
     * </P>
     *
     * <P>
     * For more information see RFC1341 for the format used for base64 encoding.
     * </P>
     *
     * @param data
     *            The input byte array
     * @return The converted data in a character stream.
     */
    public static char[] encodeBase64(byte[] data) {
        int destlen = ((data.length + 2) / 3) * 4;
        char[] dest = new char[destlen];
        int destndx = 0;

        for (int i = 0; i < data.length; i += 3) {
            int quantum = 0;
            int pad = 0;
            int bytes = data.length - i;

            if (bytes >= 1) {
                quantum = (data[i] < 0 ? (256 + (int) data[i]) : (int) data[i]);
                pad = 2;
            }

            quantum <<= 8;
            if (bytes >= 2) {
                quantum |= (data[i + 1] < 0 ? (256 + (int) data[i + 1]) : (int) data[i + 1]);
                pad = 1;
            }

            quantum <<= 8;
            if (bytes > 2) {
                quantum |= (data[i + 2] < 0 ? (256 + (int) data[i + 2]) : (int) data[i + 2]);
                pad = 0;
            }

            for (int j = 3; j >= pad; j--) {
                int ndx = (quantum >> (j * 6)) & 0x3f;
                dest[destndx++] = BASE64_CHARS[ndx];
            }

            for (int j = pad; j > 0; j--) {
                dest[destndx++] = BASE64_PAD;
            }
        }
        return dest;
    }

    /**
     * <P>
     * Decodes a character array into the corresponding byte array. The buffer
     * must be an intergral number of 4 character. I.E. size mod 4 is equal to
     * zero or an exception will be thrown. Likewise, if there is an invalid
     * character in the input array then an exception will be thrown.
     * </P>
     *
     * @param data
     *            The data stream to be filtered.
     * @return The coverted array of bytes.
     * @exception java.lang.IllegalArgumentException
     *                Thrown if an invalid buffer that cannot be decoded is
     *                passed.
     */
    public static byte[] decodeBase64(char[] data) {
        //. If the data is zero length just return a zero length byte array
        if (data.length == 0) {
            return new byte[0];
        }
        //
        // check the length, it must be an integral number of 4 characters.
        //
        if ((data.length % 4) != 0)
            throw new IllegalArgumentException("Invalid base64 encoding, improper length");

        //
        // get the raw length and check for
        // the appended padding characters
        // if any.
        //
        int rawlen = (data.length / 4) * 3;
        for (int i = 1; i <= 2; i++) {
            if (data[data.length - i] == BASE64_PAD)
                --rawlen;
        }

        //
        // allocate the new buffer
        //
        byte[] rawdata = new byte[rawlen];
        int rawndx = 0;

        //
        // convert the character array into
        // a byte array.
        //
        int quantum = 0;
        for (int i = 0; i < data.length; i++) {
            if ((i % 4) == 0 && i > 0) {
                int c = ((quantum >> 16) & 0xff);
                rawdata[rawndx++] = (byte) (c > 127 ? c - 256 : c);

                c = ((quantum >> 8) & 0xff);
                rawdata[rawndx++] = (byte) (c > 127 ? c - 256 : c);

                c = quantum & 0xff;
                rawdata[rawndx++] = (byte) (c > 127 ? c - 256 : c);

                quantum = 0;
            }
            quantum <<= 6;

            char c = data[i];
            if ((int) c >= BASE64_VALUES.length || BASE64_VALUES[(int) c] == -1)
                throw new IllegalArgumentException("Invalid character in decode stream");

            quantum |= BASE64_VALUES[(int) c];
        }

        //
        // hand the last byte(s) of data
        //
        int c = ((quantum >> 16) & 0xff);
        rawdata[rawndx++] = (byte) (c > 127 ? c - 256 : c);

        if (rawndx < rawlen) {
            c = ((quantum >> 8) & 0xff);
            rawdata[rawndx++] = (byte) (c > 127 ? c - 256 : c);
        }
        if (rawndx < rawlen) {
            c = quantum & 0xff;
            rawdata[rawndx++] = (byte) (c > 127 ? c - 256 : c);
        }

        //
        // return the raw data
        //
        return rawdata;
    }

    /** Empty, private constructor so this object will not be instantiated. */
    private Base64() {
    }

}
