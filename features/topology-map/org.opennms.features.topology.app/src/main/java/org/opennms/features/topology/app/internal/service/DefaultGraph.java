/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
