/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AttributeGroup class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AttributeGroup implements CollectionVisitable, Persistable {
    private static final Logger LOG = LoggerFactory.getLogger(AttributeGroup.class);
    
    private final CollectionResource m_resource;
    private final AttributeGroupType m_groupType;

    // When persisting certain collection sets i.e. latency statistics it is important that attributes
    // are visited in the same order as they were added
    private final Set<CollectionAttribute> m_attributes = new LinkedHashSet<>();

    /**
     * <p>Constructor for AttributeGroup.</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     * @param groupType a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
     */
    public AttributeGroup(CollectionResource resource, AttributeGroupType groupType) {
        m_resource = resource;
        m_groupType = groupType;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_groupType.getName();
    }
    
    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     */
    public CollectionResource getResource() {
        return m_resource;
    }
    
    /**
     * <p>getAttributes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<CollectionAttribute> getAttributes() {
        return m_attributes;
    }
    
    /**
     * <p>addAttribute</p>
     *
     * @param attr a {@link org.opennms.netmgt.collection.api.CollectionAttribute} object.
     */
    public void addAttribute(CollectionAttribute attr) {
        m_attributes.add(attr);
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.collection.api.CollectionSetVisitor} object.
     */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        LOG.debug("Visiting Group {}", this);
        visitor.visitGroup(this);
        
        for(CollectionAttribute attr : getAttributes()) {
            attr.visit(visitor);
        }
        
        visitor.completeGroup(this);
    }
    
    /**
     * <p>shouldPersist</p>
     *
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @return a boolean.
     */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        boolean shouldPersist = doShouldPersist();
        LOG.debug("{}.shouldPersist = {}", this, shouldPersist);
        return shouldPersist;
    }

    protected boolean doShouldPersist() {
        if (AttributeGroupType.IF_TYPE_IGNORE.equalsIgnoreCase(getIfType())) return true;
        if (AttributeGroupType.IF_TYPE_ALL.equalsIgnoreCase(getIfType())) return true;
        return false;
    }

    protected String getIfType() {
        return m_groupType.getIfType();
    }

    /**
     * <p>getGroupType</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
     */
    public AttributeGroupType getGroupType() {
        return m_groupType;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        if (m_resource.getInstance() == null) {
            return String.format("%s for [%s]@%s", m_groupType, CollectionResource.RESOURCE_TYPE_NODE, m_resource.getParent());
        } else {
            return String.format("%s for %s@%s", m_groupType, m_resource.getInstance(), m_resource.getParent());
        }
    }
    
}
