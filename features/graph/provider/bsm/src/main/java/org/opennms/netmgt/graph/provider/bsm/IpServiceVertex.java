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

package org.opennms.netmgt.graph.provider.bsm;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class IpServiceVertex extends AbstractBusinessServiceVertex {

    private final static String PROPERTY_SERVICE_ID = "ipServiceId";

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
        super(Type.IpService + ":" + ipServiceId, ipServiceName, level, Type.IpService, true, reductionKeys);
        setIpServiceId(ipServiceId);

//        setIpAddress(ipAddress); // TODO MVR this is not yet supported. Maybe IpRef or something like this could be added
        setNodeRefString(Integer.toString(nodeId));
    }

    public void setIpServiceId(Integer ipServiceId) {
        delegate.setProperty(PROPERTY_SERVICE_ID, ipServiceId);
    }

    public Integer getIpServiceId() {
        return delegate.getProperty(PROPERTY_SERVICE_ID);
    }

}
