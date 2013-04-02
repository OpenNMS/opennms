package org.opennms.features.topology.app.internal.gwt.client;

import java.util.List;

import com.vaadin.shared.AbstractComponentState;

public class TopologyComponentState extends AbstractComponentState {

    private int m_boundX;
    private int m_boundY;
    private int m_boundWidth;
    private int m_boundHeight;
    private String m_activeTool;
    private List<SharedVertex> m_vertices;
    private List<SharedEdge> m_edges;

    public void setBoundX(int boundX) {
        m_boundX = boundX;
    }

    public void setBoundY(int boundY) {
        m_boundY = boundY;
    }

    public void setBoundWidth(int width) {
        m_boundWidth = width;
    }

    public void setBoundHeight(int height) {
        m_boundHeight = height;
    }

    public void setActiveTool(String activeTool) {
        m_activeTool = activeTool;
    }

    public int getBoundX() {
        return m_boundX;
    }

    public int getBoundY() {
        return m_boundY;
    }

    public int getBoundWidth() {
        return m_boundWidth;
    }

    public int getBoundHeight() {
        return m_boundHeight;
    }

    public String getActiveTool() {
        return m_activeTool;
    }

    public List<SharedVertex> getVertices() {
        return m_vertices;
    }

    public void setVertices(List<SharedVertex> vertices) {
        m_vertices = vertices;
    }

    public List<SharedEdge> getEdges() {
        return m_edges;
    }

    public void setEdges(List<SharedEdge> edges) {
        m_edges = edges;
    }

    
}
