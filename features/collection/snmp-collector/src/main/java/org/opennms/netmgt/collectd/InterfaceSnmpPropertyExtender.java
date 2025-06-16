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
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class InterfaceSnmpPropertyExtender.
 * 
 * @author <a href="mailto:jeffg@opennms.com">Jeff Gehlbach</a> based on the work of
 * <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class InterfaceSnmpPropertyExtender implements SnmpPropertyExtender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceSnmpPropertyExtender.class);

    /** The Constant SOURCE_ATTRIBUTE. */
    private static final String SOURCE_ATTRIBUTE = "source-attribute";
    
    /** The Constant SOURCE_IFINDEX_ATTRIBUTE. */
    private static final String SOURCE_IFINDEX_ATTRIBUTE = "source-ifindex-attribute";

    /** The Constant INDEX_PATTERN. */
    private static final String IFINDEX_POINTER_COLUMN = "target-ifindex-pointer-column";

    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        if (! StringUtils.isBlank(property.getParameterValue("source-type"))) {
            LOG.warn("Hint: The interfaceSnmp property extender does not take a source-type parameter. Ignoring the one provided.");
        }

        final String sourceAttribute = property.getParameterValue(SOURCE_ATTRIBUTE);
        if (StringUtils.isBlank(sourceAttribute)) {
            LOG.warn("Cannot execute the interfaceSnmp property extender because: missing parameter {}", SOURCE_ATTRIBUTE);
            return null;
        }

        final String ifIndexPointerColumn = property.getParameterValue(IFINDEX_POINTER_COLUMN);
        if (StringUtils.isBlank(ifIndexPointerColumn)) {
            LOG.warn("Cannot execute the interfaceSnmp property extender because: missing parameter {}", IFINDEX_POINTER_COLUMN);
            return null;
        }
        
        final String ifIndexSourceColumn = property.getParameterValue(SOURCE_IFINDEX_ATTRIBUTE, "ifIndex");
        

        String targetIfIndex = null;
        Optional<CollectionAttribute> target = null;

        for(AttributeGroup group:targetResource.getGroups()) {
            for (CollectionAttribute attribute : group.getAttributes()) {
                try {
                    if (ifIndexPointerColumn.equals(attribute.getName())) {
                        targetIfIndex = attribute.getStringValue();
                    }
                } catch (Exception e) {
                    LOG.error("Error: " + e, e);
                }
            }
        }
        
        if (targetIfIndex == null) {
            LOG.warn("Could not identify ifIndex-equivalent pointer column {} on target resource {}", ifIndexPointerColumn, targetResource);
            return null;
        }
        
        CollectionResource sourceResource = null;
        final String targetIfIndexStr = targetIfIndex;
        for (CollectionAttribute srcAttr:sourceAttributes) {
            try {
                if (ifIndexSourceColumn.equals(srcAttr.getName()) && targetIfIndexStr.equals(srcAttr.getStringValue())) {
                    sourceResource = srcAttr.getResource();
                }
            } catch (Exception e) {
                LOG.error("Error: " + e, e);
            }
        }

        if (sourceResource != null) {
            final CollectionResource srcRsrc = sourceResource;
            target = sourceAttributes.stream().filter(a -> matches(sourceAttribute, srcRsrc, a)).findFirst();
        }

        if (target != null && target.isPresent()) {
            AttributeGroupType groupType = targetResource.getGroupType(property.getGroupName());
            if (groupType != null) {
                MibPropertyAttributeType type = new MibPropertyAttributeType(targetResource.getResourceType(), property, groupType);
                SnmpValue value = SnmpUtils.getValueFactory().getOctetString(target.get().getStringValue().getBytes());
                return new SnmpAttribute(targetResource, type, value);
            }
        }

        return null;
    }

    /**
     * Matches.
     *
     * @param sourceAttribute the source alias
     * @param sourceResource the source resource representing the row we want 
     * @param a the collection attribute to check
     * @return true, if successful
     */
    private boolean matches(final String sourceAttribute, final CollectionResource sourceResource, final CollectionAttribute a) {
        final CollectionResource r = a.getResource();
        return a.getName().equals(sourceAttribute) && "if".equals(r.getResourceTypeName()) && r.equals(sourceResource);
    }
}
