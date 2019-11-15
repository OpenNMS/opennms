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


import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

import com.google.common.collect.ImmutableSet;

public final class BusinessServiceVertex extends AbstractBusinessServiceVertex {

    private final static String PROPERTY_SERVICE_ID = "serviceId";
    
    public BusinessServiceVertex(GenericVertex genericVertex) {
        super(genericVertex);
        Objects.requireNonNull(getServiceId(), String.format("%s cannot be null", PROPERTY_SERVICE_ID));
        checkArgument(Type.BusinessService == genericVertex.getProperty(PROPERTY_TYPE), "%s must be %s for %s", PROPERTY_TYPE, Type.BusinessService, getClass());
    }

    public Long getServiceId() {
        return delegate.getProperty(PROPERTY_SERVICE_ID);
    }

    public static BusinessServiceVertexBuilder builder() {
        return new BusinessServiceVertexBuilder();
    }
    
    public final static class BusinessServiceVertexBuilder extends AbstractBusinessServiceVertexBuilder<BusinessServiceVertexBuilder, BusinessServiceVertex> {
        
        private BusinessServiceVertexBuilder() {}
        
        public BusinessServiceVertexBuilder serviceId(Long serviceId) {
            properties.put(PROPERTY_SERVICE_ID, serviceId);
            id(Type.BusinessService + ":" + serviceId);
            return this;
        }
        
        public BusinessServiceVertexBuilder graphVertex(GraphVertex graphVertex) {
            businessService(graphVertex.getBusinessService());
            level(graphVertex.getLevel());
            return this;
        }
        
        public BusinessServiceVertexBuilder businessService(BusinessService businessService) {
            serviceId(businessService.getId());
            label(businessService.getName()); 
            type(Type.BusinessService);
            isLeaf(false);
            reductionKeys(ImmutableSet.of());           
            return this;
        }
        
        public BusinessServiceVertex build() {
            this.type(Type.BusinessService);
            return new BusinessServiceVertex(GenericVertex.builder()
                    .namespace(BusinessServiceGraph.NAMESPACE)
                    .properties(properties).build());
        }
    }
    
}
