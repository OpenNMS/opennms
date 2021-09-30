/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.newts.converter.rrd.converter;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;

class RrdEntry implements Cloneable {
    private TreeMap<String, Double> m_entryMap;
    private long m_timestamp;
    private List<String> m_dsNames;

    RrdEntry(final long timestamp, final List<String> dsNames) {
        m_entryMap = new TreeMap<String,Double>();
        m_timestamp = timestamp;
        m_dsNames = dsNames;
    }
    
    public Double getValue(final String dsName) {
        return m_entryMap.get(dsName);
    }

    public void setValue(final String dsName, final double sample) {
        m_entryMap.put(dsName, sample);
    }

    public long getTimestamp() {
        return m_timestamp;
    }

    public void setTimestamp(final long timestamp) {
        m_timestamp = timestamp;
    }

    public List<String> getDsNames() {
        return m_dsNames;
    }

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
    
    private boolean isNumber(final Double number) {
        return number != null && !Double.isNaN(number);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("timestamp", m_timestamp)
            .append("entries", m_entryMap)
            .toString();
    }
}
