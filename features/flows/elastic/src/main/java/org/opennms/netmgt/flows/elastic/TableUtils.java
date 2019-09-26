/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
    protected static <C,V> Table<Directional<String>, C, V> sortTableByRowKeys(Table<Directional<String>, C, V> table,
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
