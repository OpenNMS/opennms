/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.util.Comparator;

/**
 * Comparator that is used to compare byte arrays. This should be used to compare
 * IP addresses using {@link java.net.InetAddress#getAddress()} and can be used to
 * compare any pair of IPv4 and/or IPv6 addresses.
 * 
 * @author Seth <seth@opennms.org>
 */
public class ByteArrayComparator implements Comparator<byte[]> {

    @Override
    public int compare(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else {
            // Make shorter byte arrays "less than" longer arrays
            int comparison = Integer.valueOf(a.length).compareTo(Integer.valueOf(b.length));
            if (comparison != 0) {
                return comparison;
            } else {
                // Compare byte-by-byte
                for (int i = 0; i < a.length; i++) {
                    int byteComparison = Integer.valueOf(unsignedByteToInt(a[i])).compareTo(Integer.valueOf(unsignedByteToInt(b[i])));
                    if (byteComparison != 0) {
                        return byteComparison;
                    }
                }
                // OK both arrays are the same length and every byte is identical so they are equal
                return 0;
            }
        }
    }

    private static int unsignedByteToInt(byte b) {
        return b < 0 ? ((int)b)+256 : ((int)b);
    }
}