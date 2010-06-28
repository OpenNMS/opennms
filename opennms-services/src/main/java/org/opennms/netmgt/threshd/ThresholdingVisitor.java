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
// Modifications:
//
// 2008 June 11: Correct logic error when checking for generic resource types; update author - jeffg@opennms.org
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

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;

/**
 * Implements CollectionSetVisitor to implement thresholding.  
 * Works by simply recording all the attributes that come in via visitAttribute 
 * into an internal data structure, per resource, and then on "completeResource", does 
 * threshold checking against that in memory structure.  
 *
 * Suggested usage is one per CollectableService; this object holds the current state of thresholds
 * for this interface/service combination 
 * (so perhaps needs a better name than ThresholdingVisitor)
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:craig@opennms.org">Craig Miskell</a>
 */
public class ThresholdingVisitor extends AbstractCollectionSetVisitor {
    
    /*
     * Holds thresholds configuration for a node/interface/service
     */
    CollectorThresholdingSet m_thresholdingSet;
    
    /*
     * Holds required attribute from CollectionResource to evaluate thresholds.
     */
    Map<String, CollectionAttribute> m_attributesMap;
    
    /*
     * Is static because successful creation depends on thresholding-enabled parameter.
     */
    public static ThresholdingVisitor create(int nodeId, String hostAddress, String serviceName, RrdRepository repo, Map<String,String> params, long interval) {
        ThreadCategory log = ThreadCategory.getInstance(ThresholdingVisitor.class);

        String enabled = params.get("thresholding-enabled");
        if (enabled != null && !"true".equals(enabled)) {
            log.info("create: Thresholds processing is not enabled. Check thresholding-enabled param on collectd package");
            return null;
        }

        CollectorThresholdingSet thresholdingSet = new CollectorThresholdingSet(nodeId, hostAddress, serviceName, repo, interval);
        if (thresholdingSet.hasThresholds()) {
            return new ThresholdingVisitor(thresholdingSet);
        }

        log.warn("create: Can't create ThresholdingVisitor for " + hostAddress + "/" + serviceName);
        return null;
    }

    /*
     * Static method create must be used to create's new ThresholdingVisitor instance
     */
    protected ThresholdingVisitor(CollectorThresholdingSet thresholdingSet) {
        m_thresholdingSet = thresholdingSet;
    }
    
    /*
     * Get a list of thresholds groups (for junit only at this time)
     */
    public List<ThresholdGroup> getThresholdGroups() {
        return m_thresholdingSet.m_thresholdGroups;
    }
    
    /*
     * Force reload thresholds configuration, and merge threshold states
     */
    public void reload() {
        m_thresholdingSet.reinitialize();
    }
    
    /*
     *  Initialize required attributes map (m_attributesMap)
     */
    @Override
    public void visitResource(CollectionResource resource) {
        m_attributesMap = new HashMap<String, CollectionAttribute>();
    }        

    /*
     * Add/Update required attributes for thresholds on m_attributeMap.
     * This is used because CollectionResource does not have direct reference to their attributes
     * (The way to get attribute is against AttributeGroup object contained on CollectioResource
     * implementations).
     */
    @Override    
    public void visitAttribute(CollectionAttribute attribute) {
        if (m_thresholdingSet.hasThresholds(attribute)) {
            String name = attribute.getName();
            m_attributesMap.put(name, attribute);
            if (log().isDebugEnabled()) {
                String value = attribute.getNumericValue();
                if (value == null)
                    value = attribute.getStringValue();
                log().debug("visitAttribute: storing value "+ value +" for attribute named " + name);
            }
        }
    }

    /*
     * Apply threshold for specific resource (and required attributes).
     * Send thresholds events (if exists)
     */
    @Override
    public void completeResource(CollectionResource resource) {
        List<Event> eventList = m_thresholdingSet.applyThresholds(resource, m_attributesMap);
        ThresholdingEventProxy proxy = ThresholdingEventProxyFactory.getFactory().getProxy();
        proxy.add(eventList);
        proxy.sendAllEvents();
    }

    @Override
    public String toString() {
        return "ThresholdingVisitor for " + m_thresholdingSet;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
}
