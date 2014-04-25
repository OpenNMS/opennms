/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;


/**
 * The Class JMXCollectionAttribute.
 */
public class JMXCollectionAttribute extends AbstractCollectionAttribute {

    /** The alias. */
    String m_alias;

    /** The value. */
    String m_value;

    /** The resource. */
    JMXCollectionResource m_resource;

    /** The attribute type. */
    CollectionAttributeType m_attribType;

    /**
     * Instantiates a new JMX collection attribute.
     *
     * @param resource
     *            the resource
     * @param attribType
     *            the attrib type
     * @param alias
     *            the alias
     * @param value
     *            the value
     */
    JMXCollectionAttribute(JMXCollectionResource resource, CollectionAttributeType attribType, String alias, String value) {
        super(attribType, resource);
        m_resource = resource;
        m_attribType = attribType;
        m_alias = alias;
        m_value = value;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionAttribute#getMetricIdentifier()
     */
    @Override
    public String getMetricIdentifier() {
        String metricId = m_attribType.getGroupType().getName();
        metricId = metricId.replace("_type_", ":type=");
        metricId = metricId.replace("_", ".");
        metricId = metricId.concat(".");
        metricId = metricId.concat(getName());
        return "JMX_".concat(metricId);

    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getNumericValue()
     */
    @Override
    public String getNumericValue() {
        return m_value;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getStringValue()
     */
    @Override
    public String getStringValue() {
        return m_value;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "alias " + m_alias + ", value " + m_value + ", resource " + m_resource + ", attributeType " + m_attribType;
    }

}
