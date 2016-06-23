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

import java.util.Set;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;
import org.opennms.netmgt.measurements.filters.impl.Utils.TableLimits;

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
@FilterInfo(name="Chomp", description="Strips leading and trailing rows that contain nothing but NaNs/null values.")
public class Chomp implements Filter {

    @FilterParam(key="stripNaNs", value="true", displayName="Strip", description="When set, leading and trailing rows containing NaNs will be removed")
    private boolean m_stripNaNs;

    @FilterParam(key="cutoffDate", value="0", displayName="Cutoff", description="Timestamp in milliseconds. Any rows before this time will be removed.")
    private double m_cutoffDate;

    protected Chomp() { }

    public Chomp(double cutOffDate, boolean stripNaNs) {
        m_cutoffDate = cutOffDate;
        m_stripNaNs = stripNaNs;
    }

    @Override
    public void filter(RowSortedTable<Long, String, Double> qrAsTable)
            throws Exception {

        int numRowsInTable = qrAsTable.rowKeySet().size();
        long lastRowToKeep = numRowsInTable;
        long firstRowToKeep = lastRowToKeep;

        // Determine the index of the first row with a timestamp
        // on/after the cutoff date
        for (long k : qrAsTable.rowKeySet()) {
            if(qrAsTable.get(k, TIMESTAMP_COLUMN_NAME) >= m_cutoffDate) {
                firstRowToKeep = k;
                break;
            }
        }

        if (m_stripNaNs) {
            // Excluding the timestamp column, determine the
            // index of the first and last rows which don't contain
            // completely NaN values
            Set<String> columnNamesNoTs = Sets.newHashSet(qrAsTable.columnKeySet());
            columnNamesNoTs.remove(TIMESTAMP_COLUMN_NAME);
            TableLimits limits = Utils.getRowsWithValues(qrAsTable, columnNamesNoTs.toArray(new String[0]));
            firstRowToKeep = Math.max(firstRowToKeep, limits.firstRowWithValues);
            lastRowToKeep = Math.min(lastRowToKeep, limits.lastRowWithValues);
        }

        Set<String> columnNames = Sets.newHashSet(qrAsTable.columnKeySet());

        // Remove all of the trailing rows
        for (long i = lastRowToKeep+1; i < numRowsInTable; i++) {
            for (String columnName : columnNames) {
                qrAsTable.remove(i, columnName);
            }
        }

        // Remove all of the leading rows
        for (long i = 0; i < firstRowToKeep; i++) {
            for (String columnName : columnNames) {
                qrAsTable.remove(i, columnName);
            }
        }

        // Bump up the indices on the remaining rows
        if (firstRowToKeep > 0) {
            long j = 0;
            for (long i = firstRowToKeep; i <= lastRowToKeep; i++) {
                for (String columnName : columnNames) {
                    Double value = qrAsTable.get(i, columnName);
                    if (value != null) {
                        qrAsTable.put(j, columnName, value);
                    }
                    qrAsTable.remove(i, columnName);
                }
                j++;
            }
        }
    }
}
