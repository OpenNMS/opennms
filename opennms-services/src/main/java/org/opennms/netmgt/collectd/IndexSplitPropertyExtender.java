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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class IndexSplitPropertyExtender implements SnmpPropertyExtender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(IndexSplitPropertyExtender.class);

    /** The Constant INDEX_PATTERN. */
    private static final String INDEX_PATTERN = "index-pattern";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpPropertyExtender#getTargetAttribute(java.util.List, org.opennms.netmgt.collectd.SnmpCollectionResource, org.opennms.netmgt.config.datacollection.MibObjProperty)
     */
    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        final String indexPattern = property.getParameterValue(INDEX_PATTERN);
        if (StringUtils.isBlank(indexPattern)) {
            LOG.warn("Cannot execute the Index Split property extender because: missing parameter {}", INDEX_PATTERN);
            return null;
        }

        Pattern p = Pattern.compile(indexPattern);
        Matcher m = p.matcher(targetResource.getInstance());
        if (m.find()) {
            final String index = m.group(1);
            AttributeGroupType groupType = targetResource.getGroupType(property.getGroupName());
            if (groupType != null) {
                MibPropertyAttributeType type = new MibPropertyAttributeType(targetResource.getResourceType(), property, groupType);
                SnmpValue value = SnmpUtils.getValueFactory().getOctetString(index.getBytes());
                return new SnmpAttribute(targetResource, type, value);
            }
        }

        return null;
    }

}
