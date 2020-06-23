/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.events.EventBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MonitoredServicesComponent {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoredServicesComponent.class);

    @Autowired
    private MonitoredServiceDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    public boolean hasStatusChanged(String previousStatus, OnmsMonitoredService targetObject) {
        boolean modified = false;
        final String status = targetObject.getStatus();
        LOG.debug("hasStatusChanged: previous status was {}, and new status is {}", previousStatus, status);
        if (status != null && previousStatus != null && !previousStatus.equals(status)) {
            modified = true;
            m_dao.update(targetObject);
            if ("S".equals(status) || ("A".equals(previousStatus) && "F".equals(status))) {
                LOG.debug("hasStatusChanged: suspending polling for service {} on node with IP {}", targetObject.getServiceName(), targetObject.getIpAddress().getHostAddress());
                sendEvent(EventConstants.SERVICE_UNMANAGED_EVENT_UEI, targetObject); // TODO ManageNodeServlet is sending this.
                sendEvent(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, targetObject);
            }
            if ("R".equals(status) || ("F".equals(previousStatus) && "A".equals(status))) {
                LOG.debug("hasStatusChanged: resuming polling for service {} on node with IP {}", targetObject.getServiceName(), targetObject.getIpAddress().getHostAddress());
                sendEvent(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, targetObject);
            }
        }
        return modified;
    }

    private void sendEvent(String eventUEI, OnmsMonitoredService dbObj) {
        final EventBuilder bldr = new EventBuilder(eventUEI, "ReST");
        bldr.setNodeid(dbObj.getNodeId());
        bldr.setInterface(dbObj.getIpAddress());
        bldr.setService(dbObj.getServiceName());
        try {
            m_eventProxy.send(bldr.getEvent());
        } catch (final EventProxyException e) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
        }
    }

}
