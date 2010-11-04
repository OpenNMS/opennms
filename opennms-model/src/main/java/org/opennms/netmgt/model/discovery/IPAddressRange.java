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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.discovery;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * IPAddressRange
 *
 * @author brozow
 * @version $Id: $
 */
public class IPAddressRange implements Iterable<IPAddress> {
    
    private final IPAddress m_begin;
    private final IPAddress m_end;
    
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

        public boolean hasNext() {
            return (m_next != null);
        }

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

        public void remove() {
            throw new UnsupportedOperationException("IPAddressRangeIterator.remove() is not yet implemented");
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

}
