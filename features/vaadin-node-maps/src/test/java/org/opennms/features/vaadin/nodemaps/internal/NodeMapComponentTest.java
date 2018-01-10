/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MapNode;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMapState;

public class NodeMapComponentTest {
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(MapNode.class.getName()).setLevel(Level.ALL);
        System.setProperty("gwt.openlayers.url", "dummy value>");
    }

    @Test
    public void testShowNodes() {
        final NodeMapState state = new NodeMapState();
        final NodeMapComponent component = new NodeMapComponent() {
            private static final long serialVersionUID = 1L;
            public NodeMapState getState() {
                return state;
            }
        };

        final Map<Integer,MapNode> entries = new HashMap<>();
        component.showNodes(entries);
        assertEquals(0, state.nodes.size());
        
        entries.put(1, createMapNode(1, "Foo", 3f, 4f));
        component.showNodes(entries);
        assertEquals(1, state.nodes.size());
        assertEquals("Foo", state.nodes.get(0).getNodeLabel());
        assertEquals(3d, state.nodes.get(0).getLongitude(), 0.1d);
        assertEquals(4d, state.nodes.get(0).getLatitude(), 0.1d);

        entries.put(1, createMapNode(1, "Bar", 6f, 8f));
        component.showNodes(entries);
        assertEquals(1, state.nodes.size());
        assertEquals("Bar", state.nodes.get(0).getNodeLabel());
        assertEquals(6d, state.nodes.get(0).getLongitude(), 0.1d);
        assertEquals(8d, state.nodes.get(0).getLatitude(), 0.1d);

        entries.remove(1);
        component.showNodes(entries);
        assertEquals(0, state.nodes.size());
    }

    @Test
    public void testStateNotUpdatedWhenNodesAreUnchanged() {
        final NodeMapState state = new NodeMapState();
        final NodeMapComponent component = new NodeMapComponent() {
            private static final long serialVersionUID = 1L;
            public NodeMapState getState() {
                return state;
            }
        };

        Map<Integer,MapNode> entries = new HashMap<>();
        entries.put(1, createMapNode(1, "Foo", 3f, 4f));
        entries.put(2, createMapNode(2, "Bar", 6f, 7f));
        component.showNodes(entries);
        List<MapNode> stateNodes = state.nodes;
        assertEquals(2, state.nodes.size());

        // Now recreate the entries using new objects and update the component
        entries = new HashMap<>();
        entries.put(1, createMapNode(1, "Foo", 3f, 4f));
        entries.put(2, createMapNode(2, "Bar", 6f, 7f));
        component.showNodes(entries);
        assertSame("An update with the same entries should not update the underlying array.", stateNodes, state.nodes);
    }

    private MapNode createMapNode(int nodeId, String nodeLabel, float longitude, float latitude) {
        MapNode mapNode = new MapNode();
        mapNode.setNodeId(String.valueOf(nodeId));
        mapNode.setNodeLabel(nodeLabel);
        mapNode.setLongitude(longitude);
        mapNode.setLatitude(latitude);
        return mapNode;
    }
}
