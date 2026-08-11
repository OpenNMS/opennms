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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Notifications;
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
 * <li><b>GET/POST/PUT/DELETE /notification-config/event-notifications[/{name}]</b> event notification CRUD (notifications.xml)</li>
 * <li><b>PUT /notification-config/event-notifications/{name}/status</b> toggle one event notification</li>
 * <li><b>GET /notification-config/destination-paths</b> all destination paths</li>
 * <li><b>GET /notification-config/destination-paths/{name}</b> one destination path</li>
 * </ul>
 */
@Component("notificationConfigRestService")
@Path("notification-config")
@Tag(name = "Notification-config", description = "Notification Configuration API")
public class NotificationConfigRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationConfigRestService.class);

    private static final int RULE_PREVIEW_LIMIT = 250;

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
    @Path("event-notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public Notifications getEventNotifications(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read event notifications");
        readLock();
        try {
            final Notifications notifications = new Notifications();
            final List<Notification> list = new ArrayList<>(getNotificationFactory().getNotifications().values());
            list.sort(Comparator.comparing(Notification::getName, String.CASE_INSENSITIVE_ORDER));
            notifications.setNotifications(list);
            return notifications;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read event notifications: {}", e.getMessage());
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("event-notifications/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Notification getEventNotification(@Context final SecurityContext securityContext, @PathParam("name") final String name) {
        assertAdmin(securityContext, "read event notifications");
        readLock();
        try {
            final Notification notification = getNotificationFactory().getNotification(name);
            if (notification == null) {
                throw getException(Status.NOT_FOUND, "Event notification {} was not found.", name);
            }
            return notification;
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read event notification {}: {}", name, e.getMessage());
        } finally {
            readUnlock();
        }
    }

    @POST
    @Path("event-notifications")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addEventNotification(@Context final SecurityContext securityContext, final Notification notification) {
        assertAdmin(securityContext, "add event notifications");
        validateEventNotification(notification);
        writeLock();
        try {
            final boolean exists = getNotificationFactory().getNotification(notification.getName()) != null;
            if (exists) {
                throw getException(Status.BAD_REQUEST, "Event notification {} already exists.", notification.getName());
            }
            getNotificationFactory().addNotification(notification);
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't add event notification {}: {}", notification.getName(), e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("event-notifications/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateEventNotification(@Context final SecurityContext securityContext, @PathParam("name") final String name, final Notification notification) {
        assertAdmin(securityContext, "update event notifications");
        validateEventNotification(notification);
        writeLock();
        try {
            if (getNotificationFactory().getNotification(name) == null) {
                throw getException(Status.NOT_FOUND, "Event notification {} was not found.", name);
            }
            if (!name.equals(notification.getName()) && getNotificationFactory().getNotification(notification.getName()) != null) {
                throw getException(Status.BAD_REQUEST, "Event notification {} already exists.", notification.getName());
            }
            getNotificationFactory().replaceNotification(name, notification);
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't update event notification {}: {}", name, e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("event-notifications/{name}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateEventNotificationStatus(@Context final SecurityContext securityContext, @PathParam("name") final String name, final NotificationStatus status) {
        assertAdmin(securityContext, "toggle event notifications");
        if (status == null || !("on".equals(status.getStatus()) || "off".equals(status.getStatus()))) {
            throw getException(Status.BAD_REQUEST, "Status must be 'on' or 'off'");
        }
        writeLock();
        try {
            if (getNotificationFactory().getNotification(name) == null) {
                throw getException(Status.NOT_FOUND, "Event notification {} was not found.", name);
            }
            getNotificationFactory().updateStatus(name, status.getStatus());
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't update status of event notification {}: {}", name, e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("event-notifications/{name}")
    public Response deleteEventNotification(@Context final SecurityContext securityContext, @PathParam("name") final String name) {
        assertAdmin(securityContext, "delete event notifications");
        writeLock();
        try {
            if (getNotificationFactory().getNotification(name) == null) {
                throw getException(Status.NOT_FOUND, "Event notification {} was not found.", name);
            }
            // notifications.xsd requires at least one notification: removing
            // the last one empties memory, then the schema-validated save
            // throws, and the divergence survives until a restart
            if (getNotificationFactory().getNotifications().size() <= 1) {
                throw getException(Status.BAD_REQUEST,
                        "The last event notification cannot be deleted; turn it off instead.");
            }
            getNotificationFactory().removeNotification(name);
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't delete event notification {}: {}", name, e.getMessage());
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
        // Build and send entirely inside the try: the status change has already
        // been persisted by the caller, so nothing here — including
        // request.getRemoteHost(), which can trigger a slow/failing reverse DNS
        // lookup — may propagate and turn a committed change into a 500.
        try {
            // same parameters the legacy UpdateNotifdStatusServlet put on the event
            final EventBuilder bldr = new EventBuilder(uei, "ReST");
            bldr.addParam("remoteUser", securityContext.getUserPrincipal().getName());
            bldr.addParam("remoteHost", request.getRemoteHost());
            bldr.addParam("remoteAddr", request.getRemoteAddr());
            m_eventProxy.send(bldr.getEvent());
        } catch (final Exception e) {
            LOG.warn("Can't send event {}", uei, e);
        }
    }

    private static NotifdConfigFactory getNotifdConfigFactory() throws Exception {
        NotifdConfigFactory.init();
        return NotifdConfigFactory.getInstance();
    }

    private static NotificationFactory getNotificationFactory() throws Exception {
        // NotificationFactory's constructor requires an initialized NotifdConfigFactory
        NotifdConfigFactory.init();
        NotificationFactory.init();
        return NotificationFactory.getInstance();
    }

    @GET
    @Path("services")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getServiceNames(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read the service list");
        try {
            final List<String> services = new ArrayList<>(getNotificationFactory().getServiceNames());
            services.sort(String.CASE_INSENSITIVE_ORDER);
            return services;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read the service list: {}", e.getMessage());
        }
    }

    @POST
    @Path("rule/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RuleValidationDTO validateRule(@Context final SecurityContext securityContext, final RuleRequestDTO request) {
        assertAdmin(securityContext, "validate a notification rule");
        final RuleValidationDTO result = new RuleValidationDTO();
        final String rule = request == null ? null : request.getRule();
        if (rule == null || rule.trim().isEmpty()) {
            result.setValid(false);
            result.setError("A rule is required");
            return result;
        }
        final FilterDao filterDao = FilterDaoFactory.getInstance();
        try {
            filterDao.validateRule(rule);
        } catch (final FilterParseException e) {
            // A malformed rule is user input; report it as an invalid result.
            // Infrastructure failures propagate to a 500 instead.
            result.setValid(false);
            result.setError(e.getMessage());
            return result;
        }
        result.setValid(true);
        // The match preview materializes a Map over every interface/service the
        // rule selects, so only build it when the caller explicitly asks. The
        // save path leaves preview false and pays only for validateRule above.
        if (request == null || !request.isPreview()) {
            return result;
        }
        try {
            final Map<InetAddress, Set<String>> map = filterDao.getIPAddressServiceMap(rule);
            result.setMatchCount(map.size());
            final List<RuleMatchDTO> matches = new ArrayList<>();
            for (final Map.Entry<InetAddress, Set<String>> entry : map.entrySet()) {
                if (matches.size() >= RULE_PREVIEW_LIMIT) {
                    break;
                }
                final RuleMatchDTO match = new RuleMatchDTO();
                match.setIpAddress(InetAddressUtils.str(entry.getKey()));
                final List<String> svcs = new ArrayList<>(entry.getValue());
                svcs.sort(String.CASE_INSENSITIVE_ORDER);
                match.setServices(svcs);
                matches.add(match);
            }
            result.setMatches(matches);
        } catch (final Exception e) {
            // The rule parsed but the address/service preview failed; it is still valid to save.
            result.setError("Rule is valid, but the match preview could not be built: " + e.getMessage());
        }
        return result;
    }

    @XmlRootElement(name = "rule-request")
    public static class RuleRequestDTO {
        private String m_rule;
        private boolean m_preview;

        @XmlElement(name = "rule")
        public String getRule() {
            return m_rule;
        }

        public void setRule(final String rule) {
            m_rule = rule;
        }

        // When false (the default, used by the save path), the endpoint only
        // validates the rule; the caller must opt in to the match preview, which
        // materializes the full interface/service map.
        @XmlElement(name = "preview")
        public boolean isPreview() {
            return m_preview;
        }

        public void setPreview(final boolean preview) {
            m_preview = preview;
        }
    }

    @XmlRootElement(name = "rule-validation")
    public static class RuleValidationDTO {
        private boolean m_valid;
        private String m_error;
        private int m_matchCount;
        private List<RuleMatchDTO> m_matches = new ArrayList<>();

        @XmlElement(name = "valid")
        public boolean isValid() {
            return m_valid;
        }

        public void setValid(final boolean valid) {
            m_valid = valid;
        }

        @XmlElement(name = "error")
        public String getError() {
            return m_error;
        }

        public void setError(final String error) {
            m_error = error;
        }

        @XmlElement(name = "matchCount")
        public int getMatchCount() {
            return m_matchCount;
        }

        public void setMatchCount(final int matchCount) {
            m_matchCount = matchCount;
        }

        @XmlElement(name = "matches")
        public List<RuleMatchDTO> getMatches() {
            return m_matches;
        }

        public void setMatches(final List<RuleMatchDTO> matches) {
            m_matches = matches;
        }
    }

    @XmlRootElement(name = "rule-match")
    public static class RuleMatchDTO {
        private String m_ipAddress;
        private List<String> m_services = new ArrayList<>();

        @XmlElement(name = "ipAddress")
        public String getIpAddress() {
            return m_ipAddress;
        }

        public void setIpAddress(final String ipAddress) {
            m_ipAddress = ipAddress;
        }

        @XmlElement(name = "services")
        public List<String> getServices() {
            return m_services;
        }

        public void setServices(final List<String> services) {
            m_services = services;
        }
    }

    /**
     * Rejects payloads the factories would die on halfway: addNotification
     * removes any existing entry before saving, and replaceNotification
     * mutates the live entry in place through setters that assert non-empty
     * (name, status, uei, rule, destinationPath, text-message, parameters) —
     * a mid-chain failure leaves a half-updated entry in memory that the next
     * unrelated save persists. Every asserting setter must be covered here.
     */
    private static void validateEventNotification(final Notification notification) {
        if (notification == null || isBlank(notification.getName())) {
            throw getException(Status.BAD_REQUEST, "The event notification and its name are required");
        }
        if (!"on".equals(notification.getStatus()) && !"off".equals(notification.getStatus())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a status of 'on' or 'off'");
        }
        if (isBlank(notification.getUei())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a uei");
        }
        if (notification.getRule() == null || isBlank(notification.getRule().getContent())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a rule");
        }
        try {
            FilterDaoFactory.getInstance().validateRule(notification.getRule().getContent());
        } catch (final FilterParseException e) {
            throw getException(Status.BAD_REQUEST, "The rule is not a valid filter: {}", e.getMessage());
        }
        // Infrastructure failures (uninitialized factory, DB outage) are not the
        // caller's fault; let them propagate to a 500 rather than a 400.
        if (isBlank(notification.getDestinationPath())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a destinationPath");
        }
        if (isBlank(notification.getTextMessage())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a text-message");
        }
        for (final org.opennms.netmgt.config.notifications.Parameter parameter : notification.getParameters()) {
            if (isBlank(parameter.getName()) || isBlank(parameter.getValue())) {
                throw getException(Status.BAD_REQUEST, "Every parameter requires a non-empty name and value");
            }
        }
        final org.opennms.netmgt.config.notifications.Varbind varbind = notification.getVarbind();
        if (varbind != null && (isBlank(varbind.getVbname()) || isBlank(varbind.getVbvalue()))) {
            throw getException(Status.BAD_REQUEST, "A varbind requires both a name and a value");
        }
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private static DestinationPathFactory getDestinationPathFactory() throws Exception {
        DestinationPathFactory.init();
        return DestinationPathFactory.getInstance();
    }
}
