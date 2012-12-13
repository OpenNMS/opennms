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
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.EditableGraphProvider;
import org.opennms.features.topology.api.SimpleEdge;
import org.opennms.features.topology.api.SimpleGroup;
import org.opennms.features.topology.api.SimpleVertex;
import org.opennms.features.topology.api.SimpleVertexContainer;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class SimpleTopologyProvider implements EditableGraphProvider {
	
	private static final Logger s_log = LoggerFactory.getLogger(SimpleTopologyProvider.class);

	private static final String SIMPLE_VERTEX_ID_PREFIX = "v";
	private static final String SIMPLE_EDGE_ID_PREFIX = "e";
	private static final String SIMPLE_GROUP_ID_PREFIX = "g";

    private final SimpleVertexContainer m_vertexContainer;
    private final BeanContainer<String, SimpleEdge> m_edgeContainer;
    private int m_counter = 0;
    private int m_edgeCounter = 0;
    private int m_groupCounter = 0;
    
    private URL m_topologyLocation = null;

	private String m_namespace;
    
    public SimpleTopologyProvider() {
        s_log.debug("Creating a new SimpleTopologyProvider");
        m_vertexContainer = new SimpleVertexContainer();
        m_edgeContainer = new BeanContainer<String, SimpleEdge>(SimpleEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
        
        URL defaultGraph = getClass().getResource("/saved-vmware-graph.xml");

        setTopologyLocation(defaultGraph);
    }
    
    @Override
    public String getNamespace() {
    	return m_namespace;
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
			m_vertexContainer.removeAllItems();
			m_edgeContainer.removeAllItems();
		}
	}

    private Vertex addVertex(String id, int x, int y, String label, String ipAddr, int nodeID) {
        if (m_vertexContainer.containsId(id)) {
            throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
        }
        s_log.debug("Adding a vertex: {}", id);
        SimpleVertex vertex = new SimpleLeafVertex(id, x, y);
        vertex.setIconKey("server");
        vertex.setLabel(label);
        vertex.setIpAddr(ipAddr);
        vertex.setNodeID(nodeID);
        m_vertexContainer.addBean(vertex);
        return vertex;
    }
    
    @Override
    public Item addGroup(String groupId, String iconKey, String label) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        s_log.debug("Adding a group: {}", groupId);
        SimpleVertex vertex = new SimpleGroup(groupId);
        vertex.setLabel(label);
        vertex.setIconKey(iconKey);
        return m_vertexContainer.addBean(vertex);
        
    }
    private Edge connectVertices(String id, Vertex sourceVertexId, Vertex targetVertexId) {

        SimpleEdge edge = new SimpleEdge("simple", id, sourceVertexId, targetVertexId);
        
        m_edgeContainer.addBean(edge);
        
        return edge;
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#removeVertex(java.lang.Object)
	 */
    @Override
	public void removeVertex(Object vertexId) {
        
        Vertex vertex = getVertex(vertexId, false);
        if (vertex == null) return;
        
        m_vertexContainer.removeItem(vertexId);
        
        for(SimpleEdge e : vertex.getEdges()) {
            m_edgeContainer.removeItem(e.getId());
        }
                
        
    }

    private Vertex getRequiredVertex(Object vertexId) {
        return getVertex(vertexId, true);
    }

    private Vertex getVertex(Object vertexId, boolean required) {
        BeanItem<Vertex> item = m_vertexContainer.getItem(vertexId);
        if (required && item == null) {
            throw new IllegalArgumentException("required vertex " + vertexId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }

    private Edge getRequiredEdge(Object edgeId) {
        return getEdge(edgeId, true);
    }

    private Edge getEdge(Object edgeId, boolean required) {
        BeanItem<SimpleEdge> item = m_edgeContainer.getItem(edgeId);
        if (required && item == null) {
            throw new IllegalArgumentException("required edge " + edgeId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }
    

    @XmlRootElement(name="graph")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SimpleGraph {
        
        @XmlElements({
                @XmlElement(name="vertex", type=SimpleLeafVertex.class),
                @XmlElement(name="group", type=SimpleGroup.class)
        })
        List<Vertex> m_vertices = new ArrayList<Vertex>();
        
        @XmlElement(name="edge")
        List<SimpleEdge> m_edges = new ArrayList<SimpleEdge>();
        
        @XmlAttribute(name="namespace")
        String m_namespace;
        
        @SuppressWarnings("unused") // except by JAXB
        public SimpleGraph() {}

        public SimpleGraph(String namespace, List<Vertex> vertices, List<SimpleEdge> edges) {
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
        List<Vertex> vertices = getBeans(m_vertexContainer);
        List<SimpleEdge> edges = getBeans(m_edgeContainer);

        SimpleGraph graph = new SimpleGraph(m_namespace, vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }
    
    @Override
	public void load(URL url) {
        SimpleGraph graph = JAXB.unmarshal(url, SimpleGraph.class);
        
        m_vertexContainer.removeAllItems();
        m_vertexContainer.addAll(graph.m_vertices);
        
        m_edgeContainer.removeAllItems();
        m_edgeContainer.addAll(graph.m_edges);
        
        if (graph.getNamespace() != null) {
        	m_namespace = graph.getNamespace();
        }
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#load(java.lang.String)
	 */
    @Override
	public void load(String filename) {
        SimpleGraph graph = JAXB.unmarshal(new File(filename), SimpleGraph.class);
        
        m_vertexContainer.removeAllItems();
        for (Vertex vertex : graph.m_vertices) {
            if (vertex.getId().startsWith(SIMPLE_GROUP_ID_PREFIX)) {
                // Find the highest index group number and start the index for new groups above it
                int groupNumber = Integer.parseInt(vertex.getId().substring(SIMPLE_GROUP_ID_PREFIX.length()));
                if (m_groupCounter <= groupNumber) {
                    m_groupCounter = groupNumber + 1;
                }
            }
        }
        m_vertexContainer.addAll(graph.m_vertices);
        
        m_edgeContainer.removeAllItems();
        m_edgeContainer.addAll(graph.m_edges);
    }
    
    private static <T> List<T> getBeans(BeanContainer<?, T> container) {
        Collection<?> itemIds = container.getItemIds();
        List<T> beans = new ArrayList<T>(itemIds.size());
        
        for(Object itemId : itemIds) {
            beans.add(container.getItem(itemId).getBean());
        }
        
        return beans;
    }

    protected String getNextVertexId() {
        return SIMPLE_VERTEX_ID_PREFIX + m_counter++;
    }

    protected String getNextEdgeId() {
        return SIMPLE_EDGE_ID_PREFIX + m_edgeCounter ++;
    }
    
    protected String getNextGroupId() {
        return SIMPLE_GROUP_ID_PREFIX + m_groupCounter++;
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#resetContainer()
	 */
    @Override
	public void resetContainer() {
        m_vertexContainer.removeAllItems();
        m_edgeContainer.removeAllItems();
        
        m_counter = 0;
        m_edgeCounter = 0;
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#addVertex(int, int)
	 */
    @Override
	public Vertex addVertex(int x, int y) {
        String nextVertexId = getNextVertexId();
//        addVertex(nextVertexId, x, y, icon, "Vertex " + nextVertexId, "127.0.0.1", -1);
        /* 
         * Passing a nodeID of -1 will disable the Events/Alarms, Node Info, and
         * Resource Graphs windows in the context menus  
         */
        return addVertex(nextVertexId, x, y, "Vertex " + nextVertexId, "64.146.64.214", -1);
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        m_vertexContainer.setParent(vertexId, parentId);
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#connectVertices(java.lang.Object, java.lang.Object)
	 */
    @Override
	public Edge connectVertices(Vertex sourceVertextId, Vertex targetVertextId) {
        String nextEdgeId = getNextEdgeId();
        return connectVertices(nextEdgeId, sourceVertextId, targetVertextId);
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#addGroup(java.lang.String)
	 */

    @Override
    public Object addGroup(String groupLabel, String groupIconKey) {
        String nextGroupId = getNextGroupId();
        addGroup(nextGroupId, groupIconKey, groupLabel);
        return nextGroupId;
    }
}
