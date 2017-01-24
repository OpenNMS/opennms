/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<AttributeGroupType, AttributeGroup> m_attributeGroups = new HashMap<AttributeGroupType, AttributeGroup>();
    
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

}
