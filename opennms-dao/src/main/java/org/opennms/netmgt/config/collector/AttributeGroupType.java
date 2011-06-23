/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collector;

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
