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

import java.util.Collection;
import java.util.Collections;

import org.opennms.features.topology.plugins.topo.bsm.ApplicationVertex;
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

            @Override
            public Boolean visit(ApplicationVertex vertex) {
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

            @Override
            public Collection<GraphVertex> visit(ApplicationVertex vertex) {
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
