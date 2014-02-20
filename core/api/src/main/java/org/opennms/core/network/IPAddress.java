package org.opennms.core.network;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class IPAddress implements Comparable<IPAddress> {
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
        return toIpAddrString(m_inetAddress);
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
                return sb.toString().intern();
            } else {
                throw new IllegalArgumentException("Unknown type of InetAddress: " + addr.getClass().getName());
            }
        }
    }

    protected String toIpAddrString(final byte[] addr) {
        if (addr.length == 4) {
            return getInetAddress(addr).getHostAddress().intern();
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

    private int unsignedByteToInt(final byte b) {
        return b < 0 ? ((int)b)+256 : ((int)b);
    }
}