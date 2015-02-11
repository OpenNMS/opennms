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
import java.util.Set;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;

/**
 * Strips leading and trailing rows that contain
 * nothing but NaNs/null values from the data source.
 *
 * This filter is useful when the values from the RRD-backed
 * data source do not cover the entire time interval used
 * in the report.
 *
 * @author jwhite
 */
public class Chomp implements Filter {
    private final ChompConfig m_config;

    public Chomp(ChompConfig config) {
        m_config = config;
    }

    @Override
    public void filter(RowSortedTable<Integer, String, Double> dsAsTable)
            throws Exception {

        int numRowsInTable = dsAsTable.rowKeySet().size();
        int lastRowToKeep = numRowsInTable;
        int firstRowToKeep = lastRowToKeep;

        // Determine the index of the first row with a timestamp
        // on/after the cutoff date
        for (int k : dsAsTable.rowKeySet()) {
            if(dsAsTable.get(k, "Timestamp") >= m_config.getCutoffDate()) {
                firstRowToKeep = k;
                break;
            }
        }

        if (m_config.getStripNaNs()) {
            // Excluding the Timestamp column, determine the
            // index of the first and last rows which don't contain
            // completely NaN values
            Set<String> columnNamesNoTs = Sets.newHashSet(dsAsTable.columnKeySet());
            columnNamesNoTs.remove("Timestamp");
            Point rowsWithValues = DataSourceUtils.getRowsWithValues(dsAsTable, columnNamesNoTs.toArray(new String[0]));
            firstRowToKeep = Math.max(firstRowToKeep, rowsWithValues.x);
            lastRowToKeep = Math.min(lastRowToKeep, rowsWithValues.y);
        }

        Set<String> columnNames = Sets.newHashSet(dsAsTable.columnKeySet());

        // Remove all of the trailing rows
        for (int i = lastRowToKeep+1; i < numRowsInTable; i++) {
            for (String columnName : columnNames) {
                dsAsTable.remove(i, columnName);
            }
        }

        // Remove all of the leading rows
        for (int i = 0; i < firstRowToKeep; i++) {
            for (String columnName : columnNames) {
                dsAsTable.remove(i, columnName);
            }
        }

        // Bump up the indices on the remaining rows
        if (firstRowToKeep > 0) {
            int j = 0;
            for (int i = firstRowToKeep; i <= lastRowToKeep; i++) {
                for (String columnName : columnNames) {
                    Double value = dsAsTable.get(i, columnName);
                    if (value != null) {
                        dsAsTable.put(j, columnName, value);
                    }
                    dsAsTable.remove(i, columnName);
                }
                j++;
            }
        }
    }
}
