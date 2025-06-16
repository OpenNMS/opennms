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
 * The Class PointerLikeIndexPropertyExtender.
 * 
 * @author <a href="mailto:jeffg@opennms.com">Jeff Gehlbach</a> based on the work of
 * <a href="mailto:agalue@opennms.org">Alejandro Galue</a> and Jean-Marie Kubek
 */
public class PointerLikeIndexPropertyExtender implements SnmpPropertyExtender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(PointerLikeIndexPropertyExtender.class);

    /** The Constant SOURCE_TYPE. */
    private static final String SOURCE_TYPE = "source-type";
    
    /** The Constant SOURCE_ATTRIBUTE. */
    private static final String SOURCE_ATTRIBUTE = "source-attribute";
    
    /** The Constant TARGET_INDEX_POINTER_COLUMN. */
    private static final String TARGET_INDEX_POINTER_COLUMN = "target-index-pointer-column";

    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        final String sourceType = property.getParameterValue(SOURCE_TYPE);
        if (StringUtils.isBlank(sourceType)) {
            LOG.warn("Cannot execute the pointer-like-index property extender because: missing parameter {}", SOURCE_TYPE);
            return null;
        }

        final String sourceAttribute = property.getParameterValue(SOURCE_ATTRIBUTE);
        if (StringUtils.isBlank(sourceAttribute)) {
            LOG.warn("Cannot execute the pointer-like-index property extender because: missing parameter {}", SOURCE_ATTRIBUTE);
            return null;
        }

        final String targetIndexPointerColumn = property.getParameterValue(TARGET_INDEX_POINTER_COLUMN);
        if (StringUtils.isBlank(targetIndexPointerColumn)) {
            LOG.warn("Cannot execute the pointer-like-index property extender because: missing parameter {}", TARGET_INDEX_POINTER_COLUMN);
            return null;
        }
        
        String pointerLikeIndexValue = null;
        Optional<CollectionAttribute> pointedToAttribute = null;

        for(AttributeGroup group:targetResource.getGroups()) {
            for (CollectionAttribute attribute : group.getAttributes()) {
                try {
                    if (targetIndexPointerColumn.equals(attribute.getName())) {
                        pointerLikeIndexValue = attribute.getStringValue();
                    }
                } catch (Exception e) {
                    LOG.error("Error: " + e, e);
                }
            }
        }
        
        if (pointerLikeIndexValue == null) {
            LOG.warn("Could not identify pointer-like-index column {} on target resource {}", targetIndexPointerColumn, targetResource);
            return null;
        }

        final String desiredIndex = pointerLikeIndexValue;
        pointedToAttribute = sourceAttributes.stream().filter(a -> matches(sourceType, sourceAttribute, desiredIndex, a)).findFirst();

        if (pointedToAttribute != null && pointedToAttribute.isPresent()) {
            AttributeGroupType groupType = targetResource.getGroupType(property.getGroupName());
            if (groupType != null) {
                MibPropertyAttributeType type = new MibPropertyAttributeType(targetResource.getResourceType(), property, groupType);
                SnmpValue value = SnmpUtils.getValueFactory().getOctetString(pointedToAttribute.get().getStringValue().getBytes());
                return new SnmpAttribute(targetResource, type, value);
            }
        }

        return null;
    }
    
    /**
     * Matches.
     *
     * @param sourceType the source type
     * @param sourceAlias the source alias
     * @param index the resource index to check
     * @param a the collection attribute to check
     * @return true, if successful
     */
    private boolean matches(final String sourceType, final String sourceAlias, final String index, final CollectionAttribute a) {
        final CollectionResource r = a.getResource();
        return a.getName().equals(sourceAlias) && r.getResourceTypeName().equals(sourceType) && r.getInstance().equals(index);
    }
}
