/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;

/**
 * The Class TcaCollectionAttribute.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class TcaCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {

	/** The Attribute Name. */
	private String m_name;

	/** The Attribute Value. */
	private String m_value;

	/** The TCA Collection Resource associated with this attribute. */
	private TcaCollectionResource m_resource;

	/** The Attribute Type. */
	private TcaCollectionAttributeType m_attribType;

	/**
	 * Instantiates a new XML collection attribute.
	 *
	 * @param resource the resource
	 * @param attribType the attribute type
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public TcaCollectionAttribute(TcaCollectionResource resource, TcaCollectionAttributeType attribType, String name, String value) {
		m_resource = resource;
		m_attribType = attribType;
		m_name = name;
		m_value = value;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getAttributeType()
	 */
        @Override
	public CollectionAttributeType getAttributeType() {
		return m_attribType;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getName()
	 */
        @Override
	public String getName() {
		return m_name;
	}
	
	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getNumericValue()
	 */
        @Override
	public String getNumericValue() {
		return m_value;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getResource()
	 */
        @Override
	public CollectionResource getResource() {
		return m_resource;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getStringValue()
	 */
        @Override
	public String getStringValue() {
		return m_value;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#shouldPersist(org.opennms.netmgt.config.collector.ServiceParameters)
	 */
        @Override
	public boolean shouldPersist(ServiceParameters params) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionAttribute#getType()
	 */
        @Override
	public String getType() {
		return m_attribType.getType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
        @Override
	public String toString() {
		return "TcaCollectionAttribute " + m_name + "=" + m_value;
	}

    @Override
    public String getMetricIdentifier() {
        return "TCA_" + m_attribType.getAttributeObjectId() + '_' + getName();
    }

}
