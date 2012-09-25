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

package org.opennms.features.topology.plugins.topo.simple.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.EditableTopologyProvider;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

public class ConnectOperation implements Operation {

    EditableTopologyProvider m_topologyProvider;
    
    public ConnectOperation(EditableTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        if(targets != null && targets.size() > 1) {
            Object sourceVertexId = operationContext.getGraphContainer().getVertexItemIdForVertexKey(targets.get(0));//(String)endPoints.get(0);
            Object targetVertextId = operationContext.getGraphContainer().getVertexItemIdForVertexKey(targets.get(1)); //(String)endPoints.get(1);
            m_topologyProvider.connectVertices(sourceVertexId, targetVertextId);
        }
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return targets.size() == 2;
    }

    @Override
    public String getId() {
        return null;
    }
}