/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;

/**
 * The Class XmlCollectionAttribute.
 * <p>This is related with the configuration class XmlObject (the attribute to be stored on a RRD file).</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {

    /** The Attribute Value. */
    private String m_value;

    /** The XML Collection Resource associated with this attribute. */
    private XmlCollectionResource m_resource;

    /** The XML Attribute Type. */
    private XmlCollectionAttributeType m_attribType;

    /**
     * Instantiates a new XML collection attribute.
     *
     * @param resource the resource
     * @param attribType the attribute type
     * @param value the attribute value
     */
    public XmlCollectionAttribute(XmlCollectionResource resource, XmlCollectionAttributeType attribType, String value) {
        m_resource = resource;
        m_attribType = attribType;
        m_value = value;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getAttributeType()
     */
    public CollectionAttributeType getAttributeType() {
        return m_attribType;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getName()
     */
    public String getName() {
        return m_attribType.getName();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getNumericValue()
     */
    public String getNumericValue() {
        try {
            Double d = Double.parseDouble(m_value); // This covers negative and scientific notation numbers.
            return d.toString();
        } catch (Exception e) {
            log().debug("getNumericValue: the value " + m_value + " is not a valid number. Removing invalid characters and try again.");
            try {
                Double d = Double.parseDouble(m_value.replaceAll("[^-\\d.]+", ""));  // Removing Units to return only a numeric value.
                return d.toString();
            } catch (Exception ex) {
                log().warn("getNumericValue: the value " + m_value + " is not parsable as a valid numeric value.");
            }
        }
        return "U"; // Ignoring value from RRDtool/JRobin point of view.
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getResource()
     */
    public CollectionResource getResource() {
        return m_resource;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#getStringValue()
     */
    public String getStringValue() {
        return m_value;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AbstractCollectionAttribute#shouldPersist(org.opennms.netmgt.config.collector.ServiceParameters)
     */
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionAttribute#getType()
     */
    public String getType() {
        return m_attribType.getType();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "XmlCollectionAttribute " + getName() + "=" + getStringValue();
    }

    /**
     * Log.
     *
     * @return the thread category
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
