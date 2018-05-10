/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.poller.LatencyCollectionAttribute;
import org.opennms.netmgt.poller.LatencyCollectionAttributeType;
import org.opennms.netmgt.poller.LatencyCollectionResource;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>LatencyThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyThresholdingSet extends ThresholdingSet {

    private final String m_location;

    private final ResourceStorageDao m_resourceStorageDao;

    /**
     * <p>Constructor for LatencyThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     * @param interval a long.
     * @throws ThresholdInitializationException 
     */
    public LatencyThresholdingSet(int nodeId, String hostAddress, String serviceName, String location, RrdRepository repository, ResourceStorageDao resourceStorageDao) throws ThresholdInitializationException {
        super(nodeId, hostAddress, serviceName, repository);
        m_resourceStorageDao = resourceStorageDao;
        m_location = location;
    }

    /*
     * Latency thresholds use ds-type="if"
     * Returns true if any attribute of the service is involved in any of defined thresholds.
     */
    /**
     * <p>hasThresholds</p>
     *
     * @param attributes a {@link java.util.Map} object.
     * @return a boolean.
     */
    public boolean hasThresholds(Map<String, Double> attributes) {
        if (hasThresholds()) {
            for (String ds : attributes.keySet())
                if (hasThresholds("if", ds))
                    return true;
        }
        return false;
    }

    /*
     * Apply thresholds definitions for specified service using attributesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    /** {@inheritDoc} */
    public List<Event> applyThresholds(String svcName, Map<String, Double> attributes) {
        LatencyCollectionResource latencyResource = new LatencyCollectionResource(svcName, m_hostAddress, m_location);
        LatencyCollectionAttributeType latencyType = new LatencyCollectionAttributeType();
        Map<String, CollectionAttribute> attributesMap = new HashMap<String, CollectionAttribute>();
        for (final Entry<String, Double> entry : attributes.entrySet()) {
            final String ds = entry.getKey();
            attributesMap.put(ds, new LatencyCollectionAttribute(latencyResource, latencyType, ds, entry.getValue()));
        }
        //The timestamp is irrelevant; latency is never a COUNTER (which is the only reason the date is used).  
        //Yes, we have to know a little too much about the implementation details of CollectionResourceWrapper to say that, but
        // we have little choice
        CollectionResourceWrapper resourceWrapper = new CollectionResourceWrapper(new Date(), m_nodeId, m_hostAddress, m_serviceName, m_repository, latencyResource, attributesMap, m_resourceStorageDao);
        return Collections.unmodifiableList(applyThresholds(resourceWrapper, attributesMap));
    }

    /*
     * Resource Filters don't make sense for Latency Thresholder.
     */
    /** {@inheritDoc} */
    @Override
    protected boolean passedThresholdFilters(CollectionResourceWrapper resource, ThresholdEntity thresholdEntity) {
        return true;
    }

}
