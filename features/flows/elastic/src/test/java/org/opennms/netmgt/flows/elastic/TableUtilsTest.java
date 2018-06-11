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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.flows.api.Directional;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

public class TableUtilsTest {

    @Test
    public void canSortEmptyTable() {
        ImmutableTable.Builder<Directional<String>, Long, Double> builder = ImmutableTable.builder();
        Table<Directional<String>, Long, Double> emptyTable = builder.build();
        assertThat(TableUtils.sortTableByRowKeys(emptyTable, Collections.emptyList()), equalTo(emptyTable));
    }

    @Test
    public void canSortTable() {
        ImmutableTable.Builder<Directional<String>, Long, Double> unsortedBuilder = ImmutableTable.builder();
        unsortedBuilder.put(new Directional<>("c", false), 1L, 1d);
        unsortedBuilder.put(new Directional<>("a", false), 1L, 1d);
        unsortedBuilder.put(new Directional<>("d", false), 1L, 1d);
        unsortedBuilder.put(new Directional<>("b", false), 1L, 1d);
        unsortedBuilder.put(new Directional<>("e", false), 1L, 1d);
        Table<Directional<String>, Long, Double> unsortedTable = unsortedBuilder.build();

        List<String> keysAfterSort = getRowKeys(TableUtils.sortTableByRowKeys(
                unsortedTable, Arrays.asList("a", "b", "c", "z")));
        assertThat(keysAfterSort, equalTo(Arrays.asList("a", "b", "c", "d", "e")));
    }

    private static List<String> getRowKeys(Table<Directional<String>, Long, Double> table) {
        return table.rowKeySet().stream()
                .map(Directional::getValue)
                .collect(Collectors.toList());
    }
}
