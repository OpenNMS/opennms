/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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
import java.util.stream.Collectors;

import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collectd.SnmpCollectionSet;
import org.opennms.netmgt.collection.api.CollectionAttribute;
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
                    if (p.getInstance().equals(r.getResourceTypeName())) {
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
            }
        } catch (Exception e) {
            LOG.error("Cannot update collection resource {}", targetResource, e);
        }
    }

}
