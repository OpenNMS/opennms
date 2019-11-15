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

import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
public final class IpServiceVertex extends AbstractBusinessServiceVertex {

    private final static String PROPERTY_SERVICE_ID = "ipServiceId";

    public IpServiceVertex(GenericVertex genericVertex) {
        super(genericVertex);
        Objects.requireNonNull(getIpServiceId(), String.format("%s cannot be null", PROPERTY_SERVICE_ID));
        checkArgument(Type.IpService == genericVertex.getProperty(PROPERTY_TYPE), "%s must be %s for %s", PROPERTY_TYPE, Type.IpService, getClass());
    }
    
    public Integer getIpServiceId() {
        return delegate.getProperty(PROPERTY_SERVICE_ID);
    }
    
    public final static IpServiceVertexBuilder builder() {
        return new IpServiceVertexBuilder();
    }
    
    public final static class IpServiceVertexBuilder extends AbstractBusinessServiceVertexBuilder<IpServiceVertexBuilder, IpServiceVertex> {
        
        private IpServiceVertexBuilder() {}
        
        public IpServiceVertexBuilder ipServiceId(Integer ipServiceId) {
            properties.put(PROPERTY_SERVICE_ID, ipServiceId);
            id(Type.IpService + ":" + ipServiceId);
            return this;
        }
        
        public IpServiceVertexBuilder graphVertex(GraphVertex graphVertex) {
            ipService(graphVertex.getIpService());
            level(graphVertex.getLevel());
            return this;
        }
        
        public IpServiceVertexBuilder ipService(IpService ipService) {
            ipServiceId(ipService.getId());
            label(ipService.getServiceName()); 
            type(Type.IpService);
            isLeaf(true);
            reductionKeys(ipService.getReductionKeys());      
            nodeRef(ipService.getNodeId());
            property("ipAddr", ipService.getIpAddress());
            property("ipAddress", ipService.getIpAddress());
            return this;
        }
        
        public IpServiceVertex build() {
            this.type(Type.IpService);
            return new IpServiceVertex(GenericVertex.builder()
                    .namespace(BusinessServiceGraph.NAMESPACE) // default but can still be changed by properties
                    .properties(properties).build());
        }
    }

}
