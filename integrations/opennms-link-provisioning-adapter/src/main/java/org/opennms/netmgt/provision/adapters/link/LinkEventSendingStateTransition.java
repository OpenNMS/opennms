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

/**
 * <p>LinkEventSendingStateTransition class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState.LinkStateTransition;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
public class LinkEventSendingStateTransition implements LinkStateTransition {

    private DataLinkInterface m_dataLinkInterface;
    private EventForwarder m_eventForwarder;
    private NodeLinkService m_nodeLinkService;

    /**
     * <p>Constructor for LinkEventSendingStateTransition.</p>
     *
     * @param dataLinkInterface a {@link org.opennms.netmgt.model.DataLinkInterface} object.
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param nodeLinkService a {@link org.opennms.netmgt.provision.adapters.link.NodeLinkService} object.
     */
    public LinkEventSendingStateTransition(DataLinkInterface dataLinkInterface, EventForwarder eventForwarder, NodeLinkService nodeLinkService) {
        m_dataLinkInterface = dataLinkInterface;
        m_eventForwarder = eventForwarder;
        m_nodeLinkService = nodeLinkService;
    }
    
    /**
     * <p>onLinkDown</p>
     */
    @Override
    public void onLinkDown() {
        sendDataLinkEvent(EventConstants.DATA_LINK_FAILED_EVENT_UEI);
    }

    private void sendDataLinkEvent(String uei) {
        String endPoint1 = m_dataLinkInterface.getNode().getLabel();
        String endPoint2 = m_nodeLinkService.getNodeLabel(m_dataLinkInterface.getNodeParentId());
        
        
        Event e = new EventBuilder(uei, "EventCorrelator")
            .addParam(EventConstants.PARM_ENDPOINT1, LinkProvisioningAdapter.min(endPoint1, endPoint2))
            .addParam(EventConstants.PARM_ENDPOINT2, LinkProvisioningAdapter.max(endPoint1, endPoint2))
            .getEvent();
        m_eventForwarder.sendNow(e);
    }

    /**
     * <p>onLinkUp</p>
     */
    @Override
    public void onLinkUp() {
        sendDataLinkEvent(EventConstants.DATA_LINK_RESTORED_EVENT_UEI);
    }

    /**
     * <p>onLinkUnknown</p>
     */
    @Override
    public void onLinkUnknown() {
        sendDataLinkEvent(EventConstants.DATA_LINK_UNMANAGED_EVENT_UEI);
    }
    
    
}
