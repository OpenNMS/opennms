package org.opennms.features.topology.api.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class SavedHistory{
    private int m_szl = -1;
    private BoundingBox m_boundBox;
    private Map<VertexRef, Point> m_locations = new HashMap<VertexRef, Point>();
    
    public SavedHistory(GraphContainer graphContainer) {
        m_szl = graphContainer.getSemanticZoomLevel();
        m_boundBox = graphContainer.getMapViewManager().getCurrentBoundingBox();
        saveLocations(graphContainer.getGraph());
        
    }
    

    private void saveLocations(Graph graph) {
        Collection<? extends Vertex> vertices = graph.getDisplayVertices();
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

    public void apply(GraphContainer graphContainer) {
        applySavedLocations(graphContainer.getGraph().getLayout());
        graphContainer.setSemanticZoomLevel(getSemanticZoomLevel());
        graphContainer.getMapViewManager().setBoundingBox(getBoundingBox());
    }


    private void applySavedLocations(Layout layout) {
        for(VertexRef ref : m_locations.keySet()) {
            layout.setLocation(ref, m_locations.get(ref));
        }
    }
}