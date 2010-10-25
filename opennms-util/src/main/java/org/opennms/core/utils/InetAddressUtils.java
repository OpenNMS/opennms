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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;

/**
 * <p>Abstract InetAddressUtils class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
abstract public class InetAddressUtils {

    /**
     * <p>getInetAddress</p>
     *
     * @param ipAddrOctets an array of byte.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getInetAddress(byte[] ipAddrOctets) {
        try {
            return InetAddress.getByAddress(ipAddrOctets);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress "+ipAddrOctets+" with length "+ipAddrOctets.length);
        }
        
    }

    /**
     * <p>getInetAddress</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getInetAddress(String dottedNotation) {
        try {
            return InetAddress.getByName(dottedNotation);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress "+dottedNotation);
        }
    }

    /**
     * <p>getInetAddress</p>
     *
     * @param ipAddrAs32bitNumber a long.
     * @return a {@link java.net.InetAddress} object.
     * @deprecated Dealing with IP addresses as 'long' type is not compatible with IPv6
     */
    public static InetAddress getInetAddress(long ipAddrAs32bitNumber) {
        return getInetAddress(toIpAddrBytes(ipAddrAs32bitNumber));
    }
    
    /**
     * <p>toIpAddrBytes</p>
     *
     * @param address a long.
     * @return an array of byte.
     * @deprecated Dealing with IP addresses as 'long' type is not compatible with IPv6
     */
    public static byte[] toIpAddrBytes(long address) {
    
        byte[] octets = new byte[4];
        octets[0] = ((byte) ((address >>> 24) & 0xff));
        octets[1] = ((byte) ((address >>> 16) & 0xff));
        octets[2] = ((byte) ((address >>> 8) & 0xff));
        octets[3] = ((byte) (address & 0xff));
        
        return octets;
    }
    
    /**
     * <p>toIpAddrBytes</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return an array of byte.
     */
    public static byte[] toIpAddrBytes(String dottedNotation) {
        return getInetAddress(dottedNotation).getAddress();
    }

    /**
     * <p>toIpAddrBytes</p>
     *
     * @param addr a {@link java.net.InetAddress} object.
     * @return an array of byte.
     */
    public static byte[] toIpAddrBytes(InetAddress addr) {
        return addr.getAddress();
    }
    
    /**
     * <p>toIpAddrLong</p>
     *
     * @param address an array of byte.
     * @return a long.
     * @deprecated Dealing with IP addresses as 'long' type is not compatible with IPv6
     */
    public static long toIpAddrLong(byte[] address) {
        if (address.length != 4) {
            throw new IllegalArgumentException("address "+address+" has the wrong length "+address.length);
        }
        long[] octets = new long[address.length];
        octets[0] = unsignedByteToLong(address[0]);
        octets[1] = unsignedByteToLong(address[1]);
        octets[2] = unsignedByteToLong(address[2]);
        octets[3] = unsignedByteToLong(address[3]);
        
        long result = octets[0] << 24 
            | octets[1] << 16
            | octets[2] << 8
            | octets[3];
        
        return result;
        
    }
    
    /**
     * <p>toIpAddrLong</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return a long.
     * @deprecated Dealing with IP addresses as 'long' type is not compatible with IPv6
     */
    public static long toIpAddrLong(String dottedNotation) {
        return toIpAddrLong(toIpAddrBytes(dottedNotation));
    }
    
    /**
     * <p>toIpAddrLong</p>
     *
     * @param addr a {@link java.net.InetAddress} object.
     * @return a long.
     * @deprecated Dealing with IP addresses as 'long' type is not compatible with IPv6
     */
    public static long toIpAddrLong(InetAddress addr) {
        return toIpAddrLong(addr.getAddress());
    }

    /*
    public class InetAddressByteArrayComparator implements Comparator<byte[]> {

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

    }
    */

    
    private static long unsignedByteToLong(byte b) {
        return b < 0 ? ((long)b)+256 : ((long)b);
    }

    /**
     * <p>toIpAddrString</p>
     *
     * @param ipAddr a long.
     * @return a {@link java.lang.String} object.
     * @deprecated Dealing with IP addresses as 'long' type is not compatible with IPv6
     */
    public static String toIpAddrString(long ipAddr) {
        return getInetAddress(ipAddr).getHostAddress();
    }
    
    /**
     * <p>toIpAddrString</p>
     *
     * @param addr an array of byte.
     * @return a {@link java.lang.String} object.
     */
    public static String toIpAddrString(byte[] addr) {
        return getInetAddress(addr).getHostAddress();
    }
    
    

}
