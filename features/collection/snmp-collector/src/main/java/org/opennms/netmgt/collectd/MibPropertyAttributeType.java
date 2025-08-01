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
import org.opennms.netmgt.config.datacollection.MibObjProperty;

/**
 * The Class MibPropertyAttributeType.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MibPropertyAttributeType extends SnmpAttributeType {

    /** The MIB object property. */
    private MibObjProperty property;

    /**
     * Supports type.
     *
     * @param rawType the raw type
     * @return true, if successful
     */
    public static boolean supportsType(String rawType) {
        return rawType.toLowerCase().startsWith("string");
    }

    /**
     * Instantiates a new MIB property attribute type.
     *
     * @param property the property
     * @param groupType the group type
     */
    public MibPropertyAttributeType(ResourceType resourceType, MibObjProperty property, AttributeGroupType groupType) {
        super(resourceType, null, null, groupType);
        this.property = property;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAttributeType#storeAttribute(org.opennms.netmgt.collection.api.CollectionAttribute, org.opennms.netmgt.collection.api.Persister)
     */
    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistStringAttribute(attribute);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAttributeType#getType()
     */
    @Override
    public AttributeType getType() {
        return AttributeType.STRING;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpAttributeType#getAlias()
     */
    @Override
    public String getAlias() {
        return property.getAlias();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpAttributeType#getOid()
     */
    @Override
    public String getOid() {
        return "property:" + property.getAlias();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAttributeType#getName()
     */
    @Override
    public String getName() {
        return property.getAlias();
    }

}
