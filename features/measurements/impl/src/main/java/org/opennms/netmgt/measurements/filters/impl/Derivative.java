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
package org.opennms.netmgt.measurements.filters.impl;

import java.util.Map.Entry;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.collect.RowSortedTable;

/**
 * Calculates the derivative. Useful to converting gauges into rates.
 *
 * @author jwhite
 */
@FilterInfo(name="Derivative", description="Calculates the derivative (rate of change) between rows.")
public class Derivative implements Filter {

    @FilterParam(key="inputColumn", required=true, displayName="Input", description="Input column.")
    private String m_inputColumn;

    @FilterParam(key="outputColumn", required=true, displayName="Output", description="Output column.")
    private String m_outputColumn;

    protected Derivative() { }

    public Derivative(String inputColumn, String outputColumn) {
        m_inputColumn = inputColumn;
        m_outputColumn = outputColumn;
    }

    @Override
    public void filter(RowSortedTable<Long, String, Double> qrAsTable) throws Exception {
        Entry<Long, Double> lastEntry = null;
        for (Entry<Long, Double> entry : qrAsTable.column(m_inputColumn).entrySet()) {
            double slope = Double.NaN;
            if (lastEntry != null) {
                final long step = entry.getKey() - lastEntry.getKey();
                if (step != 0) {
                    slope = (entry.getValue() - lastEntry.getValue()) / step;
                }
            }
            qrAsTable.put(entry.getKey(), m_outputColumn, slope);
            lastEntry = entry;
        }
    }
}
