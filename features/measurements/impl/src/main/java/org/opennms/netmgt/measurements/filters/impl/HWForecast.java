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

import java.util.Date;


import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;
import org.opennms.netmgt.measurements.filters.impl.Utils.TableLimits;
import org.opennms.netmgt.measurements.filters.impl.holtwinters.HoltWintersPointForecaster;
import org.opennms.netmgt.measurements.filters.impl.holtwinters.HoltWintersPointForecasterParams;
import org.opennms.netmgt.measurements.filters.impl.holtwinters.HoltWintersSeasonalityType;
import org.opennms.netmgt.measurements.filters.impl.holtwinters.HoltWintersTrainingMethod;
import org.opennms.netmgt.measurements.filters.impl.holtwinters.PointForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.RowSortedTable;

/**
 * Performs Holt-Winters forecasting.
 *
 * @author jwhite
 */
@FilterInfo(name="HoltWinters", description="Performs Holt-Winters forecasting.")
public class HWForecast implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(HWForecast.class);

    @FilterParam(key="inputColumn", required=true, displayName="Input", description="Input column.")
    private String m_inputColumn;

    @FilterParam(key="outputPrefix", value="HW", displayName="Output", description="Output prefix.")
    private String m_outputPrefix;

    @FilterParam(key="numPeriodsToForecast", value="3", displayName="# Periods", description="Number of periods to forecast.")
    private int m_numPeriodsToForecast;

    @FilterParam(key="periodInSeconds", required=true, displayName="Period", description="Size of a period in seconds.")
    private long m_periodInSeconds;

    protected HWForecast() {}

    public HWForecast(String outputPrefix, String inputColumn,
            int numPeriodsToForecast, long periodInSeconds) {
        m_outputPrefix = outputPrefix;
        m_inputColumn = inputColumn;
        m_numPeriodsToForecast = numPeriodsToForecast;
        m_periodInSeconds = periodInSeconds;
    }

    @Override
    public void filter(RowSortedTable<Long, String, Double> table) {
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

        HoltWintersPointForecasterParams params = new HoltWintersPointForecasterParams()
                .setFrequency(numSamplesPerPeriod)
                // Initial guesses, will be fitted during training
                .setAlpha(0.5)
                .setBeta(0.030)
                .setGamma(0.002)
                .setSeasonalityType(HoltWintersSeasonalityType.MULTIPLICATIVE)
                .setWarmUpPeriod(numSamplesPerPeriod)
                .setInitTrainingMethod(HoltWintersTrainingMethod.SIMPLE);

        var subject = new HoltWintersPointForecaster(params);
        long firstIndexForTraining = Math.max(limits.firstRowWithValues, (limits.lastRowWithValues - (numSamplesPerPeriod * 2L)));

        double lastValue = Double.NaN;
        for (long i = firstIndexForTraining; i < limits.lastRowWithValues; i++) {
            lastValue = table.get(i, m_inputColumn);
            subject.forecast(lastValue);
        }

        for (long i = limits.lastRowWithValues + 1; i <= (limits.lastRowWithValues + numForecasts); i++) {
            PointForecast forecast = subject.forecast(lastValue);
            long idxForecast = i - limits.lastRowWithValues;
            table.put(i, m_outputPrefix + "Fit", forecast.getValue());
            table.put(i, TIMESTAMP_COLUMN_NAME, (double)new Date(lastTimestamp.getTime() + stepInMs * idxForecast).getTime());

            // Save value for next iteration
            lastValue = forecast.getValue();
        }
    }

    public static void checkForecastSupport() {
        // noop, forecasting always supported now
    }
}
