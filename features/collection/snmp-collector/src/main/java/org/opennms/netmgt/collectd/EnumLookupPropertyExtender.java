/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class IndexSplitPropertyExtender.
 * 
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class EnumLookupPropertyExtender implements SnmpPropertyExtender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EnumLookupPropertyExtender.class);
    
    /** The Constant ENUM_ATTRIBUTE. */
    private static final String ENUM_ATTRIBUTE = "enum-attribute";
    
    /** The Constant DEFAULT_VALUE. */
    private static final String DEFAULT_VALUE = "default-value";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpPropertyExtender#getTargetAttribute(java.util.List, org.opennms.netmgt.collectd.SnmpCollectionResource, org.opennms.netmgt.config.datacollection.MibObjProperty)
     */
    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        final String defaultValue = property.getParameterValue(DEFAULT_VALUE);
        
        final String enumAttribute = property.getParameterValue(ENUM_ATTRIBUTE);

        if (StringUtils.isBlank(enumAttribute)) {
            LOG.warn("Cannot execute the enum-lookup property extender because: missing parameter {}", ENUM_ATTRIBUTE);
            return null;
        }

        for (AttributeGroup group : targetResource.getGroups()) {
            for (CollectionAttribute attribute : group.getAttributes()) {
                if (enumAttribute.equals(attribute.getName())) {
                    final String result = property.getParameterValue(attribute.getStringValue(), defaultValue);
                    if (result == null) {
                        return null;
                    }
                    AttributeGroupType groupType = targetResource.getGroupType(property.getGroupName());
                    MibPropertyAttributeType type = new MibPropertyAttributeType(targetResource.getResourceType(), property, groupType);
                    SnmpValue value = SnmpUtils.getValueFactory().getOctetString(result.getBytes());
                    return new SnmpAttribute(targetResource, type, value); 
                }
            }
        }

        return null;
    }

}
