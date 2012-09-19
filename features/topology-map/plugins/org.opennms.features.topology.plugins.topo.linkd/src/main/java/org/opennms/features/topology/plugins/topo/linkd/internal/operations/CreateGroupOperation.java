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

package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import static org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider.GROUP_ICON_KEY;
import static org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider.ROOT_GROUP_ID;
import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;

public class CreateGroupOperation implements Operation {

    TopologyProvider m_topologyProvider;
    public CreateGroupOperation(TopologyProvider topologyProveder) {
        m_topologyProvider=topologyProveder;
    }

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        if (targets != null && !targets.isEmpty() ) {
            
            GraphContainer graphContainer = operationContext.getGraphContainer();
            
            Object groupId = m_topologyProvider.addGroup(GROUP_ICON_KEY);
            Object parentGroup = null;
            for(Object target : targets) {
                Object vertexId = graphContainer.getVertexItemIdForVertexKey(target);
                Object parent = m_topologyProvider.getVertexContainer().getParent(vertexId);
                if (parentGroup == null) {
                    parentGroup = parent;
                } else if (parentGroup != parent) {
                    parentGroup = ROOT_GROUP_ID;
                }
                m_topologyProvider.setParent(vertexId, groupId);
            }
            m_topologyProvider.setParent(groupId, parentGroup == null ? ROOT_GROUP_ID : parentGroup);
            // save the topology for persisting the GROUP
            m_topologyProvider.save(null);
        }
        return null;
    }

    @Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return "LinkdCreateGroupOperation";
    }

}
