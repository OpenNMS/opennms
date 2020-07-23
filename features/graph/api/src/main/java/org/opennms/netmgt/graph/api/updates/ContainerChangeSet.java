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

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;

public class ContainerChangeSet {

    private final Date changeSetDate;
    private final String containerId;
    private final List<ImmutableGraph<?, ?>> addedGraphs;
    private final List<ImmutableGraph<?, ?>> removedGraphs;
    private final List<ChangeSet<?, ?, ?>> graphChanges;

    public ContainerChangeSet(ContainerChangeSetBuilder builder) {
        Objects.requireNonNull(builder);
        this.changeSetDate = builder.changeSetDate;
        this.containerId = builder.getContainerId();
        this.addedGraphs = Collections.unmodifiableList(builder.addedGraphs);
        this.removedGraphs = Collections.unmodifiableList(builder.removedGraphs);
        this.graphChanges = Collections.unmodifiableList(builder.graphChanges);
    }

    public Date getChangeSetDate() {
        return changeSetDate;
    }

    public List<ImmutableGraph<?, ?>> getGraphsAdded() {
        return addedGraphs;
    }

    public List<ImmutableGraph<?, ?>> getGraphsRemoved() {
        return removedGraphs;
    }

    public List<ChangeSet<?, ?, ?>> getGraphsUpdated() {
        return graphChanges;
    }

    public boolean hasChanges() {
        return !addedGraphs.isEmpty() || !removedGraphs.isEmpty() || !graphChanges.isEmpty();
    }

    public String getContainerId() {
        return containerId;
    }

    public static ContainerChangeSetBuilder builder(final ImmutableGraphContainer oldGraphContainer, final ImmutableGraphContainer newGraphContainer) {
        return builder(oldGraphContainer, newGraphContainer, new Date());
    }

    public static ContainerChangeSetBuilder builder(final ImmutableGraphContainer oldGraphContainer, ImmutableGraphContainer newGraphContainer, final Date changeSetDate) {
        return new ContainerChangeSetBuilder(oldGraphContainer, newGraphContainer, changeSetDate);
    }

    public static final class ContainerChangeSetBuilder {
        private final ImmutableGraphContainer oldGraphContainer;
        private final ImmutableGraphContainer newGraphContainer;
        private final List<ImmutableGraph<?, ?>> addedGraphs = new ArrayList<>();
        private final List<ImmutableGraph<?, ?>> removedGraphs = new ArrayList<>();
        private final List<ChangeSet<?, ?, ?>> graphChanges = new ArrayList<>();
        private final Date changeSetDate;

        private ContainerChangeSetBuilder(final ImmutableGraphContainer oldGraphContainer, final ImmutableGraphContainer newGraphContainer, final Date changeSetDate) {
            this.changeSetDate = Objects.requireNonNull(changeSetDate);
            if (oldGraphContainer == null && newGraphContainer == null) {
                throw new IllegalArgumentException("Cannot create change set if both containers are null.");
            }
            this.oldGraphContainer = oldGraphContainer;
            this.newGraphContainer = newGraphContainer;
        }

        protected String getContainerId() {
            return oldGraphContainer == null ? newGraphContainer.getId() : oldGraphContainer.getId();
        }

        private void detectChanges(final ImmutableGraphContainer<?> oldGraphContainer, final ImmutableGraphContainer<?> newGraphContainer) {
            // no old container exists, add all graphs
            if (oldGraphContainer == null && newGraphContainer != null) {
                newGraphContainer.getGraphs().forEach(g -> graphAdded(g));
            }

            // no new graph container exists, remove all
            if (oldGraphContainer != null && newGraphContainer == null) {
                oldGraphContainer.getGraphs().forEach(g -> graphRemoved(g));
            }

            // nothing to do if they are the same :)
            if (oldGraphContainer == newGraphContainer) {
                return;
            }

            // both containers exists, so calculate changes
            if (oldGraphContainer != null && newGraphContainer != null) {
                // Before changes can be calculated, ensure the containers share the same id, otherwise
                // we should bail, as this is theoretical/technical possible, but does not make sense from the
                // domain view the container reflects.
                if (!oldGraphContainer.getId().equalsIgnoreCase(newGraphContainer.getId())) {
                    throw new IllegalStateException("Cannot detect changes between different containers");
                }

                // Detect changes
                final List<String> oldNamespaces = oldGraphContainer.getNamespaces();
                final List<String> newNamespaces = newGraphContainer.getNamespaces();

                // Detect removed graphs
                final List<String> removedNamespaces = new ArrayList<>(oldNamespaces);
                removedNamespaces.removeAll(newNamespaces);
                removedNamespaces.forEach(ns -> {
                    final ImmutableGraph removedGraph = oldGraphContainer.getGraph(ns);
                    graphRemoved(removedGraph);
                });

                // Detect added graphs
                final List<String> addedNamespaces = new ArrayList<>(newNamespaces);
                addedNamespaces.removeAll(oldNamespaces);
                addedNamespaces.forEach(ns -> {
                    final ImmutableGraph addedGraph = newGraphContainer.getGraph(ns);
                    graphAdded(addedGraph);
                });

                // Detect changes
                final List<String> sharedNamespaces = new ArrayList<>(newNamespaces);
                sharedNamespaces.removeAll(addedNamespaces);
                sharedNamespaces.removeAll(removedNamespaces);
                sharedNamespaces.forEach(ns -> {
                    final ImmutableGraph oldGraph = oldGraphContainer.getGraph(ns);
                    final ImmutableGraph newGraph = newGraphContainer.getGraph(ns);
                    final ChangeSet changeSet = ChangeSet.builder(oldGraph, newGraph).withDate(changeSetDate).build();
                    if (changeSet.hasChanges()) {
                        graphChanged(changeSet);
                    }
                });
            }
        }

        private void graphAdded(ImmutableGraph<?, ?> newGraph) {
            addedGraphs.add(newGraph);
        }

        private void graphRemoved(ImmutableGraph<?, ?> removedGraph) {
            removedGraphs.add(removedGraph);
        }

        private void graphChanged(ChangeSet<?, ?, ?> changeSet) {
            graphChanges.add(changeSet);
        }

        public ContainerChangeSet build() {
            detectChanges(oldGraphContainer, newGraphContainer);
            return new ContainerChangeSet(this);
        }
    }
}
