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
package org.opennms.netmgt.collection.api;

import java.util.Map;

import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.model.ResourcePath;

import com.google.common.collect.Maps;

/**
 * <p>LatencyCollectionResource class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyCollectionResource implements CollectionResource {

    private final String m_serviceName;
    private final String m_ipAddress;
    private final String m_location;
    private final Map<AttributeGroupType, AttributeGroup> m_attributeGroups = Maps.newLinkedHashMap();

    private final Map<String, String> m_tags = Maps.newHashMap();

    private final Map<String, String> m_serviceParams = Maps.newHashMap();

    /**
     * <p>Constructor for LatencyCollectionResource.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param location a {@link java.lang.String} object.
     */
    public LatencyCollectionResource(String serviceName, String ipAddress, String location) {
        m_serviceName = serviceName;
        m_ipAddress = ipAddress;
        m_location = location;
    }

    public LatencyCollectionResource(String serviceName, String ipAddress, String location, Map<String, String> tags) {
        m_serviceName = serviceName;
        m_ipAddress = ipAddress;
        m_location = location;
        m_tags.putAll(tags);
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInstance() {
        return m_ipAddress + "[" + m_serviceName + "]";
    }

    /**
     * <p>getUnmodifiedInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getUnmodifiedInstance() {
        return getInstance();
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInterfaceLabel() {
        return m_serviceName;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_LATENCY;
    }

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean rescanNeeded() {
        return false;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        for (AttributeGroup group: m_attributeGroups.values()) {
            group.visit(visitor);
        }
        visitor.completeResource(this);
    }

    /**
     * <p>getOwnerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getOwnerName() {
        return m_ipAddress;
    }

    @Override
    public ResourcePath getPath() {
        if (LocationUtils.isDefaultLocationName(m_location)) {
            return ResourcePath.get(m_ipAddress);
        } else {
            return ResourcePath.get(ResourcePath.sanitize(m_location), m_ipAddress);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("%s on %s at %s", m_serviceName, m_ipAddress, m_location);
    }

    @Override
    public ResourcePath getParent() {
        return ResourcePath.get(m_ipAddress);
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

    @Override
    public Map<String, String> getTags() {
        return m_tags;
    }

    @Override
    public Map<String, String> getServiceParams() {
        return m_serviceParams;
    }

    public void addServiceParam(String name, String value) {
        m_serviceParams.put(name, value);
    }
}
