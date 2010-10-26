//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.model.discovery;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;

/**
 * <P>
 * The IPAddressRange object is used to encapsulate the starting and ending
 * points of a contiguous IPv4/IPv6 Address range. The class can then generate
 * either an Enumeration or Iterator that can be used to cycle through the range
 * of addresses by the object's user.
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class IPAddrRange implements Iterable<InetAddress> {
    /**
     * The starting address for the object.
     */
    private final byte[] m_begin;

    /**
     * The ending address for the object.
     */
    private final byte[] m_end;

    /**
     * <P>
     * This class is used to enumerate or iterate through one contiguous set of
     * IP addresses. The class can either be used as an iterator or as an
     * enumeration. In java 1.2 iterators were introduced and are being used in
     * favor of enumerations in new classes.
     * </P>
     * 
     */
    static class IPAddressRangeGenerator implements Enumeration<InetAddress>, Iterator<InetAddress> {
        /**
         * The next address in the range.
         */
        private BigInteger m_next;

        /**
         * The last address in the range. The remaining address are in the range
         * of [m_next .. m_end].
         */
        private final BigInteger m_end;

        /**
         * Converts an integer to an InetAdrress object and discards any
         * exceptions. If the address cannot be constructed then a null
         * reference is returned.
         * 
         * @param addr
         *            The 32-bit IP address value, in network order.
         * 
         * @return An Internet Address Object.
         */
        static InetAddress make(BigInteger addr) {
            InetAddress naddr = null;
            try {
                naddr = InetAddressUtils.convertBigIntegerIntoInetAddress(addr);
            } catch (UnknownHostException uhE) {
                naddr = null;
            }
            return naddr;
        }

        /**
         * <P>
         * Creates a generator object that iterates over the range from start to
         * end, inclusive.
         * </P>
         * 
         * @param start
         *            The start address.
         * @param end
         *            The ending address.
         * 
         * @exception java.lang.IllegalArgumentException
         *                Thrown if the start address is greater than the ending
         *                address.
         * 
         */
        IPAddressRangeGenerator(byte[] start, byte[] end) {
            if (new ByteArrayComparator().compare(start, end) > 0)
                throw new IllegalArgumentException("start must be less than or equal to end");

            m_next = new BigInteger(1, start);
            m_end = new BigInteger(1, end);
        }

        /**
         * <P>
         * Returns true if the enumeration object has more elements remaining.
         * </P>
         */
        public boolean hasMoreElements() {
            return (m_next.compareTo(m_end) <= 0);
        }

        /**
         * <P>
         * Returns the next element in the enumeration. If there is no element
         * left in the enumeration an exception will be thrown.
         * </P>
         * 
         * @exception java.util.NoSuchElementException
         *                Thrown if the collection is exhausted.
         */
        public InetAddress nextElement() {
            if (!hasMoreElements())
                throw new NoSuchElementException("End of Range");

            m_next = m_next.add(new BigInteger("1"));
            InetAddress element = make(m_next);
            return element;
        }

        /**
         * <P>
         * Returns true if there are more elements in the iteration.
         * </P>
         */
        public boolean hasNext() {
            return hasMoreElements();
        }

        /**
         * <P>
         * Returns the next object in the iteration. If there are no objects
         * left in the iteration an exception will be thrown.
         * </P>
         * 
         * @exception java.util.NoSuchElementException
         *                Thrown if the collection is exhausted.
         */
        public InetAddress next() {
            return nextElement();
        }

        /**
         * <P>
         * The remove method of the iterator interface is considered optional
         * for the implemetor. For the purposes of this class it is not
         * implemented and will throw an exception.
         * </P>
         * 
         * @exception java.lang.UnsupportedOperationException
         *                Always thrown by the remove method.
         * 
         */
        public void remove() {
            throw new UnsupportedOperationException("The remove operation is not supported by the iterator");
        }

    } // end class IPAddressRangeGenerator

    /**
     * <P>
     * Creates a new IPAddressRange object that can be used to encapsulate a
     * contiguous range of IP Addresses. Once created the object can be used to
     * get either an Iterator or Enumeration object to cycle through the list of
     * address encapsulated by this object.
     * </P>
     * 
     * <P>
     * It is important to note that if the address for toIP is greater than
     * fromIP, the values will be swapped so that the iteration is always from
     * the lowest address to the highest address as defined by a 32-bit unsigned
     * quantity.
     * </P>
     * 
     * @param fromIP
     *            The starting address, resolved by InetAddress.
     * @param toIP
     *            The ending address, resolved by InetAddress.
     * 
     * @see java.net.InetAddress#getByName(java.lang.String)
     * 
     * @exception java.net.UnknownHostException
     *                Thrown by the InetAddress class if the hostname cannot be
     *                resolved.
     * 
     */
    IPAddrRange(String fromIP, String toIP) throws java.net.UnknownHostException {
        this(InetAddress.getByName(fromIP), InetAddress.getByName(toIP));
    }

    /**
     * <P>
     * Creates a new IPAddressRange object that can be used to encapsulate a
     * contiguous range of IP Addresses. Once created the object can be used to
     * get either an Iterator or Enumeration object to cycle through the list of
     * address encapsulated by this object.
     * </P>
     * 
     * <P>
     * It is important to note that if the address for start is greater than
     * end, the values will be swapped so that the iteration is always from the
     * lowest address to the highest address as defined by a 32-bit unsigned
     * quantity.
     * </P>
     * 
     * @param start
     *            The starting address.
     * @param end
     *            The ending address.
     * 
     */
    IPAddrRange(InetAddress start, InetAddress end) {
        byte[] from = start.getAddress();
        byte[] to = end.getAddress();

        if (new ByteArrayComparator().compare(from, to) > 0) {
            ThreadCategory.getInstance(this.getClass()).warn("The beginning of the address range is greater than the end of the address range (" +  start.getHostAddress() + " - " + end.getHostAddress() + "), swapping values to create a valid IP address range");
            m_end = from;
            m_begin = to;
        } else {
            m_begin = from;
            m_end = to;
        }
    }

    /**
     * This method may be used to determine if the specified IP address is
     * contained within the IP address range.
     * 
     * @param ipAddr
     *            IP address (InetAddress) to compare
     * 
     * @return 'true' if the specified IP address falls within the IP address
     *         range. 'false' otherwise.
     */
    boolean contains(InetAddress ipAddr) {
        return InetAddressUtils.isInetAddressInRange(ipAddr.getAddress(), m_begin, m_end);
    }

    /**
     * This method may be used to determine if the specified IP address is
     * contained within the IP address range.
     * 
     * @param ipAddr
     *            IP address (String) to compare
     * 
     * @return 'true' if the specified IP address falls within the IP address
     *         range. 'false' otherwise.
     */
    boolean contains(String ipAddr) throws java.net.UnknownHostException {
        return contains(InetAddress.getByName(ipAddr));
    }

    /**
     * <P>
     * Returns an Iterator object that can be used to step through all the
     * address encapsulated in the object.
     * </P>
     *
     * <P>
     * The iterator returns objects of type
     * {@link java.net.InetAddress InetAddress}or <code>null</code> if the
     * address is unknown.
     * </p>
     *
     * @see java.net.InetAddress
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<InetAddress> iterator() {
        return new IPAddressRangeGenerator(m_begin, m_end);
    }

    /**
     * <P>
     * Returns an Enumeration object that can be used to list out all the
     * address contained in the encapsulated range.
     * </P>
     * 
     * <P>
     * The iterator returns objects of type
     * {@link java.net.InetAddress InetAddress}or <code>null</code> if the
     * address is unknown.
     * </p>
     * 
     * @see java.net.InetAddress
     */
    Enumeration<InetAddress> elements() {
        return new IPAddressRangeGenerator(m_begin, m_end);
    }
}
