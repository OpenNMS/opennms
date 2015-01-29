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
import java.util.Map;

import org.opennms.netmgt.integrations.R.RScriptException;
import org.opennms.netmgt.integrations.R.RScriptExecutor;
import org.opennms.netmgt.integrations.R.RScriptInput;
import org.opennms.netmgt.integrations.R.RScriptOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Performs Holt-Winters forecasting on a given column of
 * the data source with R.
 *
 * @author jwhite
 */
public class HWForecast implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(HWForecast.class);
    private static final String PATH_TO_R_SCRIPT = "/org/opennms/netmgt/jasper/analytics/holtWinters.R";
    private final HWForecastConfig m_config;

    public HWForecast(HWForecastConfig config) {
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
            LOG.error("Insufficent values in column for forecasting. Excluding forecast columns from data source.");
            return;
        }

        // Determine the step size
        Date lastTimestamp = new Date(table.get(lastRowWithValues, "Timestamp").longValue());
        long stepInMs = (long)(table.get(lastRowWithValues, "Timestamp") - table.get(lastRowWithValues-1, "Timestamp"));

        // Calculate the number of samples per period
        int numSamplesPerPeriod = (int)Math.floor(m_config.getPeriod() * 1000 / stepInMs);
        numSamplesPerPeriod = Math.max(1, numSamplesPerPeriod);

        // Calculate the number of steps to forecast
        int numForecasts = numSamplesPerPeriod * m_config.getNumPeriodsToForecast();

        // Script arguments
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("columnToForecast", m_config.getInputColumn());
        arguments.put("numSamplesPerSeason", numSamplesPerPeriod);
        arguments.put("numForecasts", numForecasts);
        arguments.put("confidenceLevel", m_config.getConfidenceLevel());
        // Array indices in R start at 1
        arguments.put("firstIndex", firstRowWithValues+1);
        arguments.put("lastIndex", lastRowWithValues+1);

        // Make the forecasts
        RScriptExecutor executor = new RScriptExecutor();
        RScriptOutput output = executor.exec(PATH_TO_R_SCRIPT, new RScriptInput(table, arguments));
        ImmutableTable<Integer, String, Double> outputTable = output.getTable();

        // The output table contains the fitted values, followed
        // by the requested number of forecasted values
        int numOutputRows = outputTable.rowKeySet().size();
        int numFittedValues = numOutputRows - numForecasts;

        // Add the fitted values to rows where the input column has values
        for (int i = 0; i < numFittedValues; i++) {
            int idxTarget = i + (numSampleRows - numFittedValues) + firstRowWithValues + 1;
            table.put(idxTarget, m_config.getOutputPrefix() + "Fit", outputTable.get(i, "fit"));
        }

        // Append the forecasted values and include the time stamp with the appropriate step
        for (int i = numFittedValues; i < numOutputRows; i++) {
            int idxForecast = i - numFittedValues + 1;
            int idxTarget = lastRowWithValues + idxForecast;
            table.put(idxTarget, m_config.getOutputPrefix() + "Fit", outputTable.get(i, "fit"));
            table.put(idxTarget, m_config.getOutputPrefix() + "Lwr", outputTable.get(i, "lwr"));
            table.put(idxTarget, m_config.getOutputPrefix() + "Upr", outputTable.get(i, "upr"));
            table.put(idxTarget, "Timestamp", (double)new Date(lastTimestamp.getTime() + stepInMs * idxForecast).getTime());
        }
    }
}
