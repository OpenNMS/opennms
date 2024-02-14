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

import java.util.Map;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.collect.RowSortedTable;

/**
 * Calculates the percentile of a column and stores it as a constant in another.
 *
 * @author jwhite
 */
@FilterInfo(name="Percentile", description="Calculates percentiles")
public class Percentile implements Filter {

    @FilterParam(key="inputColumn", required=true, displayName="Input", description="Input column.")
    private String m_inputColumn;

    @FilterParam(key="outputColumn", required=true, displayName="Output", description="Output column.")
    private String m_outputColumn;

    @FilterParam(key="quantile", value="0.95", displayName="Quantile", description="Quantile level. Must be > 0 and <= 1.")
    private double m_quantile;

    protected Percentile() { }

    public Percentile(String inputColumn, String outputColumn, double quantile) {
        m_inputColumn = inputColumn;
        m_outputColumn = outputColumn;
        m_quantile = quantile;
    }

    @Override
    public void filter(RowSortedTable<Long, String, Double> qrAsTable) {

        // Extract the values of the input column as a primitive array
        final Map<Long, Double> column = qrAsTable.column(m_inputColumn);
        final double values[] = new double[column.size()];
        int k = 0;
        for (Double value : column.values()) {
            values[k++] = value;
        }

        // Calculate the percentile
        org.apache.commons.math3.stat.descriptive.rank.Percentile percentileCalculator = new org.apache.commons.math3.stat.descriptive.rank.Percentile();
        Double nthPercentile = percentileCalculator.evaluate(values, 100 * m_quantile);

        // Set the values of the output column to the calculated statistics
        for (Long rowKey : column.keySet()) {
            qrAsTable.put(rowKey, m_outputColumn, nthPercentile);
        }
    }
}
