/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

    private enum Property {
        vertexType, name, ipAdress, serviceTypeId
    }

    public enum VertexType {
        application, service
    }

    public ApplicationVertex(GenericVertex vertex) {
        super(vertex);
    }

    public ApplicationVertex(OnmsApplication application) {
        this(application.getId().toString(), application.getName());
        setVertexType(VertexType.application);
    }

    public ApplicationVertex(OnmsMonitoredService monitoredService) {
        this(monitoredService.getId().toString(), monitoredService.getServiceName());
        setVertexType(VertexType.service);
        setServiceTypeId(monitoredService.getServiceType().getId());
    }

    /**
     * Creates a new {@link ApplicationVertex}.
     * @param id the unique id of this vertex. Must be unique overall the namespace.
     */
    public ApplicationVertex(String id, String name) {
        super(ApplicationGraphProvider.TOPOLOGY_NAMESPACE, id);
        this.setName(name);
    }

    public String getName() {
        return delegate.getProperty(Property.name.name());
    }

    public void setName(String name) {
        setProperty(Property.name, name);
    }

    public VertexType getVertexType() {
        return delegate.getProperty(Property.vertexType.name());
    }

    public void setVertexType(VertexType vertexType) {
        setProperty(Property.vertexType, vertexType);
    }

    public String getIpAddress() {
        return delegate.getProperty(Property.ipAdress.name());
    }

    public void setIpAddress(String ipAddress) {
        setProperty(Property.ipAdress, ipAddress);
    }

    public VertexRef getVertexRef(){
        return delegate.getVertexRef();
    }
    
    public void setServiceTypeId(Integer serviceTypeId) {
        setProperty(Property.serviceTypeId,  Objects.requireNonNull(serviceTypeId));
    }

    public Integer getServiceTypeId() {
        return getProperty(Property.serviceTypeId);
    }

    private <T> void setProperty(Property propertyName, T property) {
        delegate.setProperty(propertyName.name(), property);
    }

    private <T> T getProperty(Property propertyName) {
        return delegate.getProperty(propertyName.name());
    }

}
