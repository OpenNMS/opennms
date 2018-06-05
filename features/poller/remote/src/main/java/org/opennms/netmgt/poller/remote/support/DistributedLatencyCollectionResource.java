/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.remote.support;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.model.ResourcePath;

/**
 * Used to persist distributed latency statistics collected by the remote poller.
 *
 * @author jwhite
 */
public class DistributedLatencyCollectionResource implements CollectionResource {

    private final String m_locationMonitorId;
    private final String m_ipAddress;
    private final Map<AttributeGroupType, AttributeGroup> m_attributeGroups = new LinkedHashMap<>();

    public DistributedLatencyCollectionResource(String locationMonitorId, String ipAddress) {
        m_locationMonitorId = locationMonitorId;
        m_ipAddress = ipAddress;
    }

    @Override
    public String getInstance() {
        return String.format("%s[%s]", m_locationMonitorId, m_ipAddress);
    }

    @Override
    public String getInterfaceLabel() {
        return m_locationMonitorId;
    }

    @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_IF;
    }

    @Override
    public boolean rescanNeeded() {
        return false;
    }

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /**
     * Adds the given attribute into the collection for this resource
     *
     * @param attr The Attribute to add
     */
    public void addAttribute(CollectionAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        group.addAttribute(attr);
    }

    /**
     * Finds, or creates, and returns the AttributeGroup for the given group Type
     *
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     * @return a {@link org.opennms.netmgt.collection.api.AttributeGroup} object.
     */
    public final AttributeGroup getGroup(AttributeGroupType groupType) {
        AttributeGroup group = m_attributeGroups.get(groupType);
        if (group == null) {
            group = new AttributeGroup(this, groupType);
            m_attributeGroups.put(groupType, group);
        }
        return group;
    }

    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        for (AttributeGroup group: m_attributeGroups.values()) {
            group.visit(visitor);
        }
        visitor.completeResource(this);
    }

    @Override
    public String getOwnerName() {
        return m_locationMonitorId;
    }

    @Override
    public ResourcePath getPath() {
        return ResourcePath.get("distributed", m_locationMonitorId, m_ipAddress);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", m_locationMonitorId, m_ipAddress);
    }

    @Override
    public ResourcePath getParent() {
        return ResourcePath.get(m_ipAddress);
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

    public String getLocationMonitorId() {
        return m_locationMonitorId;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

}
