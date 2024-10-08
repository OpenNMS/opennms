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
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;

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
public class IPPollRange implements Iterable<IPPollAddress>, Serializable {
    private static final long serialVersionUID = -287583115922481242L;

    /**
     * The range to cycle over.
     */
    private final IPAddrRange m_range;

    private final String m_foreignSource;

    private final String m_location;

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
            return new IPPollAddress(m_foreignSource, m_location, (InetAddress) m_range.nextElement(), m_timeout, m_retries);
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
    public IPPollRange(String foreignSource, String location, String fromIP, String toIP, long timeout, int retries) throws java.net.UnknownHostException {
        m_range = new IPAddrRange(fromIP, toIP);
        m_foreignSource = foreignSource;
        m_location = location;
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
    public IPPollRange(String foreignSource, String location, InetAddress start, InetAddress end, long timeout, int retries) {
        this(foreignSource, location, new IPAddrRange(start, end), timeout, retries);
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
    IPPollRange(String foreignSource, String location, IPAddrRange range, long timeout, int retries) {
        m_range = range;
        m_foreignSource = foreignSource;
        m_location = location;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * Foreign source where this address should be persisted.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    /**
     * Network location of this address.
     */
    public String getLocation() {
        return m_location;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("foreignSource", m_foreignSource)
            .append("location", m_location)
            .append("range", m_range)
            .append("timeout", m_timeout)
            .append("retries", m_retries)
            .toString();
    }
}
