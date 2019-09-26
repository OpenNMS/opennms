/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import static org.opennms.netmgt.events.api.EventConstants.PARAM_TOPOLOGY_NAMESPACE;

import java.util.List;

import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:matt@opennms.org">Matt Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
@EventListener(name="enlinkd")
public final class EventProcessor {

    private EnhancedLinkd m_linkd;

    /**
     * @param linkd the linkd to set
     */
    public void setLinkd(EnhancedLinkd linkd) {
        this.m_linkd = linkd;
    }

    public EnhancedLinkd getLinkd() {
        return m_linkd;
    }

    /**
     * Handle a Node Deleted Event
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAdded(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        m_linkd.addNode(event.getNodeid().intValue());
    }

    /**
     * Handle a Node Deleted Event
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeleted(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        m_linkd.deleteNode(event.getNodeid().intValue());
    }

    /**
     * Handle a Node Gained Service Event if service is SNMP
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);
        EventUtils.checkService(event);

        if (event.getService().equals("SNMP"))
        	m_linkd.scheduleNodeCollection(event.getNodeid().intValue());
    }

    /**
     * Handle a Node Lost Service Event when service lost is SNMP
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void handleNodeLostService(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);
        EventUtils.checkService(event);

        if (event.getService().equals("SNMP"))
        	m_linkd.suspendNodeCollection(event.getNodeid().intValue());
    }

    /**
     * Handle a Node Regained Service Event where service is SNMP
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void handleRegainedService(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);
        EventUtils.checkService(event);

        if (event.getService().equals("SNMP"))
        	m_linkd.wakeUpNodeCollection(event.getNodeid().intValue());
    }
    
    /**
     * <p>handleForceRescan</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.FORCE_RESCAN_EVENT_UEI)
    public void handleForceRescan(Event e) {
    	m_linkd.rescheduleNodeCollection(new Long(e.getNodeid()).intValue());
    }
    

    /**
     * <p>handleRealodDaemonconfig</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadDaemonConfig(Event e) {
        List<Parm> parmCollection = e.getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Enlinkd".equalsIgnoreCase(parm.getValue().getContent())) {
                m_linkd.reloadConfig();
                break;
            }
        }
    }
    
    @EventHandler(uei = EventConstants.RELOAD_TOPOLOGY_UEI)
    public void handleReloadTopology(Event e) {
        final String topologyNamespace = EventUtils.getParm(e, PARAM_TOPOLOGY_NAMESPACE);
        if (topologyNamespace == null || "all".equalsIgnoreCase(topologyNamespace) || OnmsTopology.TOPOLOGY_NAMESPACE_LINKD.equalsIgnoreCase(topologyNamespace)) {
            m_linkd.reloadTopology();
        }
    }

} // end class
