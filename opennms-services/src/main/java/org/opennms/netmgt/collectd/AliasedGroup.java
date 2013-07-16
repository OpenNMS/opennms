/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.util.Collection;

import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AliasedGroup class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AliasedGroup extends AttributeGroup {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(AliasedGroup.class);
	
	AttributeGroup m_group;

	/**
	 * <p>Constructor for AliasedGroup.</p>
	 *
	 * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
	 * @param group a {@link org.opennms.netmgt.config.collector.AttributeGroup} object.
	 */
	public AliasedGroup(CollectionResource resource, AttributeGroup group) {
		super(resource, group.getGroupType());
		m_group = group;
	}

	/**
	 * <p>addAttribute</p>
	 *
	 * @param attr a {@link org.opennms.netmgt.collectd.SnmpAttribute} object.
	 */
	public void addAttribute(SnmpAttribute attr) {
		m_group.addAttribute(attr);
	}

	/** {@inheritDoc} */
        @Override
	public boolean equals(Object obj) {
		return m_group.equals(obj);
	}

	/**
	 * <p>getAttributes</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
        @Override
	public Collection<CollectionAttribute> getAttributes() {
		return m_group.getAttributes();
	}

	/**
	 * <p>getGroupType</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.collector.AttributeGroupType} object.
	 */
        @Override
	public AttributeGroupType getGroupType() {
		return m_group.getGroupType();
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String getName() {
		return m_group.getName();
	}

	/**
	 * <p>hashCode</p>
	 *
	 * @return a int.
	 */
        @Override
	public int hashCode() {
		return m_group.hashCode();
	}

	/** {@inheritDoc} */
        @Override
	public boolean shouldPersist(ServiceParameters params) {
		return m_group.shouldPersist(params);
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return m_group.toString();
	}
	
	/** {@inheritDoc} */
        @Override
	public void visit(CollectionSetVisitor visitor) {
		visitor.visitGroup(this);
		
		for(CollectionAttribute attr : getAttributes()) {
		    AliasedAttribute aliased = new AliasedAttribute(getResource(), (SnmpAttribute)attr);
		    LOG.debug("visiting at aliased  = {}", aliased);
		    aliased.visit(visitor);
		}
		
		visitor.completeGroup(this);
	}

}
