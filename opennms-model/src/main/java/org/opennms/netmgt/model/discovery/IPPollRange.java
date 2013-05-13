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

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * <p>
 * This class is designed to encapsulate the information about an address range
 * plus the retry &amp; timeout information. The class is designed so that it
 * can return either an {@link java.util.Enumeration enumeration}or an
 * {@link java.util.Iterator iterator}to traverse the range of addresses.
 * </p>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 */
public class IPPollRange implements Iterable<IPPollAddress> {
    /**
     * The range to cycle over.
     */
    private final IPAddrRange m_range;

    /**
     * The timeout in milliseconds (1/1000th)
     */
    private final long m_timeout;

    /**
     * The number of retries for each generate object.
     */
    private final int m_retries;

    /**
     * <P>
     * The purpose of the IPPollRangeGenerator class is to provide an
     * Enumeration or Iterator object that can be returned by the encapsulating
     * class. The class implements the new style Iterator interface, as well as
     * the old style Enumeration to allow the developer freedom of choice when
     * cycling over ranges.
     * </P>
     * 
     * @see java.util.Iterator
     * @see java.util.Enumeration
     */
    final class IPPollRangeGenerator implements Enumeration<IPPollAddress>, Iterator<IPPollAddress> {
        /**
         * <P>
         * The range of address to generate.
         * </P>
         */
        private Enumeration<InetAddress> m_range;

        /**
         * <P>
         * Creates a poll range generator object.
         * </P>
         * 
         * @param en
         *            The Enumeration to use for address generation.
         */
        public IPPollRangeGenerator(Enumeration<InetAddress> en) {
            m_range = en;
        }

        /**
         * <P>
         * Returns true if the Enumeration described by this object still has
         * more elements.
         * </P>
         */
        @Override
        public boolean hasMoreElements() {
            return m_range.hasMoreElements();
        }

        /**
         * <P>
         * Returns the next IPPollAddress in the enumeration.
         * </P>
         * 
         * @exception java.util.NoSuchElementException
         *                Thrown if there are no more elements in the iteration.
         */
        @Override
        public IPPollAddress nextElement() {
            return new IPPollAddress((InetAddress) m_range.nextElement(), m_timeout, m_retries);
        }

        /**
         * <P>
         * If there are more elements left in the iteration then a value of true
         * is returned. Else a false value is returned.
         * </P>
         */
        @Override
        public boolean hasNext() {
            return hasMoreElements();
        }

        /**
         * <P>
         * Returns the next object in the iteration and increments the internal
         * pointer.
         * </P>
         * 
         * @exception java.util.NoSuchElementException
         *                Thrown if there are no more elements in the iteration.
         */
        @Override
        public IPPollAddress next() {
            return nextElement();
        }

        /**
         * The remove method is part of the Iterator interface and is optional.
         * Since it is not implemnted it will always throw an
         * UnsupportedOperationException.
         * 
         * @exception java.lang.UnsupportedOperationException
         *                Always thrown by this method.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove operation not supported");
        }

    } // end IPPollRangeGenerator

    /**
     * <P>
     * Creates an IPPollRange object that can be used to generate IPPollAddress
     * objects. The addresses are encapsulated by the range object and the
     * values of timeout and retry are set in each generated IPPollAddress
     * object.
     * </P>
     *
     * @param fromIP
     *            The start of the address range to cycle over.
     * @param toIP
     *            The end of the address range to cycle over.
     * @param timeout
     *            The timeout for each generated IPPollAddress.
     * @param retries
     *            The number of retries for generated addresses.
     * @see IPPollAddress
     * @see IPAddrRange
     * @throws java.net.UnknownHostException if any.
     */
    public IPPollRange(String fromIP, String toIP, long timeout, int retries) throws java.net.UnknownHostException {
        m_range = new IPAddrRange(fromIP, toIP);
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * <P>
     * Creates an IPPollRange object that can be used to generate IPPollAddress
     * objects. The addresses are encapsulated by the range [start..end] and the
     * values of timeout and retry are set in each generated IPPollAddress
     * object.
     * </P>
     * 
     * @param start
     *            The start of the address range to cycle over.
     * @param end
     *            The end of the address range to cycle over.
     * @param timeout
     *            The timeout for each generated IPPollAddress.
     * @param retries
     *            The number of retries for generated addresses.
     * 
     * @see IPPollAddress
     * @see IPAddrRange
     * 
     */
    IPPollRange(InetAddress start, InetAddress end, long timeout, int retries) {
        m_range = new IPAddrRange(start, end);
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * <P>
     * Creates an IPPollRange object that can be used to generate IPPollAddress
     * objects. The addresses are encapsulated by the range object and the
     * values of timeout and retry are set in each generated IPPollAddress
     * object.
     * </P>
     * 
     * @param range
     *            The address range to cycle over.
     * @param timeout
     *            The timeout for each generated IPPollAddress.
     * @param retries
     *            The number of retries for generated addresses.
     * 
     * @see IPPollAddress
     * 
     */
    IPPollRange(IPAddrRange range, long timeout, int retries) {
        m_range = range;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * <P>
     * Returns the timeout set for the object. The timeout should be in 1/1000th
     * of a second increments.
     * </P>
     *
     * @return a long.
     */
    public long getTimeout() {
        return m_timeout;
    }

    /**
     * <P>
     * Returns the retry count for the object.
     * </P>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <P>
     * Returns the configured address ranges that are encapsulated by this
     * object.
     * </P>
     *
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddrRange} object.
     */
    public IPAddrRange getAddressRange() {
        return m_range;
    }

    /**
     * <P>
     * Returns an Enumeration that can be used to cycle over the range of
     * pollable addresses.
     * </P>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration<IPPollAddress> elements() {
        return new IPPollRangeGenerator(m_range.elements());
    }

    /**
     * <P>
     * Returns an Iterator object that can be used to cycle over the range of
     * pollable address information.
     * </P>
     *
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<IPPollAddress> iterator() {
        return new IPPollRangeGenerator(m_range.elements());
    }
}
