/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */


package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.RrdRepository;

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
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

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
    public String getOwnerName() {
        return m_agent.getHostAddress();    }

    /** {@inheritDoc} */
    public File getResourceDir(RrdRepository repository) {
        return new File(repository.getRrdBaseDir(), Integer.toString(m_agent.getNodeId()));
    }

    /**
     * Adds the given attribute into the collection for this resource
     *
     * @param attr The Attribute to add
     */
    protected void addAttribute(CollectionAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        log().debug("Adding attribute " + attr.getClass().getName() + ": "
                     + attr + " to group " + group);
        group.addAttribute(attr);
    }

    /**
     * Finds, or creates, and returns the AttributeGroup for the given group Type
     *
     * @param groupType a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
     * @return a {@link org.opennms.netmgt.collectd.AttributeGroup} object.
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
    public abstract int getType();

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    public abstract boolean rescanNeeded();

    /** {@inheritDoc} */
    public abstract boolean shouldPersist(ServiceParameters params);

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return null;
    }

}
