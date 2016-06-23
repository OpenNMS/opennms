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

    @FilterParam(key="quantile", value="0.95", displayName="Quantile", description="Quantile level. Must be > 0 and <= 100. Any values greater than the calculated percentile will be replaced with an interpolated value.")
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
