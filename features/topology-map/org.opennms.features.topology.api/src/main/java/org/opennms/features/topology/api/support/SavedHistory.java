package org.opennms.features.topology.api.support;

import org.opennms.features.topology.api.GraphContainer;

public class SavedHistory{
    private int m_szl = -1;
    private double m_scale = 0.0;
    
    public SavedHistory(GraphContainer graphContainer) {
        m_szl = graphContainer.getSemanticZoomLevel();
        m_scale = graphContainer.getScale();
    }
    
    public int getSemanticZoomLevel() {
        return m_szl;
    }
    
    public double getScale() {
        return m_scale;
    }
    
    public String getFragment() {
        return "szl" + m_szl + "scale" + m_scale;
    }

    public void apply(GraphContainer graphContainer) {
        graphContainer.setSemanticZoomLevel(getSemanticZoomLevel());
        graphContainer.setScale(getScale());
    }
}