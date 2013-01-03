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
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.onmsdao.internal.OnmsTopologyProvider;
import org.slf4j.LoggerFactory;


public class RemoveVertexOperation implements Operation {

    OnmsTopologyProvider m_topologyProvider;
    
    public RemoveVertexOperation(OnmsTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        DisplayState graphContainer = operationContext.getGraphContainer();
        
        if (targets == null) {
            LoggerFactory.getLogger(getClass()).debug("need to handle selection!!!");
        } else {
            for(VertexRef target : targets) {
                m_topologyProvider.removeVertex(target);
            }
            
            
        	graphContainer.redoLayout();
        }
        return null;
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        if(targets != null) {
            for(VertexRef target : targets) {
                if(!m_topologyProvider.containsVertexId(target)) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }
}