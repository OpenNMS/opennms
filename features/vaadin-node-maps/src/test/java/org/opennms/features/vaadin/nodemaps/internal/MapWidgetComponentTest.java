package org.opennms.features.vaadin.nodemaps.internal;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.vaadin.nodemaps.internal.NodeMapComponent.NodeEntry;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MapNode;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMapState;

public class MapWidgetComponentTest {
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(MapNode.class.getName()).setLevel(Level.ALL);
    }

    @Test
    public void testShowNodes() {
        final NodeMapState state = new NodeMapState();
        final MapWidgetComponent component = new MapWidgetComponent() {
            private static final long serialVersionUID = 1L;
            public NodeMapState getState() {
                return state;
            }
        };

        final Map<Integer,NodeEntry> entries = new HashMap<Integer,NodeEntry>();
        component.showNodes(entries);
        assertEquals(0, state.nodes.size());
        
        entries.put(1, new NodeEntry(1, "Foo", 3f, 4f));
        component.showNodes(entries);
        assertEquals(1, state.nodes.size());
        assertEquals("Foo", state.nodes.get(0).getNodeLabel());
        assertEquals(3d, state.nodes.get(0).getLongitude(), 0.1d);
        assertEquals(4d, state.nodes.get(0).getLatitude(), 0.1d);

        entries.put(1, new NodeEntry(1, "Bar", 6f, 8f));
        component.showNodes(entries);
        assertEquals(1, state.nodes.size());
        assertEquals("Bar", state.nodes.get(0).getNodeLabel());
        assertEquals(6d, state.nodes.get(0).getLongitude(), 0.1d);
        assertEquals(8d, state.nodes.get(0).getLatitude(), 0.1d);

        entries.remove(1);
        component.showNodes(entries);
        assertEquals(0, state.nodes.size());
    }
}
