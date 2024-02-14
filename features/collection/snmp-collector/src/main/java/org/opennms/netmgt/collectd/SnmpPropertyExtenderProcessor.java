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
import java.util.stream.Collectors;

import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collectd.SnmpCollectionSet;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.MibObjProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SnmpPropertyExtenderProcessor.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpPropertyExtenderProcessor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SnmpPropertyExtenderProcessor.class);

    /** The data collection configuration DAO. */
    private DataCollectionConfigDao m_dataCollectionConfigDao;

    /**
     * Gets the data collection configuration DAO.
     *
     * @return the data collection configuration DAO
     */
    private DataCollectionConfigDao getDataCollectionConfigDao() {
        if (m_dataCollectionConfigDao == null) {
            setDataCollectionConfigDao(DataCollectionConfigFactory.getInstance());
        }
        return m_dataCollectionConfigDao;
    }

    /**
     * Sets the data collection configuration DAO.
     *
     * @param config the new data collection configuration DAO
     */
    public void setDataCollectionConfigDao(DataCollectionConfigDao config) {
        m_dataCollectionConfigDao = config;
    }

    /**
     * Process.
     *
     * @param collectionSet the collection set
     * @param collectionName the collection name
     */
    public void process(final SnmpCollectionSet collectionSet, final String collectionName, final String sysObjectId, final String ipAddress) {
        final List<MibObjProperty> mibObjProperties = getDataCollectionConfigDao().getMibObjProperties(collectionName, sysObjectId, ipAddress);
        if (mibObjProperties.isEmpty()) {
            LOG.debug("process: there are no custom MibObj properties defined for sysObjectID {} in collection {}", collectionName, sysObjectId);
        } else {
            LOG.debug("process: analyzing properties for sysObjectID {} in collection {}: {}", sysObjectId, collectionName, mibObjProperties);
            // Retrieve all the string attributes
            List<CollectionAttribute> stringAttributes = collectionSet.getResources().stream()
                    .flatMap(r -> ((SnmpCollectionResource) r).getStringAttributes().stream())
                    .collect(Collectors.toList());
            // Apply MIB Properties
            collectionSet.getResources().forEach(r -> {
                mibObjProperties.forEach(p -> {
                    // Property extenders should be able to index by ifIndex similar to how other mibObjs do. See
                    // NMS-15342.
                    String resourceName = r.getResourceTypeName();
                    if (resourceName.equals(CollectionResource.RESOURCE_TYPE_IF)) {
                        resourceName = "ifIndex";
                    }
                    if (p.getInstance().equals(resourceName)) {
                        updateCollectionResource(stringAttributes, (SnmpCollectionResource) r, p);
                    }
                });
            });
        }
    }

    /**
     * Update collection resource.
     *
     * @param sourceAttributes the source attributes
     * @param targetResource the target resource
     * @param property the MIB object property
     */
    private void updateCollectionResource(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        try {
            String className = property.getClassName();
            if (className == null) {
                className = org.opennms.netmgt.collectd.RegExPropertyExtender.class.getName();
            }
            Class<?> clazz = Class.forName(className);
            SnmpPropertyExtender extender = (SnmpPropertyExtender) clazz.newInstance();
            SnmpAttribute targetAttribute = extender.getTargetAttribute(sourceAttributes, targetResource, property);
            if (targetAttribute != null) {
                LOG.debug("updateCollectionResource: adding property {} to resource {} with value {}", targetAttribute.getName(), targetResource, targetAttribute.getStringValue());
                targetResource.setAttributeValue((SnmpAttributeType) targetAttribute.getAttributeType(), targetAttribute.getValue());
            } else {
                LOG.debug("updateCollectionResource: no match for resource {} (instance={}, class-name={})", targetResource, property.getInstance(), clazz.getSimpleName());
            }
        } catch (Exception e) {
            LOG.error("Cannot update collection resource {}", targetResource, e);
        }
    }

}
