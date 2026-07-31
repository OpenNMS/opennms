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
package org.opennms.web.rest.v1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST access to the notifd on/off status and the read side of the
 * destination path configuration, delegating to the same file-backed config
 * factories the legacy servlets use — notifd-configuration.xml and
 * destinationPaths.xml remain the system of record and stay fully editable
 * by hand.
 *
 * <ul>
 * <li><b>GET /notification-config/status</b> global notifd on/off status</li>
 * <li><b>PUT /notification-config/status</b> turn notifd on or off</li>
 * <li><b>GET /notification-config/destination-paths</b> all destination paths</li>
 * <li><b>GET /notification-config/destination-paths/{name}</b> one destination path</li>
 * </ul>
 */
@Component("notificationConfigRestService")
@Path("notification-config")
@Tag(name = "Notification-config", description = "Notification Configuration API")
public class NotificationConfigRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationConfigRestService.class);

    @Autowired
    @Qualifier("eventProxy")
    protected EventProxy m_eventProxy;

    @XmlRootElement(name = "notification-status")
    public static class NotificationStatus {
        private String m_status;

        public NotificationStatus() {
        }

        public NotificationStatus(final String status) {
            m_status = status;
        }

        @XmlAttribute(name = "status")
        public String getStatus() {
            return m_status;
        }

        public void setStatus(final String status) {
            m_status = status;
        }
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public NotificationStatus getStatus(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read the notification status");
        try {
            return new NotificationStatus(getNotifdConfigFactory().getNotificationStatus());
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read notifd status: {}", e.getMessage());
        }
    }

    @PUT
    @Path("status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setStatus(@Context final SecurityContext securityContext, @Context final HttpServletRequest request, final NotificationStatus status) {
        assertAdmin(securityContext, "change the notification status");
        if (status == null || !("on".equals(status.getStatus()) || "off".equals(status.getStatus()))) {
            throw getException(Status.BAD_REQUEST, "Status must be 'on' or 'off'");
        }
        writeLock();
        try {
            LOG.info("Setting notifd status to {} for user {}", status.getStatus(), securityContext.getUserPrincipal().getName());
            if ("on".equals(status.getStatus())) {
                getNotifdConfigFactory().turnNotifdOn();
                sendStatusEvent("uei.opennms.org/internal/notificationsTurnedOn", securityContext, request);
            } else {
                getNotifdConfigFactory().turnNotifdOff();
                sendStatusEvent("uei.opennms.org/internal/notificationsTurnedOff", securityContext, request);
            }
            return Response.noContent().build();
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't update notifd status: {}", e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @GET
    @Path("destination-paths")
    @Produces(MediaType.APPLICATION_JSON)
    public DestinationPaths getDestinationPaths(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read destination paths");
        readLock();
        try {
            final DestinationPaths paths = new DestinationPaths();
            final List<org.opennms.netmgt.config.destinationPaths.Path> list = new ArrayList<>(getDestinationPathFactory().getPaths().values());
            list.sort(Comparator.comparing(org.opennms.netmgt.config.destinationPaths.Path::getName, String.CASE_INSENSITIVE_ORDER));
            paths.setPaths(list);
            return paths;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read destination paths: {}", e.getMessage());
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("destination-paths/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public org.opennms.netmgt.config.destinationPaths.Path getDestinationPath(@Context final SecurityContext securityContext, @PathParam("name") final String name) {
        assertAdmin(securityContext, "read destination paths");
        readLock();
        try {
            final org.opennms.netmgt.config.destinationPaths.Path path = getDestinationPathFactory().getPath(name);
            if (path == null) {
                throw getException(Status.NOT_FOUND, "Destination path {} was not found.", name);
            }
            return path;
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read destination path {}: {}", name, e.getMessage());
        } finally {
            readUnlock();
        }
    }

    private static void assertAdmin(final SecurityContext securityContext, final String operation) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw getException(Status.FORBIDDEN, "User {} does not have access to {}.", securityContext.getUserPrincipal().getName(), operation);
        }
    }

    private void sendStatusEvent(final String uei, final SecurityContext securityContext, final HttpServletRequest request) {
        // same parameters the legacy UpdateNotifdStatusServlet put on the event
        final EventBuilder bldr = new EventBuilder(uei, "ReST");
        bldr.addParam("remoteUser", securityContext.getUserPrincipal().getName());
        bldr.addParam("remoteHost", request.getRemoteHost());
        bldr.addParam("remoteAddr", request.getRemoteAddr());
        try {
            m_eventProxy.send(bldr.getEvent());
        } catch (final Exception e) {
            LOG.warn("Can't send event {}", uei, e);
        }
    }

    private static NotifdConfigFactory getNotifdConfigFactory() throws Exception {
        NotifdConfigFactory.init();
        return NotifdConfigFactory.getInstance();
    }

    private static DestinationPathFactory getDestinationPathFactory() throws Exception {
        DestinationPathFactory.init();
        return DestinationPathFactory.getInstance();
    }


}
