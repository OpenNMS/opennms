/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.remote.JMXServiceURL;

import org.opennms.core.network.IPAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract InetAddressUtils class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public abstract class InetAddressUtils {

    private static final Logger LOG = LoggerFactory.getLogger(InetAddressUtils.class);

    public static final String INVALID_BRIDGE_ADDRESS = "000000000000";
    public static final String INVALID_STP_BRIDGE_ID  = "0000000000000000";
    public static final String INVALID_STP_BRIDGE_DESIGNATED_PORT = "0000";

    private static final ByteArrayComparator s_BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();
    public static final InetAddress UNPINGABLE_ADDRESS;
    public static final InetAddress UNPINGABLE_ADDRESS_IPV6;
    public static final InetAddress ZEROS = addr("0.0.0.0");
    public static final InetAddress TWO_FIFTY_FIVES = addr("255.255.255.255");
    public static final InetAddress ONE_TWENTY_SEVEN = addr("127.0.0.1");

    static {
        try {
            // This address (192.0.2.123) is within a range of test IPs that
            // that is guaranteed to be non-routed.
            //
            UNPINGABLE_ADDRESS = InetAddress.getByAddress(new byte[] {(byte)192, (byte)0, (byte)2, (byte)123});
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }

        try {
            // This address is within a subnet of "Unique Unicast" IPv6 addresses
            // that are defined by RFC4193. This is the IPv6 equivalent of the
            // 192.168.0.0/16 subnet and because the IPv6 address space is so large,
            // you can just randomly generate the first portion of the address. :)
            // I used an online address generator to get this particular address.
            //
            // http://www.rfc-editor.org/rfc/rfc4193.txt
            //
            UNPINGABLE_ADDRESS_IPV6 = InetAddress.getByName("fd25:28a0:ba2f:6b78:0000:0000:0000:0001");
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    public static enum AddressType {
        IPv4,
        IPv6
    }

    public static InetAddress getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            LOG.warn("getLocalHostAddress: Could not lookup the host address for the local host machine, address set to '127.0.0.1'.", e);
            return addr("127.0.0.1");
        }
    }

    public static String getLocalHostAddressAsString() {
        final String localhost = str(getLocalHostAddress());
        return localhost == null? "127.0.0.1" : localhost;
    }

    public static String getLocalHostName() {
        final InetAddress localHostAddress = getLocalHostAddress();
        if (localHostAddress == null) {
            LOG.warn("getLocalHostName: Could not lookup the host name for the local host machine, name set to 'localhost'.");
            return "localhost";
        }
        return localHostAddress.getHostName();
    }

    public static String incr(final String address) throws UnknownHostException {
        return InetAddressUtils.toIpAddrString(incr(InetAddressUtils.toIpAddrBytes(address)));
    }

    public static byte[] incr(final byte[] address) throws UnknownHostException {
        final BigInteger addr = new BigInteger(1, address).add(BigInteger.ONE);
        return convertBigIntegerIntoInetAddress(addr).getAddress();
    }

    public static String decr(final String address) throws UnknownHostException {
        return InetAddressUtils.toIpAddrString(decr(InetAddressUtils.toIpAddrBytes(address)));
    }

    public static byte[] decr(final byte[] address) throws UnknownHostException {
        final BigInteger addr = new BigInteger(1, address).subtract(BigInteger.ONE);
        return convertBigIntegerIntoInetAddress(addr).getAddress();
    }

    public static InetAddress getInetAddress(final int[] octets, final int offset, final int length) {
        final byte[] addressBytes = new byte[length];
        for (int i = 0; i < addressBytes.length; i++) {
            addressBytes[i] = Integer.valueOf(octets[i + offset]).byteValue();
        }
        return getInetAddress(addressBytes);
    }

    public static InetAddress getInetAddress(final byte[] ipAddrOctets) {
        return new IPAddress(ipAddrOctets).toInetAddress();

    }

    /**
     * <p>getInetAddress</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getInetAddress(final String dottedNotation) {
        return new IPAddress(dottedNotation).toInetAddress();
    }

    /**
     * <p>toIpAddrBytes</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return an array of byte.
     */
    public static byte[] toIpAddrBytes(final String dottedNotation) {
        return new IPAddress(dottedNotation).toOctets();
    }

    /**
     * <p>toIpAddrString</p>
     *
     * @param addr IP address
     * @return a {@link java.lang.String} object.
     */
    public static String toIpAddrString(final InetAddress addr) {
        return new IPAddress(addr).toDbString();
    }

    /**
     * <p>toIpAddrString</p>
     *
     * @param addr an array of byte.
     * @return a {@link java.lang.String} object.
     */
    public static String toIpAddrString(final byte[] addr) {
        return new IPAddress(addr).toDbString();
    }

    /**
     * Method that wraps IPv6 addresses in square brackets so that they are parsed
     * correctly by the {@link JMXServiceURL} class.
     */
    public static String toUrlIpAddress(InetAddress addr) {
        if (addr instanceof Inet6Address) {
            return String.format("[%s]", str(addr));
        } else {
            return str(addr);
        }
    }

    /**
     * Given a list of IP addresses, return the lowest as determined by the
     * numeric representation and not the alphanumeric string.
     *
     * @param addresses a {@link java.util.List} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getLowestInetAddress(final List<InetAddress> addresses) {
        if (addresses == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        InetAddress lowest = null;
        // Start with the highest conceivable IP address value
        final byte[] originalBytes = toIpAddrBytes("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
        byte[] lowestBytes = originalBytes;
        for (final InetAddress temp : addresses) {
            byte[] tempBytes = temp.getAddress();

            if (s_BYTE_ARRAY_COMPARATOR.compare(tempBytes, lowestBytes) < 0) {
                lowestBytes = tempBytes;
                lowest = temp;
            }
        }

        return s_BYTE_ARRAY_COMPARATOR.compare(originalBytes, lowestBytes) == 0 ? null : lowest;
    }

    public static BigInteger difference(final String addr1, final String addr2) {
        return difference(getInetAddress(addr1), getInetAddress(addr2));
    }

    public static BigInteger difference(final InetAddress addr1, final InetAddress addr2) {
        return new BigInteger(1, addr1.getAddress()).subtract(new BigInteger(1, addr2.getAddress()));
    }

    public static boolean isInetAddressInRange(final byte[] laddr, final String beginString, final String endString) {
        final byte[] begin = InetAddressUtils.toIpAddrBytes(beginString);
        final byte[] end = InetAddressUtils.toIpAddrBytes(endString);
        return isInetAddressInRange(laddr, begin, end);
    }

    public static boolean isInetAddressInRange(final String addrString, final String beginString, final String endString) {
        final byte[] addr = InetAddressUtils.toIpAddrBytes(addrString);
        final byte[] begin = InetAddressUtils.toIpAddrBytes(beginString);
        if (s_BYTE_ARRAY_COMPARATOR.compare(addr, begin) > 0) {
            final byte[] end = InetAddressUtils.toIpAddrBytes(endString);
            return (s_BYTE_ARRAY_COMPARATOR.compare(addr, end) <= 0);
        } else if (s_BYTE_ARRAY_COMPARATOR.compare(addr, begin) == 0) {
            return true;
        } else { 
            return false;
        }
    }

    public static boolean inSameScope(final InetAddress addr1, final InetAddress addr2) {
        if (addr1 instanceof Inet4Address) {
            return (addr2 instanceof Inet4Address);
        } else {
            if (addr2 instanceof Inet4Address) {
                return false;
            } else {
                // Compare the IPv6 scope IDs
                return Integer.valueOf(((Inet6Address)addr1).getScopeId()).compareTo(((Inet6Address)addr2).getScopeId()) == 0;
            }
        }
    }

    public static InetAddress getNetwork(InetAddress ipaddress, InetAddress netmask) {
        final byte[] ipAddress = ipaddress.getAddress();
        final byte[] netMask = netmask.getAddress();
        final byte[] netWork = new byte[4];

        for (int i=0;i< 4; i++) {
                netWork[i] = Integer.valueOf(ipAddress[i] & netMask[i]).byteValue();

        }
        return InetAddressUtils.getInetAddress(netWork);
    }

    public static boolean inSameNetwork(final InetAddress addr1, final InetAddress addr2, final InetAddress mask) {
        if (!(addr1 instanceof Inet4Address) || !(addr2 instanceof Inet4Address) || !(mask instanceof Inet4Address)) 
        		return false;
 
        final byte[] ipAddress1 = addr1.getAddress();
        final byte[] ipAddress2 = addr2.getAddress();
        final byte[] netMask = mask.getAddress();

        for (int i=0;i< 4; i++) {
        	if ((ipAddress1[i] & netMask[i]) != (ipAddress2[i] & netMask[i]))
        		return false;

        }
        return true;
    	
    }

    public static boolean isInetAddressInRange(final byte[] addr, final byte[] begin, final byte[] end) {
        if (s_BYTE_ARRAY_COMPARATOR.compare(addr, begin) > 0) {
            return (s_BYTE_ARRAY_COMPARATOR.compare(addr, end) <= 0);
        } else {
            return s_BYTE_ARRAY_COMPARATOR.compare(addr, begin) == 0;
        }
    }

    public static boolean isInetAddressInRange(final String ipAddr, final byte[] begin, final byte[] end) {
        return isInetAddressInRange(InetAddressUtils.toIpAddrBytes(ipAddr), begin, end);
    }

    public static InetAddress convertCidrToInetAddressV4(int cidr) {
        if (cidr < 0 || cidr > 32) {
            throw new IllegalArgumentException("Illegal IPv4 CIDR mask length: " + cidr);
        }
        StringBuilder binaryString = new StringBuilder();
        int i = 0;
        for (; i < cidr; i++) {
            binaryString.append('1');
        }
        for (; i < 32; i++) {
            binaryString.append('0');
        }
        try {
            return convertBigIntegerIntoInetAddress(new BigInteger(binaryString.toString(), 2));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Could not convert CIDR mask to InetAddress: " + e.getMessage());
        }
    }

    public static InetAddress convertCidrToInetAddressV6(int cidr) {
        if (cidr < 0 || cidr > 128) {
            throw new IllegalArgumentException("Illegal IPv6 CIDR mask length: " + cidr);
        }
        StringBuilder binaryString = new StringBuilder();
        int i = 0;
        for (; i < cidr; i++) {
            binaryString.append('1');
        }
        for (; i < 128; i++) {
            binaryString.append('0');
        }
        try {
            return convertBigIntegerIntoInetAddress(new BigInteger(binaryString.toString(), 2));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Could not convert CIDR mask to InetAddress: " + e.getMessage());
        }
    }

    public static InetAddress convertBigIntegerIntoInetAddress(final BigInteger i) throws UnknownHostException {
        if (i.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("BigInteger is negative, cannot convert into an IP address: " + i.toString());
        } else {
            // Note: This function will return the two's complement byte array so there will always
            // be a bit of value '0' (indicating positive sign) at the first position of the array
            // and it will be padded to the byte boundry. For example:
            //
            // 255.255.255.255 => 00 FF FF FF FF (5 bytes)
            // 127.0.0.1 => 0F 00 00 01 (4 bytes)
            //
            final byte[] bytes = i.toByteArray();

            if (bytes.length == 0) {
                return InetAddress.getByAddress(new byte[] {0, 0, 0, 0});
            } else if (bytes.length <= 4) {
                // This case covers an IPv4 address with the most significant bit of zero (the MSB
                // will be used as the two's complement sign bit)
                final byte[] addressBytes = new byte[4];
                int k = 3;
                for (int j = bytes.length - 1; j >= 0; j--, k--) {
                    addressBytes[k] = bytes[j];
                }
                return InetAddress.getByAddress(addressBytes);
            } else if (bytes.length <= 5 && bytes[0] == 0) {
                // This case covers an IPv4 address (4 bytes + two's complement sign bit of zero)
                final byte[] addressBytes = new byte[4];
                int k = 3;
                for (int j = bytes.length - 1; j >= 1; j--, k--) {
                    addressBytes[k] = bytes[j];
                }
                return InetAddress.getByAddress(addressBytes);
            } else if (bytes.length <= 16) {
                // This case covers an IPv6 address with the most significant bit of zero (the MSB
                // will be used as the two's complement sign bit)
                final byte[] addressBytes = new byte[16];
                int k = 15;
                for (int j = bytes.length - 1; j >= 0; j--, k--) {
                    addressBytes[k] = bytes[j];
                }
                return InetAddress.getByAddress(addressBytes);
            } else if (bytes.length <= 17 && bytes[0] == 0) {
                // This case covers an IPv6 address (16 bytes + two's complement sign bit of zero)
                final byte[] addressBytes = new byte[16];
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

    public static InetAddress addr(final String ipAddrString) {
        return ipAddrString == null ? null : getInetAddress(ipAddrString.trim());
    }

    /**
     * This function is used to ensure that an IP address string is in fully-qualified
     * format without any "::" segments for an IPv6 address.
     * 
     * FIXME: do we lose
     */
    public static String normalize(final String ipAddrString) {
        return ipAddrString == null? null : toIpAddrString(addr(ipAddrString.trim()));
    }

    public static String str(final InetAddress addr) {
        return addr == null ? null : toIpAddrString(addr);
    }

    public static BigInteger toInteger(final InetAddress ipAddress) {
        return new BigInteger(1, ipAddress.getAddress());
    }

    public static String toOid(final InetAddress addr) {
        if (addr == null) return null;

        if (addr instanceof Inet4Address) {
            return str(addr);
        } else if (addr instanceof Inet6Address) {
            // This is horribly inefficient, I'm sure, but good enough for now.
            final byte[] buf = addr.getAddress();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < buf.length; i++) {
                sb.append(buf[i] & 0xff);
                if (i != (buf.length - 1)) {
                    sb.append(".");
                }
            }
            return sb.toString();
        } else {
            LOG.debug("don't know how to handle {}", addr);
            return null;
        }
    }

    public static byte[] macAddressStringToBytes(String macAddress) {
        if (macAddress == null) {
            throw new IllegalArgumentException("Cannot decode null MAC address");
        }

        byte[] contents = new byte[6];
        String[] digits = macAddress.split(":");
        if (digits.length != 6) {
            // If the MAC address is 12 hex digits long
            if (macAddress.length() == 12) {
                digits = new String[] {
                        macAddress.substring(0, 2),
                        macAddress.substring(2, 4),
                        macAddress.substring(4, 6),
                        macAddress.substring(6, 8),
                        macAddress.substring(8, 10),
                        macAddress.substring(10)
                };
            } else {
                throw new IllegalArgumentException("Cannot decode MAC address: '" + macAddress + "'");
            }
        }
        // Decode each MAC address digit into a hexadecimal byte value
        for (int i = 0; i < 6; i++) {
            // Prefix the value with "0x" so that Integer.decode() knows which base to use
            contents[i] = Integer.decode("0x" + digits[i]).byteValue();
        }
        return contents;
    }

    public static String macAddressBytesToString(byte[] macAddress) {
        if (macAddress.length != 6) {
            throw new IllegalArgumentException("Cannot decode MAC address: " + Arrays.toString(macAddress));
        }

        return String.format(
                             //"%02X:%02X:%02X:%02X:%02X:%02X", 
                             "%02x%02x%02x%02x%02x%02x", 
                             macAddress[0],
                             macAddress[1],
                             macAddress[2],
                             macAddress[3],
                             macAddress[4],
                             macAddress[5]
                );
    }

    public static String normalizeMacAddress(String macAddress) {
        return macAddressBytesToString(macAddressStringToBytes(macAddress));
    }
    
    public static boolean isValidStpDesignatedPort(String bridgeDesignatedPort) {
        if (bridgeDesignatedPort == null || bridgeDesignatedPort.equals(INVALID_STP_BRIDGE_DESIGNATED_PORT))
                return false;
        Pattern pattern = Pattern.compile("([0-9a-f]{4})");
        Matcher matcher = pattern.matcher(bridgeDesignatedPort);
        return matcher.matches();
    }
    
    public static int getBridgeDesignatedPortNumber(String stpPortDesignatedPort) {
        return 8191 & Integer.parseInt(stpPortDesignatedPort,
                16);
    }

    public static boolean isValidBridgeAddress(String bridgeAddress) {
            if (bridgeAddress == null || bridgeAddress.equals(INVALID_BRIDGE_ADDRESS))
                    return false;
            Pattern pattern = Pattern.compile("([0-9a-f]{12})");
            Matcher matcher = pattern.matcher(bridgeAddress);
            return matcher.matches();
    }

    public static boolean isValidStpBridgeId(String bridgeId) {
            if (bridgeId == null || bridgeId.equals(INVALID_STP_BRIDGE_ID))
                    return false;
            Pattern pattern = Pattern.compile("([0-9a-f]{16})");
            Matcher matcher = pattern.matcher(bridgeId);
            return matcher.matches();
    }
    
    public static String getBridgeAddressFromStpBridgeId(String bridgeId) {
        return bridgeId.substring(4, 16);
    }
    
    public static InetAddress getIpAddressByHexString(String ipaddrhexstrng) {

        long ipAddr = Long.parseLong(ipaddrhexstrng, 16);
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (ipAddr & 0xff);
        bytes[2] = (byte) ((ipAddr >> 8) & 0xff);
        bytes[1] = (byte) ((ipAddr >> 16) & 0xff);
        bytes[0] = (byte) ((ipAddr >> 24) & 0xff);

        return InetAddressUtils.getInetAddress(bytes);
   }

}
