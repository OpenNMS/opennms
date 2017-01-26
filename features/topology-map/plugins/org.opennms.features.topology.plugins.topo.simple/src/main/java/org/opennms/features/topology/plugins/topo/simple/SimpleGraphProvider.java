/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.simple;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleGroup;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.WrappedEdge;
import org.opennms.features.topology.api.topo.WrappedGraph;
import org.opennms.features.topology.api.topo.WrappedGroup;
import org.opennms.features.topology.api.topo.WrappedLeafVertex;
import org.opennms.features.topology.api.topo.WrappedVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SimpleGraphProvider extends AbstractTopologyProvider implements GraphProvider {

	protected static final String TOPOLOGY_NAMESPACE_SIMPLE = "simple";

	private static final Logger LOG = LoggerFactory.getLogger(SimpleGraphProvider.class);

	private URI m_topologyLocation = null;

    public SimpleGraphProvider() {
        this(TOPOLOGY_NAMESPACE_SIMPLE);
    }

    public SimpleGraphProvider(String namespace) {
        super(namespace);
        LOG.debug("Creating a new SimpleTopologyProvider with namespace {}", namespace);
    }

	public URI getTopologyLocation() {
		return m_topologyLocation;
	}

	public void setTopologyLocation(URI topologyLocation) throws MalformedURLException, JAXBException {
		m_topologyLocation = topologyLocation;
		
		if (m_topologyLocation != null && new File(m_topologyLocation).exists()) {
			LOG.debug("Loading topology from " + m_topologyLocation);
			load(m_topologyLocation);
		} else {
			LOG.debug("Setting topology location to null");
			clearVertices();
			clearEdges();
		}
	}

	public void save(String filename) throws MalformedURLException, JAXBException {
		m_topologyLocation = new File(filename).toURI();
		save();
	}

    @Override
    public void save() {
        List<WrappedVertex> vertices = new ArrayList<WrappedVertex>();
        for (Vertex vertex : getVertices()) {
            if (vertex.isGroup()) {
                vertices.add(new WrappedGroup(vertex));
            } else {
                vertices.add(new WrappedLeafVertex(vertex));
            }
        }
        List<WrappedEdge> edges = new ArrayList<WrappedEdge>();
        for (Edge edge : getEdges()) {
            WrappedEdge newEdge = new WrappedEdge(edge, new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getSource().getVertex())), new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getTarget().getVertex())));
            edges.add(newEdge);
        }

        WrappedGraph graph = new WrappedGraph(getVertexNamespace(), vertices, edges);

        try {
        	JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class, WrappedLeafVertex.class, WrappedGroup.class, WrappedEdge.class);
        	Marshaller u = jc.createMarshaller();
        	u.marshal(graph, new File(getTopologyLocation()));
        } catch (JAXBException e) {
        	LOG.error(e.getMessage(), e);
        }
    }
    
    private void load(final URI source, final WrappedGraph graph) {
        String namespace = graph.m_namespace == null ? TOPOLOGY_NAMESPACE_SIMPLE : graph.m_namespace;
        if (getVertexNamespace() != namespace) { 
            LoggerFactory.getLogger(this.getClass()).info("Creating new vertex provider with namespace {}", namespace);
            m_vertexProvider = new SimpleVertexProvider(namespace);
        }
        if (getEdgeNamespace() != namespace) { 
            LoggerFactory.getLogger(this.getClass()).info("Creating new edge provider with namespace {}", namespace);
            m_edgeProvider = new SimpleEdgeProvider(namespace);
        }
        resetContainer();
        for (WrappedVertex vertex : graph.m_vertices) {
            if (vertex.namespace == null) {
                vertex.namespace = getVertexNamespace();
                LoggerFactory.getLogger(this.getClass()).warn("Setting namespace on vertex to default: {}", vertex);
            } 

            if (vertex.id == null) {
                LoggerFactory.getLogger(this.getClass()).warn("Invalid vertex unmarshalled from {}: {}", source.toString(), vertex);
            }
            AbstractVertex newVertex;
            if (vertex.group) {
                newVertex = new SimpleGroup(vertex.namespace, vertex.id);
                if (vertex.x != null) newVertex.setX(vertex.x);
                if (vertex.y != null) newVertex.setY(vertex.y);
            } else {
                newVertex = new SimpleLeafVertex(vertex.namespace, vertex.id, vertex.x, vertex.y);
            }
            newVertex.setIconKey(vertex.iconKey);
            newVertex.setIpAddress(vertex.ipAddr);
            newVertex.setLabel(vertex.label);
            newVertex.setLocked(vertex.locked);
            if (vertex.nodeID != null) newVertex.setNodeID(vertex.nodeID);
            if (!newVertex.equals(vertex.parent)) newVertex.setParent(vertex.parent);
            newVertex.setSelected(vertex.selected);
            newVertex.setStyleName(vertex.styleName);
            newVertex.setTooltipText(vertex.tooltipText);
            addVertices(newVertex);
        }
        
        for (WrappedEdge edge : graph.m_edges) {
            if (edge.namespace == null) {
                edge.namespace = getEdgeNamespace();
                LoggerFactory.getLogger(this.getClass()).warn("Setting namespace on edge to default: {}", edge);
            } 

            if (edge.id == null || edge.source == null || edge.target == null) {
                LoggerFactory.getLogger(this.getClass()).warn("Invalid edge unmarshalled from {}: {}", source.toString(), edge);
            } else if (edge.id.startsWith(SIMPLE_EDGE_ID_PREFIX)) {
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
                    // Ignore this edge ID since it doesn't conform to our pattern for auto-generated IDs
                }
            }
            AbstractEdge newEdge = connectVertices(edge.id, edge.source, edge.target, edge.namespace);
            newEdge.setLabel(edge.label);
            newEdge.setTooltipText(edge.tooltipText);
            //addEdges(newEdge);
        }

        for (WrappedVertex vertex: graph.m_vertices) {
            if (vertex.parent != null && !vertex.equals(vertex.parent)) {
                LoggerFactory.getLogger(this.getClass()).debug("Setting parent of " + vertex + " to " + vertex.parent);
                setParent(vertex, vertex.parent);
            }
        }
    }

    @Override
    public void refresh() {
        try {
            load(getTopologyLocation());
        } catch (JAXBException e) {
            LOG.error(e.getMessage(), e);
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Criteria getDefaultCriteria() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    void load(URI url) throws JAXBException, MalformedURLException {
    	if (url == null) {
    		throw new IllegalArgumentException("URI for SimpleGraphProvider configuration must not be null");
    	}
    	JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class);
    	Unmarshaller u = jc.createUnmarshaller();
    	WrappedGraph graph = (WrappedGraph) u.unmarshal(url.toURL());
    	load(url, graph);
    }

    @Override
    public void load(String filename) throws MalformedURLException, JAXBException {
        if (filename != null) {
            setTopologyLocation(new File(filename).toURI());
        } else {
            load(getTopologyLocation());
        }
    }

    protected SelectionChangedListener.Selection getSelection(String namespace, List<VertexRef> selectedVertices, ContentType type) {
        final Set<Integer> nodeIds = selectedVertices.stream()
                .filter(v -> namespace.equals(v.getNamespace()))
                .filter(v -> v instanceof AbstractVertex)
                .map(v -> (AbstractVertex) v)
                .map(v -> v.getNodeID())
                .filter(nodeId -> nodeId != null)
                .collect(Collectors.toSet());
        if (type == ContentType.Alarm) {
            return new SelectionChangedListener.AlarmNodeIdSelection(nodeIds);
        }
        if (type == ContentType.Node) {
            return new SelectionChangedListener.IdSelection<>(nodeIds);
        }
        return SelectionChangedListener.Selection.NONE;
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType contentType) {
            return getSelection(TOPOLOGY_NAMESPACE_SIMPLE, selectedVertices, contentType);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return Sets.newHashSet(ContentType.Alarm, ContentType.Node).contains(type);
    }
}
