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

import java.util.Collection;
import java.util.Collections;

import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertexVisitor;
import org.opennms.features.topology.plugins.topo.bsm.IpServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.ReductionKeyVertex;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class RootCauseAnalysisOperation extends AbstractAnalysisOperation {

    @Override
    public BusinessServiceVertexVisitor<Boolean> getVisitorForSupportedVertices() {
        return new BusinessServiceVertexVisitor<Boolean>() {
            @Override
            public Boolean visit(BusinessServiceVertex vertex) {
                return true;
            }

            @Override
            public Boolean visit(IpServiceVertex vertex) {
                return false;
            }

            @Override
            public Boolean visit(ReductionKeyVertex vertex) {
                return false;
            }
        };
    }

    @Override
    public BusinessServiceVertexVisitor<Collection<GraphVertex>> getVisitorForVerticesToFocus(final BusinessServiceStateMachine stateMachine) {
        return new BusinessServiceVertexVisitor<Collection<GraphVertex>>() {
            @Override
            public Collection<GraphVertex> visit(BusinessServiceVertex vertex) {
                final BusinessService businessService = getBusinessServiceManager().getBusinessServiceById(vertex.getServiceId());
                return stateMachine.calculateRootCause(businessService);
            }

            @Override
            public Collection<GraphVertex> visit(IpServiceVertex vertex) {
                return Collections.emptyList();
            }

            @Override
            public Collection<GraphVertex> visit(ReductionKeyVertex vertex) {
                return Collections.emptyList();
            }
        };
    }

    @Override
    public String getMessageForNoResultDialog() {
        return "No root cause was found for the selected vertices.";
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }
}
