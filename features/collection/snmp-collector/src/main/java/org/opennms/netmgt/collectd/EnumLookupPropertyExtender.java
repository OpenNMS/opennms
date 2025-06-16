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
 * The Class EnumLookupPropertyExtender.
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
