/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collectd.AliasedResource;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>CollectorThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class CollectorThresholdingSet extends ThresholdingSet {
    private static final Logger LOG = LoggerFactory.getLogger(CollectorThresholdingSet.class);

    private final ResourceStorageDao m_resourceStorageDao;

    // CollectionSpecification parameters
    boolean storeByIfAlias = false;
    boolean counterReset = false;
    ServiceParameters svcParams;

    /**
     * <p>Constructor for CollectorThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     * @param svcParams a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @throws ThresholdInitializationException 
     */
    public CollectorThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository, ServiceParameters svcParams, ResourceStorageDao resourceStorageDao) throws ThresholdInitializationException {
        super(nodeId, hostAddress, serviceName, repository);
        m_resourceStorageDao = resourceStorageDao;
        String storeByIfAliasString = svcParams.getStoreByIfAlias();
        storeByIfAlias = storeByIfAliasString != null && "true".equalsIgnoreCase(storeByIfAliasString);
        this.svcParams = svcParams;
    }

    public void setCounterReset(boolean counterReset) {
        this.counterReset = counterReset;
    }

    /*
     * Returns true if the specified attribute is involved in any of defined thresholds for node/address/service
     */
    /**
     * <p>hasThresholds</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collection.api.CollectionAttribute} object.
     * @return a boolean.
     */
    public boolean hasThresholds(CollectionAttribute attribute) {
        CollectionResource resource = attribute.getResource();
        if (!isCollectionEnabled(attribute.getResource()))
            return false;
        if (resource instanceof AliasedResource && !storeByIfAlias)
            return false;
        return hasThresholds(resource.getResourceTypeName(), attribute.getName());
    }

    /*
     * Apply thresholds definitions for specified resource using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    /** {@inheritDoc} */
    public List<Event> applyThresholds(CollectionResource resource, Map<String, CollectionAttribute> attributesMap, Date collectionTimestamp) {
        if (!isCollectionEnabled(resource)) {
            LOG.debug("applyThresholds: Ignoring resource {} because data collection is disabled for this resource.", resource);
            return new LinkedList<>();
        }
		CollectionResourceWrapper resourceWrapper = new CollectionResourceWrapper(
				collectionTimestamp, m_nodeId, m_hostAddress, m_serviceName,
				m_repository, resource, attributesMap, m_resourceStorageDao);
		resourceWrapper.setCounterReset(counterReset);
        return applyThresholds(resourceWrapper, attributesMap);
    }

    /*
     * Check Valid Interface Resource based on suggestions from Bug 2711
     */
    /** {@inheritDoc} */
    @Override
    protected boolean passedThresholdFilters(CollectionResourceWrapper resource, ThresholdEntity thresholdEntity) {
        if (resource.isAnInterfaceResource() && !resource.isValidInterfaceResource()) {
            LOG.info("passedThresholdFilters: Could not get data interface information for '{}' or this interface has an invalid ifIndex.  Not evaluating threshold.", resource.getIfLabel());
            return false;
        }
        return super.passedThresholdFilters(resource, thresholdEntity);
    }
    
    protected boolean isCollectionEnabled(CollectionResource resource) {
        return resource.shouldPersist(svcParams);
    }

}
