/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.model.graph.internal;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class GraphAlgorithmsTest {

    @Test
    @SuppressWarnings("unchecked")
    public void canGeneratePowerSets() {
        Set<Set<String>> setContainingEmptySet = Sets.newHashSet();
        setContainingEmptySet.add(Sets.newHashSet());
        verifyPowerSetGeneration(
                Lists.newArrayList(),
                setContainingEmptySet);

        verifyPowerSetGeneration(
                Lists.newArrayList("a1", "a2"),
                Sets.newHashSet(
                        Sets.newHashSet(),
                        Sets.newHashSet("a1"),
                        Sets.newHashSet("a2"),
                        Sets.newHashSet("a1", "a2")
                ));

        verifyPowerSetGeneration(
                Lists.newArrayList("a1", "a2", "a3"),
                Sets.newHashSet(
                        Sets.newHashSet(),
                        Sets.newHashSet("a1"),
                        Sets.newHashSet("a2"),
                        Sets.newHashSet("a3"),
                        Sets.newHashSet("a1", "a2"),
                        Sets.newHashSet("a1", "a3"),
                        Sets.newHashSet("a2", "a3"),
                        Sets.newHashSet("a1", "a2", "a3")
                ));

        verifyPowerSetGeneration(
                Lists.newArrayList("a1", "a2", "a3", "a4"),
                Sets.newHashSet(
                        Sets.newHashSet(),
                        Sets.newHashSet("a1"),
                        Sets.newHashSet("a2"),
                        Sets.newHashSet("a3"),
                        Sets.newHashSet("a4"),
                        Sets.newHashSet("a1", "a2"),
                        Sets.newHashSet("a1", "a3"),
                        Sets.newHashSet("a1", "a4"),
                        Sets.newHashSet("a2", "a3"),
                        Sets.newHashSet("a2", "a4"),
                        Sets.newHashSet("a3", "a4"),
                        Sets.newHashSet("a1", "a2", "a3"),
                        Sets.newHashSet("a1", "a2", "a4"),
                        Sets.newHashSet("a1", "a3", "a4"),
                        Sets.newHashSet("a2", "a3", "a4"),
                        Sets.newHashSet("a1", "a2", "a3", "a4")
                ));
    }

    private static <T> void verifyPowerSetGeneration(List<T> elements, Set<Set<T>> expectedPowerSet) {
        assertEquals(expectedPowerSet, GraphAlgorithms.generatePowerSet(elements));
    }
}
