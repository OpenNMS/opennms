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


import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NumericAttributeType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NumericAttributeType extends SnmpAttributeType {
    private static final Logger LOG = LoggerFactory.getLogger(NumericAttributeType.class);
    
    private static String[] s_supportedTypes = new String[] { "counter", "gauge", "timeticks", "integer", "octetstring" };
    
    /**
     * <p>supportsType</p>
     *
     * @param rawType a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean supportsType(String rawType) {
        String type = rawType.toLowerCase();
        for (int i = 0; i < s_supportedTypes.length; i++) {
            String supportedType = s_supportedTypes[i];
            if (type.startsWith(supportedType))
                return true;
        }
        return false;
    }



    static final String DST_COUNTER = "COUNTER";
    /**
     * <p>Constructor for NumericAttributeType.</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param collectionName a {@link java.lang.String} object.
     * @param mibObj a {@link org.opennms.netmgt.config.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.config.collector.AttributeGroupType} object.
     */
    public NumericAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(resourceType, collectionName, mibObj, groupType);
        
            // Assign the data source object identifier and instance
            LOG.debug("buildDataSourceList: ds_name: {} ds_oid: {}.{}", getName(), getOid(), getInstance());
            
            String alias = getAlias();
            if (alias.length() > PersistOperationBuilder.MAX_DS_NAME_LENGTH) {
                logNameTooLong();
            }


    }
    
    /** {@inheritDoc} */
    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistNumericAttribute(attribute);
    }

    void logNameTooLong() {
        LOG.warn("buildDataSourceList: Mib object name/alias '{}' exceeds 19 char maximum for RRD data source names, truncating.", getAlias());
   }
}
