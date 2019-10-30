/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.focus;

public class FocusStrategyTest {

//    @Test
//    public void testFocusStrategyAll() {
//
//        // graph with vertices
//        GenericGraph graph = TestObjectCreator.createGraph();
//        assertEquals(graph.getVertices(), FocusStrategy.ALL.getFocus(graph));
//
//        // graph without vertices
//        assertEquals(Collections.emptyList(), FocusStrategy.ALL.getFocus(TestObjectCreator.createEmptyGraph()));
//    }
//
//    @Test
//    public void testFocusStrategyEmpty() {
//
//        // graph with vertices
//        GenericGraph graph = TestObjectCreator.createGraph();
//        assertEquals(Collections.emptyList(), FocusStrategy.EMPTY.getFocus(graph));
//
//        // graph without vertices
//        assertEquals(Collections.emptyList(), FocusStrategy.EMPTY.getFocus(TestObjectCreator.createEmptyGraph()));
//    }
//
//    @Test
//    public void testFocusStrategyFirst() {
//
//        // graph with vertices
//        GenericGraph graph = TestObjectCreator.createGraph();
//        assertEquals(1, FocusStrategy.FIRST.getFocus(graph).size());
//
//        // graph without vertices
//        assertEquals(Collections.emptyList(), FocusStrategy.FIRST.getFocus(TestObjectCreator.createEmptyGraph()));
//    }
//
//    @Test
//    public void testFocusStrategySpecific() {
//
//        // graph with vertices
//        GenericGraph graph = TestObjectCreator.createGraph();
//        List<GenericVertex> specificVertices = Arrays.asList(TestObjectCreator.createVertex(), TestObjectCreator.createVertex());
//        graph.addVertices(specificVertices);
//        List<String> specificVertexIds = new ArrayList<>();
//        specificVertices.forEach(v -> specificVertexIds.add(v.getId()));
//        assertEquals(specificVertices, new FocusStrategy.SpecificFocus(specificVertexIds).getFocus(graph));
//
//        // ask for an unknown vertex: unknown vertices are ignored
//        Focus focus = new FocusStrategy.SpecificFocus(Collections.singletonList(TestObjectCreator.createVertex("unknown", "alsoUnknown").getId()));
//        assertEquals(Collections.emptyList(), focus.getFocus(graph));
//
//        // graph without vertices
//        assertEquals(Collections.emptyList(), FocusStrategy.FIRST.getFocus(TestObjectCreator.createEmptyGraph()));
//    }

}