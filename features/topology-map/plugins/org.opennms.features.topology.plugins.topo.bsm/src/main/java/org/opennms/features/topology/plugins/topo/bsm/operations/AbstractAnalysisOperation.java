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

import static org.opennms.features.topology.plugins.topo.bsm.GraphVertexToTopologyVertexConverter.createTopologyVertex;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertexVisitor;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.vaadin.core.InfoDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public abstract class AbstractAnalysisOperation implements Operation {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAnalysisOperation.class);

    private BusinessServiceManager businessServiceManager;

    public abstract BusinessServiceVertexVisitor<Boolean> getVisitorForSupportedVertices();

    public abstract BusinessServiceVertexVisitor<Collection<GraphVertex>> getVisitorForVerticesToFocus(BusinessServiceStateMachine stateMachine);

    public abstract String getMessageForNoResultDialog();

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        final List<AbstractBusinessServiceVertex> vertices = getVertices(targets);
        final BusinessServiceStateMachine stateMachine = SimulationAwareStateMachineFactory.createStateMachine(businessServiceManager,
                operationContext.getGraphContainer().getCriteria());
        final Set<GraphVertex> graphVerticesToFocus = Sets.newHashSet();
        final BusinessServiceVertexVisitor<Collection<GraphVertex>> visitor = getVisitorForVerticesToFocus(stateMachine);
        for (AbstractBusinessServiceVertex vertex : vertices) {
            graphVerticesToFocus.addAll(vertex.accept(visitor));
        }
        LOG.debug("Found {} business services.", graphVerticesToFocus.size());

        if (graphVerticesToFocus.isEmpty()) {
            new InfoDialog("No result", getMessageForNoResultDialog()).open();
        } else {
            focusOnVertices(targets.get(0), graphVerticesToFocus, operationContext.getGraphContainer());
        }
    }

    public List<AbstractBusinessServiceVertex> getVertices(List<VertexRef> targets) {
        if (targets == null) {
            return Collections.emptyList();
        }
        final BusinessServiceVertexVisitor<Boolean> visitor = getVisitorForSupportedVertices();
        return targets.stream()
                    .filter(v -> v instanceof AbstractBusinessServiceVertex)
                    .map(v -> (AbstractBusinessServiceVertex)v)
                    .filter(v -> v.accept(visitor))
                    .collect(Collectors.toList());
    }

    private void focusOnVertices(VertexRef target, Set<GraphVertex> graphVerticesToFocus, GraphContainer container) {
        // add to focus
        removeHopCriteria(container);
        graphVerticesToFocus.forEach(graphVertex -> container.addCriteria(
                new DefaultVertexHopCriteria(createTopologyVertex(graphVertex))));
        // add the context vertex because it is missing in the root cause result
        container.addCriteria(new DefaultVertexHopCriteria(target));
        container.setSemanticZoomLevel(0);

        // Remove the current selection before redrawing the layout in order
        // to avoid centering on the current vertex
        container.getSelectionManager().setSelectedVertexRefs(Collections.emptyList());
        container.getSelectionManager().setSelectedEdgeRefs(Collections.emptyList());
        container.redoLayout();
    }

    private void removeHopCriteria(GraphContainer container) {
        Criteria[] currentCriteria = container.getCriteria();
        for (Criteria c : Arrays.copyOf(currentCriteria, currentCriteria.length)) {
            if (c instanceof DefaultVertexHopCriteria) {
                container.removeCriteria(c);
            }
        }
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        if (operationContext.getDisplayLocation() == DisplayLocation.MENUBAR) {
            return false;
        }
        return getVertices(targets).size() > 0;
    }

    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        return getVertices(targets).size() > 0;
    }

    public BusinessServiceManager getBusinessServiceManager() {
        return businessServiceManager;
    }
  
    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
    }
}
