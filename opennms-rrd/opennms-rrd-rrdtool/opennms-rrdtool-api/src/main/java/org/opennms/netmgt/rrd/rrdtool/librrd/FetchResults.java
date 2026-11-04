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
package org.opennms.netmgt.rrd.rrdtool.librrd;

import java.util.HashMap;
import java.util.Map;

/**
 * Results of {@link LibRrd#fetch} or {@link LibRrd#xport}.
 *
 * <p>Values are column-major: {@code getValues()[column][row]}. Row {@code i}
 * is at timestamp {@code getStart() + i * getStep()}.
 */
public class FetchResults {

    private final long m_start;
    private final long m_end;
    private final long m_step;
    private final String[] m_columns;
    private final double[][] m_values;

    public FetchResults(final long start, final long end, final long step, final String[] columns, final double[][] values) {
        m_start = start;
        m_end = end;
        m_step = step;
        m_columns = columns;
        m_values = values;
    }

    public long getStart() {
        return m_start;
    }

    public long getEnd() {
        return m_end;
    }

    public long getStep() {
        return m_step;
    }

    public String[] getColumns() {
        return m_columns;
    }

    public double[][] getValues() {
        return m_values;
    }

    public long[] getTimestamps() {
        final int numRows = m_columns.length > 0 ? m_values[0].length : 0;
        final long[] timestamps = new long[numRows];
        for (int i = 0; i < numRows; i++) {
            timestamps[i] = m_start + m_step * i;
        }
        return timestamps;
    }

    public Map<String, double[]> getColumnsWithValues() {
        final Map<String, double[]> mappedValues = new HashMap<>(m_columns.length);
        for (int i = 0; i < m_columns.length; i++) {
            mappedValues.put(m_columns[i], m_values[i]);
        }
        return mappedValues;
    }
}
