package org.opennms.features.topology.api.support;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

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
        // LoggerFactory.getLogger(this.getClass()).debug("Created " + toString());
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
        StringBuffer retval = new StringBuffer().append("(").append(m_szl).append("),").append(m_boundBox.fragment()).append(",").append(m_boundBox.getCenter());
        // Add a CRC of all of the key-value pairs in m_settings to make the fragment unique
        CRC32 crc = new CRC32();
        for (Map.Entry<String,String> entry : m_settings.entrySet()) {
            try {
                crc.update(entry.getKey().getBytes("UTF-8"));
                crc.update(entry.getValue().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        
        retval.append(String.format(",(%X)", crc.getValue()));
        
        CRC32 vCrc = new CRC32();
        for(Map.Entry<VertexRef, Point> entry : m_locations.entrySet()) {
            try {
                vCrc.update(entry.getKey().getId().getBytes("UTF-8"));
                vCrc.update(entry.getValue().getX());
                vCrc.update(entry.getValue().getY());
            } catch(UnsupportedEncodingException e) {
                
            }
        }
        
        retval.append(String.format(",(%X)", vCrc.getValue()));
        return retval.toString();
    }

    public void apply(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        // LoggerFactory.getLogger(this.getClass()).debug("Applying " + toString());
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
    
    @Override
    public String toString() {
        StringBuffer retval = new StringBuffer().append(this.getClass().getSimpleName()).append(": ").append(getFragment());
        for (Map.Entry<String,String> entry : m_settings.entrySet()) {
            retval.append(",[").append(entry.getKey()).append("->").append(entry.getValue()).append("]");
        }
        return retval.toString();
    }
}