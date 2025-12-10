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

import com.google.common.base.Strings;
import com.jayway.jsonpath.*;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonPropertyExtender implements SnmpPropertyExtender {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpPropertyExtender.class);
    public static final String SOURCE_ATTRIBUTE = "source-attribute";
    public static final String TARGET_TYPE = "target-type";
    public static final String DEFAULT_VALUE = "default-value";
    public static final String JSON_PATH = "json-path";
    private static final Configuration JSON_PATH_CONFIG = Configuration.builder()
            .options(Option.AS_PATH_LIST)
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();

    @Override
    public SnmpAttribute getTargetAttribute(final List<CollectionAttribute> sourceAttributes, final SnmpCollectionResource targetResource, final MibObjProperty property) {
        final String sourceAttribute = property.getParameterValue(SOURCE_ATTRIBUTE);
        final String targetType = property.getParameterValue(TARGET_TYPE, AttributeType.STRING.getName());
        final AttributeType targetAttributeType = AttributeType.parse(targetType);
        final String defaultValue = property.getParameterValue(DEFAULT_VALUE);
        final String jsonPathExpression = property.getParameterValue(JSON_PATH);

        if (Strings.isNullOrEmpty(sourceAttribute)) {
            LOG.warn("Cannot execute the Json property extender because: missing parameter {}", SOURCE_ATTRIBUTE);
            return null;
        }

        if (Strings.isNullOrEmpty(jsonPathExpression)) {
            LOG.warn("Cannot execute the Json property extender because: missing parameter {}", JSON_PATH);
            return null;
        }

        if (targetAttributeType == null) {
            LOG.warn("Cannot execute the Json property extender because: error parsing target-type {}", targetType);
            return null;
        }

        for (final AttributeGroup group : targetResource.getGroups()) {
            for (final CollectionAttribute attribute : group.getAttributes()) {
                if (sourceAttribute.equals(attribute.getName())) {
                    final JsonPath jsonPath;
                    try {
                        jsonPath = JsonPath.compile(jsonPathExpression);
                    } catch (InvalidPathException e) {
                        LOG.warn("Cannot execute the Json property extender because of invalid Json path expression {}", targetType);
                        return null;
                    }

                    final List<String> entries = JsonPath.using(JSON_PATH_CONFIG).parse(attribute.getStringValue()).read(jsonPath);

                    final String result;

                    if (entries.isEmpty()) {
                        if (defaultValue != null) {
                            result = defaultValue;
                        } else {
                            return null;
                        }
                    } else {
                        result = JsonPath.parse(attribute.getStringValue()).read(jsonPath).toString();
                    }

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