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

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <p>AliasedAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AliasedAttribute extends SnmpAttribute {
    
	
	/**
	 * <p>Constructor for AliasedAttribute.</p>
	 *
	 * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
	 * @param attr a {@link org.opennms.netmgt.collectd.SnmpAttribute} object.
	 */
	public AliasedAttribute(CollectionResource resource, SnmpAttribute attr) {
		super(resource, (SnmpAttributeType)attr.getAttributeType(), attr.getValue());
		m_attr = attr;
	}

	private final SnmpAttribute m_attr;

	/** {@inheritDoc} */
        @Override
	public boolean equals(Object obj) {
		return m_attr.equals(obj);
	}

	/**
	 * <p>getValue</p>
	 *
	 * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
	 */
        @Override
	public SnmpValue getValue() {
		return m_attr.getValue();
	}

	/**
	 * <p>hashCode</p>
	 *
	 * @return a int.
	 */
        @Override
	public int hashCode() {
		return m_attr.hashCode();
	}

	/** {@inheritDoc} */
        @Override
	public boolean shouldPersist(ServiceParameters params) {
		return m_attr.shouldPersist(params);
	}

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return getResource()+"."+getAttributeType()+" = "+getValue();
    }

	

}
