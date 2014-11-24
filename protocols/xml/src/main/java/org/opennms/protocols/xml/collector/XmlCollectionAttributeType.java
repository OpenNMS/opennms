/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.protocols.xml.config.XmlObject;

/**
 * The Class XmlCollectionAttributeType.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectionAttributeType extends AbstractCollectionAttributeType {

    /** The associated XML Object. */
    private final XmlObject m_object;

    /**
     * Instantiates a new XML collection attribute type.
     *
     * @param object the XML object
     * @param groupType the group type
     */
    public XmlCollectionAttributeType(XmlObject object, AttributeGroupType groupType) {
        super(groupType);
        m_object = object;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionAttributeType#storeAttribute(org.opennms.netmgt.config.collector.CollectionAttribute, org.opennms.netmgt.config.collector.Persister)
     */
    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        if ("string".equalsIgnoreCase(m_object.getDataType())) {
            persister.persistStringAttribute(attribute);
        } else {
            persister.persistNumericAttribute(attribute);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.AttributeDefinition#getName()
     */
    @Override
    public String getName() {
        return m_object.getName();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.AttributeDefinition#getType()
     */
    @Override
    public String getType() {
        return m_object.getDataType();
    }
}
