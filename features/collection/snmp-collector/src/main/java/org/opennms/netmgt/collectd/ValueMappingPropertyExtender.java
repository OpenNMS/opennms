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

import java.util.List;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ValueMappingPropertyExtender implements SnmpPropertyExtender {
    private static final Logger LOG = LoggerFactory.getLogger(ValueMappingPropertyExtender.class);
    private static final String SOURCE_ATTRIBUTE = "source-attribute";
    private static final String TARGET_TYPE = "target-type";
    private static final String DEFAULT_VALUE = "default-value";

    @Override
    public SnmpAttribute getTargetAttribute(final List<CollectionAttribute> sourceAttributes, final SnmpCollectionResource targetResource, final MibObjProperty property) {
        final String sourceAttribute = property.getParameterValue(SOURCE_ATTRIBUTE);
        final String targetType = property.getParameterValue(TARGET_TYPE, AttributeType.STRING.getName());
        final AttributeType targetAttributeType = AttributeType.parse(targetType);
        final String defaultValue = property.getParameterValue(DEFAULT_VALUE);

        if (Strings.isNullOrEmpty(sourceAttribute)) {
            LOG.warn("Cannot execute the value-mapping property extender because: missing parameter {}", SOURCE_ATTRIBUTE);
            return null;
        }

        if (targetAttributeType == null) {
            LOG.warn("Cannot execute the value-mapping property extender because: error parsing target-type {}", targetType);
            return null;
        }

        for (final AttributeGroup group : targetResource.getGroups()) {
            for (final CollectionAttribute attribute : group.getAttributes()) {
                if (sourceAttribute.equals(attribute.getName())) {
                    final String result = property.getParameterValue(attribute.getStringValue(), defaultValue);

                    if (result == null) {
                        return null;
                    }

                    final AttributeGroupType groupType = targetResource.getGroupType(property.getGroupName());

                    return new SnmpAttribute(
                            targetResource,
                            new ValueMappingAttributeType(targetResource.getResourceType(), property, groupType, targetAttributeType),
                            SnmpUtils.getValueFactory().getOctetString(result.getBytes())
                    );
                }
            }
        }
        return null;
    }
}
