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
import org.opennms.features.topology.api.support.hops.CriteriaUtils;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.support.hops.WrappedVertexHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

public class RemoveFocusOtherVerticesOperation implements Operation {

    @Override
    public void execute(List<VertexRef> targets, final OperationContext operationContext) {
        final GraphContainer graphContainer = operationContext.getGraphContainer();
        final Set<VertexHopCriteria> criteriaForGraphContainer = Criteria.getCriteriaForGraphContainer(graphContainer, VertexHopCriteria.class);
        boolean didRemoveCriteria = false;
        for (VertexHopCriteria eachCriteria : criteriaForGraphContainer) {
            boolean shouldRemove = true;
            for (VertexRef vertex : eachCriteria.getVertices()) {
                if (targets.contains(vertex)) {
                    // The criteria references at least one of our targets, so
                    // we shouldn't remove it
                    shouldRemove = false;
                    break;
                }
            }

            if (shouldRemove) {
                graphContainer.removeCriteria(eachCriteria);
                didRemoveCriteria = true;
            }
        }

        if (didRemoveCriteria) {
            // Only update the layout if any changes were made
            graphContainer.redoLayout();
        }
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return enabled(targets, operationContext);
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        if (targets == null || targets.isEmpty()) {
            return false;
        }
        final GraphContainer graphContainer = operationContext.getGraphContainer();
        final WrappedVertexHopCriteria wrappedVertexHopCriteria = CriteriaUtils.getWrappedVertexHopCriteria(graphContainer);
        if (wrappedVertexHopCriteria.isEmpty()) {
            return false;
        }

        // Are the selected vertices in focus?
        for (VertexRef target : targets) {
            if (!wrappedVertexHopCriteria.contains(target)) {
                return false;
            }
        }

        // Are there any other vertices in focus?
        for (VertexRef vertex : wrappedVertexHopCriteria.getVertices()) {
            if (!targets.contains(vertex)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }
}
