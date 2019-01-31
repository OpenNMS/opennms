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

package org.opennms.features.graph.updates.change;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.opennms.features.graph.api.Graph;
import org.opennms.features.graph.api.GraphContainer;
import org.opennms.features.graph.updates.listener.GraphContainerChangeListener;
import org.opennms.features.graph.updates.listener.GraphContainerChangeSetListener;

// TODO MVR detect info changes
// TODO MVR enforce graph type
public class ContainerChangeSet {

    private final Date changeSetDate;
    // TODO MVR adding and removing may be sufficient to just persist the namespace
    private List<Graph<?, ?>> addedGraphs = new ArrayList<>();
    private List<Graph<?, ?>> removedGraphs = new ArrayList<>();
    private List<ChangeSet<?, ?, ?>> graphChanges = new ArrayList<>();

    public ContainerChangeSet(GraphContainer oldGraphContainer, GraphContainer newGraphContainer) {
        this(oldGraphContainer, newGraphContainer, new Date());
    }
    public ContainerChangeSet(GraphContainer oldGraphContainer, GraphContainer newGraphContainer, Date changeSetDate) {
        this.changeSetDate = changeSetDate;
        detectChanges(oldGraphContainer, newGraphContainer);
    }

    public Date getChangeSetDate() {
        return changeSetDate;
    }

    public List<Graph<?, ?>> getGraphsAdded() {
        return new ArrayList<>(addedGraphs);
    }

    public List<Graph<?, ?>> getGraphsRemoved() {
        return new ArrayList<>(removedGraphs);
    }

    public List<ChangeSet<?, ?, ?>> getGraphsUpdated() {
        return new ArrayList<>(graphChanges);
    }

    public void accept(GraphContainerChangeSetListener listener) {
        Objects.requireNonNull(listener);
        listener.graphContainerChanged(this);
    }

    public void accept(GraphContainerChangeListener listener) {
        if (hasChanges()) {
            addedGraphs.forEach(graph -> listener.handleGraphAdded(graph));
            removedGraphs.forEach(graph -> listener.handleGraphRemoved(graph));
            graphChanges.forEach(changeSet -> listener.handleGraphUpdated(changeSet));
        }
    }

    public boolean hasChanges() {
        return !addedGraphs.isEmpty() || !removedGraphs.isEmpty() || !graphChanges.isEmpty();
    }

    protected void detectChanges(GraphContainer<?, ?, ?> oldGraphContainer, GraphContainer<?, ?, ?> newGraphContainer) {
        // no old container exists, add all graphs
        if (oldGraphContainer == null && newGraphContainer != null) {
            newGraphContainer.getGraphs().forEach(g -> graphAdded(g));
        }

        // no new graph container exists, remove all
        if (oldGraphContainer != null && newGraphContainer == null) {
            oldGraphContainer.getGraphs().forEach(g -> graphRemoved(g));
        }

        // both containers exists, so calculate changes
        if (oldGraphContainer != null && newGraphContainer != null) {
            // TODO MVR detect info changes
            final List<String> oldNamespaces = oldGraphContainer.getNamespaces();
            final List<String> newNamespaces = newGraphContainer.getNamespaces();

            // Detect removed graphs
            final List<String> removedNamespaces = new ArrayList<>(oldNamespaces);
            removedNamespaces.removeAll(newNamespaces);
            removedNamespaces.forEach(ns -> {
                final Graph removedGraph = oldGraphContainer.getGraph(ns);
                graphRemoved(removedGraph);
            });

            // Detect added graphs
            final List<String> addedNamespaces = new ArrayList<>(newNamespaces);
            addedNamespaces.removeAll(oldNamespaces);
            addedNamespaces.forEach(ns -> {
                final Graph addedGraph = newGraphContainer.getGraph(ns);
                graphAdded(addedGraph);
            });

            // Detect changes
            final List<String> sharedNamespaces = new ArrayList<>(newNamespaces);
            sharedNamespaces.removeAll(addedNamespaces);
            sharedNamespaces.removeAll(removedNamespaces);
            sharedNamespaces.forEach(ns -> {
                final Graph oldGraph = oldGraphContainer.getGraph(ns);
                final Graph newGraph = newGraphContainer.getGraph(ns);
                final ChangeSet changeSet = new ChangeSet(oldGraph, newGraph, changeSetDate);
                if (changeSet.hasChanges()) {
                    graphChanged(changeSet);
                }
            });
        }
    }

    private void graphAdded(Graph<?, ?> newGraph) {
        addedGraphs.add(newGraph);
    }

    private void graphRemoved(Graph<?, ?> removedGraph) {
        removedGraphs.add(removedGraph);
    }

    private void graphChanged(ChangeSet<?, ?, ?> changeSet) {
        graphChanges.add(changeSet);
    }
}
