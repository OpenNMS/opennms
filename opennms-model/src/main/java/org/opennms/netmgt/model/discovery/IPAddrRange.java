/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model.discovery;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.network.IPAddress;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public final class IPAddrRange implements Iterable<InetAddress>, Serializable {

    private static final long serialVersionUID = -106414771861377679L;

    private static final Logger LOG = LoggerFactory.getLogger(IPAddrRange.class);

    /**
     * The starting address for the object.
     */
    private final byte[] m_begin;

    /**
     * The ending address for the object.
     */
    private byte[] m_end;

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
         *            The IP address value, in network order.
         * 
         * @return A {@link java.net.InetAddress} object.
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

            m_next = new BigInteger(1, Arrays.copyOf(start, start.length));
            m_end = new BigInteger(1, Arrays.copyOf(end, end.length));
        }

        /**
         * <P>
         * Returns true if the enumeration object has more elements remaining.
         * </P>
         */
        @Override
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
        @Override
        public InetAddress nextElement() {
            if (!hasMoreElements())
                throw new NoSuchElementException("End of Range");

            InetAddress element = make(m_next);
            m_next = m_next.add(BigInteger.ONE);
            return element;
        }

        /**
         * <P>
         * Returns true if there are more elements in the iteration.
         * </P>
         */
        @Override
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
        @Override
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
        @Override
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
     * the lowest address to the highest address.
     * </P>
     * 
     * @param fromIP
     *            The starting address, resolved by InetAddress.
     * @param toIP
     *            The ending address, resolved by InetAddress.
     * 
     * @see java.net.InetAddressUtils.addr(java.lang.String)
     * 
     * @exception java.net.UnknownHostException
     *                Thrown by the InetAddress class if the hostname cannot be
     *                resolved.
     * 
     */
    IPAddrRange(String fromIP, String toIP) throws java.net.UnknownHostException {
        this(InetAddressUtils.addr(fromIP), InetAddressUtils.addr(toIP));
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
     * lowest address to the highest address.
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
            LOG.warn("The beginning of the address range is greater than the end of the address range ({} - {}), swapping values to create a valid IP address range", InetAddressUtils.str(start), InetAddressUtils.str(end));
            m_end = from;
            m_begin = to;
        } else {
            m_begin = from;
            m_end = to;
        }
    }

    public byte[] getBegin() {
        return m_begin;
    }

    public byte[] getEnd() {
        return m_end;
    }

    public void incrementEnd() {
        m_end = new IPAddress(m_end).incr().toOctets();
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
    	return InetAddressUtils.isInetAddressInRange(ipAddr, m_begin, m_end);
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
    @Override
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("begin", InetAddressUtils.getInetAddress(m_begin))
            .append("end", InetAddressUtils.getInetAddress(m_end))
            .toString();
    }

    /**
     * <P>
     * Returns the size of this range.
     * </P>
     */
    public BigInteger size() {
        return InetAddressUtils.difference(InetAddressUtils.getInetAddress(m_end) , InetAddressUtils.getInetAddress(m_begin)).add(BigInteger.ONE);
    }

}
