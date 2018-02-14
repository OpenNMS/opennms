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

/**
 * Fits a trend line to the samples in a column using R.
 *
 * @author jwhite
 */
@FilterInfo(name="Trend", description="Fits a trend line or polynomial to a given column.", backend="R")
public class TrendLine implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(TrendLine.class);
    private static final String PATH_TO_R_SCRIPT = "/org/opennms/netmgt/measurements/filters/impl/trendLine.R";

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

        // Determine the step size
        Date lastTimestamp = new Date(table.get(limits.lastRowWithValues, TIMESTAMP_COLUMN_NAME).longValue());
        long stepInMs = (long)(table.get(limits.lastRowWithValues, TIMESTAMP_COLUMN_NAME) - table.get(limits.lastRowWithValues-1, Filter.TIMESTAMP_COLUMN_NAME));

        // Num steps ahead
        int numStepsAhead = (int)Math.floor(m_secondsAhead * 1000d / stepInMs);
        numStepsAhead = Math.max(1, numStepsAhead);

        // Script arguments
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("inputColumn", m_inputColumn);
        arguments.put("polynomialOrder", m_polynomialOrder);
        // Array indices in R start at 1
        arguments.put("firstIndex", limits.firstRowWithValues+1);
        arguments.put("lastIndex", limits.lastRowWithValues+1);
        arguments.put("numStepsAhead", numStepsAhead);
        arguments.put("stepInMs", stepInMs);

        // Calculate the trend line/curve
        RScriptExecutor executor = new RScriptExecutor();
        RScriptOutput output = executor.exec(PATH_TO_R_SCRIPT, new RScriptInput(table, arguments));
        ImmutableTable<Long, String, Double> outputTable = output.getTable();

        // Calculate the value of the polynomial for all of the samples
        // and the requested number of steps ahead
        long j = 0;
        for (long i = limits.firstRowWithValues; i <= (limits.lastRowWithValues + numStepsAhead); i++) {
            if (i >= limits.lastRowWithValues) {
                table.put(i, TIMESTAMP_COLUMN_NAME, (double)new Date(lastTimestamp.getTime() + stepInMs * (i-limits.lastRowWithValues)).getTime());
            }
            table.put(i, m_outputColumn, outputTable.get(j++, "x"));
        }
    }
}
