//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.web.services;

import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>DefaultPollerService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultPollerService implements PollerService {
	
	private EventProxy m_eventProxy;
	
	/**
	 * <p>setEventProxy</p>
	 *
	 * @param eventProxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
	 */
	public void setEventProxy(EventProxy eventProxy) {
		m_eventProxy = eventProxy;
	}
	
	/** {@inheritDoc} */
	public void poll(OnmsMonitoredService monSvc, int pollResultId) {
		
		
		Event demandPollEvent = new Event();
		demandPollEvent.setUei(EventConstants.DEMAND_POLL_SERVICE_EVENT_UEI);
		demandPollEvent.setNodeid(monSvc.getNodeId());
		demandPollEvent.setInterface(monSvc.getIpAddress());
		demandPollEvent.setIfIndex(monSvc.getIfIndex());
		demandPollEvent.setService(monSvc.getServiceType().getName());
        demandPollEvent.setCreationTime(EventConstants.formatToString(new Date()));
        demandPollEvent.setTime(demandPollEvent.getCreationTime());
        demandPollEvent.setSource("PollerService");
		
		EventUtils.addParam(demandPollEvent, EventConstants.PARM_DEMAND_POLL_ID, pollResultId);

		sendEvent(demandPollEvent);
	}

	private void sendEvent(Event demandPollEvent) {
		try {
			m_eventProxy.send(demandPollEvent);
		} catch (EventProxyException e) {
			throw new ServiceException("Exception occurred sending demandPollEvent", e);
		}
	}

}
