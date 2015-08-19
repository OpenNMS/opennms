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
 * The command for outlier removal and interpolation has the following structure:
 *
 * ANALYTICS:OutlierFilter=inputOuputColumn(:probability)
 *
 * Outlier removal is performed by calculating a quantile using the defined
 * probability. Any values greater than the the quantile will be replaced
 * with an interpolated value.
 *
 * In simpler terms, values that are greater than "most" (defined by the
 * probability) of the other values in the series will be replaced
 * by values that "provide a better fit".
 *
 * @author jwhite
 */
public class OutlierFilterConfig {
    private final String m_inputColumn;
    private final double m_probability;

    protected OutlierFilterConfig(String inputColumn, double probability) {
        m_inputColumn = inputColumn;
        m_probability = probability;   
    }

    public static OutlierFilterConfig parse(AnalyticsCommand cmd) {
        double probability = cmd.getDoubleArgument(0, 0.975, "(OutlierFiler) probability");
        return new OutlierFilterConfig(cmd.getColumnNameOrPrefix(), probability);
    }

    public String getInputColumn() {
        return m_inputColumn;
    }

    public double getProbability() {
        return m_probability;
    }
}
