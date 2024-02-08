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
