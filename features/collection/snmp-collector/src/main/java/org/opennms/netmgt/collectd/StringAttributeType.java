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
 * <p>StringAttributeType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class StringAttributeType extends SnmpAttributeType {

    /**
     * <p>supportsType</p>
     *
     * @param rawType a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean supportsType(String rawType) {
        return rawType.toLowerCase().startsWith("string");
    }

    @Override
    public AttributeType getType() {
        return AttributeType.STRING;
    }

    /**
     * <p>Constructor for StringAttributeType.</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param collectionName a {@link java.lang.String} object.
     * @param mibObj a {@link org.opennms.netmgt.config.datacollection.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     */
    public StringAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(resourceType, collectionName, mibObj, groupType);
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistStringAttribute(attribute);
    }
}
