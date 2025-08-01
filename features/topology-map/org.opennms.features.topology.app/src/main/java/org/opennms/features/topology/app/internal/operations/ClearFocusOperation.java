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
package org.opennms.features.topology.app.internal.operations;

import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.TopologyUI;

/**
 * Removes all elements from focus.
 *
 * @author mvrueden
 */
public class ClearFocusOperation implements Operation {

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        final GraphContainer graphContainer = operationContext.getGraphContainer();
        final Set<VertexHopCriteria> focusCriteria = graphContainer.findCriteria(VertexHopCriteria.class);
        for (VertexHopCriteria eachFocusCriteria : focusCriteria) {
            graphContainer.removeCriteria(eachFocusCriteria);
        }
        graphContainer.redoLayout();
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return (operationContext.getDisplayLocation() == OperationContext.DisplayLocation.MENUBAR)
                || (targets != null && targets.size() == 0);
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        return true; // always enabled
    }

    @Override
    public String getId() {
        return "ClearFocus";
    }
    public void execute(final GraphContainer graphContainer) {
        graphContainer.setDirty(true);
        graphContainer.redoLayout();
        TopologyUI.getCurrent().markAsDirtyRecursive();
    }
}
