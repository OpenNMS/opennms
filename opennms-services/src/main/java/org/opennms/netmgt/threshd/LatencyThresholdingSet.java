//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.threshd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.poller.LatencyCollectionAttribute;
import org.opennms.netmgt.poller.LatencyCollectionResource;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>LatencyThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyThresholdingSet extends ThresholdingSet {
    
    /**
     * <p>Constructor for LatencyThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     * @param interval a long.
     */
    public LatencyThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository, long interval) {
        super(nodeId, hostAddress, serviceName, repository, interval);
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
     * Apply thresholds definitions for specified service using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    /** {@inheritDoc} */
    public List<Event> applyThresholds(String svcName, Map<String, Double> attributes) {
        LatencyCollectionResource latencyResource = new LatencyCollectionResource(svcName, m_hostAddress);
        Map<String, CollectionAttribute> attributesMap = new HashMap<String, CollectionAttribute>();
        for (String ds : attributes.keySet()) {
            attributesMap.put(ds, new LatencyCollectionAttribute(latencyResource, ds, attributes.get(ds)));
        }
        CollectionResourceWrapper resourceWrapper = new CollectionResourceWrapper(m_interval, m_nodeId, m_hostAddress, m_serviceName, m_repository, latencyResource, attributesMap);
        return applyThresholds(resourceWrapper, attributesMap);
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
