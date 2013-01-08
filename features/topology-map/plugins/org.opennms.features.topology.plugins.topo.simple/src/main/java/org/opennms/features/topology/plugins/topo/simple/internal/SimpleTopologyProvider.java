/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.simple.internal;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.SimpleGroup;
import org.opennms.features.topology.api.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

	protected static final String TOPOLOGY_NAMESPACE_SIMPLE = "simple";

	private static final Logger s_log = LoggerFactory.getLogger(SimpleTopologyProvider.class);

    private URL m_topologyLocation = null;

	private String m_namespace;
    
    public SimpleTopologyProvider() {
        this(TOPOLOGY_NAMESPACE_SIMPLE);
    }

    public SimpleTopologyProvider(String namespace) {
        super(namespace);
        s_log.debug("Creating a new SimpleTopologyProvider with namespace {}", namespace);
        
        URL defaultGraph = getClass().getResource("/saved-vmware-graph.xml");

        setTopologyLocation(defaultGraph);
    }

	public URL getTopologyLocation() {
		return m_topologyLocation;
	}

	public void setTopologyLocation(URL topologyLocation) {
		m_topologyLocation = topologyLocation;
		
		if (m_topologyLocation != null) {
			s_log.debug("Loading topology from " + m_topologyLocation);
			load(m_topologyLocation);
		} else {
			s_log.debug("Setting topology location to null");
			clearVertices();
			clearEdges();
		}
	}

    private Vertex addVertex(String id, int x, int y, String label, String ipAddr, int nodeID) {
        if (containsVertexId(id)) {
            throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
        }
        s_log.debug("Adding a vertex: {}", id);
        AbstractVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SIMPLE, id, x, y);
        vertex.setIconKey("server");
        vertex.setLabel(label);
        vertex.setIpAddress(ipAddr);
        vertex.setNodeID(nodeID);
        addVertices(vertex);
        return vertex;
    }
    
    private Vertex addGroup(String groupId, String iconKey, String label) {
        if (containsVertexId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists: " + getVertex(TOPOLOGY_NAMESPACE_SIMPLE, groupId).toString());
        }
        s_log.debug("Adding a group: {}", groupId);
        AbstractVertex vertex = new SimpleGroup(TOPOLOGY_NAMESPACE_SIMPLE, groupId);
        vertex.setLabel(label);
        vertex.setIconKey(iconKey);
        addVertices(vertex);
        return vertex;
    }

    @XmlRootElement(name="graph")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SimpleGraph {
        
        @XmlElements({
                @XmlElement(name="vertex", type=SimpleLeafVertex.class),
                @XmlElement(name="group", type=SimpleGroup.class)
        })
        List<AbstractVertex> m_vertices = new ArrayList<AbstractVertex>();
        
        @XmlElement(name="edge")
        List<AbstractEdge> m_edges = new ArrayList<AbstractEdge>();
        
        @XmlAttribute(name="namespace")
        String m_namespace;
        
        @SuppressWarnings("unused") // except by JAXB
        public SimpleGraph() {}

        public SimpleGraph(String namespace, List<AbstractVertex> vertices, List<AbstractEdge> edges) {
        	m_namespace = namespace;
            m_vertices = vertices;
            m_edges = edges;
        }
        
        public String getNamespace() { return m_namespace; }

    }
    
    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#save(java.lang.String)
	 */
    @Override
	public void save(String filename) {
        List<AbstractVertex> vertices = new ArrayList<AbstractVertex>();
        for (Vertex vertex : getVertices()) {
            vertices.add((AbstractVertex)vertex);
        }
        List<AbstractEdge> edges = new ArrayList<AbstractEdge>();
        for (Edge edge : getEdges()) {
            edges.add((AbstractEdge)edge);
        }

        SimpleGraph graph = new SimpleGraph(m_namespace, vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }
    
    private void load(URL url) {
        SimpleGraph graph = JAXB.unmarshal(url, SimpleGraph.class);
        
        clearVertices();
        addVertices(graph.m_vertices.toArray(new Vertex[] {}));
        
        clearEdges();
        addEdges(graph.m_edges.toArray(new Edge[] {}));
        
        if (graph.getNamespace() != null) {
        	m_namespace = graph.getNamespace();
        }
    }

    @Override
	public void load(String filename) {
        SimpleGraph graph = JAXB.unmarshal(new File(filename), SimpleGraph.class);
        
        clearVertices();
        for (Vertex vertex : graph.m_vertices) {
            if (vertex.getNamespace() == null || vertex.getId() == null) {
                LoggerFactory.getLogger(this.getClass()).warn("Invalid vertex unmarshalled from {}: {}", filename, vertex);
            } else if (vertex.getId().startsWith(SIMPLE_GROUP_ID_PREFIX)) {
                // Find the highest index group number and start the index for new groups above it
                int groupNumber = Integer.parseInt(vertex.getId().substring(SIMPLE_GROUP_ID_PREFIX.length()));
                if (m_groupCounter <= groupNumber) {
                    m_groupCounter = groupNumber + 1;
                }
            }
        }
        addVertices(graph.m_vertices.toArray(new Vertex[] {}));
        
        clearEdges();
        addEdges(graph.m_edges.toArray(new Edge[] {}));
    }

    @Override
    public Vertex addGroup(String groupLabel, String groupIconKey) {
        String nextGroupId = getNextGroupId();
        return addGroup(nextGroupId, groupIconKey, groupLabel);
    }
}
