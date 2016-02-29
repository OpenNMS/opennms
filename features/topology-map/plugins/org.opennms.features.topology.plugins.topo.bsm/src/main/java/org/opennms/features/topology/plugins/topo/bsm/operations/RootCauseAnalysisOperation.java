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

import static org.opennms.features.topology.plugins.topo.bsm.GraphVertexToTopologyVertexConverter.createTopologyVertex;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.vaadin.core.InfoDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootCauseAnalysisOperation implements Operation {
    private static final Logger LOG = LoggerFactory.getLogger(RootCauseAnalysisOperation.class);
    private static final String OPERATION_ID = "contextRootCauseAnalysis";

    private BusinessServiceManager businessServiceManager;
    private BusinessServiceStateMachine businessServiceStateMachine;

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        List<BusinessServiceVertex> vertices = getVertices(targets);
        List<GraphVertex> verticesToFocus = vertices.stream()
            .map(v -> businessServiceManager.getBusinessServiceById(v.getServiceId()))
            .map(b -> businessServiceStateMachine.calculateRootCause(b))
            .flatMap(l -> l.stream())
            .collect(Collectors.toList());
        LOG.info("Found {} edges for root cause.", verticesToFocus.size());

        if (verticesToFocus.isEmpty()) {
            new InfoDialog("No result", "No root cause was found for the selected vertices.").open();
        } else {
            // add to focus
            GraphContainer container = operationContext.getGraphContainer();
            container.clearCriteria();
            verticesToFocus.forEach(graphVertex -> container.addCriteria(
                    new DefaultVertexHopCriteria(createTopologyVertex(graphVertex))));
            // add the context vertex because it is missing in the root cause result
            container.addCriteria(new DefaultVertexHopCriteria(targets.get(0)));
            container.setSemanticZoomLevel(0);
            container.redoLayout();
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

    public List<BusinessServiceVertex> getVertices(List<VertexRef> targets) {
        if (targets == null) {
            return Collections.emptyList();
        }
        return targets.stream()
                    .filter(v -> v instanceof BusinessServiceVertex)
                    .map(v -> (BusinessServiceVertex) v)
                    .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return OPERATION_ID;
    }

    public void setBusinessServiceStateMachine(BusinessServiceStateMachine businessServiceStateMachine) {
        this.businessServiceStateMachine = Objects.requireNonNull(businessServiceStateMachine);
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
    }
}
