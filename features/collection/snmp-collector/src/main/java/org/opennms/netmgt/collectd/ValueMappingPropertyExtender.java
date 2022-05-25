/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
