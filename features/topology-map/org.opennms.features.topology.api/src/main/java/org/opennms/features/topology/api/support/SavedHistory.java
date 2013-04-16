package org.opennms.features.topology.api.support;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryOperation;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

/**
 * Immutable class that stores a snapshot of the topology layout at a given time.
 */
public class SavedHistory {
    private final int m_szl;
    private final BoundingBox m_boundBox;
    private final Map<VertexRef, Point> m_locations = new HashMap<VertexRef, Point>();
    private final Set<VertexRef> m_selectedVertices;

    /**
     * A map of key-value settings for the HistoryOperation components that are registered.
     */
    private final Map<String,String> m_settings = new HashMap<String,String>();
    
    public SavedHistory(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        m_szl = graphContainer.getSemanticZoomLevel();
        m_boundBox = graphContainer.getMapViewManager().getCurrentBoundingBox();
        saveLocations(graphContainer.getGraph());

        HashSet<VertexRef> selectedVertices = new HashSet<VertexRef>();
        selectedVertices.addAll(graphContainer.getSelectionManager().getSelectedVertexRefs());
        m_selectedVertices = Collections.unmodifiableSet(selectedVertices);

        for (HistoryOperation operation : operations) {
            m_settings.putAll(operation.createHistory(graphContainer));
        }
        LoggerFactory.getLogger(this.getClass()).debug("Created " + toString());
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
        CRC32 settingsCrc = new CRC32();
        for (Map.Entry<String,String> entry : m_settings.entrySet()) {
            try {
                settingsCrc.update(entry.getKey().getBytes("UTF-8"));
                settingsCrc.update(entry.getValue().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        retval.append(String.format(",(%X)", settingsCrc.getValue()));

        CRC32 locationsCrc = new CRC32();
        for(Map.Entry<VertexRef, Point> entry : m_locations.entrySet()) {
            try {
                locationsCrc.update(entry.getKey().getId().getBytes("UTF-8"));
                locationsCrc.update(entry.getValue().getX());
                locationsCrc.update(entry.getValue().getY());
            } catch(UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        retval.append(String.format(",(%X)", locationsCrc.getValue()));

        CRC32 selectionsCrc = new CRC32();
        for(VertexRef entry : m_selectedVertices) {
            try {
                selectionsCrc.update(entry.getNamespace().getBytes("UTF-8"));
                selectionsCrc.update(entry.getId().getBytes("UTF-8"));
            } catch(UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        retval.append(String.format(",(%X)", selectionsCrc.getValue()));

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

        // Apply the selected vertices
        graphContainer.getSelectionManager().setSelectedVertexRefs(m_selectedVertices);

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
        for (VertexRef entry : m_selectedVertices) {
            retval.append(",[").append(entry.getNamespace()).append(":").append(entry.getId()).append("]");
        }
        return retval.toString();
    }
}