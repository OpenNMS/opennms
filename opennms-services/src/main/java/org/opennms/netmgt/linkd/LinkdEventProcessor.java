//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Oct 01: Add ability to update database when an interface is deleted. - ayres@opennms.org
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Jan 31: Cleaned up some unused imports.
// 2004 Sep 08: Completely reorganize to clean up the delete code.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.linkd;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:matt@opennms.org">Matt Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
@EventListener(name="OpenNMS.Linkd")
final class LinkdEventProcessor {

    private Linkd m_linkd;

    /**
     * @param linkd the linkd to set
     */
    public void setLinkd(Linkd linkd) {
        this.m_linkd = linkd;
    }

    public Linkd getLinkd() {
        return m_linkd;
    }

    /**
     * Handle a Node Deleted Event
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeleted(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        // Remove the deleted node from the scheduler if it's an SNMP node
        m_linkd.deleteNode((int)event.getNodeid());
        // set to status = D in all the rows in table
        // atinterface, iprouteinterface, datalinkinterface,stpnode, stpinterface
    }

    /**
     * Handle Interface Deleted Event
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.INTERFACE_DELETED_EVENT_UEI)
    public void handleInterfaceDeleted(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);
        EventUtils.checkInterfaceOrIfIndex(event);
        int ifIndex = -1;
        if(event.hasIfIndex()) {
            ifIndex = event.getIfIndex();
        }

        m_linkd.deleteInterface((int)event.getNodeid(), event.getInterface(), ifIndex);
        // set to status = D in all the rows in table
        // atinterface, iprouteinterface, datalinkinterface, stpinterface
    }

    /**
     * Handle a Node Gained Service Event if service is SNMP
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        m_linkd.scheduleNodeCollection((int)event.getNodeid());
    }

    /**
     * Handle a Node Lost Service Event when service lost is SNMP
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void handleNodeLostService(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        // Remove the deleted node from the scheduler
        m_linkd.suspendNodeCollection((int)event.getNodeid());
        // set to status = N in all the rows in table
        // atinterface, iprouteinterface, datalinkinterface,
    }

    /**
     * Handle a Node Regained Service Event where service is SNMP
     * 
     * @param event
     */
    @EventHandler(uei=EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void handleRegainedService(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        m_linkd.wakeUpNodeCollection((int)event.getNodeid());
    }
} // end class
