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

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.config.MibObject;

/**
 * <p>HexStringAttributeType class.</p>
 *
 * @author jwhite
 */
public class HexStringAttributeType extends SnmpAttributeType {
    /**
     * <p>supportsType</p>
     *
     * @param rawType a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean supportsType(String rawType) {
        return rawType.toLowerCase().startsWith("hexstring");
    }

    /**
     * <p>Constructor for HexStringAttributeType.</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param collectionName a {@link java.lang.String} object.
     * @param mibObj a {@link org.opennms.netmgt.config.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     */
    public HexStringAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(resourceType, collectionName, mibObj, groupType);
    }

    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        CollectionAttribute attributeToPersist = attribute;
        if (attribute instanceof SnmpAttribute) {
            // When storing SNMP attributes alter the getStringValue() value method
            // so that the hex string is returned instead of the display string
            attributeToPersist = new SnmpAttributeWrapper((SnmpAttribute)attribute);
        }
        persister.persistStringAttribute(attributeToPersist);
    }

    /**
     * Used to alter the behavior the getStringValue() value method.
     */
    private static class SnmpAttributeWrapper extends SnmpAttribute {
        private final SnmpAttribute m_attribute;

        public SnmpAttributeWrapper(SnmpAttribute attribute) {
            super(attribute.getResource(), (SnmpAttributeType)attribute.getAttributeType(), attribute.getValue());
            m_attribute = attribute;
        }

        @Override
        public String getStringValue() {
            return m_attribute.getValue().toHexString();
        }
    }
}
