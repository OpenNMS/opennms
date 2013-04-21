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

package org.opennms.features.topology.api;

import java.util.Collection;

import org.opennms.features.topology.api.topo.Criteria;
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

	Criteria getCriteria(String namespace);

	void setCriteria(Criteria critiera);

	void addChangeListener(ChangeListener listener);

	void removeChangeListener(ChangeListener listener);

	SelectionManager getSelectionManager();

	void setSelectionManager(SelectionManager selectionManager);

	Graph getGraph();

	Collection<VertexRef> getVertexRefForest(Collection<VertexRef> vertexRefs);
    
	MapViewManager getMapViewManager();

	Property getScaleProperty();

    StatusProvider getStatusProvider();

    void setStatusProvider(StatusProvider statusProvider);
}
