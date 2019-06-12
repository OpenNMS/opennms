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
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.simple.SimpleVertex;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class ApplicationVertex extends SimpleVertex {

    private interface Property {
        String VERTEX_TYPE = "vertexType";
        String NAME = "name";
        String IP_ADDRESS = "ipAddress";
        String SERVICE_TYPE_ID = "serviceTypeId";
    }

    public ApplicationVertex(OnmsApplication application) {
        this(createVertexId(application), application.getName());
        setVertexType(ApplicationVertexType.Application);
    }

    public ApplicationVertex(OnmsMonitoredService monitoredService) {
        this(createVertexId(monitoredService), monitoredService.getServiceName());
        setVertexType(ApplicationVertexType.Service);
        setServiceTypeId(monitoredService.getServiceType().getId());
    }

    public ApplicationVertex(GenericVertex vertex) {
        super(vertex);
    }

    /**
     * Creates a new {@link ApplicationVertex}.
     * @param id the unique id of this vertex. Must be unique overall the namespace.
     */
    private ApplicationVertex(String id, String name) {
        super(ApplicationGraph.TOPOLOGY_NAMESPACE, id);
        this.setName(name);
    }

    public String getName() {
        return delegate.getProperty(Property.NAME);
    }

    public void setName(String name) {
        setProperty(Property.NAME, name);
        setLabel(name);
    }

    public ApplicationVertexType getVertexType() {
        return delegate.getProperty(Property.VERTEX_TYPE);
    }

    public void setVertexType(ApplicationVertexType vertexType) {
        setProperty(Property.VERTEX_TYPE, vertexType);
    }

    public String getIpAddress() {
        return delegate.getProperty(Property.IP_ADDRESS);
    }

    public void setIpAddress(String ipAddress) {
        setProperty(Property.IP_ADDRESS, ipAddress);
    }

    public VertexRef getVertexRef(){
        return delegate.getVertexRef();
    }
    
    public void setServiceTypeId(Integer serviceTypeId) {
        setProperty(Property.SERVICE_TYPE_ID,  Objects.requireNonNull(serviceTypeId));
    }

    public Integer getServiceTypeId() {
        return getProperty(Property.SERVICE_TYPE_ID);
    }

    private <T> void setProperty(String key, T property) {
        Objects.requireNonNull(key);
        delegate.setProperty(key, property);
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

}
