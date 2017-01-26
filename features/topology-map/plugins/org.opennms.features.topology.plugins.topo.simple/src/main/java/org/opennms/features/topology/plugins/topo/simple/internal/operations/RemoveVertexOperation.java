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

package org.opennms.features.topology.plugins.topo.simple.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;


public class RemoveVertexOperation implements Operation {

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        GraphContainer graphContainer = operationContext.getGraphContainer();
        
        if (targets == null) {
            LoggerFactory.getLogger(getClass()).debug("need to handle selection!!!");
        } else {
            for(VertexRef target : targets) {
            	if (operationContext.getGraphContainer().getBaseTopology().getVertexNamespace().equals(target.getNamespace())) {
            		operationContext.getGraphContainer().getBaseTopology().removeVertex(target);
            	}
            }
            
            
        	graphContainer.redoLayout();
        }
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        if(targets != null) {
            for(VertexRef target : targets) {
                if(operationContext.getGraphContainer().getBaseTopology().getVertex(target, operationContext.getGraphContainer().getCriteria()) == null) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getId() {
        return "RemoveVertex";
    }
}
