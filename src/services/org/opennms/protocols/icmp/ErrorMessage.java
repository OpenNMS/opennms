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

import org.opennms.protocols.ip.IPHeader;

/**
 * Defines the default error handler object for processing ICMP error messages.
 * All error messages follow the same format. The first 8 bytes is the ICMP
 * header. Immediantly after the ICMP header is the IP packet in error,
 * including any option data. After the IP header is the first 8 bytes of
 * protocol data. This is enough to hold a UDP header or the first 8 bytes of a
 * TCP header.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class ErrorMessage extends ICMPHeader {
    private IPHeader m_iphdr;

    private byte[] m_protoData;

    /**
     * Creates a new ICMP Error Message object.
     * 
     * @param type
     *            The ICMP type.
     * @param code
     *            The specific code for the message.
     * 
     */
    protected ErrorMessage(byte type, byte code) {
        super(type, code);
        m_iphdr = null;
        m_protoData = null;
    }

    /**
     * Creates a new ICMP timestamp reply from the spcified data at the specific
     * offset.
     * 
     * @param buf
     *            The buffer containing the data.
     * @param offset
     *            The start of the icmp data.
     * 
     * @exception java.lang.IndexOutOfBoundsException
     *                Thrown if there is not sufficent data in the buffer.
     */
    public ErrorMessage(byte[] buf, int offset) {
        super();
        loadFromBuffer(buf, offset);
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
     */
    public final int loadFromBuffer(byte[] buf, int offset) {
        //
        // minimum length
        //
        if (buf.length < (offset + 36))
            throw new IndexOutOfBoundsException("Insufficient data to load ICMP error message");

        offset = super.loadFromBuffer(buf, offset);

        //
        // create the header
        //
        m_iphdr = new IPHeader(buf, offset);

        //
        // check the minimum length again
        //
        if (buf.length < (offset + 16 + m_iphdr.getHeaderLength()))
            throw new IndexOutOfBoundsException("Insufficient data to load ICMP error message");

        offset += m_iphdr.getHeaderLength();

        //
        // load the extra 8-bytes
        //
        m_protoData = new byte[8];
        for (int x = 0; x < 8; x++) {
            m_protoData[x] = buf[offset++];
        }

        return offset;
    }

    /**
     * Used to access the IP Header that caused the ICMP error message to be
     * generated.
     * 
     * @return The IP Header in error.
     * 
     */
    public final IPHeader getIPHeader() {
        return m_iphdr;
    }

    /**
     * Sets the IP header in error.
     * 
     * @param hdr
     *            The IP header in error
     */
    protected void setIPHeader(IPHeader hdr) {
        m_iphdr = hdr;
    }

    /**
     * Retreives the 8 bytes of protocol data that caused the error.
     * 
     * @return The first 8 bytes of the packet in error.
     * 
     */
    public final byte[] getProtocolData() {
        return m_protoData;
    }

    /**
     * Sets the protocol data that caused the error.
     * 
     * @param pd
     *            The 8 bytes of protocol data.
     * 
     * @exception java.lang.IndexOutOfBoundsException
     *                Thrown when pd.length is less than 8.
     * @exception java.lang.IllegalArgumentException
     *                Thrown when pd.length is greater than 8.
     * 
     */
    protected void setProtocolData(byte[] pd) {
        if (pd.length < 8)
            throw new IndexOutOfBoundsException("Protocol data must be 8 bytes in length");

        if (pd.length > 8)
            throw new IllegalArgumentException("Protocol data cannot be more than 8 bytes in length");

        m_protoData = pd;
    }
}
