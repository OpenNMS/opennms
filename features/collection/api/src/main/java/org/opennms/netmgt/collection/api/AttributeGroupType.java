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
package org.opennms.netmgt.collection.api;

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

    private final String m_name;
    private final String m_ifType;
    
    /**
     * TODO Document this value.
     */
    public static final String IF_TYPE_ALL = "all";
    
    /**
     * TODO Document this value.
     */
    public static final String IF_TYPE_IGNORE = "ignore";
    
    private SortedSet<CollectionAttributeType> m_attributeTypes = new TreeSet<CollectionAttributeType>(new ByNameComparator());

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
    @Override
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
    @Override
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
     * @param attrType a {@link org.opennms.netmgt.collectd.CollectionAttributeType} object.
     */
    public void addAttributeType(CollectionAttributeType attrType) {
        m_attributeTypes.add(attrType);
    }
    
    /**
     * <p>getAttributeTypes</p>
     *
     * @return a {@link java.util.SortedSet} object.
     */
    public SortedSet<CollectionAttributeType> getAttributeTypes() {
        return Collections.unmodifiableSortedSet(m_attributeTypes);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "AttrGroupType[name="+m_name+", ifType="+m_ifType+']' ;
    }
    
    

}
