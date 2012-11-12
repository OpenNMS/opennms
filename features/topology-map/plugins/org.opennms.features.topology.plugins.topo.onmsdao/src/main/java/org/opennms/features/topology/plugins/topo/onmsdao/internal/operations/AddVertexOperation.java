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

package org.opennms.features.topology.plugins.topo.onmsdao.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.onmsdao.internal.OnmsTopologyProvider;
import org.slf4j.LoggerFactory;

public class AddVertexOperation implements Operation{
    
    private OnmsTopologyProvider m_topologyProvider;
    
    private String m_icon;
    public AddVertexOperation(String icon, OnmsTopologyProvider topologyProvider) {
        m_icon = icon;
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets,OperationContext operationContext) {
        if(targets.size() > 1) return false;
        
        Object itemId = targets.size() == 1 ? targets.get(0) : null;
        
        return itemId == null || operationContext.getGraphContainer().containsVertexId(itemId);
    }

	@Override
    public String getId() {
        return null;
    }

    void connectNewVertex(String vertexId, String icon, DisplayState graphContainer) {
        Object vertId1 = m_topologyProvider.addVertex(-1, 0, 0, icon);
        m_topologyProvider.setParent(vertId1, Constants.ROOT_GROUP_ID);
        m_topologyProvider.connectVertices(vertexId, vertId1);
        
    }

    public String getIcon() {
        return m_icon;
    }

    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        LoggerFactory.getLogger(getClass()).debug("execute()");
        Object vertexId = targets.isEmpty() ? null : targets.get(0);
        String icon = getIcon();
        if (vertexId == null) {
            if (operationContext.getGraphContainer().containsVertexId(Constants.CENTER_VERTEX_ID)) {
            	connectNewVertex(Constants.CENTER_VERTEX_ID, Constants.SERVER_ICON, operationContext.getGraphContainer());
            }
            else {
                Object vertId = m_topologyProvider.addVertex(-1,50, 50, Constants.SERVER_ICON);
                m_topologyProvider.setParent(vertId, Constants.ROOT_GROUP_ID);
                
            }
        } else {
            
            connectNewVertex(vertexId.toString(), icon, operationContext.getGraphContainer());
        }
        operationContext.getGraphContainer().redoLayout();
        
        return null;
    }
    
}