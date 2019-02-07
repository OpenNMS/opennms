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

import org.opennms.netmgt.graph.api.generic.GenericVertex;
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
    public GenericVertex asGenericVertex() {
        final GenericVertex genericVertex = super.asGenericVertex();
        genericVertex.setProperty("serviceId", getServiceId());
        return genericVertex;
    }

//    @Override
//    public <T> T accept(BusinessServiceVertexVisitor<T> visitor) {
//        return visitor.visit(this);
//    }
}
