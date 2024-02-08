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
