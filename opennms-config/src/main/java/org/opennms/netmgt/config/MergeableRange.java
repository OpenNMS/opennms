/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.math.BigInteger;
import java.net.UnknownHostException;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.snmp.Range;

/**
 * This class wraps the castor generated Range class found in the SnmpConfig class.
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
final class MergeableRange implements Comparable<Range> {
    private static final Logger LOG = LoggerFactory.getLogger(MergeableRange.class);
    private Range m_range;
    private static final RangeComparator m_comparator = new RangeComparator();
    private final MergeableSpecific m_first;
    private final MergeableSpecific m_last;
    
    /**
     * <p>Constructor for MergeableRange.</p>
     *
     * @param range a {@link org.opennms.netmgt.config.common.Range} object.
     */
    public MergeableRange(Range range) {
        m_range = range;
        m_first = new MergeableSpecific(range.getBegin());
        m_last = new MergeableSpecific(range.getEnd());
    }
    
    /*
     * Compares two snmp-config.xml ranges
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(T)
     */
    /**
     * <p>compareTo</p>
     *
     * @param range a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a int.
     */
    @Override
    public int compareTo(final Range range) {
        return m_comparator.compare(getRange(), range);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        boolean equals = false;
        
        if (obj == null) {
            equals = false;
        } else if (obj instanceof Range ) {
            equals = equalsRange((Range)obj);
        }
        return equals;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * <p>equals</p>
     *
     * @param range a {@link org.opennms.netmgt.config.MergeableRange} object.
     * @return a boolean.
     */
    private boolean equalsMergeableRange(MergeableRange range) {
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
    private boolean equalsRange(Range range) {
        return equalsMergeableRange(new MergeableRange(range));
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
        return new BigInteger("-1").equals(InetAddressUtils.difference(m_last.getSpecific(), nextRange.getBegin()));
    }
    
    /**
     * <p>isAdjacentToEnd</p>
     *
     * @param nextRange a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean isAdjacentToEnd(Range nextRange) {
        return new BigInteger("-1").equals(InetAddressUtils.difference(m_first.getSpecific(), nextRange.getEnd()));
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
            throw new IllegalArgumentException("Specific: "+spec+", doesn't affect range: ");

        MergeableSpecific specific = new MergeableSpecific(spec);
        
        Range newRange = null;
        
        ByteArrayComparator comparator = new ByteArrayComparator();
        try {
            if (comparator.compare(specific.getValue(), getFirst().getValue()) == 0) {
                getRange().setBegin(InetAddressUtils.incr(specific.getSpecific()));
            } else if (comparator.compare(specific.getValue(), getLast().getValue()) == 0) {
                getRange().setEnd(InetAddressUtils.decr(specific.getSpecific()));
            } else {
                newRange = new Range();
                newRange.setBegin(InetAddressUtils.incr(specific.getSpecific()));
                newRange.setEnd(getRange().getEnd());
                getRange().setEnd(InetAddressUtils.decr(specific.getSpecific()));
            }
        } catch (UnknownHostException e) {
            LOG.error("Error converting string to IP address", e);
        }

        return newRange;
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
