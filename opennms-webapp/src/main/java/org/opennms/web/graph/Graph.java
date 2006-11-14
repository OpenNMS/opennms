package org.opennms.web.graph;

import java.util.Date;

public class Graph implements Comparable {
    private PrefabGraph m_graph = null;
    private String m_parentResourceType = null;
    private String m_parentResource = null;
    private String m_resource = null;
    private Date m_start = null;
    private Date m_end = null;
    private String m_resourceType;

    public Graph(PrefabGraph graph, String parentResourceType,
            String parentResource, String resourceType,
            String resource, Date start, Date end) {
	m_graph = graph;
        m_parentResourceType = parentResourceType;
        m_parentResource = parentResource;
        m_resourceType = resourceType;
        m_resource = resource;
	m_start = start;
	m_end = end;
    }


    public String getParentResourceType() {
        return m_parentResourceType;
    }

    public String getParentResource() {
        return m_parentResource;
    }
    
    public String getResourceType() {
        return m_resourceType;
    }

    public String getResource() {
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

    public int compareTo(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (!(obj instanceof Graph)) {
            throw new IllegalArgumentException("Can only compare to Graph objects.");
        }

        Graph otherGraph = (Graph) obj;

	return m_graph.compareTo(otherGraph.m_graph);
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
