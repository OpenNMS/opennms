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
