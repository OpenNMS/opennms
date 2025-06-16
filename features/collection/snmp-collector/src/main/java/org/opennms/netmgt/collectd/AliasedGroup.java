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
package org.opennms.netmgt.collectd;

import java.util.Collection;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
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
	
	private final AttributeGroup m_group;

	/**
	 * <p>Constructor for AliasedGroup.</p>
	 *
	 * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
	 * @param group a {@link org.opennms.netmgt.collection.api.AttributeGroup} object.
	 */
	public AliasedGroup(SnmpCollectionResource resource, AttributeGroup group) {
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
	 * @return a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
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
