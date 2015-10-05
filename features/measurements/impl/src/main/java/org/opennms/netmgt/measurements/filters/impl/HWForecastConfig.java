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

import org.opennms.netmgt.measurements.api.FilterConfig;
import org.opennms.netmgt.measurements.api.FilterParam;

/**
 * Configuration options for the HWForecast filter.
 *
 * @author jwhite
 */
public class HWForecastConfig implements FilterConfig {

    @FilterParam(name="outputPrefix", required=true, description="Output prefix.")
    private String m_outputPrefix;

    @FilterParam(name="inputColumn", required=true, description="Input column.")
    private String m_inputColumn;

    @FilterParam(name="numPeriodsToForecast", value="3", description="Number of periods to forecast.")
    private int m_numPeriodsToForecast;

    @FilterParam(name="periodInSeconds", required=true, description="Size of a period in seconds.")
    private long m_periodInSeconds;

    @FilterParam(name="confidenceLevel", value="0.95", description="Size of a period in seconds.")
    private double m_confidenceLevel;

    protected HWForecastConfig() {}

    public HWForecastConfig(String outputPrefix, String inputColumn,
            int numPeriodsToForecast, long periodInSeconds,
            double confidenceLevel) {
        m_outputPrefix = outputPrefix;
        m_inputColumn = inputColumn;
        m_numPeriodsToForecast = numPeriodsToForecast;
        m_periodInSeconds = periodInSeconds;
        m_confidenceLevel = confidenceLevel;
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

    public long getPeriodInSeconds() {
        return m_periodInSeconds;
    }

    public double getConfidenceLevel() {
        return m_confidenceLevel;
    }
}
