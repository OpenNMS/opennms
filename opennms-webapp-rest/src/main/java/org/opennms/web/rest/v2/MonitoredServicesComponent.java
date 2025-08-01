/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
