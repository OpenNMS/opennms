//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.util.Collection;

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>AliasedGroup class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AliasedGroup extends AttributeGroup {
	
	AttributeGroup m_group;

	/**
	 * <p>Constructor for AliasedGroup.</p>
	 *
	 * @param resource a {@link org.opennms.netmgt.collectd.CollectionResource} object.
	 * @param group a {@link org.opennms.netmgt.collectd.AttributeGroup} object.
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
	public boolean equals(Object obj) {
		return m_group.equals(obj);
	}

	/**
	 * <p>getAttributes</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<CollectionAttribute> getAttributes() {
		return m_group.getAttributes();
	}

	/**
	 * <p>getGroupType</p>
	 *
	 * @return a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
	 */
	public AttributeGroupType getGroupType() {
		return m_group.getGroupType();
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_group.getName();
	}

	/**
	 * <p>hashCode</p>
	 *
	 * @return a int.
	 */
	public int hashCode() {
		return m_group.hashCode();
	}

	/** {@inheritDoc} */
	public boolean shouldPersist(ServiceParameters params) {
		return m_group.shouldPersist(params);
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return m_group.toString();
	}
	
	ThreadCategory log(){
		return ThreadCategory.getInstance(getClass());
	}

	/** {@inheritDoc} */
	public void visit(CollectionSetVisitor visitor) {
		visitor.visitGroup(this);
		
		for(CollectionAttribute attr : getAttributes()) {
		    AliasedAttribute aliased = new AliasedAttribute(getResource(), (SnmpAttribute)attr);
		    log().debug("visiting at aliased  = " + aliased);
		    aliased.visit(visitor);
		}
		
		visitor.completeGroup(this);
	}

}
