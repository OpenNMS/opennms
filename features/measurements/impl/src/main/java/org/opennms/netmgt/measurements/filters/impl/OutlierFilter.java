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
import java.util.Map.Entry;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;

import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Performs outlier removal and interpolation using R.
 *
 * @see {@link org.opennms.netmgt.measurements.filters.impl.OutlierFilterConfig}
 * @author jwhite
 */
@FilterInfo(name="Outlier", description="Removes outliers and replaces them with interpolated values.")
public class OutlierFilter implements Filter {

    @FilterParam(key="inputColumn", required=true, displayName="Input", description="Input column.")
    private String m_inputColumn;

    @FilterParam(key="quantile", value="0.95", displayName="Quantile", description="Quantile level. Must be > 0 and <= 1. Any values greater than the calculated percentile will be replaced with an interpolated value.")
    private double m_quantile;

    protected OutlierFilter() {}

    public OutlierFilter(String inputColumn, double quantile) {
        m_inputColumn = inputColumn;
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

        // Replace values greater than the percentile with NaNs
        for (Entry<Long, Double> entry : column.entrySet()) {
            if (!entry.getValue().isNaN() && entry.getValue() > nthPercentile) {
                entry.setValue(Double.NaN);
            }
        }

        // Perform linear interpolation on missing values
        linearInterpolation(qrAsTable);
    }

    public void linearInterpolation(RowSortedTable<Long, String, Double> qrAsTable) {
        final Map<Long, Double> column = qrAsTable.column(m_inputColumn);
        final Map<Long, Double> interpolatedValues = Maps.newHashMap();

        Long x0 = null;
        for (Map.Entry<Long, Double> entry : column.entrySet()) {
            long x = entry.getKey();
            double y = entry.getValue();

            if (!Double.isNaN(y)) {
                // If there was a gap in values
                if (x0 != null && x0 != x-1) {
                    double y0 = column.get(x0);

                    // Calculate the slope (m) and intercept (b) for the line
                    // passing between the current point, and the last known value
                    double m = (y0 - y) / (x0 - x);
                    double b = y0 - m * x0;

                    // Interpolate the missing values
                    for (long xnot = x0 + 1; xnot < x; xnot++) {
                        double ynot = m * xnot + b;
                        interpolatedValues.put(xnot, ynot);
                    }
                }

                // Update the index of the last known value
                x0 = x;
            }
        }

        // Update the column values
        column.putAll(interpolatedValues);
    }
}
