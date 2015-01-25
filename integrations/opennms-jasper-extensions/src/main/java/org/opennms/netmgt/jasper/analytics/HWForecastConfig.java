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

/**
 * The command for the Holt-Winters forecasts has the following structure:
 * 
 * ANALYTICS:HoltWinters=outputPrefix
 *  :inputColumn
 *  (:numPeriodsToForecast)
 *  (:periodInSeconds)
 *  (:confidenceLevel)
 *
 * where:
 *   - inputColumn is the name of the column whose values we want to forecast
 *   - numPeriodsToForecast is the number of periods we want to forecast
 *   - periodInSeconds is the length of a period (or season) in seconds, if
 *   this value is < 1, then we assume that every sample is a period.
 *   - confidenceLevel a number from 0 to 1 (exclusive) used to compute
 *   the lower and upper bounds of the confidence interval
 *
 * @author jwhite
 */
public class HWForecastConfig {
    private final String m_outputPrefix;
    private final String m_inputColumn;
    private final int m_numPeriodsToForecast;
    private final long m_period;
    private final double m_confidenceLevel;

    protected HWForecastConfig(String outputPrefix, String inputColumn,
            int numPeriodsToForecast, long periodInSeconds,
            double confidenceLevel) {
        m_outputPrefix = outputPrefix;
        m_inputColumn = inputColumn;
        m_numPeriodsToForecast = numPeriodsToForecast;
        m_period = periodInSeconds;
        m_confidenceLevel = confidenceLevel;
    }

    public static HWForecastConfig parse(AnalyticsCommand cmd) {
        String inputColumn = cmd.getStringArgument(0, "(HWForecast) input column");
        int numPeriodsToForecast = cmd.getIntArgument(1, 3, "(HWForecast) number of periods to forecast");
        long periodInSeconds = cmd.getLongArgument(2, 0, "(HWForecast) period in seconds");
        double confidenceLevel = cmd.getDoubleArgument(3, 0.95, "(HWForecast) confidence level");

        return new HWForecastConfig(cmd.getColumnNameOrPrefix(), inputColumn, numPeriodsToForecast, periodInSeconds, confidenceLevel);
    }

    public String getOutputPrefix() {
        return m_outputPrefix;
    }

    public String getInputColumn() {
        return m_inputColumn;
    }

    public int getNumPeriodsToForecast() {
        return m_numPeriodsToForecast;
    }

    public long getPeriod() {
        return m_period;
    }

    public double getConfidenceLevel() {
        return m_confidenceLevel;
    }
}
