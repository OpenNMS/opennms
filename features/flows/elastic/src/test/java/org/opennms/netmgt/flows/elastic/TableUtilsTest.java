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
