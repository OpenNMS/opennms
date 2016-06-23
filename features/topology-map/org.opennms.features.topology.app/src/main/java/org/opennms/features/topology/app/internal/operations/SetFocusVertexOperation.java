/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

import java.util.List;

public class SetFocusVertexOperation implements Operation {

    @Override
    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        if(targets == null || targets.isEmpty()) return null;

        final GraphContainer graphContainer = operationContext.getGraphContainer();
        graphContainer.clearCriteria();

        VertexHopGraphProvider.FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(graphContainer);
        for(VertexRef target : targets){
            criteria.add(target);
        }
        graphContainer.redoLayout();

        return null;
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return enabled(targets, operationContext);
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        if(targets == null || targets.isEmpty()) return false;

        final GraphContainer graphContainer = operationContext.getGraphContainer();

        for(Criteria crit : graphContainer.getCriteria()){
            if(crit instanceof VertexHopGraphProvider.VertexHopCriteria) {
                if(((VertexHopGraphProvider.VertexHopCriteria) crit).getVertices().containsAll(targets)){
                    return false;
                }
            }
        }

        VertexHopGraphProvider.FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(graphContainer, false);
        if (criteria != null) {
            for (VertexRef target : targets) {
                if(criteria.contains(target)) return false;
            }
        }

        return true;
    }

    @Override
    public String getId() {
        return "SetFocusVertex";
    }
}
