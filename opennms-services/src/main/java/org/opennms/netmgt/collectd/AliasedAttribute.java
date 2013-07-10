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

import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;
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
	 * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
	 * @param attr a {@link org.opennms.netmgt.collectd.SnmpAttribute} object.
	 */
	public AliasedAttribute(CollectionResource resource, SnmpAttribute attr) {
		super(resource, attr.getAttributeType(), attr.getValue());
		m_attr = attr;
	}

	private SnmpAttribute m_attr;

	/** {@inheritDoc} */
        @Override
	public boolean equals(Object obj) {
		return m_attr.equals(obj);
	}

	/**
	 * <p>getAttributeType</p>
	 *
	 * @return a {@link org.opennms.netmgt.collectd.SnmpAttributeType} object.
	 */
        @Override
	public SnmpAttributeType getAttributeType() {
		return m_attr.getAttributeType();
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String getName() {
		return m_attr.getName();
	}

	/**
	 * <p>getType</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String getType() {
		return m_attr.getType();
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
