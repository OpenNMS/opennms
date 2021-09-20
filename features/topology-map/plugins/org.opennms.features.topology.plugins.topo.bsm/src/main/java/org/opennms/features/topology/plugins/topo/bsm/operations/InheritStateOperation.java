/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm.operations;

import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.simulate.InheritStateCriteria;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;

public class InheritStateOperation extends AbstractCheckedOperation  {

    private static final InheritStateCriteria crit = new InheritStateCriteria();

    @Override
    protected boolean isChecked(GraphContainer container) {
        return container.findSingleCriteria(InheritStateCriteria.class) != null;
    }

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        final GraphContainer container = operationContext.getGraphContainer();
        if (isChecked(operationContext.getGraphContainer())) {
            container.removeCriteria(crit);
        } else {
            container.addCriteria(crit);
        }
        // Force a refresh to update the status
        container.redoLayout();
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    protected boolean enabled(GraphContainer container) {
        return SimulationAwareStateMachineFactory.isInSimulationMode(container.getCriteria());
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        // pass
    }
}
