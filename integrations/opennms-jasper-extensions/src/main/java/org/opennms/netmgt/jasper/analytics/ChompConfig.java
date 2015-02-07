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
 * The Chomp command has the following structure:
 * 
 * ANALYTICS:Chomp=cutoffTimestampInSeconds
 *  (:stripNaNs)
 *
 * where:
 *   - cutoffTimestampInSeconds is a time stamp, in seconds. Any rows before
 *     this time will be removed
 *   - stripNans is a boolean value (i.e. true or false). When set,
 *     leading and trailing rows containing NaNs will be removed
 * @author jwhite
 */
public class ChompConfig {
    private final double m_cutoffDate;
    private final boolean m_stripNaNs;

    protected ChompConfig(double cutoffDate, boolean stringNaNs) {
        m_cutoffDate = cutoffDate;
        m_stripNaNs = stringNaNs;
    }

    public static ChompConfig parse(AnalyticsCommand cmd) {
        double cutoffDate = Double.parseDouble(cmd.getColumnNameOrPrefix()) * 1000;
        boolean stripNaNs = cmd.getBooleanArgument(0, false, "(Chomp) stripNaNs)");
        return new ChompConfig(cutoffDate, stripNaNs);
    }

    public double getCutoffDate() {
        return m_cutoffDate;
    }

    public boolean getStripNaNs() {
        return m_stripNaNs;
    }
}
