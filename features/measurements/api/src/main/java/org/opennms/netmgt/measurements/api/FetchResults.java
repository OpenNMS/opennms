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
package org.opennms.netmgt.measurements.api;

import java.util.Arrays;
import java.util.Map;

import org.opennms.netmgt.measurements.model.QueryMetadata;

import com.google.common.base.MoreObjects;
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

    private final QueryMetadata m_metadata;

    public FetchResults(final long[] timestamps, Map<String, double[]> columns, final long step, final Map<String, Object> constants, final QueryMetadata metadata) {
        Preconditions.checkNotNull(timestamps, "timestamps argument");
        Preconditions.checkNotNull(columns, "columns argument");
        Preconditions.checkNotNull(constants, "constants argument");

        m_timestamps = timestamps;
        m_columns = columns;
        m_step = step;
        m_constants = constants;
        m_metadata = metadata;
    }

    /**
     * Used when applying filters.
     */
    public FetchResults(final RowSortedTable<Long, String, Double> table, final long step, final Map<String, Object> constants, final QueryMetadata metadata) {
        Preconditions.checkNotNull(table, "table argument");
        Preconditions.checkNotNull(constants, "constants argument");

        m_step = step;
        m_constants = constants;
        m_metadata = metadata;

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

        for (String columnName : table.columnKeySet()) {
            final Map<Long, Double> columnMap = table.column(columnName);
            if (Filter.TIMESTAMP_COLUMN_NAME.equals(columnName)) {
                for (int k = 0; k < numRows; k++) {
                    Double value = columnMap.get((long)k);
                    Preconditions.checkNotNull(value, "filter timestamps must be contiguous");
                    m_timestamps[k] = value.longValue();
                }
            } else {
                double column[] = new double[numRows];
                m_columns.put(columnName, column);

                for (int k = 0; k < numRows; k++) {
                    Double value = columnMap.get((long)k);
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

    public QueryMetadata getMetadata() {
        return m_metadata;
    }

    public String toString() {
       return MoreObjects.toStringHelper(this.getClass())
            .add("timestamps", Arrays.toString(m_timestamps))
            .add("columns", m_columns)
            .add("step", m_step)
            .add("constants", m_constants)
            .add("metadata", m_metadata)
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
