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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.features.topology.api.SimpleGroup;
import org.opennms.features.topology.api.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

	protected static final String TOPOLOGY_NAMESPACE_SIMPLE = "simple";

	private static final Logger s_log = LoggerFactory.getLogger(SimpleTopologyProvider.class);

    private URI m_topologyLocation = null;

    public SimpleTopologyProvider() {
        this(TOPOLOGY_NAMESPACE_SIMPLE);
    }

    public SimpleTopologyProvider(String namespace) {
        super(namespace);
        s_log.debug("Creating a new SimpleTopologyProvider with namespace {}", namespace);
        
        //URL defaultGraph = getClass().getResource("/saved-vmware-graph.xml");

        //setTopologyLocation(defaultGraph);
    }

	public URI getTopologyLocation() {
		return m_topologyLocation;
	}

	public void setTopologyLocation(URI topologyLocation) {
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

    /*
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
    */
    
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
    public static class SimpleGraph {

        @XmlElements({
            @XmlElement(name="vertex", type=SimpleLeafVertex.class),
            @XmlElement(name="group", type=SimpleGroup.class)
        })
        List<AbstractVertex> m_vertices = new ArrayList<AbstractVertex>();

        @XmlElement(name="edge")
        List<JaxbEdge> m_edges = new ArrayList<JaxbEdge>();

        private String m_namespace;

        /**
         * No-arg constructor for JAXB.
         */
        public SimpleGraph() {}

        public SimpleGraph(String namespace, List<AbstractVertex> vertices, List<JaxbEdge> edges) {
            m_namespace = namespace;
            m_vertices = vertices;
            m_edges = edges;
        }

        @XmlAttribute
        public String getNamespace() { return m_namespace; }
        public void setNamespace(String namespace) { m_namespace = namespace; }
    }

    public static class JaxbEdge {

    	// Required
    	private String m_edgeNamespace;
    	// Required
    	private String m_id;
    	// Required
    	private AbstractVertex m_source;
    	// Required
    	private AbstractVertex m_target;

    	private String m_label;
    	private String m_tooltipText;
    	private String m_styleName;

    	/**
    	 * No-arg constructor for JAXB.
    	 */
    	public JaxbEdge() {}

    	/**
    	 * This JAXB function is used to set the namespace since we expect it to be set in the parent object.
    	 */
    	public void afterUnmarshal(Unmarshaller u, Object parent) {
    		if (m_edgeNamespace == null) {
    			try {
    				BeanInfo info = Introspector.getBeanInfo(parent.getClass());
    				for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
    					if ("namespace".equals(descriptor.getName())) {
    						m_edgeNamespace = (String)descriptor.getReadMethod().invoke(parent);
    					}
    				}
    			} catch (IntrospectionException e) {
    				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
    			} catch (IllegalArgumentException e) {
    				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
    			} catch (IllegalAccessException e) {
    				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
    			} catch (InvocationTargetException e) {
    				LoggerFactory.getLogger(this.getClass()).warn("Exception thrown when trying to fetch namespace from parent class " + parent.getClass(), e);
    			}
    		}
    	}

    	@XmlID
    	public String getId() {
    		return m_id;
    	}

    	/**
    	 * This setter is private so that it can only be used by JAXB.
    	 */
    	private final void setId(String id) {
    		m_id = id;
    	}

    	@XmlTransient
    	public final String getNamespace() {
    		return m_edgeNamespace;
    	}

    	public final void setNamespace(String namespace) {
    		m_edgeNamespace = namespace;
    	}

    	public String getLabel() {
    		return m_label;
    	}

    	public String getTooltipText() {
    		return m_tooltipText;
    	}

    	public String getStyleName() {
    		return m_styleName;
    	}

    	public final void setLabel(String label) {
    		m_label = label;
    	}

    	public final void setTooltipText(String tooltipText) {
    		m_tooltipText = tooltipText;
    	}

    	public final void setStyleName(String styleName) {
    		m_styleName = styleName;
    	}

    	@XmlIDREF
    	public final AbstractVertex getSource() {
    		return m_source;
    	}

    	public void setSource(AbstractVertex source) {
    		m_source = source;
    	}

    	@XmlIDREF
    	public final AbstractVertex getTarget() {
    		return m_target;
    	}
    	
    	public void setTarget(AbstractVertex target) {
    		m_target = target;
    	}

    	@Override
    	public String toString() { return "JaxbEdge:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 
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
        List<JaxbEdge> edges = new ArrayList<JaxbEdge>();
        for (Edge edge : getEdges()) {
            JaxbEdge newEdge = new JaxbEdge();
            newEdge.setNamespace(edge.getNamespace());
            newEdge.setId(edge.getId());
            newEdge.setLabel(edge.getLabel());
            newEdge.setTooltipText(edge.getTooltipText());
            newEdge.setStyleName(edge.getStyleName());
            newEdge.setSource((AbstractVertex)edge.getSource().getVertex());
            newEdge.setTarget((AbstractVertex)edge.getTarget().getVertex());
            edges.add(newEdge);
        }

        SimpleGraph graph = new SimpleGraph(getVertexNamespace(), vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }
    
    private void load(SimpleGraph graph) {
        String namespace = graph.m_namespace == null ? TOPOLOGY_NAMESPACE_SIMPLE : graph.m_namespace;
        if (getVertexNamespace() != namespace) { 
            m_vertexProvider = new SimpleVertexProvider(namespace);
        }
        if (getEdgeNamespace() != namespace) { 
            m_edgeProvider = new SimpleEdgeProvider(namespace);
        }

        clearVertices();
        for (Vertex vertex : graph.m_vertices) {
            if (vertex.getNamespace() == null || vertex.getId() == null) {
                LoggerFactory.getLogger(this.getClass()).warn("Invalid vertex unmarshalled from {}: {}", graph, vertex);
            } else if (vertex.getId().startsWith(SIMPLE_GROUP_ID_PREFIX)) {
                try {
                    // Find the highest index group number and start the index for new groups above it
                    int groupNumber = Integer.parseInt(vertex.getId().substring(SIMPLE_GROUP_ID_PREFIX.length()));
                    if (m_groupCounter <= groupNumber) {
                        m_groupCounter = groupNumber + 1;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        addVertices(graph.m_vertices.toArray(new Vertex[0]));
        
        clearEdges();
        for (JaxbEdge edge : graph.m_edges) {
            if (edge.getNamespace() == null || edge.getId() == null) {
                LoggerFactory.getLogger(this.getClass()).warn("Invalid edge unmarshalled from {}: {}", graph, edge);
            } else if (edge.getId().startsWith(SIMPLE_EDGE_ID_PREFIX)) {
                try {
                    /*
                     * This code will be necessary if we allow edges to be created
                    
                    // Find the highest index group number and start the index for new groups above it
                    int edgeNumber = Integer.parseInt(edge.getId().substring(SIMPLE_EDGE_ID_PREFIX.length()));
                    
                    if (m_edgeCounter <= edgeNumber) {
                        m_edgeCounter = edgeNumber + 1;
                    }
                    */
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            Edge newEdge = new AbstractEdge(edge.getNamespace(), edge.getId(), edge.getSource(), edge.getTarget());
            addEdges(newEdge);
        }
    }

    private void load(URI url) {
        SimpleGraph graph = JAXB.unmarshal(url, SimpleGraph.class);
        load(graph);
    }

    @Override
    public void load(String filename) {
        load(new File(filename).toURI());
    }

    @Override
    public Vertex addGroup(String groupLabel, String groupIconKey) {
        String nextGroupId = getNextGroupId();
        return addGroup(nextGroupId, groupIconKey, groupLabel);
    }
}
