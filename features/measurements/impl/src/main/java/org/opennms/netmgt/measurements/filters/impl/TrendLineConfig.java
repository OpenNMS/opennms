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
 * Configuration options for the TrendLine filter.
 *
 * @author jwhite
 */
public class TrendLineConfig implements FilterConfig {

    @FilterParam(name="outputColumn", required=true, description="Output column.")
    private String m_outputColumn;

    @FilterParam(name="inputColumn", required=true, description="Input column.")
    private String m_inputColumn;

    @FilterParam(name="secondsAhead", required=true, description="Number seconds ahead the of the column for which we want to include the trend line")
    private long m_secondsAhead;

    @FilterParam(name="polynomialOrder", value="1", description="Polynomial order of the trend line/curve. Set this to 1 for a line.")
    private int m_polynomialOrder;

    protected TrendLineConfig() {}

    public TrendLineConfig(String outputColumn, String inputColumn, long secondsAhead, int polynomialOrder) {
        m_outputColumn = outputColumn;
        m_inputColumn = inputColumn;
        m_secondsAhead = secondsAhead;
        m_polynomialOrder = polynomialOrder;
    }

    public String getOutputColumn() {
        return m_outputColumn;
    }

    public String getInputColumn() {
        return m_inputColumn;
    }

    public long getSecondsAhead() {
        return m_secondsAhead;
    }

    public int getPolynomialOrder() {
        return m_polynomialOrder;
    }
}
