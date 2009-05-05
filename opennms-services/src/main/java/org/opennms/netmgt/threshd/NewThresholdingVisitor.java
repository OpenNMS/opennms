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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class NewThresholdingVisitor extends AbstractCollectionSetVisitor {
    
    private ThresholdingSet m_thresholdingSet;
    
    private Map<String, CollectionAttribute> m_attributesMap;
    
    /*
     * Is static because successful creation depends on thresholding-enabled.
     */
    public static NewThresholdingVisitor create(final int nodeId, final String hostAddress, final String serviceName, final RrdRepository repo, final Map<String,String> params) {
        Category log = ThreadCategory.getInstance(NewThresholdingVisitor.class);

        // Use the "thresholding-enable" to use Thresholds processing on Collectd
        String enabled = params.get("thresholding-enabled");
        if (enabled == null || !enabled.equals("true")) {
            log.info("createThresholdingVisitor: Thresholds processing is not enabled. Check thresholding-enabled param on collectd package");
            return null;
        }

        ThresholdingSet thresholdingSet = new ThresholdingSet(nodeId, hostAddress, serviceName, repo);
        if (thresholdingSet.hasThresholds()) {
            return new NewThresholdingVisitor(thresholdingSet);
        }

        log.warn("createThresholdingVisitor: Can't create ThresholdingVisitor for " + hostAddress + "/" + serviceName);
        return null;
    }

    protected NewThresholdingVisitor(ThresholdingSet thresholdingSet) {
        m_thresholdingSet = thresholdingSet;
    }
    
    /*
     * Force reload thresholds configuration, and merge threshold states
     */
    public void reload() {
        m_thresholdingSet.reinitialize();
    }
    
    
    @Override
    public void visitResource(CollectionResource resource) {
        // Re-initialize attributes Map.
        m_attributesMap = new HashMap<String, CollectionAttribute>();
    }        

    /*
     * Must be used to hold all attributes needed because CollectionResource does not have a connection to their attributes.
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
                log().debug("visitAttribute; storing value "+ value +" for attribute named " + name);
            }
        }
    }

    @Override
    public void completeResource(CollectionResource resource) {
        List<Event> eventList = m_thresholdingSet.applyThresholds(resource, m_attributesMap);
        if (eventList.size() > 0) {
            Events events = new Events();
            for (Event event: eventList) {
                events.addEvent(event);
            }
            try {                
                Log eventLog = new Log();
                eventLog.setEvents(events);
                //Used to use a proxy for this, but the threshd implementation was  just a simple wrapper around the following call
                // (not even any other code).  Rather than try to get an Event Proxy into this class, it's easier to just call direct.
                EventIpcManagerFactory.getIpcManager().sendNow(eventLog);
            } catch (Exception e) {
                log().info("completeResource: Failed sending threshold events: " + e, e);
            }
        }
    }

    @Override
    public String toString() {
        return "ThresholdingVisitor for " + m_thresholdingSet;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
}
