/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * <p>Abstract InetAddressUtils class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
abstract public class InetAddressUtils {

    private static final ByteArrayComparator s_BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();
    public static final InetAddress UNPINGABLE_ADDRESS;
    public static final InetAddress UNPINGABLE_ADDRESS_IPV6;

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
            LogUtils.warnf(InetAddressUtils.class, e, "getLocalHostAddress: Could not lookup the host address for the local host machine, address set to '127.0.0.1'.");
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
            LogUtils.warnf(InetAddressUtils.class, "getLocalHostName: Could not lookup the host name for the local host machine, name set to 'localhost'.");
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

    /**
     * <p>getInetAddress</p>
     *
     * @param ipAddrOctets an array of byte.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getInetAddress(final int[] octets, final int offset, final int length) {
		final byte[] addressBytes = new byte[length];
    	for (int i = 0; i < addressBytes.length; i++) {
    		addressBytes[i] = Integer.valueOf(octets[i + offset]).byteValue();
    	}
    	return getInetAddress(addressBytes);
    }

    /**
     * <p>getInetAddress</p>
     *
     * @param ipAddrOctets an array of byte.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getInetAddress(final byte[] ipAddrOctets) {
        try {
            return InetAddress.getByAddress(ipAddrOctets);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress " + ipAddrOctets + " with length " + ipAddrOctets.length);
        }

    }

    /**
     * <p>getInetAddress</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public static InetAddress getInetAddress(final String dottedNotation) {
        try {
            return InetAddress.getByName(dottedNotation);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress " + dottedNotation);
        }
    }

    public static InetAddress resolveHostname(final String hostname, final boolean preferInet6Address) throws UnknownHostException {
        return resolveHostname(hostname, preferInet6Address, true);
    }

    /**
     * This function is used inside XSLT documents, do a string search before refactoring.
     */
    public static InetAddress resolveHostname(final String hostname, final boolean preferInet6Address, final boolean throwException) throws UnknownHostException {
        InetAddress retval = null;
        //System.out.println(String.format("%s (%s)", hostname, preferInet6Address ? "6" : "4"));

        // Do a special case for localhost since the DNS server will generally not
        // return valid A and AAAA records for "localhost".
        if ("localhost".equals(hostname)) {
            return preferInet6Address ? InetAddress.getByName("::1") : InetAddress.getByName("127.0.0.1");
        }

        try {
            // 2011-05-22 - Matt is seeing some platform-specific inconsistencies when using
            // InetAddress.getAllByName(). It seems to miss some addresses occasionally on Mac.
            // We need to use dnsjava here instead since it should be 100% reliable.
            //
            // InetAddress[] addresses = InetAddress.getAllByName(hostname);
            //
            List<InetAddress> v4Addresses = new ArrayList<InetAddress>();
            try {
                Record[] aRecs = new Lookup(hostname, Type.A).run();
                if (aRecs != null) {
                    for (Record aRec : aRecs) {
                        if (aRec instanceof ARecord) {
                            InetAddress addr = ((ARecord)aRec).getAddress();
                            if (addr instanceof Inet4Address) {
                                v4Addresses.add(addr);
                            } else {
                                // Should never happen
                                throw new UnknownHostException("Non-IPv4 address found via A record DNS lookup of host: " + hostname + ": " + addr.toString());
                            }
                        }
                    }
                } else {
                    //throw new UnknownHostException("No IPv4 addresses found via A record DNS lookup of host: " + hostname);
                }
            } catch (final TextParseException e) {
                final UnknownHostException ex = new UnknownHostException("Could not perform A record lookup for host: " + hostname);
                ex.initCause(e);
                throw ex;
            }

            final List<InetAddress> v6Addresses = new ArrayList<InetAddress>();
            try {
                final Record[] quadARecs = new Lookup(hostname, Type.AAAA).run();
                if (quadARecs != null) {
                    for (final Record quadARec : quadARecs) {
                        final InetAddress addr = ((AAAARecord)quadARec).getAddress();
                        if (addr instanceof Inet6Address) {
                            v6Addresses.add(addr);
                        } else {
                            // Should never happen
                            throw new UnknownHostException("Non-IPv6 address found via AAAA record DNS lookup of host: " + hostname + ": " + addr.toString());
                        }
                    }
                } else {
                    // throw new UnknownHostException("No IPv6 addresses found via AAAA record DNS lookup of host: " + hostname);
                }
            } catch (final TextParseException e) {
                final UnknownHostException ex = new UnknownHostException("Could not perform AAAA record lookup for host: " + hostname);
                ex.initCause(e);
                throw ex;
            }

            final List<InetAddress> addresses = new ArrayList<InetAddress>();
            if (preferInet6Address) {
                addresses.addAll(v6Addresses);
                addresses.addAll(v4Addresses);
            } else {
                addresses.addAll(v4Addresses);
                addresses.addAll(v6Addresses);
            }

            for (final InetAddress address : addresses) {
                retval = address;
                if (!preferInet6Address && retval instanceof Inet4Address) break;
                if (preferInet6Address && retval instanceof Inet6Address) break;
            }
            if (preferInet6Address && !(retval instanceof Inet6Address)) {
                throw new UnknownHostException("No IPv6 address could be found for the hostname: " + hostname);
            }
        } catch (final UnknownHostException e) {
            if (throwException) {
                throw e;
            } else {
                //System.out.println(String.format("UnknownHostException for : %s (%s)", hostname, preferInet6Address ? "6" : "4"));
                //e.printStackTrace();
                return null;
            }
        }
        return retval;
    }

    /**
     * <p>toIpAddrBytes</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     * @return an array of byte.
     */
    public static byte[] toIpAddrBytes(final String dottedNotation) {
        return getInetAddress(dottedNotation).getAddress();
    }

    /**
     * <p>toIpAddrString</p>
     *
     * @param addr IP address
     * @return a {@link java.lang.String} object.
     */
    public static String toIpAddrString(final InetAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException("Cannot convert null InetAddress to a string");
        } else {
        	final byte[] address = addr.getAddress();
            if (address == null) {
                // This case can occur when Jersey uses Spring bean classes which use
                // CGLIB bytecode manipulation to generate InetAddress classes. This will
                // occur during REST calls. {@see org.opennms.web.rest.NodeRestServiceTest}
                //
                throw new IllegalArgumentException("InetAddress instance violates contract by returning a null address from getAddress()");
            } else if (addr instanceof Inet4Address) {
                return toIpAddrString(address);
            } else if (addr instanceof Inet6Address) {
            	final Inet6Address addr6 = (Inet6Address)addr;
            	final StringBuilder sb = new StringBuilder(toIpAddrString(address));
            	if (addr6.getScopeId() != 0) {
            		sb.append("%").append(addr6.getScopeId());
            	}
            	return sb.toString();
            } else {
                throw new IllegalArgumentException("Unknown type of InetAddress: " + addr.getClass().getName());
            }
        }
    }

    /**
     * <p>toIpAddrString</p>
     *
     * @param addr an array of byte.
     * @return a {@link java.lang.String} object.
     */
    public static String toIpAddrString(final byte[] addr) {
        if (addr.length == 4) {
            return getInetAddress(addr).getHostAddress();
        } else if (addr.length == 16) {
            return String.format("%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x",
                                 addr[0],
                                 addr[1],
                                 addr[2],
                                 addr[3],
                                 addr[4],
                                 addr[5],
                                 addr[6],
                                 addr[7],
                                 addr[8],
                                 addr[9],
                                 addr[10],
                                 addr[11],
                                 addr[12],
                                 addr[13],
                                 addr[14],
                                 addr[15]
            ).intern();
        } else {
            throw new IllegalArgumentException("IP address has an illegal number of bytes: " + addr.length);
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
            if (s_BYTE_ARRAY_COMPARATOR.compare(addr, end) <= 0) {
                return true;
            } else { 
                return false;
            }
        } else if (s_BYTE_ARRAY_COMPARATOR.compare(addr, begin) == 0) {
            return true;
        } else { 
            return false;
        }
    }

    public static boolean inSameScope(final InetAddress addr1, final InetAddress addr2) {
        if (addr1 instanceof Inet4Address) {
            if (addr2 instanceof Inet4Address) {
                return true;
            } else {
                return false;
            }
        } else {
            if (addr2 instanceof Inet4Address) {
                return false;
            } else {
                // Compare the IPv6 scope IDs
                return Integer.valueOf(((Inet6Address)addr1).getScopeId()).compareTo(((Inet6Address)addr2).getScopeId()) == 0;
            }
        }
    }

    public static boolean isInetAddressInRange(final byte[] addr, final byte[] begin, final byte[] end) {
        if (s_BYTE_ARRAY_COMPARATOR.compare(addr, begin) > 0) {
            if (s_BYTE_ARRAY_COMPARATOR.compare(addr, end) <= 0) {
                return true;
            } else { 
                return false;
            }
        } else if (s_BYTE_ARRAY_COMPARATOR.compare(addr, begin) == 0) {
            return true;
        } else { 
            return false;
        }
    }

	public static boolean isInetAddressInRange(final String ipAddr, final byte[] begin, final byte[] end) {
		return isInetAddressInRange(InetAddressUtils.toIpAddrBytes(ipAddr), begin, end);
	}

    public static InetAddress convertCidrToInetAddressV4(int cidr) {
        if (cidr < 0 || cidr > 32) {
            throw new IllegalArgumentException("Illegal IPv4 CIDR mask length: " + cidr);
        }
        StringBuffer binaryString = new StringBuffer();
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
        StringBuffer binaryString = new StringBuffer();
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
			LogUtils.debugf(InetAddressUtils.class, "don't know how to handle %s", addr);
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
                throw new IllegalArgumentException("Cannot decode MAC address: " + macAddress);
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
            throw new IllegalArgumentException("Cannot decode MAC address: " + macAddress);
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
}
