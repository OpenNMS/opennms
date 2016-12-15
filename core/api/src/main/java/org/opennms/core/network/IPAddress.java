/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.core.network;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddress implements Comparable<IPAddress> {
    private static final Pattern LEADING_ZEROS = Pattern.compile("^0:[0:]+");
    protected final InetAddress m_inetAddress;

    public IPAddress(final IPAddress addr) {
        m_inetAddress = addr.m_inetAddress;
    }

    public IPAddress(final String dottedNotation) {
        m_inetAddress = getInetAddress(dottedNotation);
    }

    public IPAddress(final InetAddress inetAddress) {
        m_inetAddress = inetAddress;
    }

    public IPAddress(final byte[] ipAddrOctets) {
        try {
            m_inetAddress = InetAddress.getByAddress(ipAddrOctets);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Cannot convert bytes to an InetAddress.", e);
        }
    }

    public static IPAddress min(final IPAddress a, final IPAddress b) {
        return (a.isLessThan(b) ? a : b);
    }

    public InetAddress toInetAddress() {
        return m_inetAddress;
    }

    public byte[] toOctets() {
        return m_inetAddress.getAddress();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IPAddress) {
            return Arrays.equals(m_inetAddress.getAddress(), ((IPAddress) obj).m_inetAddress.getAddress());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return m_inetAddress.hashCode();
    }

    @Override
    public int compareTo(final IPAddress o) {
        return compare(m_inetAddress.getAddress(), o.m_inetAddress.getAddress());
    }

    public String toUserString() {
        if (m_inetAddress instanceof Inet4Address) {
            return toIpAddrString(m_inetAddress);
        } else if (m_inetAddress instanceof Inet6Address) {
            /*
             * <p>From: <a href="https://code.google.com/p/guava-libraries/source/browse/guava/src/com/google/common/primitives/Ints.java">Guava</a>.</p>
             */
            final byte[] bytes = m_inetAddress.getAddress();
            final int[] hextets = new int[8];
            for (int i = 0; i < hextets.length; i++) {
                hextets[i] = fromBytes(
                    (byte) 0,
                    (byte) 0,
                    bytes[2 * i],
                    bytes[2 * i + 1]
                );
            }
            compressLongestRunOfZeroes(hextets);
            return hextetsToIPv6String(hextets);
        } else {
            System.err.println("Not an Inet4Address nor an Inet6Address! " + m_inetAddress.getClass());
            return m_inetAddress.getHostAddress();
        }
    }

    @Override
    public String toString() {
        return toUserString();
    }

    public String toDbString() {
        return toIpAddrString(m_inetAddress);
    }

    /** {@inheritDoc} */
    public BigInteger toBigInteger() {
        return new BigInteger(1, m_inetAddress.getAddress());
    }

    /**
     * <p>incr</p>
     *
     * @return a {@link org.opennms.core.network.IPAddress} object.
     */
    public IPAddress incr() {
        final byte[] current = m_inetAddress.getAddress();
        final byte[] b = new byte[current.length];

        int carry = 1;
        for(int i = current.length-1; i >= 0; i--) {
            b[i] = (byte)(current[i] + carry);
            // if overflow we need to carry to the next byte
            carry = b[i] == 0 ? carry : 0;
        }

        if (carry > 0) {
            // we have overflowed the address
            throw new IllegalStateException("you have tried to increment the max ip address");
        }

        return new IPAddress(b);
    }

    /**
     * <p>decr</p>
     *
     * @return a {@link org.opennms.core.network.IPAddress} object.
     */
    public IPAddress decr() {
        final byte[] current = m_inetAddress.getAddress();
        final byte[] b = new byte[current.length];

        int borrow = 1;
        for(int i = current.length-1; i >= 0; i--) {
            b[i] = (byte)(current[i] - borrow);
            // if underflow then we need to borrow from the next byte
            borrow = b[i] == (byte)0xff ? borrow : 0;
        }

        if (borrow > 0) {
            // we have underflowed the address
            throw new IllegalStateException("you have tried to decrement the '0' ip address");
        }

        return new IPAddress(b);

    }

    /**
     * <p>isPredecessorOf</p>
     *
     * @param other a {@link org.opennms.core.network.IPAddress} object.
     * @return a boolean.
     */
    public boolean isPredecessorOf(final IPAddress other) {
        return other.decr().equals(this);
    }

    /**
     * <p>isSuccessorOf</p>
     *
     * @param other a {@link org.opennms.core.network.IPAddress} object.
     * @return a boolean.
     */
    public boolean isSuccessorOf(final IPAddress other) {
        return other.incr().equals(this);
    }

    /**
     * <p>isLessThan</p>
     *
     * @param other a {@link org.opennms.core.network.IPAddress} object.
     * @return a boolean.
     */
    public boolean isLessThan(final IPAddress other) {
        return compareTo(other) < 0;
    }

    /**
     * <p>isLessThanOrEqualTo</p>
     *
     * @param other a {@link org.opennms.core.network.IPAddress} object.
     * @return a boolean.
     */
    public boolean isLessThanOrEqualTo(final IPAddress other) {
        return compareTo(other) <= 0;
    }

    /**
     * <p>isGreaterThan</p>
     *
     * @param other a {@link org.opennms.core.network.IPAddress} object.
     * @return a boolean.
     */
    public boolean isGreaterThan(final IPAddress other) {
        return compareTo(other) > 0;
    }

    /**
     * <p>isGreaterThanOrEqualTo</p>
     *
     * @param other a {@link org.opennms.core.network.IPAddress} object.
     * @return a boolean.
     */
    public boolean isGreaterThanOrEqualTo(final IPAddress other) {
        return compareTo(other) >= 0;
    }

    /**
     * <p>max</p>
     *
     * @param a a {@link org.opennms.core.network.IPAddress} object.
     * @param b a {@link org.opennms.core.network.IPAddress} object.
     * @return a {@link org.opennms.core.network.IPAddress} object.
     */
    public static IPAddress max(final IPAddress a, final IPAddress b) {
        return (a.isGreaterThan(b) ? a : b);
    }

    protected String toIpAddrString(final InetAddress addr) {
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

    protected String toIpAddrString(final byte[] addr) {
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

    protected byte[] toIpAddrBytes(final String dottedNotation) {
        return getInetAddress(dottedNotation).getAddress();
    }

    private InetAddress getInetAddress(final byte[] ipAddrOctets) {
        try {
            return InetAddress.getByAddress(ipAddrOctets);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress " + Arrays.toString(ipAddrOctets) + " with length " + ipAddrOctets.length);
        }

    }

    private InetAddress getInetAddress(final String dottedNotation) {
        try {
            return dottedNotation == null? null : InetAddress.getByName(dottedNotation);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress " + dottedNotation);
        }
    }

    private int compare(final byte[] a, final byte[] b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else {
            // Make shorter byte arrays "less than" longer arrays
            if (a.length < b.length) {
                return -1;
            } else if (a.length > b.length) {
                return 1;
            } else {
                // Compare byte-by-byte
                for (int i = 0; i < a.length; i++) {
                    final int aInt = unsignedByteToInt(a[i]);
                    final int bInt = unsignedByteToInt(b[i]);
                    if (aInt < bInt) {
                        return -1;
                    } else if (aInt > bInt) {
                        return 1;
                    }
                }
                // OK both arrays are the same length and every byte is identical so they are equal
                return 0;
            }
        }
    }

    /**
     * Returns the {@code int} value whose byte representation is the given 4
     * bytes, in big-endian order; equivalent to {@code Ints.fromByteArray(new
     * byte[] {b1, b2, b3, b4})}.
     *
     * <p>From: <a href="https://code.google.com/p/guava-libraries/source/browse/guava/src/com/google/common/primitives/Ints.java">Guava</a>.</p>
     */
    public static int fromBytes(final byte b1, final byte b2, final byte b3, final byte b4) {
      return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

    /**
     * Identify and mark the longest run of zeroes in an IPv6 address.
     *
     * <p>Only runs of two or more hextets are considered.  In case of a tie, the
     * leftmost run wins.  If a qualifying run is found, its hextets are replaced
     * by the sentinel value -1.
     *
     * <p>From: <a href="https://code.google.com/p/guava-libraries/source/browse/guava/src/com/google/common/primitives/Ints.java">Guava</a>.</p>
     *
     * @param hextets {@code int[]} mutable array of eight 16-bit hextets
     */
    private static void compressLongestRunOfZeroes(final int[] hextets) {
        int bestRunStart = -1;
        int bestRunLength = -1;
        int runStart = -1;
        for (int i = 0; i < hextets.length + 1; i++) {
            if (i < hextets.length && hextets[i] == 0) {
                if (runStart < 0) {
                    runStart = i;
                }
            } else if (runStart >= 0) {
                int runLength = i - runStart;
                if (runLength > bestRunLength) {
                    bestRunStart = runStart;
                    bestRunLength = runLength;
                }
                runStart = -1;
            }
        }
        if (bestRunLength >= 2) {
            Arrays.fill(hextets, bestRunStart, bestRunStart + bestRunLength, -1);
        }
    }

    /**
     * Convert a list of hextets into a human-readable IPv6 address.
     *
     * <p>In order for "::" compression to work, the input should contain negative
     * sentinel values in place of the elided zeroes.<p>
     *
     * <p>From: <a href="https://code.google.com/p/guava-libraries/source/browse/guava/src/com/google/common/primitives/Ints.java">Guava</a>.</p>
     *
     * @param hextets {@code int[]} array of eight 16-bit hextets, or -1s
     */
    private static String hextetsToIPv6String(final int[] hextets) {
        /*
         * While scanning the array, handle these state transitions:
         *   start->num => "num"     start->gap => "::"
         *   num->num   => ":num"    num->gap   => "::"
         *   gap->num   => "num"     gap->gap   => ""
         */
        StringBuilder buf = new StringBuilder(39);
        boolean lastWasNumber = false;
        for (int i = 0; i < hextets.length; i++) {
            final boolean thisIsNumber = hextets[i] >= 0;
            if (thisIsNumber) {
                if (lastWasNumber) {
                    buf.append(':');
                }
                buf.append(Integer.toHexString(hextets[i]));
            } else {
                if (i == 0 || lastWasNumber) {
                    buf.append("::");
                }
            }
            lastWasNumber = thisIsNumber;
        }
        final Matcher matcher = LEADING_ZEROS.matcher(buf.toString());
        return matcher.replaceAll(":");
    }

    private int unsignedByteToInt(final byte b) {
        return b < 0 ? ((int)b)+256 : ((int)b);
    }
}