package org.opennms.features.topology.api.support;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.GraphContainer;

public class SavedHistory{
    private int m_szl = -1;
    private double m_scale = 0.0;
    private BoundingBox m_boundBox;
    
    public SavedHistory(GraphContainer graphContainer) {
        m_szl = graphContainer.getSemanticZoomLevel();
        m_scale = graphContainer.getScale();
        m_boundBox = graphContainer.getMapViewManager().getCurrentBoundingBox();
    }
    
    public int getSemanticZoomLevel() {
        return m_szl;
    }
    
    public double getScale() {
        return m_scale;
    }
    
    public BoundingBox getBoundingBox() {
        return m_boundBox;
    }
    
    public String getFragment() {
        return "szl" + m_szl + "scale" + m_scale + "bBox" + m_boundBox.fragment() + "_center:" + m_boundBox.getCenter();
    }

    public void apply(GraphContainer graphContainer) {
        graphContainer.setSemanticZoomLevel(getSemanticZoomLevel());
        graphContainer.setScale(getScale());
        graphContainer.getMapViewManager().setBoundingBox(getBoundingBox());
    }
}