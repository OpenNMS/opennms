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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.IteratorIterator;

/**
 * IPAddressSet
 *
 * @author brozow
 */
public class IPAddressSet implements Iterable<IPAddress> {
    
    private IPAddressRange m_firstRange;
    private IPAddressSet m_remainingRanges;
    
    public static final IPAddressSet EMPTY = new IPAddressSet();
    
    public IPAddressSet() {
        this((IPAddressRange)null, (IPAddressSet)null);
    }
    
    public IPAddressSet(IPAddress addr) {
    	this(new IPAddressRange(addr, addr), null);
    }
    
    public IPAddressSet(IPAddressRange range) {
    	this(range, null);
    }
    
    public IPAddressSet(IPAddressSet set) {
        this(set.m_firstRange, set.m_remainingRanges);
    }

    private IPAddressSet(IPAddressRange firstRange, IPAddressSet remainingRanges) {
		m_firstRange = firstRange;
		m_remainingRanges = remainingRanges;
	}

    public IPAddressSet(IPAddress begin, IPAddress end) {
        this(end.isLessThan(begin) ? null : new IPAddressRange(begin, end), null);
    }

    public IPAddressSet union(IPAddress addr) {
        return union(new IPAddressRange(addr, addr));
    }

    public IPAddressSet union(IPAddressRange range) {
        if (isEmpty()) {
            // 0. if current set is empty just make a new set containing the range
            return new IPAddressSet(range);
        } else if (range.overlaps(m_firstRange) || range.adjoins(m_firstRange)) {
            // 1. range overlaps or is adjacent to m_firstRange
            IPAddressRange newRange = union(range, m_firstRange);
            return (m_remainingRanges == null ? new IPAddressSet(newRange) : m_remainingRanges.union(newRange));
        } else if (range.comesBefore(m_firstRange)) {
            // 2. range comes entirely before and not adjacent m_firstRange
            return new IPAddressSet(range, this);
        } else {
            // 3. range comes entirely after and not adjacent to m_firstRange
            IPAddressSet remaining = m_remainingRanges == null ? new IPAddressSet(range) : m_remainingRanges.union(range);
            return new IPAddressSet(m_firstRange, remaining);
        }
    }

    private IPAddressRange union(IPAddressRange rangeA, IPAddressRange rangeB) {
        IPAddress newBegin = IPAddress.min(rangeA.getBegin(), rangeB.getBegin());
        IPAddress newEnd = IPAddress.max(rangeA.getEnd(), rangeB.getEnd());
        return new IPAddressRange(newBegin, newEnd);
    }

    public IPAddressSet union(IPAddressSet set) {
        if (isEmpty()) {
            return set;
        } else if (m_remainingRanges == null) {
            return set.union(m_firstRange);
        } else {
            return m_remainingRanges.union(set).union(m_firstRange);
        }
    }

    public boolean contains(IPAddress addr) {
        if (m_firstRange == null) {
            return false;
        } else if (m_firstRange.contains(addr)) {
            return true;
        } else  if (m_remainingRanges == null){
            return false;
        } else {
            return m_remainingRanges.contains(addr);
        }
    }

    public boolean containsAll(IPAddressSet c) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isEmpty() {
        return (m_firstRange == null);
    }

    @SuppressWarnings("unchecked")
    public Iterator<IPAddress> iterator() {
        if (isEmpty()) {
            return Collections.EMPTY_SET.iterator();
        } else if (m_remainingRanges == null) {
            return m_firstRange.iterator();
        } else {
            return new IteratorIterator<IPAddress>(m_firstRange.iterator(), m_remainingRanges.iterator());
        }
    }

    public IPAddressSet minus(IPAddress addr) {
        return minus(new IPAddressRange(addr, addr));
    }
    
    public IPAddressSet minus(IPAddressRange range) {
        if (isEmpty()) {
            return this;
        } else if (m_remainingRanges == null) {
            return minus(m_firstRange, range);
        } else {
            return minus(m_firstRange, range).union(m_remainingRanges.minus(range));
        }
    }

    private IPAddressSet minus(IPAddressRange rangeA, IPAddressRange rangeB) {
        if (!rangeA.overlaps(rangeB)) {
            return this;
        } else {
            return new IPAddressSet(rangeA.getBegin(), rangeB.getBegin().decr()).union(new IPAddressSet(rangeB.getEnd().incr(), rangeA.getEnd()));
        }
    }
    
    public IPAddressSet minus(IPAddressSet set) {
        if (set.isEmpty()) {
            return this;
        } else if (set.m_remainingRanges == null) {
            return minus(set.m_firstRange);
        } else {
            return minus(set.m_firstRange).intersect(minus(set.m_remainingRanges));
        }
    }
    
    public IPAddressSet intersect(IPAddressRange range) {
        if (isEmpty()) {
            return this;
        }
        
        IPAddress newBegin = IPAddress.max(m_firstRange.getBegin(), range.getBegin());
        IPAddress newEnd = IPAddress.min(m_firstRange.getEnd(), range.getEnd());
        
        if (m_remainingRanges == null) {
            return new IPAddressSet(newBegin, newEnd);
        } else if (newEnd.isLessThan(newBegin)) {
            return m_remainingRanges.intersect(range);
        } else {
            return new IPAddressSet(new IPAddressRange(newBegin, newEnd), m_remainingRanges.intersect(range));
        }
    }

    public IPAddressSet intersect(IPAddressSet set) {
        if (set.isEmpty()) {
            return this;
        } else if (set.m_remainingRanges == null) {
            return intersect(set.m_firstRange);
        } else {
            return intersect(set.m_firstRange).union(intersect(set.m_remainingRanges));
        }
    }

    public long size() {
        if (m_firstRange == null) {
            return 0;
        } else if (m_remainingRanges == null) {
            return m_firstRange.size();
        } else {
            return m_firstRange.size()+m_remainingRanges.size();
        }
    }
    
    public int getRangeCount() {
        if (m_firstRange == null) {
            return 0;
        } else if (m_remainingRanges == null) {
            return 1;
        } else {
            return m_remainingRanges.getRangeCount()+1;
        }
    }
    
    public IPAddressRange[] getRanges() {
        List<IPAddressRange> accumulator = new LinkedList<IPAddressRange>();
        computeRanges(accumulator);
        return (IPAddressRange[]) accumulator.toArray(new IPAddressRange[accumulator.size()]);
    }
    
    private void computeRanges(List<IPAddressRange> accumulator) {
        if (m_firstRange != null) {
            accumulator.add(m_firstRange);
        }
        if (m_remainingRanges != null) {
            computeRanges(accumulator);
        }
    }

    @Override
    public String toString() {
        return "{" + toStringHelper(new StringBuilder(), false) + "}";
    }

    private StringBuilder toStringHelper(StringBuilder buf, boolean leadingComma) {
        if (m_firstRange != null) {
            if (leadingComma) {
                buf.append(", ");
            }
            buf.append(m_firstRange);
        }
        if (m_remainingRanges != null) {
            m_remainingRanges.toStringHelper(buf, true);
        }
        return buf;
    }
    
    
 
}
