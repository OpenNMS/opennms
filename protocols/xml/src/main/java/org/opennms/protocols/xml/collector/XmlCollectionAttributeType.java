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

import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.protocols.xml.config.XmlObject;

/**
 * The Class XmlCollectionAttributeType.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectionAttributeType implements CollectionAttributeType {

    /** The associated XML Object. */
    private XmlObject m_object;

    /** The Attribute Group Type. */
    private AttributeGroupType m_groupType;

    /**
     * Instantiates a new XML collection attribute type.
     *
     * @param object the XML object
     * @param groupType the group type
     */
    public XmlCollectionAttributeType(XmlObject object, AttributeGroupType groupType) {
        m_groupType = groupType;
        m_object = object;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionAttributeType#getGroupType()
     */
    public AttributeGroupType getGroupType() {
        return m_groupType;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionAttributeType#storeAttribute(org.opennms.netmgt.config.collector.CollectionAttribute, org.opennms.netmgt.config.collector.Persister)
     */
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        if (m_object.getDataType().equalsIgnoreCase("string")) {
            persister.persistStringAttribute(attribute);
        } else {
            persister.persistNumericAttribute(attribute);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.AttributeDefinition#getName()
     */
    public String getName() {
        return m_object.getName();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.AttributeDefinition#getType()
     */
    public String getType() {
        return m_object.getDataType();
    }
}
