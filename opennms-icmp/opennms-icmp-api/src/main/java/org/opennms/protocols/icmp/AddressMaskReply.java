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
// Modifications:
//
// 2003 Mar 05: Changes to support response times and more platforms.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.protocols.icmp;

import org.opennms.protocols.ip.OC16ChecksumProducer;

/**
 * This is the implementation of an ICMP Address Mask Reply object. The object
 * can be stored in a buffer to send or loaded from a received buffer. The class
 * is marked final since it is not intended to be extended.
 * 
 * @author Brian Weaver <weave@oculan.com>
 * 
 */
public final class AddressMaskReply extends ICMPHeader {
    /**
     * The address mask
     */
    private int m_mask;

    /**
     * Creates a new ICMP Address Mask Request object.
     * 
     */
    public AddressMaskReply() {
        super(ICMPHeader.TYPE_ADDRESS_MASK_REPLY, (byte) 0);
        m_mask = 0;
    }

    /**
     * Creates a new ICMP Address mask reply from the spcified data at the
     * specific offset.
     * 
     * @param buf
     *            The buffer containing the data.
     * @param offset
     *            The start of the icmp data.
     * 
     * @exception java.lang.IndexOutOfBoundsException
     *                Thrown if there is not sufficent data in the buffer.
     * @exception java.lang.IllegalArgumentException
     *                Thrown if the ICMP type is not an Address Mask reply.
     */
    public AddressMaskReply(byte[] buf, int offset) {
        super();
        loadFromBuffer(buf, offset);
    }

    /**
     * Computes the ones compliment 16-bit checksum for the ICMP message.
     * 
     */
    public final void computeChecksum() {
        OC16ChecksumProducer summer = new OC16ChecksumProducer();
        super.computeChecksum(summer);

        summer.add(m_mask);
        setChecksum(summer.getChecksum());
    }

    /**
     * Writes the ICMP address mask reply out to the specified buffer at the
     * starting offset. If the buffer does not have sufficent data to store the
     * information then an IndexOutOfBoundsException is thrown.
     * 
     * @param buf
     *            The storage buffer.
     * @param offset
     *            The location to start in buf.
     * 
     * @return The new offset after storing to the buffer.
     * 
     * @exception java.lang.IndexOutOfBoundsException
     *                Thrown if the buffer does not have enough storage space.
     * 
     */
    public final int storeToBuffer(byte[] buf, int offset) {
        if (buf.length < (offset + 12))
            throw new IndexOutOfBoundsException("Array index overflow in buffer build");

        computeChecksum();
        offset = super.storeToBuffer(buf, offset);

        //
        // add in the 32-bit zero mask
        //
        buf[offset++] = (byte) ((m_mask >> 24) & 0xff);
        buf[offset++] = (byte) ((m_mask >> 16) & 0xff);
        buf[offset++] = (byte) ((m_mask >> 8) & 0xff);
        buf[offset++] = (byte) (m_mask & 0xff);

        return offset;
    }

    /**
     * Reads the ICMP Address Mask Reqeust from the specified buffer and sets
     * the internal fields equal to the data. If the buffer does not have
     * sufficent data to restore the header then an IndexOutOfBoundsException is
     * thrown by the method. If the buffer does not contain an address mask
     * reqeust then an IllegalArgumentException is thrown.
     * 
     * @param buf
     *            The buffer to read the data from.
     * @param offset
     *            The offset to start reading data.
     * 
     * @return The new offset after reading the data.
     * 
     * @exception java.lang.IndexOutOfBoundsException
     *                Thrown if there is not sufficent data in the buffer.
     * @exception java.lang.IllegalArgumentException
     *                Thrown if the ICMP type is not an Address Mask reply.
     */
    public final int loadFromBuffer(byte[] buf, int offset) {
        if (buf.length < (offset + 12))
            throw new IndexOutOfBoundsException("Insufficient data to load ICMP header");

        offset = super.loadFromBuffer(buf, offset);

        if (getType() != TYPE_ADDRESS_MASK_REPLY)
            throw new IllegalArgumentException("The buffer did not contain an Address Mask Reply");

        //
        // get the mask
        //
        m_mask = (byteToInt(buf[offset++]) << 24) | (byteToInt(buf[offset++]) << 16) | (byteToInt(buf[offset++]) << 8) | byteToInt(buf[offset++]);

        return offset;
    }

    /**
     * Used to get the IPv4 32-bit address mask.
     */
    public final int getAddressMask() {
        return m_mask;
    }

    /**
     * Used to set the IPv4 32-bit address mask.
     */
    public final void setAddressMask(int mask) {
        m_mask = mask;
    }

    /**
     * Converts the object to a stream of bytes.
     */
    public final byte[] toBytes() {
        byte[] buf = new byte[12];
        storeToBuffer(buf, 0);
        return buf;
    }
}
