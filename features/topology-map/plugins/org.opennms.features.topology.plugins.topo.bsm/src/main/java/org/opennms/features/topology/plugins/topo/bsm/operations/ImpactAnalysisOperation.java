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
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.IpServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.ReductionKeyVertex;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.ReadOnlyBusinessService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.vaadin.core.InfoDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ImpactAnalysisOperation implements Operation {
    private static final Logger LOG = LoggerFactory.getLogger(ImpactAnalysisOperation.class);
    private static final String OPERATION_ID = "contextImpactAnalysis";

    private BusinessServiceManager businessServiceManager;
    private BusinessServiceStateMachine businessServiceStateMachine;

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        List<AbstractBusinessServiceVertex> vertices = getVertices(targets);

        Set<GraphVertex> graphVerticesToFocus = Sets.newHashSet();
        for (AbstractBusinessServiceVertex vertex : vertices) {
            if (vertex instanceof BusinessServiceVertex) {
                ReadOnlyBusinessService businessService = businessServiceManager.getBusinessServiceById(((BusinessServiceVertex) vertex).getServiceId());
                graphVerticesToFocus.addAll(businessServiceStateMachine.calculateImpact(businessService));
            }
            if (vertex instanceof IpServiceVertex) {
                IpService ipService = businessServiceManager.getIpServiceById(((IpServiceVertex) vertex).getIpServiceId());
                graphVerticesToFocus.addAll(businessServiceStateMachine.calculateImpact(ipService));
            }
            if (vertex instanceof ReductionKeyVertex) {
                graphVerticesToFocus.addAll(businessServiceStateMachine.calculateImpact(((ReductionKeyVertex) vertex).getReductionKey()));
            }
        }
        LOG.info("Found {} business services impacted.", graphVerticesToFocus.size());

        if (graphVerticesToFocus.isEmpty()) {
            new InfoDialog("No result", "No vertices are impacted by the selected vertices.").open();
        } else {
            // add to focus
            GraphContainer container = operationContext.getGraphContainer();
            container.clearCriteria();
            graphVerticesToFocus.forEach(graphVertex -> container.addCriteria(
                    new VertexHopGraphProvider.DefaultVertexHopCriteria(createTopologyVertex(graphVertex))));
            // add the context vertex because it is missing in the root cause result
            container.addCriteria(new VertexHopGraphProvider.DefaultVertexHopCriteria(targets.get(0)));
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

    public List<AbstractBusinessServiceVertex> getVertices(List<VertexRef> targets) {
        if (targets == null) {
            return Collections.emptyList();
        }
        return targets.stream()
                    .filter(v -> v instanceof AbstractBusinessServiceVertex)
                    .map(v -> (AbstractBusinessServiceVertex) v)
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
