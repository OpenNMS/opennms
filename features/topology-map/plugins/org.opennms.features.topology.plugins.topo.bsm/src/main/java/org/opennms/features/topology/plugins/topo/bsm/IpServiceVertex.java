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

import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class IpServiceVertex extends AbstractBusinessServiceVertex {

    private final Integer ipServiceId;
    private final Set<String> reductionKeys;

    public IpServiceVertex(IpService ipService, int level) {
        this(ipService.getId(),
            ipService.getServiceName(),
            ipService.getIpAddress(),
            ipService.getReductionKeys(),
            ipService.getNodeId(),
            level);
    }

    public IpServiceVertex(GraphVertex graphVertex) {
        this(graphVertex.getIpService(), graphVertex.getLevel());
    }

    private IpServiceVertex(int ipServiceId, String ipServiceName, String ipAddress, Set<String> reductionKeys, int nodeId, int level) {
        super(Type.IpService + ":" + ipServiceId, ipServiceName, level);
        this.ipServiceId = ipServiceId;
        this.reductionKeys = reductionKeys;
        setIpAddress(ipAddress);
        setLabel(ipServiceName);
        setTooltipText(String.format("IP Service '%s' on %s", ipServiceName, ipAddress));
        setIconKey("bsm.ip-service");
        setNodeID(nodeId);
    }

    public Integer getIpServiceId() {
        return ipServiceId;
    }

    @Override
    public Type getType() {
        return Type.IpService;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    @Override
    public <T> T accept(BusinessServiceVertexVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
