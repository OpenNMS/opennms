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

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class CollectorThresholdingSet extends ThresholdingSet {
    
    public CollectorThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository) {
        super(nodeId, hostAddress, serviceName, repository);
    }
    
    /*
     * Returns true if the specified attribute is involved in any of defined thresholds for node/address/service
     */
    public boolean hasThresholds(CollectionAttribute attribute) {
        CollectionResource resource = attribute.getResource();
        return hasThresholds(resource.getResourceTypeName(), attribute.getName());
    }

    /*
     * Apply thresholds definitions for specified resource using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    public List<Event> applyThresholds(CollectionResource resource, Map<String, CollectionAttribute> attributesMap) {
        CollectionResourceWrapper resourceWrapper = new CollectionResourceWrapper(m_nodeId, m_hostAddress, m_serviceName, m_repository, resource, attributesMap);
        return applyThresholds(resourceWrapper, attributesMap);
    }

    /*
     * Check Valid Interface Resource based on suggestions from Bug 2711
     */
    @Override
    protected boolean passedThresholdFilters(CollectionResourceWrapper resource, ThresholdEntity thresholdEntity) {
        if (resource.isAnInterfaceResource() && !resource.isValidInterfaceResource()) {
            log().info("passedThresholdFilters: Could not get data interface information for '" + resource.getIfLabel() + "' or this interface has an invalid ifIndex.  Not evaluating threshold.");
            return false;
        }
        return super.passedThresholdFilters(resource, thresholdEntity);
    }

}
