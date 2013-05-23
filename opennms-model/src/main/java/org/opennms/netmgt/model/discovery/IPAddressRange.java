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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * IPAddressRange
 *
 * @author brozow
 * @version $Id: $
 */
public class IPAddressRange implements Comparable<IPAddressRange>, Iterable<IPAddress> {
    
    private final IPAddress m_begin;
    private final IPAddress m_end;
    
    public IPAddressRange(String singleton) {
        this(singleton, singleton);
    }
    
    public IPAddressRange(IPAddress singleton) {
        this(singleton, singleton);
    }

    /**
     * <p>Constructor for IPAddressRange.</p>
     *
     * @param begin a {@link java.lang.String} object.
     * @param end a {@link java.lang.String} object.
     */
    public IPAddressRange(String begin, String end) {
        this(new IPAddress(begin), new IPAddress(end));
    }
    
    /**
     * <p>Constructor for IPAddressRange.</p>
     *
     * @param begin a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @param end a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddressRange(IPAddress begin, IPAddress end) {
        if (begin.isGreaterThan(end)) {
            throw new IllegalArgumentException(String.format("beginning of range (%s) must come before end of range (%s)", begin, end));
        }
        m_begin = begin;
        m_end = end;
    }
    
    /**
     * <p>getBegin</p>
     *
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress getBegin() {
        return m_begin;
    }
    
    /**
     * <p>getEnd</p>
     *
     * @return a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     */
    public IPAddress getEnd() {
        return m_end;
    }
    
    /**
     * <p>size</p>
     *
     * @return a long.
     */
    public BigInteger size() {
        BigInteger size = m_end.toBigInteger();
        size = size.subtract(m_begin.toBigInteger());
        // Add 1 because the range is inclusive of beginning and end
        size = size.add(new BigInteger("1"));
        return size;
    }

    /**
     * <p>contains</p>
     *
     * @param addr a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean contains(IPAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException("addr should not be null");
        }
        return addr.isGreaterThanOrEqualTo(m_begin) && addr.isLessThanOrEqualTo(m_end);
    }
    
    /**
     * <p>contains</p>
     *
     * @param addr a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean contains(String addr) {
        return contains(new IPAddress(addr));
    }
    
    /**
     * <p>contains</p>
     *
     * @param range a {@link org.opennms.netmgt.model.discovery.IPAddressRange} object.
     * @return a boolean.
     */
    public boolean contains(IPAddressRange range) {
        return this.contains(range.getBegin()) && this.contains(range.getEnd());
    }
    
    /**
     * <p>overlaps</p>
     *
     * @param range a {@link org.opennms.netmgt.model.discovery.IPAddressRange} object.
     * @return a boolean.
     */
    public boolean overlaps(IPAddressRange range) {
        return this.contains(range.getBegin()) || this.contains(range.getEnd())
        || range.contains(this.getBegin()) || range.contains(this.getEnd());
    }
    
    /**
     * <p>comesBefore</p>
     *
     * @param addr a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean comesBefore(IPAddress addr) {
        return m_end.isLessThan(addr);
    }
    
    /**
     * <p>comesBefore</p>
     *
     * @param range a {@link org.opennms.netmgt.model.discovery.IPAddressRange} object.
     * @return a boolean.
     */
    public boolean comesBefore(IPAddressRange range) {
        return comesBefore(range.getBegin());
    }
    
    /**
     * <p>comesAfter</p>
     *
     * @param addr a {@link org.opennms.netmgt.model.discovery.IPAddress} object.
     * @return a boolean.
     */
    public boolean comesAfter(IPAddress addr) {
        return m_begin.isGreaterThan(addr);
    }
    
    /**
     * <p>comesAfter</p>
     *
     * @param range a {@link org.opennms.netmgt.model.discovery.IPAddressRange} object.
     * @return a boolean.
     */
    public boolean comesAfter(IPAddressRange range) {
        return comesAfter(range.getEnd());
    }
    
    /**
     * <p>adjoins</p>
     *
     * @param range a {@link org.opennms.netmgt.model.discovery.IPAddressRange} object.
     * @return a boolean.
     */
    public boolean adjoins(IPAddressRange range) {
        return this.comesImmediatelyBefore(range) || this.comesImmediatelyAfter(range);
    }

    private boolean comesImmediatelyAfter(IPAddressRange range) {
        return this.comesAfter(range) && getBegin().isSuccessorOf(range.getEnd());
    }

    private boolean comesImmediatelyBefore(IPAddressRange range) {
        return this.comesBefore(range) && getEnd().isPredecessorOf(range.getBegin());
    }
    
    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<IPAddress> iterator() {
        return new IPAddressRangeIterator(this);
    }
    
    private static class IPAddressRangeIterator implements Iterator<IPAddress> {
        
        private final IPAddressRange m_range;
        private IPAddress m_next;

        public IPAddressRangeIterator(IPAddressRange range) {
            m_range = range;
            m_next = range.getBegin();
        }

        @Override
        public boolean hasNext() {
            return (m_next != null);
        }

        @Override
        public IPAddress next() {
            if (m_next == null) {
                throw new NoSuchElementException("Already returned the last element");
            }
            
            final IPAddress next = m_next;
            m_next = next.incr();
            if (!m_range.contains(m_next)) {
                m_next = null;
            }
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("IPAddressRangeIterator.remove() is not yet implemented");
        }
        
    }
    
    @Override
    public int compareTo(IPAddressRange r) {
        if (this.comesBefore(r)) {
            // this is less than 
            return -1;
        } else if (this.comesAfter(r)) {
            // this is greater than
            return 1;
        } else {
            // otherwise it overlaps
            return 0;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IPAddressRange) {
            IPAddressRange other = (IPAddressRange) obj;
            return this.m_begin.equals(other.m_begin) && this.m_end.equals(other.m_end);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 31 * m_begin.hashCode() + m_end.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append('[').append(m_begin).append(',').append(m_end).append(']');
        return buf.toString();
    }

    public boolean isSingleton() {
        return getBegin().equals(getEnd());
    }

    public boolean combinable(IPAddressRange range) {
        return overlaps(range) || adjoins(range);
    }

    public IPAddressRange combine(IPAddressRange range) {
        if (!combinable(range)) {
            throw new IllegalArgumentException(String.format("Range %s is not combinable with range %s", this, range));
        }
        return new IPAddressRange(IPAddress.min(range.getBegin(), getBegin()),IPAddress.max(getEnd(), range.getEnd()));
    }

    public IPAddressRange[] remove(IPAddressRange range) {
        if (range.contains(this)) {
            return new IPAddressRange[0];
        } else if (!overlaps(range)) {
            return new IPAddressRange[] { this };
        } else {
            List<IPAddressRange> ranges = new ArrayList<IPAddressRange>(2);
            if (getBegin().isLessThan(range.getBegin())) {
                ranges.add(new IPAddressRange(getBegin(), range.getBegin().decr()));
            }
            if (range.getEnd().isLessThan(getEnd())) {
                ranges.add(new IPAddressRange(range.getEnd().incr(), getEnd()));
            }
            return ranges.toArray(new IPAddressRange[ranges.size()]);
            
        }
    }

}
