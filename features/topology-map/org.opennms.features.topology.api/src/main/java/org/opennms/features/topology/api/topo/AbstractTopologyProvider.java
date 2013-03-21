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

package org.opennms.features.topology.api.topo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

public abstract class AbstractTopologyProvider extends DelegatingVertexEdgeProvider implements GraphProvider {

	protected static final String SIMPLE_VERTEX_ID_PREFIX = "v";
	protected static final String SIMPLE_GROUP_ID_PREFIX = "g";
	protected static final String SIMPLE_EDGE_ID_PREFIX = "e";

    private int m_counter = 0;
    protected int m_groupCounter = 0;
    private int m_edgeCounter = 0;

    protected AbstractTopologyProvider(String namespace) {
		super(namespace);
	}

    protected String getNextVertexId() {
        return SIMPLE_VERTEX_ID_PREFIX + m_counter++;
    }

    protected String getNextGroupId() {
        return SIMPLE_GROUP_ID_PREFIX + m_groupCounter++;
    }

    protected String getNextEdgeId() {
        return SIMPLE_EDGE_ID_PREFIX + m_edgeCounter++;
    }
    
    @Override
    public final void removeVertex(VertexRef... vertexId) {
        for (VertexRef vertex : vertexId) {
            if (vertex == null) continue;
            
            getSimpleVertexProvider().remove(vertexId);
            
            removeEdges(getEdgeIdsForVertex(vertex));
        }
    }

    @Override
    public final void addVertices(Vertex... vertices) {
        getSimpleVertexProvider().add(vertices);
    }

    @Override
    public final AbstractVertex addVertex(int x, int y) {
        String id = getNextVertexId();
        return addVertex(id, x, y);
    }
    
    protected final AbstractVertex addVertex(String id, int x, int y) {
        LoggerFactory.getLogger(getClass()).debug("Adding vertex in {} with ID: {}", getClass().getSimpleName(), id);
        AbstractVertex vertex = new SimpleLeafVertex(getVertexNamespace(), id, x, y);
        getSimpleVertexProvider().add(vertex);
        return vertex;
    }

    @Override
    public final AbstractVertex addGroup(String groupName, String groupIconKey) {
        String nextGroupId = getNextGroupId();
        return addGroup(nextGroupId, groupIconKey, groupName);
    }

    protected final AbstractVertex addGroup(String groupId, String iconKey, String label) {
        if (containsVertexId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        LoggerFactory.getLogger(this.getClass()).debug("Adding a group: {}", groupId);
        AbstractVertex vertex = new SimpleGroup(getVertexNamespace(), groupId);
        vertex.setLabel(label);
        vertex.setIconKey(iconKey);
        addVertices(vertex);
        return vertex;
    }

    @Override
    public final void addEdges(Edge... edges) {
        getSimpleEdgeProvider().add(edges);
    }

    @Override
    public final void removeEdges(EdgeRef... edge) {
        getSimpleEdgeProvider().remove(edge);
    }

    @Override
    public final EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
        if (vertex == null) return new EdgeRef[0];
        List<EdgeRef> retval = new ArrayList<EdgeRef>();
        for (Edge edge : getEdges()) {
            // If the vertex is connected to the edge then add it
            if (new RefComparator().compare(edge.getSource().getVertex(), vertex) == 0 || new RefComparator().compare(edge.getTarget().getVertex(), vertex) == 0) {
                retval.add(edge);
            }
        }
        return retval.toArray(new EdgeRef[0]);
    }

    @Override
	public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
        String nextEdgeId = getNextEdgeId();
        return connectVertices(nextEdgeId, sourceVertextId, targetVertextId);
    }

    protected final AbstractEdge connectVertices(String id, VertexRef sourceId, VertexRef targetId) {
        SimpleConnector source = new SimpleConnector(getEdgeNamespace(), sourceId.getId()+"-"+id+"-connector", sourceId);
        SimpleConnector target = new SimpleConnector(getEdgeNamespace(), targetId.getId()+"-"+id+"-connector", targetId);

        AbstractEdge edge = new AbstractEdge(getEdgeNamespace(), id, source, target);

        addEdges(edge);
        
        return edge;
    }

    @Override
    public void resetContainer() {
        clearVertices();
        clearEdges();

        m_counter = 0;
        m_groupCounter = 0;
        m_edgeCounter = 0;
    }

}
