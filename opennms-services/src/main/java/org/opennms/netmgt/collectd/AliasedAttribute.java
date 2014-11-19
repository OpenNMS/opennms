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
