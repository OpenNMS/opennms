/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>ForceRescanState class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ForceRescanState {
    
    private CollectionAgent m_agent;
    private EventProxy m_eventProxy;
    
    private boolean m_forceRescanSent = false;

    /**
     * <p>Constructor for ForceRescanState.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param eventProxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public ForceRescanState(CollectionAgent agent, EventProxy eventProxy) {
        m_agent = agent;
        m_eventProxy = eventProxy;
    }
    
    /**
     * <p>getEventProxy</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public EventProxy getEventProxy() {
        return m_eventProxy;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>createForceResanEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createForceResanEvent() {
        // create the event to be sent
        EventBuilder bldr = new EventBuilder(EventConstants.FORCE_RESCAN_EVENT_UEI, "SnmpCollector");
        
        bldr.setNodeid(m_agent.getNodeId());

        bldr.setInterface(m_agent.getInetAddress());
        
        bldr.setService(SnmpCollector.SERVICE_NAME);
        
        bldr.setHost(InetAddressUtils.getLocalHostName());

        return bldr.getEvent();
    }

    /**
     * <p>getAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public CollectionAgent getAgent() {
        return m_agent;
    }

    /**
     * This method is responsible for building a Capsd forceRescan event object
     * and sending it out over the EventProxy.
     * @param eventProxy
     *            proxy over which an event may be sent to eventd
     * @param ifAddress
     *            interface address to which this event pertains
     * @param nodeId TODO
     */
    void sendForceRescanEvent() {
        // Log4j category
    	if (log().isDebugEnabled()) {
    		log().debug("generateForceRescanEvent: interface = " + getAgent().getHostAddress());
    	}
    
    	// Send event via EventProxy
    	try {
            getEventProxy().send(createForceResanEvent());
    	} catch (EventProxyException e) {
    		log().error("generateForceRescanEvent: Unable to send "
    				+ "forceRescan event.", e);
    	}
    }
    
    void rescanIndicated() {
        if (!m_forceRescanSent) {
            sendForceRescanEvent();
            m_forceRescanSent = true;
        }
    }

}
