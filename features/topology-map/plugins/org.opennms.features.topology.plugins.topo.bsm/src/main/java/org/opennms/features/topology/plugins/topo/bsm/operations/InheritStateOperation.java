/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
