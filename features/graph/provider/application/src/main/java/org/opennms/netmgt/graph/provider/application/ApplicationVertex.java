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
