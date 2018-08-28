package org.opennms.netmgt.model;

import java.util.HashSet;
import java.util.Set;

public class OnmsTopology {

    private Set<OnmsTopologyVertex> m_vertices;
    private Set<OnmsTopologyEdge> m_edges;
    
    public OnmsTopology() {
        m_vertices = new HashSet<OnmsTopologyVertex>();
        m_edges = new HashSet<OnmsTopologyEdge>();
    }

    public OnmsTopologyVertex getVertex(String id) {
        return m_vertices.stream().filter(vertex -> id.equals(vertex.getId())).findAny().orElse(null);
    }

    public OnmsTopologyEdge getEdge(String id) {
        return m_edges.stream().filter(edge -> id.equals(edge.getId())).findAny().orElse(null);
    }

    public Set<OnmsTopologyVertex> getVertices() {
        return m_vertices;
    }

    public void setVertices(Set<OnmsTopologyVertex> vertices) {
        m_vertices = vertices;
    }

    public Set<OnmsTopologyEdge> getEdges() {
        return m_edges;
    }

    public void setConnections(Set<OnmsTopologyEdge> edges) {
        m_edges = edges;
    }    

}

