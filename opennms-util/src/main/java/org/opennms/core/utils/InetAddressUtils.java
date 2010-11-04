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

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * <p>Abstract InetAddressUtils class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
abstract public class InetAddressUtils {

    public static String incr(String address) throws UnknownHostException {
        return InetAddressUtils.toIpAddrString(incr(InetAddressUtils.toIpAddrBytes(address)));
    }

    public static byte[] incr(byte[] address) throws UnknownHostException {
        BigInteger addr = new BigInteger(1, address);
        addr = addr.add(new BigInteger("1"));
        return convertBigIntegerIntoInetAddress(addr).getAddress();
    }

    public static String decr(String address) throws UnknownHostException {
        return InetAddressUtils.toIpAddrString(decr(InetAddressUtils.toIpAddrBytes(address)));
    }

    public static byte[] decr(byte[] address) throws UnknownHostException {
        BigInteger addr = new BigInteger(1, address);
        addr = addr.subtract(new BigInteger("1"));
        return convertBigIntegerIntoInetAddress(addr).getAddress();
    }

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
     * <p>toIpAddrBytes</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return an array of byte.
     */
    public static byte[] toIpAddrBytes(String dottedNotation) {
        return getInetAddress(dottedNotation).getAddress();
    }

    private static long unsignedByteToLong(byte b) {
        return b < 0 ? ((long)b)+256 : ((long)b);
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
        byte[] lowestBytes = new byte[] { 0, 0, 0, 0 };
        ByteArrayComparator comparator = new ByteArrayComparator();
        for (InetAddress temp : addresses) {
            byte[] tempBytes = temp.getAddress();
    
            if (comparator.compare(tempBytes, lowestBytes) < 0) {
                lowestBytes = tempBytes;
                lowest = temp;
            }
        }
    
        return lowest;
    }

    public static BigInteger difference(String addr1, String addr2) {
        return new BigInteger(getInetAddress(addr1).getAddress()).subtract(new BigInteger(getInetAddress(addr2).getAddress()));
    }

    public static boolean isInetAddressInRange(final String addrString, final String beginString, final String endString) {
        final ByteArrayComparator comparator = new ByteArrayComparator();
        final byte[] addr = InetAddressUtils.toIpAddrBytes(addrString);
        final byte[] begin = InetAddressUtils.toIpAddrBytes(beginString);
        if (comparator.compare(addr, begin) > 0) {
            final byte[] end = InetAddressUtils.toIpAddrBytes(endString);
            if (comparator.compare(addr, end) <= 0) {
                return true;
            } else { 
                return false;
            }
        } else if (comparator.compare(addr, begin) == 0) {
            return true;
        } else { 
            return false;
        }
    }

    public static boolean isInetAddressInRange(final byte[] addr, final byte[] begin, final byte[] end) {
        final ByteArrayComparator comparator = new ByteArrayComparator();
        if (comparator.compare(addr, begin) > 0) {
            if (comparator.compare(addr, end) <= 0) {
                return true;
            } else { 
                return false;
            }
        } else if (comparator.compare(addr, begin) == 0) {
            return true;
        } else { 
            return false;
        }
    }

    public static InetAddress convertBigIntegerIntoInetAddress(BigInteger i) throws UnknownHostException {
        if (i.compareTo(new BigInteger("0")) < 0) {
            throw new IllegalArgumentException("BigInteger is negative, cannot convert into an IP address: " + i.toString());
        } else {
            // Note: This function will return the two's complement byte array so there will always
            // be a bit of value '0' (indicating positive sign) at the first position of the array
            // and it will be padded to the byte boundry. For example:
            //
            // 255.255.255.255 => 00 FF FF FF FF (5 bytes)
            // 127.0.0.1 => 0F 00 00 01 (4 bytes)
            //
            byte[] bytes = i.toByteArray();

            if (bytes.length == 0) {
                return InetAddress.getByAddress(new byte[] {0, 0, 0, 0});
            } else if (bytes.length <= 4) {
                // This case covers an IPv4 address with the most significant bit of zero (the MSB
                // will be used as the two's complement sign bit)
                byte[] addressBytes = new byte[4];
                int k = 3;
                for (int j = bytes.length - 1; j >= 0; j--, k--) {
                    addressBytes[k] = bytes[j];
                }
                return InetAddress.getByAddress(addressBytes);
            } else if (bytes.length <= 5 && bytes[0] == 0) {
                // This case covers an IPv4 address (4 bytes + two's complement sign bit of zero)
                byte[] addressBytes = new byte[4];
                int k = 3;
                for (int j = bytes.length - 1; j >= 1; j--, k--) {
                    addressBytes[k] = bytes[j];
                }
                return InetAddress.getByAddress(addressBytes);
            } else if (bytes.length <= 16) {
                // This case covers an IPv6 address with the most significant bit of zero (the MSB
                // will be used as the two's complement sign bit)
                byte[] addressBytes = new byte[16];
                int k = 15;
                for (int j = bytes.length - 1; j >= 0; j--, k--) {
                    addressBytes[k] = bytes[j];
                }
                return InetAddress.getByAddress(addressBytes);
            } else if (bytes.length <= 17 && bytes[0] == 0) {
                // This case covers an IPv6 address (16 bytes + two's complement sign bit of zero)
                byte[] addressBytes = new byte[16];
                int k = 15;
                for (int j = bytes.length - 1; j >= 1; j--, k--) {
                    addressBytes[k] = bytes[j];
                }
                return InetAddress.getByAddress(addressBytes);
            } else {
                throw new IllegalArgumentException("BigInteger is too large to convert into an IP address: " + i.toString());
            }
        }
    }
}
