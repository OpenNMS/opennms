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

package org.opennms.features.topology.api;

import java.util.Collection;
import java.util.Set;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Property;

public interface GraphContainer extends DisplayState {

    public interface ChangeListener {
        public void graphChanged(GraphContainer graphContainer);
    }

    GraphProvider getBaseTopology();

    void setBaseTopology(GraphProvider graphProvider);

    Criteria[] getCriteria();

    void addCriteria(Criteria criteria);

    void removeCriteria(Criteria criteria);

    // clears all criteria which are currently sets
    void clearCriteria();

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);

    SelectionManager getSelectionManager();

    void setSelectionManager(SelectionManager selectionManager);

    Graph getGraph();

    AutoRefreshSupport getAutoRefreshSupport();

    boolean hasAutoRefreshSupport();

    Collection<VertexRef> getVertexRefForest(Collection<VertexRef> vertexRefs);
    
    MapViewManager getMapViewManager();

    Property<Double> getScaleProperty();

    StatusProvider getVertexStatusProvider();

    void setVertexStatusProvider(StatusProvider statusProvider);

    Set<EdgeStatusProvider> getEdgeStatusProviders();

    // TODO move to another location. This should not be stored here! (maybe VaadinApplicationContext is the right place)
    String getSessionId();

    // TODO move to another location. This should not be stored here! (maybe VaadinApplicationContext is the right place)
    void setSessionId(String sessionId);

    void setDirty(boolean dirty);
    
    void fireGraphChanged();
}
