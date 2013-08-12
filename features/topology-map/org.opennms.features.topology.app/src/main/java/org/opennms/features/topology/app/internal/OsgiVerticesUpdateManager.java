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

package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.osgi.OnmsServiceManager;
import org.opennms.features.topology.api.osgi.VaadinApplicationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A OSGI-variant of the {@link VerticesUpdateManager}
 */
public class OsgiVerticesUpdateManager implements VerticesUpdateManager {

    // the session scope.
    private final VaadinApplicationContext applicationContext;
    private final OnmsServiceManager serviceManager;

    public OsgiVerticesUpdateManager(OnmsServiceManager serviceManager, VaadinApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.serviceManager = serviceManager;
    }

    /**
     * The focus of node ids. If a selection is made only the selected ids are in focus.
     * If no selection is made all visible (or displayable) node ids are in focus.
     */
    private final List<Integer> nodeIdFocus = new ArrayList<Integer>();

    /**
     * The currently displayable node ids.
     */
    private final List<Integer> displaybleNodeIds = new ArrayList<Integer>();

    /**
     * The currently selected node ids.
     */
    private final List<Integer> selectedNodeIds = new ArrayList<Integer>();

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        if (graphContainer == null) return;

        // set displayable Node Ids
        List<Integer> newDisplaybleNodeIds = extractNodeIds(new ArrayList<VertexRef>(graphContainer.getGraph().getDisplayVertices()));
        if (!displaybleNodeIds.equals(newDisplaybleNodeIds)) {
            synchronized (displaybleNodeIds) {
                displaybleNodeIds.clear();
                displaybleNodeIds.addAll(newDisplaybleNodeIds);
            }
        }

        // set node ids in focus
        List<Integer> nodeIdsInFocus = getNodeIdsInFocus();
        fireFocusChanged(nodeIdsInFocus);
    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        if (selectionContext == null) return;
        List<Integer> newSelectedNodeIds =  extractNodeIds(selectionContext.getSelectedVertexRefs());
        if (!newSelectedNodeIds.equals(selectedNodeIds)) {
            synchronized (selectedNodeIds) {
                selectedNodeIds.clear();
                selectedNodeIds.addAll(newSelectedNodeIds);
            }
        }
        fireFocusChanged(getNodeIdsInFocus());
    }

    private List<Integer> getNodeIdsInFocus() {
        if (selectedNodeIds.isEmpty()) return displaybleNodeIds;
        return selectedNodeIds;
    }

    /**
     * Gets the node ids from the given vertices. A node id can only be extracted from a vertex with a "nodes"' namespace.
     * For a vertex with namespace "node" the "getId()" method always returns the node id.
     *
     * @param vertices
     * @return
     */
    private List<Integer> extractNodeIds(Collection<VertexRef> vertices) {
        List<Integer> nodeIdList = new ArrayList<Integer>();
        for (VertexRef eachRef : vertices) {
            if ("nodes".equals(eachRef.getNamespace())) {
                try {
                    nodeIdList.add(Integer.valueOf(eachRef.getId()));
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(this.getClass()).warn("Cannot filter nodes with ID: {}", eachRef.getId());
                }
            }
        }
        return nodeIdList;
    }

    /**
     * Notifies all listeners that the focus of the vertices has changed.
     * @param newNodeIdFocus
     */
    synchronized private void fireFocusChanged(List<Integer> newNodeIdFocus) {
        if (newNodeIdFocus.equals(nodeIdFocus)) return;
        synchronized(nodeIdFocus) {
            nodeIdFocus.clear();
            nodeIdFocus.addAll(newNodeIdFocus);
        }
        final VerticesUpdateEvent updateEvent = new VerticesUpdateEvent(Collections.unmodifiableList(nodeIdFocus));
        applicationContext.getEventProxy(serviceManager).fireEvent(updateEvent);
    }
}
