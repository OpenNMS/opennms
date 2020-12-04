/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.updates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public final class ChangeSet<G extends ImmutableGraph<V, E>, V extends Vertex, E extends Edge> {
    private final String namespace;
    private final Date changeSetDate;
    private final List<V> verticesAdded;
    private final List<V> verticesRemoved;
    private final List<V> verticesUpdated;
    private final List<E> edgesAdded;
    private final List<E> edgesRemoved;
    private final List<E> edgesUpdated;
    private final GraphInfo currentGraphInfo;
    private final Focus currentFocus;

    public ChangeSet(final ChangeSetBuilder builder) {
        Objects.requireNonNull(builder);
        this.namespace = builder.namespace;
        this.changeSetDate = builder.changeSetDate == null ? new Date() : builder.changeSetDate;
        this.verticesAdded = Collections.unmodifiableList(builder.verticesAdded);
        this.verticesRemoved = Collections.unmodifiableList(builder.verticesRemoved);
        this.verticesUpdated = Collections.unmodifiableList(builder.verticesUpdated);
        this.edgesAdded = Collections.unmodifiableList(builder.edgesAdded);
        this.edgesRemoved = Collections.unmodifiableList(builder.edgesRemoved);
        this.edgesUpdated = Collections.unmodifiableList(builder.edgesUpdated);
        this.currentGraphInfo = builder.currentGraphInfo;
        this.currentFocus = builder.currentFocus;
    }

    public String getNamespace() {
        return namespace;
    }

    public Date getChangeSetDate() {
        return changeSetDate;
    }

    // Besides its name it may return null
    public GraphInfo getGraphInfo() {
        return currentGraphInfo;
    }

    public Focus getFocus() {
        return currentFocus;
    }

    public List<V> getVerticesAdded() {
        return verticesAdded;
    }

    public List<V> getVerticesRemoved() {
        return verticesRemoved;
    }

    public List<V> getVerticesUpdated() {
        return verticesUpdated;
    }

    public List<E> getEdgesAdded() {
        return edgesAdded;
    }

    public List<E> getEdgesRemoved() {
        return edgesRemoved;
    }

    public List<E> getEdgesUpdated() {
        return edgesUpdated;
    }

    public boolean hasGraphInfoChanged() {
        return currentGraphInfo != null;
    }

    public boolean hasFocusChanged() {
        return currentFocus != null;
    }

    public boolean hasChanges() {
        return hasGraphInfoChanged()
                || hasFocusChanged()
                || !edgesAdded.isEmpty()
                || !edgesRemoved.isEmpty()
                || !edgesUpdated.isEmpty()
                || !verticesAdded.isEmpty()
                || !verticesRemoved.isEmpty()
                || !verticesUpdated.isEmpty();
    }

    public static <G extends ImmutableGraph<V, E>, V extends Vertex, E extends Edge> ChangeSetBuilder<G, V, E> builder(G oldGraph, G newGraph) {
        return new ChangeSetBuilder<>(oldGraph, newGraph);
    }

    public static final class ChangeSetBuilder<G extends ImmutableGraph<V, E>, V extends Vertex, E extends Edge> {
        private final G newGraph;
        private final G oldGraph;
        private String namespace;
        private Date changeSetDate;
        private List<V> verticesAdded = new ArrayList<>();
        private List<V> verticesRemoved = new ArrayList<>();
        private List<V> verticesUpdated = new ArrayList<>();
        private List<E> edgesAdded = new ArrayList<>();
        private List<E> edgesRemoved = new ArrayList<>();
        private List<E> edgesUpdated = new ArrayList<>();
        private GraphInfo currentGraphInfo;
        private Focus currentFocus;

        private ChangeSetBuilder(G oldGraph, G newGraph) {
            if (oldGraph == null && newGraph == null) {
                throw new IllegalArgumentException("Cannot create change set if both graphs are null.");
            }
            this.namespace = oldGraph == null ? newGraph.getNamespace() : oldGraph.getNamespace();
            this.oldGraph = oldGraph;
            this.newGraph = newGraph;
        }

        public ChangeSetBuilder withDate(Date changeSetDate) {
            this.changeSetDate = Objects.requireNonNull(changeSetDate);
            return this;
        }

        private ChangeSetBuilder vertexAdded(V vertex) {
            verticesAdded.add(vertex);
            return this;
        }

        private ChangeSetBuilder vertexRemoved(V vertex) {
            verticesRemoved.add(vertex);
            return this;
        }

        private ChangeSetBuilder vertexUpdated(V vertex) {
            verticesUpdated.add(vertex);
            return this;
        }

        private ChangeSetBuilder edgeAdded(E edge) {
            edgesAdded.add(edge);
            return this;
        }

        private ChangeSetBuilder edgeRemoved(E edge) {
            edgesRemoved.add(edge);
            return this;
        }

        private ChangeSetBuilder edgeUpdated(E edge) {
            edgesUpdated.add(edge);
            return this;
        }

        private ChangeSetBuilder graphInfoChanged(GraphInfo graphInfo) {
            this.currentGraphInfo = graphInfo;
            return this;
        }

        private ChangeSetBuilder focusChanged(Focus newFocus) {
            this.currentFocus = newFocus;
            return this;
        }

        private void detectChanges(G oldGraph, G newGraph) {
            // no old graph exists, add all
            if (oldGraph == null && newGraph != null) {
                newGraph.getVertices().forEach(v -> vertexAdded(v));
                newGraph.getEdges().forEach(e -> edgeAdded(e));
                graphInfoChanged(createGraphInfo(newGraph));
            }
            // no new graph exists, remove all
            if (oldGraph != null && newGraph == null) {
                oldGraph.getVertices().forEach(v -> vertexRemoved(v));
                oldGraph.getEdges().forEach(e -> edgeRemoved(e));
                graphInfoChanged(null);
            }

            // Nothing to do if same
            if (oldGraph == newGraph) {
                return;
            }

            // both graph exists, so calculate changes
            if (oldGraph != null && newGraph != null) {
                // Before changes can be calculated, ensure the graphs share the same namespace, otherwise
                // we should bail, as this is theoretical/technical possible, but does not make sense from the
                // domain view the namespace reflects.
                if (!oldGraph.getNamespace().equals(newGraph.getNamespace())) {
                    throw new IllegalStateException("Cannot detect changes between different namespaces");
                }
                detectFocusChange(oldGraph, newGraph);
                detectGraphInfoChanges(oldGraph, newGraph);
                detectVertexChanges(oldGraph, newGraph);
                detectEdgeChanges(oldGraph, newGraph);
            }
        }

        private void detectFocusChange(G oldGraph, G newGraph) {
            final Focus oldFocus = oldGraph.getDefaultFocus();
            final Focus newFocus = newGraph.getDefaultFocus();
            if (!Objects.equals(oldFocus, newFocus)) {
                focusChanged(newFocus);
            }
        }

        // Graph Changes are all changes to the graph, except vertices and edges, such as description, focus, etc.
        // INFO: the namespace cannot change
        private void detectGraphInfoChanges(G oldGraph, G newGraph) {
            final GraphInfo oldGraphInfo = createGraphInfo(oldGraph);
            final GraphInfo newGraphInfo = createGraphInfo(newGraph);
            if (!Objects.equals(oldGraphInfo, newGraphInfo)) {
                graphInfoChanged(newGraphInfo); // The changes are pretty easy to detect, so we only only push the current info
            }
        }

        private GraphInfo createGraphInfo(G graph) {
            if (graph != null) {
                final DefaultGraphInfo graphInfo = new DefaultGraphInfo(graph);
                return graphInfo;
            }
            return null;
        }

        private void detectVertexChanges(G oldGraph, G newGraph) {
            // Find all vertices/edges which are in the old and new graph
            final List<String> oldVertexIds = new ArrayList<>(oldGraph.getVertexIds());
            final List<String> newVertexIds = new ArrayList<>(newGraph.getVertexIds());

            // Detect removed vertices
            // A vertex from the old graph is removed if it is no longer part of the new graphs vertex list
            final List<String> removedVertices = new ArrayList<>(oldVertexIds);
            removedVertices.removeAll(newVertexIds);
            removedVertices.forEach(id -> vertexRemoved(oldGraph.getVertex(id)));

            // Detect added vertices
            // A vertex from the new graph is added if it is not part of the old graphs vertex list
            final List<String> addedVertices = new ArrayList<>(newVertexIds);
            addedVertices.removeAll(oldVertexIds);
            addedVertices.forEach(id -> vertexAdded(newGraph.getVertex(id)));

            // Detect updated vertices
            // A vertex is updated if it part of the new and old graph's vertex list
            // and they are not equal (probably due to properties change)
            final List<String> sharedVertcies = new ArrayList<>(newVertexIds);
            sharedVertcies.removeAll(removedVertices);
            sharedVertcies.removeAll(addedVertices);
            sharedVertcies.stream().forEach(id -> {
                V oldVertex = oldGraph.getVertex(id);
                V newVertex = newGraph.getVertex(id);
                if (!oldVertex.equals(newVertex)) {
                    vertexUpdated(newVertex);
                }
            });
        }

        private void detectEdgeChanges(G oldGraph, G newGraph) {
            // Find all vertices/edges which are in the old and new graph
            final List<String> oldEdgeIds = new ArrayList<>(oldGraph.getEdgeIds());
            final List<String> newEdgeIds = new ArrayList<>(newGraph.getEdgeIds());

            // Detect removed vertices
            final List<String> removedEdges = new ArrayList<>(oldEdgeIds);
            removedEdges.removeAll(newEdgeIds);
            removedEdges.forEach(id -> edgeRemoved(oldGraph.getEdge(id)));

            // Detect added vertices
            final List<String> addedEdges = new ArrayList<>(newEdgeIds);
            addedEdges.removeAll(oldEdgeIds);
            addedEdges.forEach(id -> edgeAdded(newGraph.getEdge(id)));

            // Detect updated vertices
            final List<String> sharedEdges = new ArrayList<>(newEdgeIds);
            sharedEdges.removeAll(removedEdges);
            sharedEdges.removeAll(addedEdges);
            sharedEdges.stream().forEach(id -> {
                E oldEdge = oldGraph.getEdge(id);
                E newEdge = newGraph.getEdge(id);
                if (!oldEdge.equals(newEdge)) {
                    edgeUpdated(newEdge);
                }
            });
        }

        public ChangeSet<G, V, E> build() {
            detectChanges(oldGraph, newGraph);
            return new ChangeSet<>(this);
        }
    }
}
