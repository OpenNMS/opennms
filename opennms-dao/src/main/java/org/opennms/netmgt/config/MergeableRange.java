//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.config;

import org.opennms.netmgt.config.common.Range;

/**
 * This class wraps the castor generated Range class found in the SnmpConfig class.
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
final class MergeableRange implements Comparable {
    private Range m_range;
    private RangeComparator m_comparator;
    private final MergeableSpecific m_first;
    private final MergeableSpecific m_last;
    
    /**
     * <p>Constructor for MergeableRange.</p>
     *
     * @param range a {@link org.opennms.netmgt.config.common.Range} object.
     */
    public MergeableRange(Range range) {
        m_range = range;
        m_comparator = new RangeComparator();
        m_first = new MergeableSpecific(range.getBegin());
        m_last = new MergeableSpecific(range.getEnd());
    }
    
    /*
     * Compares two snmp-config.xml ranges
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(T)
     */
    /** {@inheritDoc} */
    public int compareTo(Object range) {
        return m_comparator.compare(getRange(), range);
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        boolean equals = false;
        
        if (obj == null) {
            equals = false;
        } else if (obj instanceof Range ) {
            equals = equals((Range)obj);
        }
        return equals;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return 0;
    }

    /**
     * <p>equals</p>
     *
     * @param range a {@link org.opennms.netmgt.config.MergeableRange} object.
     * @return a boolean.
     */
    public boolean equals(MergeableRange range) {
        boolean equals = false;
        
        if (getFirst() == range.getFirst() && getLast() == range.getLast()) {
            equals = true;
        }
        return equals;
    }
    
    /**
     * <p>equals</p>
     *
     * @param range a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean equals(Range range) {
        return equals(new MergeableRange(range));
    }
    
    /**
     * <p>getRange</p>
     *
     * @return a {@link org.opennms.netmgt.config.common.Range} object.
     */
    public Range getRange() {
        synchronized (m_range) {
            return m_range;
        }        
    }
    
    /**
     * <p>coversSpecific</p>
     *
     * @param spec a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean coversSpecific(String spec) {
        boolean covers = false;
        
        if (getFirst().compareTo(spec) <= 0 && getLast().compareTo(spec) >= 0) {
            covers = true;
        }
        return covers;
    }

    /**
     * <p>overlapsBegin</p>
     *
     * @param rng a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean overlapsBegin(Range rng) {
        boolean overlaps = false;
        if (m_first.compareTo(rng.getBegin()) < 0 
                && m_last.compareTo(rng.getBegin()) >=0 
                && m_last.compareTo(rng.getEnd()) <= 0 ) {
            overlaps = true;
        }
        return overlaps;
    }
    
    /**
     * <p>withInRange</p>
     *
     * @param rng a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean withInRange(Range rng) {
        boolean within = false;
        
        if (m_first.compareTo(rng.getBegin()) >= 0 && m_last.compareTo(rng.getEnd()) <= 0) {
            within = true;
        }
        return within;
    }
    
    /**
     * <p>overlapsEnd</p>
     *
     * @param rng a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean overlapsEnd(Range rng) {
        boolean overlaps = false;
        
        if (m_first.compareTo(rng.getBegin()) >=0 
                && m_first.compareTo(rng.getEnd()) <= 0 
                && m_last.compareTo(rng.getEnd()) > 0) {
            overlaps = true;
        }
        return overlaps;
    }

    /**
     * <p>eclipses</p>
     *
     * @param rng a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean eclipses(Range rng) {
        boolean eclipses = false;
        
        if (m_first.compareTo(rng.getBegin()) <= 0 && m_last.compareTo(rng.getEnd()) >= 0) {
            eclipses = true;
        }
        return eclipses;
    }

    /**
     * <p>isAdjacentToBegin</p>
     *
     * @param nextRange a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean isAdjacentToBegin(Range nextRange) {
        boolean adjacent = false;
        
        if (m_last.compareTo(nextRange.getBegin()) == -1) {
            adjacent = true;
        }
        return adjacent;
    }
    
    /**
     * <p>isAdjacentToEnd</p>
     *
     * @param nextRange a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean isAdjacentToEnd(Range nextRange) {
        boolean adjacent = false;
        
        if (m_first.compareTo(nextRange.getEnd()) == 1) {
            adjacent = true;
        }
        return adjacent;
    }
    
    /**
     * Changes the current Range by moving the end before the specific and
     * creates a new range to the right of the specific ending with the
     * current Range's end address.
     *
     * @return a new Range to be added to Definition
     * @param spec a {@link java.lang.String} object.
     */
    protected Range removeSpecificFromRange(final String spec) {
        
        if (!coversSpecific(spec))
            throw new IllegalArgumentException("Specific: "+spec+", doesn't effect range: ");

        MergeableSpecific specific = new MergeableSpecific(spec);
        
        Range newRange = null;
        
        if (specific.getValue() == getFirst().getValue()) {
            getRange().setBegin(SnmpPeerFactory.toIpAddr(specific.getValue()+1));
        } else if (specific.getValue() == getLast().getValue()) {
            getRange().setEnd(SnmpPeerFactory.toIpAddr(specific.getValue()-1));
        } else {
            newRange = new Range();
            newRange.setBegin(SnmpPeerFactory.toIpAddr(specific.getValue()+1));
            newRange.setEnd(getRange().getEnd());
            getRange().setEnd(SnmpPeerFactory.toIpAddr(specific.getValue()-1));
        }
        return newRange;
    }
        
    /**
     * <p>getComparator</p>
     *
     * @return the comparator
     */
    public RangeComparator getComparator() {
        return m_comparator;
    }

    /**
     * <p>getFirst</p>
     *
     * @return the first
     */
    public MergeableSpecific getFirst() {
        return m_first;
    }

    /**
     * <p>getLast</p>
     *
     * @return the last
     */
    public MergeableSpecific getLast() {
        return m_last;
    }

}
