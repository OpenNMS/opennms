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
