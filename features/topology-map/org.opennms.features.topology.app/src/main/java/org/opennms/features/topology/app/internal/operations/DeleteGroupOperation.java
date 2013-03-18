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

package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class DeleteGroupOperation implements Operation {

	@Override
	public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
		if (targets == null || targets.isEmpty() || targets.size() != 1) {
			return null;
		}

		GraphContainer graphContainer = operationContext.getGraphContainer();

		// TODO: Add a confirmation dialog before the group is deleted

		Vertex deleteMe = graphContainer.getBaseTopology().getVertex(targets.get(0));

		if (deleteMe.isGroup()) {
			Vertex parent = graphContainer.getBaseTopology().getParent(deleteMe);

			// Detach all children from the group
			for(VertexRef childRef : graphContainer.getBaseTopology().getChildren(deleteMe)) {
				graphContainer.getBaseTopology().setParent(childRef, parent);
			}

			// Remove the group from the topology
			graphContainer.getBaseTopology().removeVertex(deleteMe);

			// Save the topology
			graphContainer.getBaseTopology().save();

			graphContainer.redoLayout();
		} else {
			// Display a warning that the vertex cannot be deleted
		}

		return null;
	}

	@Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
		return targets != null && 
		targets.size() == 1 && 
		targets.get(0) != null 
		;
	}

	@Override
	public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
		// Only allow the operation on single non-leaf vertices (groups)
		return targets != null && 
		targets.size() == 1 && 
		targets.get(0) != null && 
		operationContext.getGraphContainer().getBaseTopology().getVertex(targets.get(0)).isGroup()
		;
	}

	@Override
	public String getId() {
		return "DeleteGroup";
	}
}
