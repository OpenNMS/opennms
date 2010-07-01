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
// Modifications:
//
// 2006 Aug 15: Formatting. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>AttributeGroupType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AttributeGroupType {

    private String m_name;
    private String m_ifType;
    
    private SortedSet<AttributeDefinition> m_attributeTypes = new TreeSet<AttributeDefinition>(new ByNameComparator());

    /**
     * <p>Constructor for AttributeGroupType.</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param groupIfType a {@link java.lang.String} object.
     */
    public AttributeGroupType(String groupName, String groupIfType) {
        if (groupName == null) {
        	throw new NullPointerException("groupName cannot be null");
        }
        if (groupIfType == null) {
        	throw new NullPointerException("groupIfType cannot be null");
        }
        
        m_name = groupName;
        m_ifType = groupIfType;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj instanceof AttributeGroupType) {
            AttributeGroupType groupType = (AttributeGroupType)obj;
            return m_name.equals(groupType.m_name);
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_name.hashCode();
    }

    /**
     * <p>getIfType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfType() {
        return m_ifType;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>addAttributeType</p>
     *
     * @param attrType a {@link org.opennms.netmgt.collectd.AttributeDefinition} object.
     */
    public void addAttributeType(AttributeDefinition attrType) {
        m_attributeTypes.add(attrType);
    }
    
    /**
     * <p>getAttributeTypes</p>
     *
     * @return a {@link java.util.SortedSet} object.
     */
    public SortedSet<AttributeDefinition> getAttributeTypes() {
        return Collections.unmodifiableSortedSet(m_attributeTypes);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "AttrGroupType[name="+m_name+", ifType="+m_ifType+']' ;
    }
    
    

}
