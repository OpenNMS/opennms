package org.opennms.web.graph;

import java.util.Date;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;

public class Graph implements Comparable<Graph> {
    private PrefabGraph m_graph = null;
    private OnmsResource m_resource;
    private Date m_start = null;
    private Date m_end = null;
    
    public Graph(PrefabGraph graph, OnmsResource resource,
            Date start, Date end) {
        m_graph = graph;
        m_resource = resource;
        m_start = start;
        m_end = end;
    }

    public OnmsResource getResource() {
        return m_resource;
    }

    public Date getStart() {
        return m_start;
    }

    public Date getEnd() {
        return m_end;
    }

    public String getName() {
        return m_graph.getName();
    }

    public int compareTo(Graph other) {
        if (other == null) {
            return -1;
        }

        return m_graph.compareTo(other.m_graph);
    }

    public String getGraphWidth() {
        return m_graph.getGraphWidth();
    }

    public String getGraphHeight() {
        return m_graph.getGraphHeight();
    }
    
    public PrefabGraph getPrefabGraph() {
        return m_graph;
    }
    
    public String getReport() {
        return m_graph.getName();
    }

}
