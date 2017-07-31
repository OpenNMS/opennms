/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphVisitor;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.DefaultLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class DefaultGraph implements Graph {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGraph.class);

    private final Set<Vertex> m_displayVertices = new TreeSet<>(new RefComparator());
    private final Set<Edge> m_displayEdges = new TreeSet<>(new RefComparator());
    private Layout m_layout;
    private Map<? extends EdgeRef, ? extends Status> edgeStatus = Maps.newHashMap();
    private Map<? extends VertexRef, ? extends Status> vertexStatus = Maps.newHashMap();

    public DefaultGraph(Collection<Vertex> displayVertices, Collection<Edge> displayEdges) {
        updateLayout(displayVertices, displayEdges);
        setLayout(new DefaultLayout());
    }

    @Override
    public Layout getLayout() {
        return m_layout;
    }

    @Override
    public void setLayout(Layout layout) {
        m_layout = layout;
    }

    @Override
    public Collection<Vertex> getDisplayVertices() {
        return Collections.unmodifiableCollection(m_displayVertices);
    }

    @Override
    public Collection<Edge> getDisplayEdges() {
        return Collections.unmodifiableCollection(m_displayEdges);
    }

    @Override
    public Edge getEdgeByKey(String edgeKey) {
        for(Edge e : m_displayEdges) {
            if (edgeKey.equals(e.getKey())) {
                return e;
            }
        }
        return null;
    }

    @Override
    public Vertex getVertexByKey(String vertexKey) {
        for(Vertex v : m_displayVertices) {
            if (vertexKey.equals(v.getKey())) {
                return v;
            }
        }
        return null;
    }

    @Override
    public void visit(GraphVisitor visitor) throws Exception {

        visitor.visitGraph(this);

        for(Vertex v : m_displayVertices) {
            visitor.visitVertex(v);
        }

        for(Edge e : m_displayEdges) {
            visitor.visitEdge(e);
        }

        visitor.completeGraph(this);
    }

    @Override
    public Map<? extends EdgeRef, ? extends Status> getEdgeStatus() {
        return edgeStatus;
    }

    public void setVertexStatus(Map<? extends VertexRef, ? extends Status> statusForVertices) {
        this.vertexStatus = statusForVertices;
    }

    public void setEdgeStatus(Map<? extends EdgeRef, ? extends Status> edgeStatus) {
        this.edgeStatus = edgeStatus;
    }

    @Override
    public Map<? extends VertexRef, ? extends Status> getVertexStatus() {
        return vertexStatus;
    }

    private void updateLayout(Collection<Vertex> displayVertices, Collection<Edge> displayEdges) {
        m_displayVertices.clear();
        m_displayVertices.addAll(displayVertices);
        m_displayEdges.clear();
        m_displayEdges.addAll(displayEdges);
        for (Iterator<Edge> itr = m_displayEdges.iterator(); itr.hasNext();) {
            Edge edge = itr.next();
            if (new RefComparator().compare(edge.getSource().getVertex(), edge.getTarget().getVertex()) == 0) {
                LOG.debug("Discarding edge whose source and target are the same: {}", edge);
                itr.remove();
            } else if (m_displayVertices.contains(edge.getSource().getVertex())) {
                if (m_displayVertices.contains(edge.getTarget().getVertex())) {
                    // This edge is OK, it is attached to two vertices that are in the graph
                } else {
                    LOG.debug("Discarding edge that is not attached to 2 vertices in the graph: {}", edge);
                    itr.remove();
                }
            } else {
                LOG.debug("Discarding edge that is not attached to 2 vertices in the graph: {}", edge);
                itr.remove();
            }
        }
        LOG.debug("Created a graph with {} vertices and {} edges", m_displayVertices.size(), m_displayEdges.size());
    }
}
