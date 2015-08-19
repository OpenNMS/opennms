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

import com.google.common.collect.Table;

/**
 * Helper class for converting RRD-based data sources to and from
 * table representations.
 *
 * @author jwhite
 */
public class AnalyticsFilterUtils {

    public static Point getRowsWithValues(Table<Integer, String, Double> table, String... columnNames) {
        int firstRowWithValues = -1, lastRowWithValues = -1;
        for (int k : table.rowKeySet()) {
            for (String columnName : columnNames) {
                Double value = table.get(k, columnName);
                
                if (value != null && !Double.isNaN(value)) {
                    if (firstRowWithValues < 0) {
                        firstRowWithValues = k;
                    }
                    lastRowWithValues = k;
                }
            }
        }

        return new Point(firstRowWithValues, lastRowWithValues);
    }
}
