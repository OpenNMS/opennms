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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.ReadOnlyBusinessService;
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
        if (vertices.size() == 0) {
            return;
        }

        Set<ReadOnlyBusinessService> businessServicesToFocus = Sets.newHashSet();
        for (AbstractBusinessServiceVertex vertex : vertices) {
            if (vertex instanceof BusinessServiceVertex) {
                ReadOnlyBusinessService businessService = businessServiceManager.getBusinessServiceById(((BusinessServiceVertex)vertex).getServiceId());
                businessServicesToFocus.addAll(businessServiceStateMachine.calculateImpact(businessService));
            } 
            // TODO: Support IP services and Reduction Keys
        }
        LOG.info("Found {} business services impacted.", businessServicesToFocus.size());

        // TODO: Focus on edges
        operationContext.getGraphContainer().clearCriteria();
        operationContext.getGraphContainer().setSemanticZoomLevel(0);
        operationContext.getGraphContainer().redoLayout();
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
                    .map(v -> (AbstractBusinessServiceVertex)v)
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

