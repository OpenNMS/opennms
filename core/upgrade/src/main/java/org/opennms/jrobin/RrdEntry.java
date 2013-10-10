/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.jrobin;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The Class RrdEntry.
 * <p>Copied from opennms-tools/opennms-rrd-converter.</p>
 */
public class RrdEntry implements Cloneable {
    
    /** The m_entry map. */
    private TreeMap<String, Double> m_entryMap;
    
    /** The m_timestamp. */
    private long m_timestamp;
    
    /** The m_ds names. */
    private List<String> m_dsNames;

    /**
     * Instantiates a new rrd entry.
     *
     * @param timestamp the timestamp
     * @param dsNames the ds names
     */
    RrdEntry(final long timestamp, final List<String> dsNames) {
        m_entryMap = new TreeMap<String,Double>();
        m_timestamp = timestamp;
        m_dsNames = dsNames;
    }

    /**
     * Gets the value.
     *
     * @param dsName the ds name
     * @return the value
     */
    public Double getValue(final String dsName) {
        return m_entryMap.get(dsName);
    }

    /**
     * Sets the value.
     *
     * @param dsName the ds name
     * @param sample the sample
     */
    public void setValue(final String dsName, final double sample) {
        m_entryMap.put(dsName, sample);
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return m_timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the new timestamp
     */
    public void setTimestamp(final long timestamp) {
        m_timestamp = timestamp;
    }

    /**
     * Gets the ds names.
     *
     * @return the ds names
     */
    public List<String> getDsNames() {
        return m_dsNames;
    }

    /**
     * Coalesce with.
     *
     * @param otherEntry the other entry
     */
    public void coalesceWith(final RrdEntry otherEntry) {
        for (final String key : getDsNames()) {
            final Double myValue = m_entryMap.get(key);
            if (isNumber(myValue)) {
                continue;
            }
            final Double otherValue = otherEntry.getValue(key);
            if (isNumber(otherValue)) {
                setValue(key, otherValue);
            } else {
                setValue(key, Double.NaN);
            }
        }
    }

    /**
     * Checks if is number.
     *
     * @param number the number
     * @return true, if is number
     */
    private boolean isNumber(final Double number) {
        return number != null && !Double.isNaN(number);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("timestamp", m_timestamp)
        .append("entries", m_entryMap)
        .toString();
    }
}