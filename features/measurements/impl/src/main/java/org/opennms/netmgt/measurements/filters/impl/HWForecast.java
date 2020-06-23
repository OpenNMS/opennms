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

import java.util.Date;
import java.util.Map;

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
import com.google.common.collect.TreeBasedTable;

/**
 * Performs Holt-Winters forecasting on a given column of
 * the data source with R.
 *
 * @author jwhite
 */
@FilterInfo(name="HoltWinters", description="Performs Holt-Winters forecasting.", backend="R")
public class HWForecast implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(HWForecast.class);
    private static final String PATH_TO_R_SCRIPT = "/org/opennms/netmgt/measurements/filters/impl/holtWinters.R";

    @FilterParam(key="inputColumn", required=true, displayName="Input", description="Input column.")
    private String m_inputColumn;

    @FilterParam(key="outputPrefix", value="HW", displayName="Output", description="Output prefix.")
    private String m_outputPrefix;

    @FilterParam(key="numPeriodsToForecast", value="3", displayName="# Periods", description="Number of periods to forecast.")
    private int m_numPeriodsToForecast;

    @FilterParam(key="periodInSeconds", required=true, displayName="Period", description="Size of a period in seconds.")
    private long m_periodInSeconds;

    @FilterParam(key="confidenceLevel", value="0.95", displayName="Level", description="Probability used for confidence bounds. Set this to 0 in order to disable the bounds.")
    private double m_confidenceLevel;

    protected HWForecast() {}

    public HWForecast(String outputPrefix, String inputColumn,
            int numPeriodsToForecast, long periodInSeconds,
            double confidenceLevel) {
        m_outputPrefix = outputPrefix;
        m_inputColumn = inputColumn;
        m_numPeriodsToForecast = numPeriodsToForecast;
        m_periodInSeconds = periodInSeconds;
        m_confidenceLevel = confidenceLevel;
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
            LOG.error("Insufficient values in column for forecasting. Excluding forecast columns from data source.");
            return;
        }

        // Determine the step size
        Date lastTimestamp = new Date(table.get(limits.lastRowWithValues, TIMESTAMP_COLUMN_NAME).longValue());
        long stepInMs = (long)(table.get(limits.lastRowWithValues, TIMESTAMP_COLUMN_NAME) - table.get(limits.lastRowWithValues-1, Filter.TIMESTAMP_COLUMN_NAME));

        // Calculate the number of samples per period
        int numSamplesPerPeriod = (int)Math.floor(m_periodInSeconds * 1000d / stepInMs);
        numSamplesPerPeriod = Math.max(1, numSamplesPerPeriod);

        // Calculate the number of steps to forecast
        int numForecasts = numSamplesPerPeriod * m_numPeriodsToForecast;

        // Script arguments
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("columnToForecast", m_inputColumn);
        arguments.put("numSamplesPerSeason", numSamplesPerPeriod);
        arguments.put("numForecasts", numForecasts);
        arguments.put("confidenceLevel", m_confidenceLevel);
        // Array indices in R start at 1
        arguments.put("firstIndex", limits.firstRowWithValues+1);
        arguments.put("lastIndex", limits.lastRowWithValues+1);

        // Make the forecasts
        RScriptExecutor executor = new RScriptExecutor();
        RScriptOutput output = executor.exec(PATH_TO_R_SCRIPT, new RScriptInput(table, arguments));
        ImmutableTable<Long, String, Double> outputTable = output.getTable();

        // The output table contains the fitted values, followed
        // by the requested number of forecasted values
        int numOutputRows = outputTable.rowKeySet().size();
        int numFittedValues = numOutputRows - numForecasts;

        // Add the fitted values to rows where the input column has values
        for (long i = 0; i < numFittedValues; i++) {
            long idxTarget = i + (numSampleRows - numFittedValues) + limits.firstRowWithValues + 1;
            table.put(idxTarget, m_outputPrefix + "Fit", outputTable.get(i, "fit"));
        }

        // Append the forecasted values and include the time stamp with the appropriate step
        for (long i = numFittedValues; i < numOutputRows; i++) {
            long idxForecast = i - numFittedValues + 1;
            long idxTarget = limits.lastRowWithValues + idxForecast;
            if (m_confidenceLevel > 0) {
                table.put(idxTarget, m_outputPrefix + "Fit", outputTable.get(i, "fit"));
                table.put(idxTarget, m_outputPrefix + "Lwr", outputTable.get(i, "lwr"));
                table.put(idxTarget, m_outputPrefix + "Upr", outputTable.get(i, "upr"));
            }
            table.put(idxTarget, TIMESTAMP_COLUMN_NAME, (double)new Date(lastTimestamp.getTime() + stepInMs * idxForecast).getTime());
        }
    }

    public static void checkForecastSupport() throws RScriptException {
        // Verify the HW filter
        HWForecast forecastFilter = new HWForecast("HW", "X", 1, 1, 0.95);

        // Use constant values for the Y column
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, Filter.TIMESTAMP_COLUMN_NAME, (double)(i * 1000));
            table.put(i, "X", 1.0d);
        }

        // Apply the filter
        forecastFilter.filter(table);
    }
}
