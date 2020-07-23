/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.application;

import java.util.Objects;

import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainVertex;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;

public final class ApplicationVertex extends AbstractDomainVertex {

    private interface Property {
        String VERTEX_TYPE = "vertexType";
        String NAME = "name";
        String IP_ADDRESS = "ipAddress";
        String SERVICE_TYPE_ID = "serviceTypeId";
        String APPLICATION_ID = "applicationId";
    }
    
    public ApplicationVertex(GenericVertex vertex) {
        super(vertex);
        // additional specific checks: 
        Objects.requireNonNull(getProperty(Property.VERTEX_TYPE), Property.VERTEX_TYPE + " cannot be null");
        if(ApplicationVertexType.Service == getVertexType()) {
            Objects.requireNonNull(getProperty(Property.SERVICE_TYPE_ID), Property.SERVICE_TYPE_ID + " cannot be null");
        }
        if (ApplicationVertexType.Application == getVertexType()) {
            Objects.requireNonNull(getProperty(Property.APPLICATION_ID), Property.APPLICATION_ID + " cannot be null");
        }
    }

    public String getName() {
        return delegate.getProperty(Property.NAME);
    }

    public ApplicationVertexType getVertexType() {
        return delegate.getProperty(Property.VERTEX_TYPE);
    }

    public String getIpAddress() {
        return delegate.getProperty(Property.IP_ADDRESS);
    }

    public VertexRef getVertexRef(){
        return delegate.getVertexRef();
    }

    public Integer getServiceTypeId() {
        return getProperty(Property.SERVICE_TYPE_ID);
    }

    public Integer getApplicationId() {
        return getProperty(Property.APPLICATION_ID);
    }

    private <T> T getProperty(String key) {
        Objects.requireNonNull(key);
        return delegate.getProperty(key);
    }

    static String createVertexId(OnmsApplication application) {
        Objects.requireNonNull(application);
        return ApplicationVertexType.Application + ":" + application.getId();
    }

    static String createVertexId(OnmsMonitoredService service) {
        Objects.requireNonNull(service);
        return ApplicationVertexType.Service + ":" + service.getId();
    }

    public static ApplicationVertexBuilder builder() {
        return new ApplicationVertexBuilder();
    }
    
    public static ApplicationVertex from(GenericVertex genericVertex) {
        return new ApplicationVertex(genericVertex);
    }
    
    public final static class ApplicationVertexBuilder extends AbstractDomainVertexBuilder<ApplicationVertexBuilder> {
        
        private ApplicationVertexBuilder() {}
        
        public ApplicationVertexBuilder application(OnmsApplication application) {
            this.properties.put(GenericProperties.ID, createVertexId(application));
            this.properties.put(Property.VERTEX_TYPE, ApplicationVertexType.Application);
            this.properties.put(Property.APPLICATION_ID, application.getId());
            this.properties.put(Property.NAME, application.getName());
            return this;
        }
        
        public ApplicationVertexBuilder service(OnmsMonitoredService monitoredService) {
            this.properties.put(GenericProperties.ID, createVertexId(monitoredService));
            this.properties.put(Property.VERTEX_TYPE, ApplicationVertexType.Service);
            this.properties.put(Property.SERVICE_TYPE_ID, Objects.requireNonNull(monitoredService.getServiceType().getId()));
            this.properties.put(Property.NAME, monitoredService.getServiceName());
            this.properties.put(Property.IP_ADDRESS, monitoredService.getIpAddress().toString());
            super.nodeRef(Integer.toString(monitoredService.getNodeId()));
            return this;            
        }
        
        public ApplicationVertexBuilder name(String name) {
            this.properties.put(Property.NAME, name);
            this.label(name);
            return this;
        }
        
        public ApplicationVertex build() {
            namespace(ApplicationGraph.NAMESPACE); // namespace is fixed => cannot be changed
            return new ApplicationVertex(GenericVertex.builder().properties(properties).build());
        }
    }
    
}
