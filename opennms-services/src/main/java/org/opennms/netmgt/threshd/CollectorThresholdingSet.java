/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.AliasedResource;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.model.RrdRepository;
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

    // CollectionSpecification parameters
    boolean storeByIfAlias = false;
    boolean storeByForeignSource = false;
    
    /**
     * <p>Constructor for CollectorThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public CollectorThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository, Map<String, Object> roProps) {
        super(nodeId, hostAddress, serviceName, repository);
        String storeByIfAliasString = ParameterMap.getKeyedString(roProps, "storeByIfAlias", null);
        storeByIfAlias = storeByIfAliasString != null && storeByIfAliasString.toLowerCase().equals("true");
        storeByForeignSource = isStoreByForeignSource();
        LOG.debug("storeByForeignSource = {}", storeByForeignSource);
    }
    
    public static boolean isStoreByForeignSource() {
       return Boolean.getBoolean("org.opennms.rrd.storeByForeignSource");
    }
    
    /*
     * Returns true if the specified attribute is involved in any of defined thresholds for node/address/service
     */
    /**
     * <p>hasThresholds</p>
     *
     * @param attribute a {@link org.opennms.netmgt.config.collector.CollectionAttribute} object.
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
            return new LinkedList<Event>();
        }
		CollectionResourceWrapper resourceWrapper = new CollectionResourceWrapper(
				collectionTimestamp, m_nodeId, m_hostAddress, m_serviceName,
				m_repository, resource, attributesMap);
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
        if (resource instanceof IfInfo) {
            return ((IfInfo) resource).isScheduledForCollection();
        }
        return true;
    }

}
