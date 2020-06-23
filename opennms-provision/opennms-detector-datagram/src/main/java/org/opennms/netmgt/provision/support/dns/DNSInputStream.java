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

package org.opennms.netmgt.provision.support.dns;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * <P>
 * DNSInputStrean extends a ByteArrayInputStream and has methods to decode the
 * data of a DNS response to an address request.
 * </P>
 */
public class DNSInputStream extends ByteArrayInputStream {
    /**
     * <P>
     * Constructs a new input stream for decoding DNS records.
     * </P>
     *
     * @param data
     *            The array of data to pass to the base class.
     */
    public DNSInputStream(final byte[] data) {
        super(data);
    }

    /**
     * <P>
     * Constructs a DNSInputStream object from the byte array.
     * </P>
     *
     * @param data
     *            byte array containing the response data
     * @param off
     *            offset of the data in the byte array
     * @param len
     *            length of the byte array
     */
    public DNSInputStream(final byte[] data, final int off, final int len) {
        super(data, off, len);
    }

    /**
     * <P>
     * Read a byte off the input stream.
     * </P>
     *
     * @return The integer read.
     * @exception java.io.IOException
     *                Thrown if the end-of-file is encountered trying to read
     *                the next byte.
     * @throws java.io.IOException if any.
     */
    public int readByte() throws IOException {
        final int rc = read();
        if (rc == -1)
            throw new EOFException("end of buffer on read");
        return rc;
    }

    /**
     * <P>
     * Read a 'short' off the input stream.
     * </P>
     *
     * @return The short from the input stream.
     * @exception java.io.IOException
     *                Thrown if the end-of-file is encountered trying to read
     *                the next short.
     * @throws java.io.IOException if any.
     */
    public int readShort() throws IOException {
        return (readByte() << 8 | readByte());
    }

    /**
     * <P>
     * Read an 'int' off the input stream.
     * </P>
     *
     * @return The int from the input stream.
     * @exception java.io.IOException
     *                Thrown if there is an error while read.
     * @throws java.io.IOException if any.
     */
    public long readInt() throws IOException {
        long rc = 0;
        for (int i = 0; i < 4; i++)
            rc = (rc << 8) | readByte();
        return rc;
    }

    /**
     * <P>
     * Read a 'string' off the input stream.
     * </P>
     *
     * @return the string from the input stream
     * @exception java.io.IOException
     *                Thrown if there is an error while read
     * @throws java.io.IOException if any.
     */
    public String readString() throws IOException {
        final int len = readByte();
        if (len == 0) {
            return "";
        }

        final byte[] buffer = new byte[len];
        final int rc = read(buffer);
        if (rc == -1 || rc != len) throw new EOFException("end of file while reading array");

        return new String(buffer);
    }

    /**
     * <P>
     * The readDomainName method is used to read an entire domain name from the
     * stream. The string returned will be the concatentation of several
     * substrings, each substring in the record is separated by a '.'(dot). For
     * more information see the RFC for the distributed name service.
     * </P>
     *
     * @return The domain name string.
     * @exception java.io.IOException
     *                Thrown if an error occurs decoding the string from the
     *                stream.
     * @throws java.io.IOException if any.
     */
    public String readDomainName() throws IOException {
        //
        // Check to make sure that we are not going
        // to index the array and generate an bounds
        // exception.
        //
        if (pos >= count)
            throw new EOFException("EOF reading domain name");

        //
        // check the length byte. If the upper two bits are
        // not set then it is a normal string and can be
        // decoded using the readString() method.
        //
        if ((buf[pos] & 0xc0) == 0) {
            String label = readString();
            if (label.length() > 0) {
                //
                // get the next component(s) of the
                // domain name. This is a recursive call.
                // If the length is not equal to null then
                // append the tail and return the new domain
                // name.
                //
                final String tail = readDomainName();
                if (tail.length() > 0)
                    label = label + '.' + tail;
            }

            return label;
        }

        //
        // If this point in the code is reached then
        // compression is being used!
        //

        //
        // If the upper two bits were set then this is a special
        // encoding that points us to somewhere else in the record!
        // We have to read that part of the record and to get the
        // next element in the stream.
        // 
        // Throw an I/O exception if the compression offset is
        // malformed.
        //
        if ((buf[pos] & 0xc0) != 0xc0) throw new IOException("Invalid domain name compression offset");

        //
        // read the short that is the pointer to the other
        // part of the stream. Note buf is a protected buffer.
        //
        final int offset = readShort() & 0x3fff;
        final DNSInputStream dnsIn = new DNSInputStream(buf, offset, buf.length - offset);
        try {
            return dnsIn.readDomainName();
        } finally {
            dnsIn.close();
        }
    }

    /**
     * <P>
     * Reads the resource record from the input stream.
     * </P>
     *
     * @return The DNSAddressRR that is in response to the address request.
     * @exception java.io.IOException
     *                Thrown if data does not decode to a DNSAddressRRl.
     * @throws java.io.IOException if any.
     */
    public DNSAddressRR readRR() throws IOException {
        final String rrName = readDomainName();
        final int rrType = readShort();
        final int rrClass = readShort();
        final long rrTTL = readInt();
        final int rrDataLen = readShort();

        //
        // Convert the length of data in this byte array input stream
        // into a "substream" of data. The only way this could get
        // complicated is if there are multiple threads using this
        // stream. If that is the case then synchronization code
        // should be used to wrap the next two lines -- Weave
        //
        final DNSInputStream rrDNSIn = new DNSInputStream(buf, pos, rrDataLen);
        pos += rrDataLen;
        try {
            return new DNSAddressRR(rrName, rrType, rrClass, rrTTL, rrDNSIn);
        } catch (Throwable ex) {
            throw new IOException("Unknown DNSAddressRR (type " + " (" + rrType + "))" + "\nOriginating Exception: " + ex.getMessage());
        } finally {
            if (rrDNSIn != null) {
                rrDNSIn.close();
            }
        }
    }
}
