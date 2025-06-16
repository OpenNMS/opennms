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

public class ValueMappingAttributeType extends SnmpAttributeType {
    public static class Wrapper extends SnmpAttribute {
        private Wrapper(final SnmpAttribute attribute) {
            super(attribute.getResource(), (SnmpAttributeType) attribute.getAttributeType(), attribute.getValue());
        }

        @Override
        public String getMetricIdentifier() {
            return null;
        }
    }

    private final MibObjProperty property;
    private final AttributeType attributeType;

    public ValueMappingAttributeType(final ResourceType resourceType, final MibObjProperty property, final AttributeGroupType groupType, final AttributeType attributeType) {
        super(resourceType, null, null, groupType);
        this.property = property;
        this.attributeType = attributeType;
    }

    @Override
    public void storeAttribute(final CollectionAttribute attribute, final Persister persister) {
        if (attributeType.isNumeric()) {
            persister.persistNumericAttribute(new Wrapper((SnmpAttribute) attribute));
        } else {
            persister.persistStringAttribute(new Wrapper((SnmpAttribute) attribute));
        }
    }

    @Override
    public AttributeType getType() {
        return attributeType;
    }

    @Override
    public String getAlias() {
        return property.getAlias();
    }

    @Override
    public String getOid() {
        return "property:" + property.getAlias();
    }

    @Override
    public String getName() {
        return property.getAlias();
    }
}
