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
package org.opennms.netmgt.collection.support;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.model.ResourcePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for {@link CollectionResource} objects, implementing common features (to reduce repeated code).
 * Typically used by the non-SNMP collectors (SNMP has it's own set of classes for this). Provides a basic set of attributes.
 * Provides support, via {@link #addAttribute(CollectionAttribute)} and {@link #getGroup(AttributeGroupType)} for basic 
 * "groups" of attributes. Also provides a sample "visit" implementation based on those groups, although this may well 
 * be overridden by subclasses.
 */
public abstract class AbstractCollectionResource implements CollectionResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCollectionResource.class);

    protected final CollectionAgent m_agent;
    private final Map<AttributeGroupType, AttributeGroup> m_attributeGroups = new LinkedHashMap<AttributeGroupType, AttributeGroup>();
    private final Map<String, String> m_tags = Maps.newHashMap();
    private final Map<String, String> m_serviceParams = Maps.newHashMap();
    
    /**
     * <p>Constructor for AbstractCollectionResource.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    protected AbstractCollectionResource(CollectionAgent agent) {
        m_agent=agent;
    }
    
    /**
     * <p>getOwnerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getOwnerName() {
        return m_agent.getHostAddress();
    }

    /** {@inheritDoc} */
    @Override
    public ResourcePath getPath() {
        return m_agent.getStorageResourcePath();
    }

    /**
     * Adds the given attribute into the collection for this resource
     *
     * @param attr The Attribute to add
     */
    public final void addAttribute(CollectionAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        LOG.debug("Adding attribute {}: {} to group {}", attr.getClass().getName(), attr, group);
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

    @Override
    public final ResourcePath getParent() {
        return m_agent.getStorageResourcePath();
    }

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public final boolean rescanNeeded() {
        // A rescan is not needed by default on collection resources
        return false;
    }

    /**
     * Resources should be persisted by default. Returns true.
     */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /**
     * <p>getInterfaceLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInterfaceLabel() {
        return null;
    }
    
    /**
     * @return Returns null to indicate that {@link DefaultTimeKeeper} should be used.
     */
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

    public void addTag(String name, String value) {
        m_tags.put(name, value);
    }

}
