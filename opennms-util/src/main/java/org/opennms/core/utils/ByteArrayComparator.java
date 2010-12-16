/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 7, 2007
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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

    public int compare(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else {
            // Make shorter byte arrays "less than" longer arrays
            int comparison = new Integer(a.length).compareTo(new Integer(b.length));
            if (comparison == 0) {
                // Compare byte-by-byte
                for (int i = 0; i < a.length; i++) {
                    int byteComparison = new Long(unsignedByteToLong(a[i])).compareTo(new Long(unsignedByteToLong(b[i])));
                    if (byteComparison == 0) {
                        continue;
                    } else {
                        return byteComparison;
                    }
                }
                // OK both arrays are the same length and every byte is identical so they are equal
                return 0;
            } else {
                return comparison;
            }
        }
    }

    private static long unsignedByteToLong(byte b) {
        return b < 0 ? ((long)b)+256 : ((long)b);
    }
}