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

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.EditableTopologyProvider;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

public class AddVertexOperation implements Operation{
    
    private EditableTopologyProvider m_topologyProvider;
    
    private String m_iconKey;
    public AddVertexOperation(String iconKey, EditableTopologyProvider topologyProvider) {
        m_iconKey = iconKey;
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets,OperationContext operationContext) {
    	if(targets.size() > 1) return false;
        
        Object itemId = targets.size() == 1 ? targets.get(0) : null;
        return itemId == null || operationContext.getGraphContainer().getVertexContainer().containsId(itemId);
    }

    @Override
    public String getId() {
        return null;
    }

    void connectNewVertex(String vertexId, String iconKey, DisplayState graphContainer) {
        Object vertId1 = m_topologyProvider.addVertex(0, 0);
        m_topologyProvider.setParent(vertId1, Constants.ROOT_GROUP_ID);
        m_topologyProvider.connectVertices(vertexId, vertId1);
        
    }

    public String getIconKey() {
        return m_iconKey;
    }

    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        System.err.println("/*** Executing add Vertex in AddVertexOperation ***/");
        Object vertexKey = targets.isEmpty() ? null : targets.get(0);
        Object vertexId = operationContext.getGraphContainer().getVertexItemIdForVertexKey(vertexKey);
        String icon = getIconKey();
        if (vertexId == null) {
            if (operationContext.getGraphContainer().getVertexContainer().containsId(Constants.CENTER_VERTEX_ID)) {
            	connectNewVertex(Constants.CENTER_VERTEX_ID, Constants.SERVER_ICON_KEY, operationContext.getGraphContainer());
            }
            else {
                Object vertId = m_topologyProvider.addVertex(250, 250);
                m_topologyProvider.setParent(vertId, Constants.ROOT_GROUP_ID);
                
            }
        } else {
            
            connectNewVertex(vertexId.toString(), icon, operationContext.getGraphContainer());
        }
        operationContext.getGraphContainer().redoLayout();
        
        return null;
    }
    
}
