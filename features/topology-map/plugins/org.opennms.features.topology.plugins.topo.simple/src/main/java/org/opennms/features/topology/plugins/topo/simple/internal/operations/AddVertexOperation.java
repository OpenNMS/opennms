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

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

public class AddVertexOperation implements Operation{
    
    private String m_iconKey;
    public AddVertexOperation(String iconKey) {
        m_iconKey = iconKey;
    }
    
    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<VertexRef> targets,OperationContext operationContext) {
    	if(targets.size() > 1) return false;
        return true;
    }

    @Override
    public String getId() {
        return "AddVertex";
    }

    void connectNewVertex(String vertexId, String iconKey, GraphContainer graphContainer) {
        Vertex vertId1 = graphContainer.getBaseTopology().addVertex(0, 0);
        // Make the new vertex a root node
        vertId1.setParent(null);
        graphContainer.getBaseTopology().connectVertices(graphContainer.getBaseTopology().getVertex(graphContainer.getBaseTopology().getVertexNamespace(), vertexId), vertId1);
    }

    public String getIconKey() {
        return m_iconKey;
    }

    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        LoggerFactory.getLogger(getClass()).debug("execute()");
        Object vertexId = targets.isEmpty() ? null : targets.get(0).getId();
        String icon = getIconKey();
        if (vertexId == null) {
            if (operationContext.getGraphContainer().getBaseTopology().containsVertexId(Constants.CENTER_VERTEX_ID)) {
            	connectNewVertex(Constants.CENTER_VERTEX_ID, Constants.SERVER_ICON_KEY, operationContext.getGraphContainer());
            }
            else {
                Vertex vertId = operationContext.getGraphContainer().getBaseTopology().addVertex(250, 250);
                vertId.setParent(null);
                
            }
        } else {
            
            connectNewVertex(vertexId.toString(), icon, operationContext.getGraphContainer());
        }
        operationContext.getGraphContainer().redoLayout();
        
        return null;
    }
    
}
