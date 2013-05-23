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

package org.opennms.netmgt.model.discovery;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;

/**
 * <p>IPAddress class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IPAddress implements Comparable<IPAddress> {

    final byte[] m_ipAddr;
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param addr a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress(final IPAddress addr) {
        m_ipAddr = addr.m_ipAddr;
    }
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param dottedNotation a {@link java.lang.String} object.
     */
    public IPAddress(final String dottedNotation) {
        m_ipAddr = InetAddressUtils.toIpAddrBytes(dottedNotation);
    }
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param inetAddress a {@link java.net.InetAddress} object.
     */
    public IPAddress(final InetAddress inetAddress) {
        m_ipAddr = inetAddress.getAddress();
    }
    
    /**
     * <p>Constructor for IPAddress.</p>
     *
     * @param ipAddrOctets an array of byte.
     */
    public IPAddress(final byte[] ipAddrOctets) {
        m_ipAddr = ipAddrOctets;
    }
    
    /**
     * <p>toInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress toInetAddress() {
        return InetAddressUtils.getInetAddress(m_ipAddr);
    }
    
    /**
     * <p>toOctets</p>
     *
     * @return an array of byte.
     */
    public byte[] toOctets() {
        return m_ipAddr;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IPAddress) {
        	return Arrays.equals(m_ipAddr, ((IPAddress) obj).toOctets());
        }
        return false;
    }

    @Override
    public int hashCode() {
    	return Arrays.hashCode(m_ipAddr);
    }
    
    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a int.
     */
    @Override
    public int compareTo(final IPAddress o) {
        return new ByteArrayComparator().compare(m_ipAddr, o.toOctets());
    }
    
    public String toUserString() {
        // this returns dotted notation for ipv4 or the double colon format for ipv6
        return toInetAddress().getHostAddress().replaceFirst("(^|:)(0:)+", "::");
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return toUserString();
    }

    public String toDbString() {
        return InetAddressUtils.toIpAddrString(m_ipAddr);
    }
    
    /** {@inheritDoc} */
    public BigInteger toBigInteger() {
        return new BigInteger(1, m_ipAddr);
    }
    
    
    
    /**
     * <p>incr</p>
     *
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress incr() {
    	final byte[] b = new byte[m_ipAddr.length];

        int carry = 1;
        for(int i = m_ipAddr.length-1; i >= 0; i--) {
            b[i] = (byte)(m_ipAddr[i] + carry);
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
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress decr() {
    	final byte[] b = new byte[m_ipAddr.length];
        
        int borrow = 1;
        for(int i = m_ipAddr.length-1; i >= 0; i--) {
            b[i] = (byte)(m_ipAddr[i] - borrow);
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
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isPredecessorOf(final IPAddress other) {
        return other.decr().equals(this);
    }
    
    /**
     * <p>isSuccessorOf</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isSuccessorOf(final IPAddress other) {
        return other.incr().equals(this);
    }
    
    /**
     * <p>isLessThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isLessThan(final IPAddress other) {
        return compareTo(other) < 0;
    }
    
    /**
     * <p>isLessThanOrEqualTo</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isLessThanOrEqualTo(final IPAddress other) {
        return compareTo(other) <= 0;
    }
    
    /**
     * <p>isGreaterThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isGreaterThan(final IPAddress other) {
        return compareTo(other) > 0;
    }
    
    /**
     * <p>isGreaterThanOrEqualTo</p>
     *
     * @param other a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean isGreaterThanOrEqualTo(final IPAddress other) {
        return compareTo(other) >= 0;
    }

    /**
     * <p>min</p>
     *
     * @param a a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @param b a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public static IPAddress min(final IPAddress a, final IPAddress b) {
        return (a.isLessThan(b) ? a : b);
    }

    /**
     * <p>max</p>
     *
     * @param a a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @param b a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public static IPAddress max(final IPAddress a, final IPAddress b) {
        return (a.isGreaterThan(b) ? a : b);
    }

}
