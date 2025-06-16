/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    private static final String[] s_numericTypes = new String[] { "counter", "gauge", "timeticks", "integer", "octetstring", "opaque" };

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
