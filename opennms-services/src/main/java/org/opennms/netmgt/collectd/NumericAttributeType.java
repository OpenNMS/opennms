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
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.NumericCollectionAttributeType;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NumericAttributeType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NumericAttributeType extends SnmpAttributeType implements NumericCollectionAttributeType {
    private static final Logger LOG = LoggerFactory.getLogger(NumericAttributeType.class);

    private final AttributeType m_type;

    private static final String[] s_numericTypes = new String[] { "counter", "gauge", "timeticks", "integer", "octetstring" };

    /**
     * <p>isNumericType</p>
     *
     * @param rawType a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isNumericType(String rawType) {
        String type = rawType.toLowerCase();
        for (int i = 0; i < s_numericTypes.length; i++) {
            String supportedType = s_numericTypes[i];
            if (type.startsWith(supportedType))
                return true;
        }
        return false;
    }

    public static boolean supportsType(String rawType) {
        return isNumericType(rawType);
    }

    /**
     * <p>Constructor for NumericAttributeType.</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param collectionName a {@link java.lang.String} object.
     * @param mibObj a {@link org.opennms.netmgt.config.datacollection.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     */
    public NumericAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(resourceType, collectionName, mibObj, groupType);

        if (mibObj.getType().toLowerCase().startsWith("counter")) {
            m_type = AttributeType.COUNTER;
        } else {
            m_type = AttributeType.GAUGE;
        }

        // Assign the data source object identifier and instance
        LOG.debug("buildDataSourceList: ds_name: {} ds_oid: {}.{}", getName(), getOid(), getInstance());
    }

    @Override
    public AttributeType getType() {
        return m_type;
    }

    @Override
    public String getMaxval() {
        return m_mibObj.getMaxval();
    }
    
    @Override
    public String getMinval() {
        return m_mibObj.getMinval();
    }
    
    /** {@inheritDoc} */
    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistNumericAttribute(attribute);
    }
}
