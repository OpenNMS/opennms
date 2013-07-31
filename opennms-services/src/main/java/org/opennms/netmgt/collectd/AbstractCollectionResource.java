/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base (partial) implementation of CollectionResource, implementing common features (to reduce repeated code)
 * Typically used by the non-SNMP collectors (SNMP has it's own set of classes for this).  Provides a basic group of ag
 * Provides support, via addAttribute, getGroup, and getGroups, for basic "groups" of attributes.
 * Also provides a sample "visit" implementation based on those groups, although this may well be overridden by subclasses
 *
 * @author opennms
 * @version $Id: $
 */
public abstract class AbstractCollectionResource implements CollectionResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCollectionResource.class);

    protected CollectionAgent m_agent;
    private Map<AttributeGroupType, AttributeGroup> m_attributeGroups;
    
    /**
     * <p>Constructor for AbstractCollectionResource.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    protected AbstractCollectionResource(CollectionAgent agent) {
        m_agent=agent;
        m_attributeGroups=new HashMap<AttributeGroupType, AttributeGroup>();
    }
    
    /**
     * <p>getOwnerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getOwnerName() {
        return m_agent.getHostAddress();    }

    /** {@inheritDoc} */
    @Override
    public File getResourceDir(RrdRepository repository) {
        return new File(repository.getRrdBaseDir(), m_agent.getStorageDir().toString());
    }

    /**
     * Adds the given attribute into the collection for this resource
     *
     * @param attr The Attribute to add
     */
    protected void addAttribute(CollectionAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        LOG.debug("Adding attribute {}: {} to group {}", attr.getClass().getName(), attr, group);
        group.addAttribute(attr);
    }

    /**
     * Finds, or creates, and returns the AttributeGroup for the given group Type
     *
     * @param groupType a {@link org.opennms.netmgt.config.collector.AttributeGroupType} object.
     * @return a {@link org.opennms.netmgt.config.collector.AttributeGroup} object.
     */
    protected AttributeGroup getGroup(AttributeGroupType groupType) {
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
     * <p>getType</p>
     *
     * @return a int.
     */
    @Override
    public abstract int getType();

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public abstract boolean rescanNeeded();

    /** {@inheritDoc} */
    @Override
    public abstract boolean shouldPersist(ServiceParameters params);

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return null;
    }
    
    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
