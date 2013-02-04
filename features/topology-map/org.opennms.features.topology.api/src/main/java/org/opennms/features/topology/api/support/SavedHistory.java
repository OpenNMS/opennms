package org.opennms.features.topology.api.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryOperation;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * Immutable class that stores a snapshot of the topology layout at a given time.
 */
public class SavedHistory {
    private final int m_szl;
    private final BoundingBox m_boundBox;
    private final Map<VertexRef, Point> m_locations = new HashMap<VertexRef, Point>();
    /**
     * A map of key-value settings for the HistoryOperation components that are registered.
     */
    private final Map<String,String> m_settings = new HashMap<String,String>();
    
    public SavedHistory(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        m_szl = graphContainer.getSemanticZoomLevel();
        m_boundBox = graphContainer.getMapViewManager().getCurrentBoundingBox();
        saveLocations(graphContainer.getGraph());
        for (HistoryOperation operation : operations) {
            m_settings.putAll(operation.createHistory(graphContainer));
        }
    }
    

    private void saveLocations(Graph graph) {
        Collection<? extends Vertex> vertices = graph.getDisplayVertices();
        m_locations.clear();
        for(Vertex vert : vertices) {
            m_locations.put(vert, graph.getLayout().getLocation(vert));
        }
    }


    public int getSemanticZoomLevel() {
        return m_szl;
    }
    
    public BoundingBox getBoundingBox() {
        return m_boundBox;
    }
    
    public String getFragment() {
        return "(" + m_szl + ")," + m_boundBox.fragment() + "," + m_boundBox.getCenter();
    }

    public void apply(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        // Apply the history for each registered HistoryOperation
        for (HistoryOperation operation : operations) {
            operation.applyHistory(graphContainer, m_settings);
        }
        applySavedLocations(m_locations, graphContainer.getGraph().getLayout());
        graphContainer.setSemanticZoomLevel(getSemanticZoomLevel());
        graphContainer.getMapViewManager().setBoundingBox(getBoundingBox());
    }

    private static void applySavedLocations(Map<VertexRef, Point> locations, Layout layout) {
        for(VertexRef ref : locations.keySet()) {
            layout.setLocation(ref, locations.get(ref));
        }
    }
}