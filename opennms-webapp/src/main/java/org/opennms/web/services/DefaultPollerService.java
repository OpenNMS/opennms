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

package org.opennms.web.services;


import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.events.EventBuilder;
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
        @Override
	public void poll(OnmsMonitoredService monSvc, int pollResultId) {
		
		EventBuilder bldr = new EventBuilder(EventConstants.DEMAND_POLL_SERVICE_EVENT_UEI, "PollerService");

		bldr.setNodeid(monSvc.getNodeId());
		bldr.setInterface(monSvc.getIpAddress());
		bldr.setIfIndex(monSvc.getIfIndex());
		bldr.setService(monSvc.getServiceType().getName());
		
		bldr.addParam(EventConstants.PARM_DEMAND_POLL_ID, pollResultId);

		sendEvent(bldr.getEvent());
	}

	private void sendEvent(Event demandPollEvent) {
		try {
			m_eventProxy.send(demandPollEvent);
		} catch (EventProxyException e) {
			throw new ServiceException("Exception occurred sending demandPollEvent", e);
		}
	}

}
