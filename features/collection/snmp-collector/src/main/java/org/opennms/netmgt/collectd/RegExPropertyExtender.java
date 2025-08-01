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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RegExPropertyExtender.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class RegExPropertyExtender implements SnmpPropertyExtender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(RegExPropertyExtender.class);

    /** The Constant SOURCE_TYPE. */
    private static final String SOURCE_TYPE = "source-type";

    /** The Constant SOURCE_ALIAS. */
    private static final String SOURCE_ALIAS = "source-alias";

    /** The Constant INDEX_PATTERN. */
    private static final String INDEX_PATTERN = "index-pattern";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpPropertyExtender#getTargetAttribute(java.util.List, org.opennms.netmgt.collectd.SnmpCollectionResource, org.opennms.netmgt.config.datacollection.MibObjProperty)
     */
    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        final String sourceType = property.getParameterValue(SOURCE_TYPE);
        if (StringUtils.isBlank(sourceType)) {
            LOG.warn("Cannot execute the RegEx property extender because: missing parameter {}", SOURCE_TYPE);
            return null;
        }

        final String sourceAlias = property.getParameterValue(SOURCE_ALIAS);
        if (StringUtils.isBlank(sourceAlias)) {
            LOG.warn("Cannot execute the RegEx property extender because: missing parameter {}", SOURCE_ALIAS);
            return null;
        }

        final String indexPattern = property.getParameterValue(INDEX_PATTERN);
        if (StringUtils.isBlank(indexPattern)) {
            LOG.warn("Cannot execute the RegEx property extender because: missing parameter {}", INDEX_PATTERN);
            return null;
        }

        Pattern p = Pattern.compile(indexPattern);
        Matcher m = p.matcher(targetResource.getInstance());
        Optional<CollectionAttribute> target = null;
        if (m.find()) {
            final String index = m.group(1);
            target = sourceAttributes.stream().filter(a -> matches(sourceType, sourceAlias, index, a)).findFirst();
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
