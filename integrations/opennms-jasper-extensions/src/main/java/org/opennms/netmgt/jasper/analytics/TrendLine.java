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

import java.awt.Point;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.integrations.R.RScriptException;
import org.opennms.netmgt.integrations.R.RScriptExecutor;
import org.opennms.netmgt.integrations.R.RScriptInput;
import org.opennms.netmgt.integrations.R.RScriptOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Fits a trend line to the samples in a column
 * using R.
 *
 * @author jwhite
 */
public class TrendLine implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(TrendLine.class);
    private static final String PATH_TO_R_SCRIPT = "/org/opennms/netmgt/jasper/analytics/trendLine.R";

    private final TrendLineConfig m_config;

    public TrendLine(TrendLineConfig config) {
        m_config = config;
    }

    @Override
    public void filter(RowSortedTable<Integer, String, Double> table) throws RScriptException {
        Preconditions.checkArgument(table.containsColumn("Timestamp"), "Data source must have a 'Timestamp' column.");

        // Determine the index of the first and last non-NaN values
        // Assume the values between these are contiguous
        Point rowsWithValues = DataSourceUtils.getRowsWithValues(table, m_config.getInputColumn());
        int firstRowWithValues = rowsWithValues.x;
        int lastRowWithValues = rowsWithValues.y;

        // Make sure we have some samples
        int numSampleRows = lastRowWithValues - firstRowWithValues;
        if (numSampleRows < 1) {
            LOG.error("Insufficent values in column for trending. Excluding trend from data source.");
            return;
        }

        // Determine the step size
        Date lastTimestamp = new Date(table.get(lastRowWithValues, "Timestamp").longValue());
        long stepInMs = (long)(table.get(lastRowWithValues, "Timestamp") - table.get(lastRowWithValues-1, "Timestamp"));

        // Num steps ahead
        int numStepsAhead = (int)Math.floor(m_config.getSecondsAhead() * 1000 / stepInMs);
        numStepsAhead = Math.max(1, numStepsAhead);

        // Script arguments
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("inputColumn", m_config.getInputColumn());
        arguments.put("polynomialOrder", m_config.getPolynomialOrder());
        // Array indices in R start at 1
        arguments.put("firstIndex", firstRowWithValues+1);
        arguments.put("lastIndex", lastRowWithValues+1);

        // Calculate the trend line/curve
        RScriptExecutor executor = new RScriptExecutor();
        RScriptOutput output = executor.exec(PATH_TO_R_SCRIPT, new RScriptInput(table, arguments));
        ImmutableTable<Integer, String, Double> outputTable = output.getTable();

        // Convert the result to a polynomial
        Polynomial poly = new Polynomial(outputTable.column("x").values().toArray(new Double[0]));

        // Calculate the value of the polynomial for all of the samples
        // and the requested number of steps ahead
        for (int i = firstRowWithValues; i <= (lastRowWithValues + numStepsAhead); i++) {
            if (i >= lastRowWithValues) {
                table.put(i, "Timestamp", (double)new Date(lastTimestamp.getTime() + stepInMs * (i-lastRowWithValues)).getTime());
            }
            double x = table.get(i, "Timestamp");
            table.put(i, m_config.getOutputColumn(), poly.eval(x));
        }
    }

    private static class Polynomial {
        private final List<Double> m_coeffs;
 
        public Polynomial(Double[] coeffs) {
            m_coeffs = Lists.newLinkedList();
            // R may return NaNs for some of the higher order coefficients, 
            // so we add all of the coefficients until a null or NaN is reached
            for (int i = 0; i < coeffs.length; i++) {
                if (coeffs[i] == null || Double.isNaN(coeffs[i])) {
                    break;
                }
                m_coeffs.add(coeffs[i]);
            }
        }

        public double eval(double x) {
            double sum = 0;
            int k = 0;
            for (Double coeff : m_coeffs) {
                sum += coeff * Math.pow(x, k++);
            }
            return sum;
        }

        public String toString() {
            return "Polynomial [" + m_coeffs + "]";
        }
    }
}
