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

package org.opennms.netmgt.jasper.analytics;

import java.util.Map;

import org.opennms.netmgt.integrations.R.RScriptException;
import org.opennms.netmgt.integrations.R.RScriptExecutor;
import org.opennms.netmgt.integrations.R.RScriptInput;
import org.opennms.netmgt.integrations.R.RScriptOutput;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Performs outlier removal and interpolation using R.
 *
 * @see {@link org.opennms.netmgt.jasper.analytics.OutlierFilterConfig}
 * @author jwhite
 */
public class OutlierFilter implements Filter {
    private static final String PATH_TO_R_SCRIPT = "/org/opennms/netmgt/jasper/analytics/outlierFilter.R";
    private final OutlierFilterConfig m_config;

    public OutlierFilter(OutlierFilterConfig config) {
        m_config = config;
    }

    @Override
    public void filter(RowSortedTable<Integer, String, Double> dsAsTable) throws RScriptException {
        String columnToFilter = m_config.getInputColumn();

        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("columnToFilter", columnToFilter);
        arguments.put("probability", m_config.getProbability());

        RScriptExecutor executor = new RScriptExecutor();
        RScriptOutput output = executor.exec(PATH_TO_R_SCRIPT, new RScriptInput(dsAsTable, arguments));
        ImmutableTable<Integer, String, Double> outputTable = output.getTable();

        // Replace all of the values in the given column with those returned
        // by the script
        int numRowsInTable = dsAsTable.rowKeySet().size();
        for (int i = 0; i < numRowsInTable; i++) {
            dsAsTable.put(i, columnToFilter, outputTable.get(i, columnToFilter));
        }

        // Perform linear interpolation on missing values
        linearInterpolation(dsAsTable);
    }

    public void linearInterpolation(RowSortedTable<Integer, String, Double> dsAsTable) {
        final String columnToFilter = m_config.getInputColumn();
        final Map<Integer, Double> column = dsAsTable.column(columnToFilter);
        final Map<Integer, Double> interpolatedValues = Maps.newHashMap();

        Integer x0 = null;
        for (Map.Entry<Integer, Double> entry : column.entrySet()) {
            int x = entry.getKey();
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
                    for (int xnot = x0 + 1; xnot < x; xnot++) {
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
