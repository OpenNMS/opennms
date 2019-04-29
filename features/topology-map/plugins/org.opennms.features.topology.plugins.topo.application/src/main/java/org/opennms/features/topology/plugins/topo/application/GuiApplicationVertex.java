/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.application;

import org.opennms.features.topology.api.topo.AbstractLevelAwareVertex;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.netmgt.graph.provider.application.ApplicationVertex;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

public class GuiApplicationVertex extends AbstractLevelAwareVertex implements LevelAware {

    private Integer serviceTypeId;

    public GuiApplicationVertex(ApplicationVertex vertex) {
        this(vertex.getId(), vertex.getName());

        final boolean isApplication = (vertex.getVertexType() == ApplicationVertex.VertexType.application);
        if(isApplication) {
            setTooltipText(String.format("Application '%s'", vertex.getName()));
            setIconKey("application.application");
        } else {
            setTooltipText(String.format("Service '%s', IP: %s", vertex.getName(), vertex.getIpAddress()));
            setIpAddress(vertex.getIpAddress());
            setNodeID(Integer.valueOf(vertex.getNodeRefString()));
            setServiceTypeId(vertex.getServiceTypeId());
            setIconKey("application.monitored-service");
        }
    }

    public GuiApplicationVertex(OnmsApplication application) {
        this(application.getId().toString(), application.getName());
        setTooltipText(String.format("Application '%s'", application.getName()));
        setIconKey("application.application");
    }

    public GuiApplicationVertex(OnmsMonitoredService monitoredService) {
        this(monitoredService.getId().toString(), monitoredService.getServiceName());
        setTooltipText(String.format("Service '%s', IP: %s", monitoredService.getServiceName(), monitoredService.getIpAddress().toString()));
        setIpAddress(monitoredService.getIpAddress().toString());
        setNodeID(monitoredService.getNodeId());
        setServiceTypeId(monitoredService.getServiceType().getId());
        setIconKey("application.monitored-service");
    }

    /**
     * Creates a new {@link GuiApplicationVertex}.
     * @param id the unique id of this vertex. Must be unique overall the namespace.
     */
    public GuiApplicationVertex(String id, String label) {
        super(ApplicationTopologyProvider.TOPOLOGY_NAMESPACE, id, label);
    }
    
    public void setServiceTypeId(Integer serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    public Integer getServiceTypeId() {
        return serviceTypeId;
    }

    public boolean isPartOf(String applicationId) {
        return applicationId != null && applicationId.equals(getRoot().getId());
    }

    public GuiApplicationVertex getRoot() {
        if (isRoot()) {
            return this;
        }
        return ((GuiApplicationVertex)getParent()).getRoot();
    }

    @Override
    public int getLevel() {
        return isRoot() ? 0 : 1;
    }
}
