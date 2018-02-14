/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.api;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

/**
 * Used to store the results of a fetch.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public class FetchResults {

    private final long[] m_timestamps;

    private final Map<String, double[]> m_columns;

    private final long m_step;

    private final Map<String, Object> m_constants;

    public FetchResults(final long[] timestamps, Map<String, double[]> columns, final long step, final Map<String, Object> constants) {
        Preconditions.checkNotNull(timestamps, "timestamps argument");
        Preconditions.checkNotNull(columns, "columns argument");
        Preconditions.checkNotNull(constants, "constants argument");

        m_timestamps = timestamps;
        m_columns = columns;
        m_step = step;
        m_constants = constants;
    }

    /**
     * Used when applying filters.
     */
    public FetchResults(final RowSortedTable<Long, String, Double> table, final long step, final Map<String, Object> constants) {
        Preconditions.checkNotNull(table, "table argument");
        Preconditions.checkNotNull(constants, "constants argument");

        m_step = step;
        m_constants = constants;

        if (table.size() < 1) {
            // No rows
            m_timestamps = new long[0];
            m_columns = Maps.newHashMapWithExpectedSize(0);
            return;
        }

        Long firstIndex = null;
        Long lastIndex = null;
        Map<Long, Double> timestampsByIndex = table.column(Filter.TIMESTAMP_COLUMN_NAME);
        for (Long index : timestampsByIndex.keySet()) {
            if (firstIndex == null) {
                firstIndex = index;
            } else {
                Preconditions.checkState(index == (lastIndex + 1), "filter timestamps must be contiguous");
            }
            lastIndex = index;
        }

        int numRows = (int)(lastIndex - firstIndex) + 1;
        m_columns = Maps.newLinkedHashMap(); // preserve ordering
        m_timestamps = new long[numRows];

        for (int k = 0; k < numRows; k++) {
            for (String columnName : table.columnKeySet()) {
                Double value = table.get(Long.valueOf(k), columnName);

                if (Filter.TIMESTAMP_COLUMN_NAME.equals(columnName)) {
                    Preconditions.checkNotNull(value, "filter timestamps must be contiguous");
                    m_timestamps[k] = value.longValue();
                } else {
                    double column[] = m_columns.get(columnName);
                    if (column == null) {
                        column = new double[numRows];
                        m_columns.put(columnName, column);
                    }

                    if (value == null) {
                        column[k] = Double.NaN;
                    } else {
                        column[k] = value;
                    }
                }
            }
        }
    }

    public long[] getTimestamps() {
        return m_timestamps;
    }

    public Map<String, double[]> getColumns() {
        return m_columns;
    }

    public long getStep() {
        return m_step;
    }

    public Map<String, Object> getConstants() {
        return m_constants;
    }

    public String toString() {
       return Objects.toStringHelper(this.getClass())
            .add("timestamps", Arrays.toString(m_timestamps))
            .add("columns", m_columns)
            .add("step", m_step)
            .add("constants", m_constants)
            .toString();
    }

    public RowSortedTable<Long, String, Double> asRowSortedTable() {
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();

        for (int i = 0; i < m_timestamps.length; i++) {
            table.put(Long.valueOf(i), Filter.TIMESTAMP_COLUMN_NAME, (double)m_timestamps[i]);
            for (String column : m_columns.keySet()) {
                table.put(Long.valueOf(i), column, m_columns.get(column)[i]);
            }
        }
        return table;
    }
}
