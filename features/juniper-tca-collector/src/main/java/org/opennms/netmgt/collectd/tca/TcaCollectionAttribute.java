/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;

/**
 * The Class TcaCollectionAttribute.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class TcaCollectionAttribute extends AbstractCollectionAttribute {

	/** The Attribute Value. */
	private final String m_value;

	/**
	 * Instantiates a new XML collection attribute.
	 *
	 * @param resource the resource
	 * @param attribType the attribute type
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public TcaCollectionAttribute(TcaCollectionResource resource, TcaCollectionAttributeType attribType, String value) {
		super(attribType, resource);
		m_value = value;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getNumericValue()
	 */
        @Override
	public String getNumericValue() {
		return m_value;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getStringValue()
	 */
        @Override
	public String getStringValue() {
		return m_value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
        @Override
	public String toString() {
		return "TcaCollectionAttribute " + getName() + "=" + m_value;
	}

    @Override
    public String getMetricIdentifier() {
        return "TCA_" + ((TcaCollectionAttributeType)m_attribType).getAttributeObjectId() + '_' + getName();
    }

}
