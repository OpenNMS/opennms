/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import java.util.Date;
import java.util.Map;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.opennms.netmgt.integrations.R.RScriptException;
import org.opennms.netmgt.integrations.R.RScriptExecutor;
import org.opennms.netmgt.integrations.R.RScriptInput;
import org.opennms.netmgt.integrations.R.RScriptOutput;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;
import org.opennms.netmgt.measurements.filters.impl.Utils.TableLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Fits a trend line to the samples in a column.
 *
 * @author jwhite
 */
@FilterInfo(name="Trend", description="Fits a trend line or polynomial to a given column.")
public class TrendLine implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(TrendLine.class);

    @FilterParam(key="inputColumn", required=true, displayName="Input", description="Input column.")
    private String m_inputColumn;

    @FilterParam(key="outputColumn", required=true, displayName="Output", description="Output column.")
    private String m_outputColumn;

    @FilterParam(key="secondsAhead", value="0", displayName="Forecast", description="Number seconds ahead the of the column for which we want to include the trend line.")
    private long m_secondsAhead;

    @FilterParam(key="polynomialOrder", value="1", displayName="Order", description="Polynomial order of the trend line/curve. Set this to 1 for a line.")
    private int m_polynomialOrder;

    protected TrendLine() {}

    public TrendLine(String outputColumn, String inputColumn, long secondsAhead, int polynomialOrder) {
        m_outputColumn = outputColumn;
        m_inputColumn = inputColumn;
        m_secondsAhead = secondsAhead;
        m_polynomialOrder = polynomialOrder;
    }

    @Override
    public void filter(RowSortedTable<Long, String, Double> table) throws RScriptException {
        Preconditions.checkArgument(table.containsColumn(TIMESTAMP_COLUMN_NAME), String.format("Data source must have a '%s' column.", Filter.TIMESTAMP_COLUMN_NAME));

        // Determine the index of the first and last non-NaN values
        // Assume the values between these are contiguous
        TableLimits limits = Utils.getRowsWithValues(table, m_inputColumn);

        // Make sure we have some samples
        long numSampleRows = limits.lastRowWithValues - limits.firstRowWithValues;
        if (numSampleRows < 1) {
            LOG.error("Insufficient values in column for trending. Excluding trend from data source.");
            return;
        }

        // Gather the values [(x,y),...]
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        for (long i = limits.firstRowWithValues; i < limits.lastRowWithValues; i++) {
            obs.add(table.get(i, TIMESTAMP_COLUMN_NAME), table.get(i, m_inputColumn));
        }

        // Fit the polynomial
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(m_polynomialOrder);
        final double[] coeff = fitter.fit(obs.toList());
        PolynomialFunction polynomialFunction = new PolynomialFunction(coeff);

        // Determine the step size
        Date lastTimestamp = new Date(table.get(limits.lastRowWithValues, TIMESTAMP_COLUMN_NAME).longValue());
        long stepInMs = (long)(table.get(limits.lastRowWithValues, TIMESTAMP_COLUMN_NAME) - table.get(limits.lastRowWithValues-1, Filter.TIMESTAMP_COLUMN_NAME));

        // Num steps ahead
        int numStepsAhead = (int)Math.floor(m_secondsAhead * 1000d / stepInMs);
        numStepsAhead = Math.max(1, numStepsAhead);

        // Calculate the value of the polynomial for all the samples and the requested number of steps ahead
        for (long i = limits.firstRowWithValues; i <= (limits.lastRowWithValues + numStepsAhead); i++) {
            if (i >= limits.lastRowWithValues) {
                table.put(i, TIMESTAMP_COLUMN_NAME, (double)new Date(lastTimestamp.getTime() + stepInMs * (i-limits.lastRowWithValues)).getTime());
            }
            Double timestamp = table.get(i, TIMESTAMP_COLUMN_NAME);
            // Compute the value
            table.put(i, m_outputColumn, polynomialFunction.value(timestamp));
        }
    }
}
