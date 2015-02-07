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
 * The command for the trend line calculation has the following structure:
 * 
 * ANALYTICS:TrendLine=outputColumn
 *  :inputColumn
 *  (:secondsAhead)
 *  (:polynomialOrder)
 *
 * where:
 *   - inputColumn is the name of the column whose values we want to trend
 *   - secondsAhead is the number seconds ahead the of the column for
 *   which we want to include the trend line
 *   - polynomialOrder is the polynomial order of the trend line/curve
 *   keep this to 1 for a line
 *
 * @author jwhite
 */
public class TrendLineConfig {
    private final String m_outputColumn;
    private final String m_inputColumn;
    private final long m_secondsAhead;
    private final int m_polynomialOrder;

    public TrendLineConfig(String outputColumn, String inputColumn, long secondsAhead, int polynomialOrder) {
        m_outputColumn = outputColumn;
        m_inputColumn = inputColumn;
        m_secondsAhead = secondsAhead;
        m_polynomialOrder = polynomialOrder;
    }

    public static TrendLineConfig parse(AnalyticsCommand cmd) {
        String inputColumn = cmd.getStringArgument(0, "(TrendLine) input column");
        long secondsAhead = cmd.getLongArgument(1, -1, "(TrendLine) seconds ahead"); 
        int polynomialOrder = cmd.getIntArgument(2, 1, "(TrendLine) polynomial order");

        return new TrendLineConfig(cmd.getColumnNameOrPrefix(), inputColumn, secondsAhead, polynomialOrder);
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
