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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.protocols.ip;

/**
 * Produces a one's compliment 16-bit checksum from data that is "added" to the
 * sum. The producer handles objects from 8-bits to 64-bit values.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public final class OC16ChecksumProducer extends Object {
    private int m_cksum;

    private boolean m_finalized;

    /**
     * Converts a 8-bit value to a 32-bit integer value. The conversion is done
     * in a unsigned fashion.
     * 
     * @param b
     *            The 8-bit value to convert
     * @return The converted 32-bit value
     */
    private static int byteToInt(byte b) {
        if (b < 0)
            return (int) b + 0x100;
        return (int) b;
    }

    /**
     * Converts a 16-bit value to a 32-bit value. The conversion is done in an
     * unsigned fashion.
     * 
     * @param s
     *            The 16-bit value to convert
     * @return The converted 32-bit value
     * 
     */
    private static int shortToInt(short s) {
        if (s < 0)
            return (int) s + 0x10000;
        return (int) s;
    }

    /**
     * Finalizes the result by adding the carried out bits back into the
     * checksum.
     * 
     */
    private void finalizeSum() {
        //
        // Add the high carry to the low
        //
        if ((m_cksum & 0xffff0000) != 0) {
            //
            // Add the carry out back in
            //
            m_cksum = ((m_cksum >> 16) & 0xffff) + (m_cksum & 0xffff);

            // 
            // now add back the carry one more time!
            //
            m_cksum += (m_cksum >>> 16);
        }
        m_finalized = true;
    }

    /**
     * Default constructor.
     */
    public OC16ChecksumProducer() {
        m_cksum = 0;
        m_finalized = false;
    }

    /**
     * Adds the specified 8-bit value to the checksum. Since the checksum must
     * be in 16-bits, the byte is copied to the upper 8-bits of the 16-bit
     * value. The lower 8-bits are set to zero.
     * 
     * @param b
     *            The 8-bit value to add to the checksum.
     * 
     */
    public void add(byte b) {
        add(b, (byte) 0);
    }

    /**
     * Adds the specified 8-bit values to the checksum. The checksum is
     * performed using 16-bit values, thus the first byte is places into the
     * high 8-bits of the word. The second byte is placed into the lower 8-bits
     * of the 16-bit word. Then the result is added to the 16-bit checksum.
     * 
     * @param a
     *            The high order 8-bits
     * @param b
     *            The low order 8-bits
     * 
     */
    public void add(byte a, byte b) {
        m_cksum += (byteToInt(a) << 8) | byteToInt(b);
        m_finalized = false;
    }

    /**
     * Adds the specified 16-bit value to the checksum total.
     * 
     * @param s
     *            The 16-bit value to add
     * 
     */
    public void add(short s) {
        m_cksum += shortToInt(s);
        m_finalized = false;
    }

    /**
     * Adds the specified 32-bit value to the checksum. The 32-bit value is
     * broken into two 16-bit values for the purpose of computing the checksum.
     * 
     * @param i
     *            The 32-bit value.
     * 
     */
    public void add(int i) {
        m_cksum += ((i >> 16) & 0xffff);
        m_cksum += (i & 0xffff);
        m_finalized = false;
    }

    /**
     * Adds the specified 64-bit value to the checksum. The 64-bit value is
     * split into 4 16-bit quantities and added to the checksum.
     * 
     * @param l
     *            The 64-bit value to add
     */
    public void add(long l) {
        m_cksum += ((int) (l >> 48) & 0xffff);
        m_cksum += ((int) (l >> 32) & 0xffff);
        m_cksum += ((int) (l >> 16) & 0xffff);
        m_cksum += ((int) l & 0xffff);
        m_finalized = false;
    }

    /**
     * Resets the object to a zero state.
     * 
     */
    public void reset() {
        m_cksum = 0;
        m_finalized = false;
    }

    /**
     * Returns the current checksum value that has been computed for the object.
     * 
     * @return The 16-bit ones compliment checksum
     * 
     */
    public short getChecksum() {
        if (m_finalized == false)
            finalizeSum();

        return (short) ((~m_cksum) & 0xffff);
    }
}
