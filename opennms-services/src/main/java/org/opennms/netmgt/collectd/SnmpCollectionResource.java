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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Abstract SnmpCollectionResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class SnmpCollectionResource implements CollectionResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionResource.class);
    
    private ResourceType m_resourceType;

    private Map<AttributeGroupType, AttributeGroup> m_groups = new HashMap<AttributeGroupType, AttributeGroup>();

    /**
     * <p>Constructor for SnmpCollectionResource.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.ResourceType} object.
     */
    public SnmpCollectionResource(final ResourceType def) {
        m_resourceType = def;
    }
    
    /**
     * <p>getResourceType</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.ResourceType} object.
     */
    public ResourceType getResourceType() {
        return m_resourceType;
    }
    
    /**
     * <p>getCollectionAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public final CollectionAgent getCollectionAgent() {
        return m_resourceType.getAgent();
    }

    /** {@inheritDoc} */
    @Override
    public abstract boolean shouldPersist(ServiceParameters params);

    /**
     * <p>getOwnerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getOwnerName() {
        return getCollectionAgent().getHostAddress();
    }

    /** {@inheritDoc} */
    @Override
    public abstract File getResourceDir(RrdRepository repository);
    
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
    public boolean rescanNeeded() {
    	return false;
    }
    
    /**
     * <p>setAttributeValue</p>
     *
     * @param type a {@link org.opennms.netmgt.collectd.SnmpAttributeType} object.
     * @param val a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public void setAttributeValue(final SnmpAttributeType type, final SnmpValue val) {
        SnmpAttribute attr = new SnmpAttribute(this, type, val);
        addAttribute(attr);
    }

    private void addAttribute(final SnmpAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        LOG.debug("Adding attribute {}: {} to group {}", attr.getClass().getName(), attr, group);
        group.addAttribute(attr);
    }

    private AttributeGroup getGroup(final AttributeGroupType groupType) {
        AttributeGroup group = m_groups.get(groupType);
        if (group == null) {
            group = new AttributeGroup(this, groupType);
            m_groups.put(groupType, group);
        }
        return group;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        
        for (AttributeGroup group : getGroups()) {
            group.visit(visitor);
        }
        
        visitor.completeResource(this);
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    protected Collection<AttributeGroup> getGroups() {
        return m_groups.values();
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
