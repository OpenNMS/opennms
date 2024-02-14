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
package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Sets;

public class BusinessServiceVertex extends AbstractBusinessServiceVertex {

    private final Long serviceId;

    public BusinessServiceVertex(BusinessService businessService, int level) {
        this(businessService.getId(), businessService.getName(), level);
    }

    public BusinessServiceVertex(GraphVertex graphVertex) {
        this(graphVertex.getBusinessService(), graphVertex.getLevel());
    }

    public BusinessServiceVertex(Long serviceId, String name, int level) {
        super(Type.BusinessService + ":" + serviceId, name, level);
        this.serviceId = serviceId;
        setLabel(name);
        setTooltipText(String.format("Business Service '%s'", name));
        setIconKey("bsm.business-service");
    }

    public Long getServiceId() {
        return serviceId;
    }

    @Override
    public Type getType() {
        return Type.BusinessService;
    }

    @Override
    public Set<String> getReductionKeys() {
        return Sets.newHashSet();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public <T> T accept(BusinessServiceVertexVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
