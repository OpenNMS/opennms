package org.opennms.features.topology.plugins.devutils.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="graph")
@XmlAccessorType(XmlAccessType.FIELD)
class WrappedGraph {
    
    @XmlElements({
            @XmlElement(name="vertex", type=WrappedLeafVertex.class),
            @XmlElement(name="group", type=WrappedGroup.class)
    })
    List<WrappedVertex> m_vertices = new ArrayList<WrappedVertex>();
    
    @XmlElement(name="edge")
    List<WrappedEdge> m_edges = new ArrayList<WrappedEdge>();
    
    public WrappedGraph() {}

    public WrappedGraph(List<WrappedVertex> vertices, List<WrappedEdge> edges) {
        m_vertices = vertices;
        m_edges = edges;
    }

}