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
// 2006 Oct 30: Convert to use Java 5 generics - dj@opennms.org
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

package org.opennms.core.utils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience class for retrieving the lowest, highest IP address in a given
 * list.
 *
 * @author ranger
 * @version $Id: $
 */
public class IPSorter extends Object {
    /**
     * <P>
     * Converts an 8-bit byte to a 64-bit long integer. If the quantity is a
     * sign extended negative number then the value of 256 is added to wrap the
     * conversion into the range of [0..255].
     * </P>
     * 
     * @param b
     *            The byte to convert
     * 
     * @return The converted 64-bit unsigned value.
     */
    static long byteToLong(byte b) {
        long r = (long) b;
        if (r < 0)
            r += 256;
        return r;
    }

    /**
     * <P>
     * The convertToLong method takes an array of bytes and shifts them into a
     * long value. The bytes at the front of the array are shifted into the MSB
     * of the long as each new byte is added to the LSB of the long. if the
     * array is of sufficent size the first bytes of the array may be shifted
     * out of the returned long.
     * </P>
     *
     * @param addr
     *            The array to convert to a long.
     * @return The created long value.
     * @exception IllegalArgumentException
     *                Thrown if the addr parameter is null.
     */
    public static long convertToLong(byte[] addr) {
        if (addr == null)
            throw new IllegalArgumentException("The passed array must not be null");

        long address = 0;
        for (int i = 0; i < addr.length; i++) {
            address <<= 8;
            address |= byteToLong(addr[i]);
        }
        return address;
    }

    /**
     * This method is used to convert a dotted decimal IP address composed of
     * four octets into a long value. The long is returned in network byte order
     * so that it can be compared using simple equality operators.
     *
     * @param ipAddressString
     *            The dotted decimal address string to convert
     * @return The created long value
     * @exception IllegalArgumentException
     *                Thrown if the ipAddressString parameter is null.
     */
    public static long convertToLong(String ipAddressString) {
        long result = 0;
        int octet = 0;
        byte[] buf = ipAddressString.getBytes();

        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == '.') {
                result = (result << 8) | octet;
                octet = 0;
            } else {
                // 48 is decimal for the character '0'
                octet = octet * 10 + (buf[i] - 48);
            }
        }
        return (result << 8) | (long) octet;
    }

    /**
     * Given a list of IP addresses, return the lowest as determined by the
     * numeric representation and not the alphanumeric string.
     *
     * @param addresses an array of {@link java.net.InetAddress} objects.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getLowestInetAddress(InetAddress[] addresses) {
        if (addresses == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return getLowestInetAddress(Arrays.asList(addresses));
    }

    /**
     * Given a list of IP addresses, return the lowest as determined by the
     * numeric representation and not the alphanumeric string.
     *
     * @param addresses a {@link java.util.List} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getLowestInetAddress(List<InetAddress> addresses) {
        if (addresses == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        InetAddress lowest = null;
        long lowestLong = Long.MAX_VALUE;
        for (InetAddress temp : addresses) {
            long tempLong = convertToLong(temp.getAddress());

            if (tempLong < lowestLong) {
                lowestLong = tempLong;
                lowest = temp;
            }
        }

        return lowest;
    }

}
