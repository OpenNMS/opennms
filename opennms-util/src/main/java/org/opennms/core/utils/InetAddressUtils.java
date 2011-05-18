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
import java.net.Inet4Address;
import java.net.Inet6Address;
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

    public static final InetAddress UNPINGABLE_ADDRESS;

    static {
        try {
            // This address (169.254.254.254) is within the link-local IPv4 range
            // so it should almost never be pingable unless the network has an
            // oddball link-local IPv4 setup.
            UNPINGABLE_ADDRESS = InetAddress.getByAddress(new byte[] {(byte)169, (byte)254, (byte)254, (byte)254});
        } catch (UnknownHostException e) {
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
    	final BigInteger addr = new BigInteger(1, address)
    		.add(BigInteger.ONE);
        return convertBigIntegerIntoInetAddress(addr).getAddress();
    }

    public static String decr(final String address) throws UnknownHostException {
        return InetAddressUtils.toIpAddrString(decr(InetAddressUtils.toIpAddrBytes(address)));
    }

    public static byte[] decr(final byte[] address) throws UnknownHostException {
    	final BigInteger addr = new BigInteger(1, address)
    		.subtract(BigInteger.ONE);
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
            return getInetAddress(dottedNotation, false);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress " + dottedNotation);
        }
    }

    public static InetAddress getInetAddress(final String hostname, final boolean preferInet6Address) throws UnknownHostException {
        return getInetAddress(hostname, preferInet6Address, true);
    }

    /**
     * This function is used inside XSLT documents, do a string search before refactoring.
     */
    public static InetAddress getInetAddress(final String hostname, final boolean preferInet6Address, final boolean throwException) throws UnknownHostException {
        InetAddress retval = null;
        try {
            InetAddress[] addresses = InetAddress.getAllByName(hostname);
            for (InetAddress address : addresses) {
                retval = address;
                if (!preferInet6Address && retval instanceof Inet4Address) break;
                if (preferInet6Address && retval instanceof Inet6Address) break;
            }
            if (preferInet6Address && !(retval instanceof Inet6Address)) {
                throw new UnknownHostException("No IPv6 address could be found for the hostname: " + hostname);
            }
        } catch (UnknownHostException e) {
            if (throwException) {
                throw e;
            } else {
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
            );
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
        final ByteArrayComparator comparator = new ByteArrayComparator();
        for (final InetAddress temp : addresses) {
            byte[] tempBytes = temp.getAddress();

            if (comparator.compare(tempBytes, lowestBytes) < 0) {
                lowestBytes = tempBytes;
                lowest = temp;
            }
        }

        return comparator.compare(originalBytes, lowestBytes) == 0 ? null : lowest;
    }

    public static BigInteger difference(final String addr1, final String addr2) {
        return difference(getInetAddress(addr1), getInetAddress(addr2));
    }

    public static BigInteger difference(final InetAddress addr1, final InetAddress addr2) {
        return new BigInteger(addr1.getAddress()).subtract(new BigInteger(addr2.getAddress()));
    }

	public static boolean isInetAddressInRange(final byte[] laddr, final String beginString, final String endString) {
        final byte[] begin = InetAddressUtils.toIpAddrBytes(beginString);
        final byte[] end = InetAddressUtils.toIpAddrBytes(endString);
        return isInetAddressInRange(laddr, begin, end);
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
                return new Integer(((Inet6Address)addr1).getScopeId()).compareTo(((Inet6Address)addr2).getScopeId()) == 0;
            }
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

	public static boolean isInetAddressInRange(final String ipAddr, final byte[] begin, final byte[] end) {
		return isInetAddressInRange(InetAddressUtils.toIpAddrBytes(ipAddr), begin, end);
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
        return ipAddrString == null ? null : getInetAddress(ipAddrString);
    }
    
    // FIXME: do we lose 
    public static String normalize(final String ipAddrString) {
    	return ipAddrString == null? null : toIpAddrString(addr(ipAddrString));
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
			byte[] buf = addr.getAddress();
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
}
