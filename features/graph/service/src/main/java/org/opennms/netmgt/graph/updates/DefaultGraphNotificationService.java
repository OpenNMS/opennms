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
package org.opennms.netmgt.graph.updates;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.updates.ChangeSet;
import org.opennms.netmgt.graph.api.updates.ContainerChangeSet;
import org.opennms.netmgt.graph.api.updates.GraphNotificationService;
import org.opennms.netmgt.graph.api.updates.listener.GraphChangeListener;
import org.opennms.netmgt.graph.api.updates.listener.GraphChangeSetListener;
import org.opennms.netmgt.graph.api.updates.listener.GraphContainerChangeListener;
import org.opennms.netmgt.graph.api.updates.listener.GraphContainerChangeSetListener;

public class DefaultGraphNotificationService implements GraphNotificationService {

    private List<GraphChangeSetListener> graphChangeSetListeners = new CopyOnWriteArrayList<>();
    private List<GraphContainerChangeSetListener> graphContainerChangeSetListeners = new CopyOnWriteArrayList<>();

    private Map<GraphContainerChangeListener, GraphContainerChangeSetListener> graphContainerChangeListenerToChangeSetListener = new ConcurrentHashMap<>();
    private Map<GraphChangeListener, GraphChangeSetListener> graphChangeListenerToChangeSetListener = new ConcurrentHashMap<>();

    @Override
    public void containerChanged(ImmutableGraphContainer oldContainer, ImmutableGraphContainer newContainer) {
        final ContainerChangeSet containerChangeSet = ContainerChangeSet.builder(oldContainer, newContainer).build();
        containerChanged(containerChangeSet);
    }

    @Override
    public void containerChanged(ContainerChangeSet containerChangeSet) {
        Objects.requireNonNull(containerChangeSet);
        if (containerChangeSet.hasChanges()) {
            for (GraphContainerChangeSetListener listener : graphContainerChangeSetListeners) {
                listener.graphContainerChanged(containerChangeSet);

                // If a graph was updated, inform all GraphChange(Set)Listener as well
                if (!containerChangeSet.getGraphsUpdated().isEmpty()) {
                    containerChangeSet.getGraphsUpdated().forEach(this::graphChanged);
                }
            }
        }
    }

    @Override
    public void graphChanged(ImmutableGraph oldGraph, ImmutableGraph newGraph) {
        final ChangeSet changeSet = ChangeSet.builder(oldGraph, newGraph).build();
        graphChanged(changeSet);
    }

    @Override
    public void graphChanged(ChangeSet graphChangeSet) {
        Objects.requireNonNull(graphChangeSet);
        if (graphChangeSet.hasChanges()) {
            for (GraphChangeSetListener listener : graphChangeSetListeners) {
                listener.graphChanged(graphChangeSet);
            }
        }
    }

    public synchronized void onBind(GraphChangeListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        final GraphChangeSetListener wrappedListener = changeSet -> {
            if (!changeSet.getVerticesAdded().isEmpty()) {
                listener.handleVerticesAdded(changeSet.getVerticesAdded());
            }
            if (!changeSet.getVerticesRemoved().isEmpty()) {
                listener.handleVerticesRemoved(changeSet.getVerticesRemoved());
            }
            if (!changeSet.getVerticesUpdated().isEmpty()) {
                listener.handleVerticesUpdated(changeSet.getVerticesUpdated());
            }
            if (!changeSet.getEdgesAdded().isEmpty()) {
                listener.handleEdgesAdded(changeSet.getEdgesAdded());
            }
            if (!changeSet.getEdgesRemoved().isEmpty()) {
                listener.handleEdgesRemoved(changeSet.getEdgesRemoved());
            }
            if (!changeSet.getEdgesUpdated().isEmpty()) {
                listener.handleEdgesUpdated(changeSet.getEdgesUpdated());
            }
            if (changeSet.getFocus() != null) {
                listener.handleFocusChanged(changeSet.getFocus());
            }
            if (changeSet.getGraphInfo() != null) {
                listener.handleGraphInfoChanged(changeSet.getGraphInfo());
            }
        };
        // Remember which one was actually wrapped, to help removing it later
        graphChangeListenerToChangeSetListener.put(listener, wrappedListener);
        graphChangeSetListeners.add(wrappedListener);
    }

    public synchronized void onBind(GraphChangeSetListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        graphChangeSetListeners.add(listener);
    }

    public synchronized void onBind(GraphContainerChangeListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        // The call is wrapped
        final GraphContainerChangeSetListener wrappedListener = changeSet -> {
            if (!changeSet.getGraphsAdded().isEmpty()) {
                changeSet.getGraphsAdded().forEach(listener::handleGraphAdded);
            }
            if (!changeSet.getGraphsRemoved().isEmpty()) {
                changeSet.getGraphsRemoved().forEach(listener::handleGraphRemoved);
            }
            if (!changeSet.getGraphsUpdated().isEmpty()) {
                changeSet.getGraphsUpdated().forEach(listener::handleGraphUpdated);
            }
        };
        // Remember which one was actually wrapped, to help removing it later
        graphContainerChangeListenerToChangeSetListener.put(listener, wrappedListener);
        graphContainerChangeSetListeners.add(wrappedListener);
    }

    public synchronized void onBind(GraphContainerChangeSetListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        graphContainerChangeSetListeners.add(listener);
    }

    public synchronized void onUnbind(GraphChangeListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        final GraphChangeSetListener wrappedListener = graphChangeListenerToChangeSetListener.remove(listener);
        if (wrappedListener != null) {
            graphChangeSetListeners.remove(wrappedListener);
        }
    }

    public synchronized void onUnbind(GraphChangeSetListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        graphChangeSetListeners.remove(listener);
    }

    public synchronized void onUnbind(GraphContainerChangeListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        final GraphContainerChangeSetListener wrappedListener = graphContainerChangeListenerToChangeSetListener.remove(listener);
        if (wrappedListener != null) {
            graphContainerChangeSetListeners.remove(wrappedListener);
        }
    }

    public synchronized void onUnbind(GraphContainerChangeSetListener listener, Map<String, String> props) {
        if (listener == null) {
            return;
        }
        graphContainerChangeSetListeners.remove(listener);
    }

}
