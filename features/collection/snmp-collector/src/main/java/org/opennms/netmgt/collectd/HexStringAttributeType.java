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
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.config.datacollection.MibObject;

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
     * @param mibObj a {@link org.opennms.netmgt.config.datacollection.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     */
    public HexStringAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(resourceType, collectionName, mibObj, groupType);
    }

    @Override
    public AttributeType getType() {
        return AttributeType.STRING;
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
