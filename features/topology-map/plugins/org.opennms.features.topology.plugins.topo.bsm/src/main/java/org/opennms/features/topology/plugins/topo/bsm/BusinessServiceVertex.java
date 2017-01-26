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

import org.opennms.netmgt.bsm.service.model.ReadOnlyBusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Sets;

public class BusinessServiceVertex extends AbstractBusinessServiceVertex {

    private final Long serviceId;

    public BusinessServiceVertex(ReadOnlyBusinessService businessService, int level, Status status) {
        this(businessService.getId(), businessService.getName(), level, status);
    }

    public BusinessServiceVertex(GraphVertex graphVertex) {
        this(graphVertex.getBusinessService(), graphVertex.getLevel(), graphVertex.getStatus());
    }

    public BusinessServiceVertex(Long serviceId, String name, int level, Status status) {
        super(Type.BusinessService + ":" + serviceId, name, level, status);
        this.serviceId = serviceId;
        setLabel(name);
        setTooltipText(String.format("Business Service '%s'", name));
        setIconKey("business-service");
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
}
