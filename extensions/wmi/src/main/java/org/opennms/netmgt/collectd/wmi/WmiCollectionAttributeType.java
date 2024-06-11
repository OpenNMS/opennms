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
package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.config.wmi.Attrib;

public class WmiCollectionAttributeType extends AbstractCollectionAttributeType {
        Attrib m_attribute;

        public WmiCollectionAttributeType(final Attrib attribute, final AttributeGroupType groupType) {
            super(groupType);
            m_attribute = attribute;
        }

        @Override
        public void storeAttribute(final CollectionAttribute attribute, final Persister persister) {
            if (m_attribute.getType() == AttributeType.STRING) {
                persister.persistStringAttribute(attribute);
            } else {
                persister.persistNumericAttribute(attribute);
            }
        }

        @Override
        public String getName() {
            return m_attribute.getAlias();
        }

        @Override
        public AttributeType getType() {
            return m_attribute.getType();
        }

}
