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

import com.google.common.collect.Table;

/**
 * Helper functions for manipulating tables.
 *
 * @author jwhite
 */
public class Utils {

    public static class TableLimits {
        long firstRowWithValues = -1;
        long lastRowWithValues = -1;
    }

    public static TableLimits getRowsWithValues(Table<Long, String, Double> table, String... columnNames) {
        TableLimits limits = new TableLimits();
        for (long k : table.rowKeySet()) {
            for (String columnName : columnNames) {
                Double value = table.get(k, columnName);
                
                if (value != null && !Double.isNaN(value)) {
                    if (limits.firstRowWithValues < 0) {
                        limits.firstRowWithValues = k;
                    }
                    limits.lastRowWithValues = k;
                }
            }
        }

        return limits;
    }
}
