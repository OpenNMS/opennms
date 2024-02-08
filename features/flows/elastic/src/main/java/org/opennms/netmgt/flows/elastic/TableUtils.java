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
package org.opennms.netmgt.flows.elastic;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.flows.api.Directional;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

public class TableUtils {

    /**
     * Given a table, sort the rows such that the row keys appear in the same order
     * as the given list. Any additional rows should be appended after these in the same
     * order that they appeared.
     *
     * @param table the table to sort
     * @param rowKeys list of row keys to match
     * @return a sorted table
     */
    public static <C,V> Table<Directional<String>, C, V> sortTableByRowKeys(Table<Directional<String>, C, V> table,
                                                                                List<String> rowKeys) {

        final ImmutableTable.Builder<Directional<String>, C, V> builder = ImmutableTable.builder();
        // Iterate over the keys in the given order, and append any matching rows to the builder
        for (String rowKeyValue : rowKeys) {
            for (Directional<String> rowKey : table.rowKeySet()) {
                if (Objects.equals(rowKeyValue, rowKey.getValue())) {
                    table.row(rowKey).forEach((columnKey,value) -> builder.put(rowKey, columnKey, value));
                }
            }
        }

        final Set<String> knownKeys = Sets.newHashSet(rowKeys);
        for (Directional<String> rowKey : table.rowKeySet()) {
            // Append any rows that were not previously matched, in the same order as they appear
            if (!knownKeys.contains(rowKey.getValue())) {
                table.row(rowKey).forEach((columnKey,value) -> builder.put(rowKey, columnKey, value));
            }
        }

        return builder.build();
    }
}
