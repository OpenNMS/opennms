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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotificationCommandFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsPathOutage;
import org.opennms.netmgt.model.events.EventBuilder;
import org.springframework.dao.DataAccessException;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;

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
 * <li><b>GET/POST/PUT/DELETE /notification-config/destination-paths[/{name}]</b> destination path CRUD (destinationPaths.xml)</li>
 * <li><b>GET /notification-config/commands</b> notification commands (notificationCommands.xml, read-only)</li>
 * <li><b>GET /notification-config/on-call-roles</b> on-call role names (groups.xml, read-only)</li>
 * <li><b>GET/POST/DELETE /notification-config/path-outages</b> critical paths (pathoutage table)</li>
 * </ul>
 */
@Component("notificationConfigRestService")
@Path("notification-config")
@Tag(name = "Notification-config", description = "Notification Configuration API")
public class NotificationConfigRestService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationConfigRestService.class);

    private static final int RULE_PREVIEW_LIMIT = 250;

    @Autowired
    protected EventProxy eventProxy;

    // Per-instance read/write/update lock serializing config mutations, carried
    // over from the base REST service this was split out of when it moved to v2.
    private final ReadWriteUpdateLock globalLock = new ReentrantReadWriteUpdateLock();
    private final Lock readLock = globalLock.updateLock();
    private final Lock writeLock = globalLock.writeLock();

    private void readLock() {
        readLock.lock();
    }

    private void readUnlock() {
        readLock.unlock();
    }

    private void writeLock() {
        writeLock.lock();
    }

    private void writeUnlock() {
        writeLock.unlock();
    }

    private static WebApplicationException getException(final Status status, final String msg, final String... params) {
        final String formatted = params != null ? MessageFormatter.arrayFormat(msg, params).getMessage() : msg;
        LOG.error(formatted);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(formatted).build());
    }

    private static WebApplicationException getException(final Status status, final Throwable t) {
        LOG.error(t.getMessage(), t);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
    }

    @Autowired
    private PathOutageDao pathOutageDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SessionUtils sessionUtils;

    // Flush/clear the Hibernate session every N rows during a bulk path-outage
    // apply so a rule that matches many nodes doesn't accumulate the whole batch
    // in the persistence context.
    private static final int PATH_OUTAGE_BATCH = 200;

    public static class NotificationStatus {
        private String status;

        public NotificationStatus() {
        }

        public NotificationStatus(final String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
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
            assertDestinationPathExists(notification.getDestinationPath());
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
            assertDestinationPathExists(notification.getDestinationPath());
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

    @POST
    @Path("destination-paths")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDestinationPath(@Context final SecurityContext securityContext, final org.opennms.netmgt.config.destinationPaths.Path path) {
        assertAdmin(securityContext, "add destination paths");
        validateDestinationPath(path);
        writeLock();
        try {
            if (getDestinationPathFactory().getPath(path.getName()) != null) {
                throw getException(Status.BAD_REQUEST, "Destination path {} already exists.", path.getName());
            }
            validateDestinationPathReferences(path);
            getDestinationPathFactory().addPath(path);
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            reloadDestinationPathsQuietly();
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't add destination path {}: {}", path.getName(), e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("destination-paths/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDestinationPath(@Context final SecurityContext securityContext, @PathParam("name") final String name, final org.opennms.netmgt.config.destinationPaths.Path path) {
        assertAdmin(securityContext, "update destination paths");
        validateDestinationPath(path);
        writeLock();
        try {
            if (getDestinationPathFactory().getPath(name) == null) {
                throw getException(Status.NOT_FOUND, "Destination path {} was not found.", name);
            }
            final boolean renamed = !name.equals(path.getName());
            if (renamed && getDestinationPathFactory().getPath(path.getName()) != null) {
                throw getException(Status.BAD_REQUEST, "Destination path {} already exists.", path.getName());
            }
            validateDestinationPathReferences(path);
            final org.opennms.netmgt.config.destinationPaths.Path oldPath = getDestinationPathFactory().getPath(name);
            getDestinationPathFactory().replacePath(name, path);
            if (renamed) {
                // keep event notifications pointing at the renamed path
                final List<Notification> touched = new ArrayList<>();
                for (final Notification n : getNotificationFactory().getNotifications().values()) {
                    if (name.equals(n.getDestinationPath())) {
                        n.setDestinationPath(path.getName());
                        touched.add(n);
                    }
                }
                if (!touched.isEmpty()) {
                    try {
                        getNotificationFactory().saveCurrent();
                    } catch (final Exception notifSaveError) {
                        // The path rename already persisted; undo it and revert the
                        // in-memory notifications so destinationPaths.xml and
                        // notifications.xml stay consistent instead of leaving
                        // notifications pointing at a name that no longer exists.
                        try {
                            getDestinationPathFactory().replacePath(path.getName(), oldPath);
                        } catch (final Exception rollbackError) {
                            notifSaveError.addSuppressed(rollbackError);
                        }
                        for (final Notification n : touched) {
                            n.setDestinationPath(name);
                        }
                        throw notifSaveError;
                    }
                }
            }
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            reloadDestinationPathsQuietly();
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't update destination path {}: {}", name, e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("destination-paths/{name}")
    public Response deleteDestinationPath(@Context final SecurityContext securityContext, @PathParam("name") final String name) {
        assertAdmin(securityContext, "delete destination paths");
        writeLock();
        try {
            if (getDestinationPathFactory().getPath(name) == null) {
                throw getException(Status.NOT_FOUND, "Destination path {} was not found.", name);
            }
            // destinationPaths.xsd requires at least one path, so removing the
            // last one would fail schema validation on marshal (a raw 500).
            // Reject it up front with an explanation instead.
            if (getDestinationPathFactory().getPaths().size() <= 1) {
                throw getException(Status.BAD_REQUEST, "Destination path {} is the only one configured; at least one must remain.", name);
            }
            // deleting a path that notifications still reference would leave them
            // pointing at a name that no longer exists (rename cascades; delete
            // must refuse — the asymmetric case)
            final List<String> referencing = new ArrayList<>();
            for (final Notification n : getNotificationFactory().getNotifications().values()) {
                if (name.equals(n.getDestinationPath())) {
                    referencing.add(n.getName());
                }
            }
            if (!referencing.isEmpty()) {
                throw getException(Status.CONFLICT, "Destination path {} is used by event notification(s): {}. Repoint or delete them first.", name, String.join(", ", referencing));
            }
            getDestinationPathFactory().removePath(name);
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            reloadDestinationPathsQuietly();
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't delete destination path {}: {}", name, e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @GET
    @Path("commands")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Command> getCommands(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read notification commands");
        readLock();
        try {
            final List<Command> commands = new ArrayList<>(getNotificationCommandFactory().getCommands().values());
            commands.sort(Comparator.comparing(Command::getName, String.CASE_INSENSITIVE_ORDER));
            return commands;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read notification commands: {}", e.getMessage());
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("on-call-roles")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getOnCallRoles(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read on-call roles");
        readLock();
        try {
            final GroupManager manager = getGroupManager();
            // getRoleNames() reads cached state without checking the file
            manager.update();
            final List<String> roles = new ArrayList<>(java.util.Arrays.asList(manager.getRoleNames()));
            roles.sort(String.CASE_INSENSITIVE_ORDER);
            return roles;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read on-call roles: {}", e.getMessage());
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
            eventProxy.send(bldr.getEvent());
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
        readLock();
        try {
            final List<String> services = new ArrayList<>(getNotificationFactory().getServiceNames());
            services.sort(String.CASE_INSENSITIVE_ORDER);
            return services;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read the service list: {}", e.getMessage());
        } finally {
            readUnlock();
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

    public static class RuleRequestDTO {
        private String rule;
        private boolean preview;

        public String getRule() {
            return rule;
        }

        public void setRule(final String rule) {
            this.rule = rule;
        }

        // When false (the default, used by the save path), the endpoint only
        // validates the rule; the caller must opt in to the match preview, which
        // materializes the full interface/service map.
        public boolean isPreview() {
            return preview;
        }

        public void setPreview(final boolean preview) {
            this.preview = preview;
        }
    }

    public static class RuleValidationDTO {
        private boolean valid;
        private String error;
        private int matchCount;
        private List<RuleMatchDTO> matches = new ArrayList<>();

        public boolean isValid() {
            return valid;
        }

        public void setValid(final boolean valid) {
            this.valid = valid;
        }

        public String getError() {
            return error;
        }

        public void setError(final String error) {
            this.error = error;
        }

        public int getMatchCount() {
            return matchCount;
        }

        public void setMatchCount(final int matchCount) {
            this.matchCount = matchCount;
        }

        public List<RuleMatchDTO> getMatches() {
            return matches;
        }

        public void setMatches(final List<RuleMatchDTO> matches) {
            this.matches = matches;
        }
    }

    public static class RuleMatchDTO {
        private String ipAddress;
        private List<String> services = new ArrayList<>();

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(final String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public List<String> getServices() {
            return services;
        }

        public void setServices(final List<String> services) {
            this.services = services;
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
        if (notification == null || StringUtils.isBlank(notification.getName())) {
            throw getException(Status.BAD_REQUEST, "The event notification and its name are required");
        }
        if (!"on".equals(notification.getStatus()) && !"off".equals(notification.getStatus())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a status of 'on' or 'off'");
        }
        if (StringUtils.isBlank(notification.getUei())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a uei");
        }
        if (notification.getRule() == null || StringUtils.isBlank(notification.getRule().getContent())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a rule");
        }
        try {
            FilterDaoFactory.getInstance().validateRule(notification.getRule().getContent());
        } catch (final FilterParseException e) {
            throw getException(Status.BAD_REQUEST, "The rule is not a valid filter: {}", e.getMessage());
        }
        // Infrastructure failures (uninitialized factory, DB outage) are not the
        // caller's fault; let them propagate to a 500 rather than a 400.
        if (StringUtils.isBlank(notification.getDestinationPath())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a destinationPath");
        }
        if (StringUtils.isBlank(notification.getTextMessage())) {
            throw getException(Status.BAD_REQUEST, "The event notification requires a text-message");
        }
        for (final org.opennms.netmgt.config.notifications.Parameter parameter : notification.getParameters()) {
            if (StringUtils.isBlank(parameter.getName()) || StringUtils.isBlank(parameter.getValue())) {
                throw getException(Status.BAD_REQUEST, "Every parameter requires a non-empty name and value");
            }
        }
        final org.opennms.netmgt.config.notifications.Varbind varbind = notification.getVarbind();
        if (varbind != null && (StringUtils.isBlank(varbind.getVbname()) || StringUtils.isBlank(varbind.getVbvalue()))) {
            throw getException(Status.BAD_REQUEST, "A varbind requires both a name and a value");
        }
        // notifications.xsd leaves event-severity a free string, but notifd
        // matches it against event severities; anything outside the canonical
        // names can never match and would fail silently at delivery time.
        final String severity = notification.getEventSeverity().orElse(null);
        if (severity != null && VALID_EVENT_SEVERITIES.stream().noneMatch(s -> s.equalsIgnoreCase(severity))) {
            throw getException(Status.BAD_REQUEST, "Unknown event severity {}; valid values are {}", severity, String.join(", ", VALID_EVENT_SEVERITIES));
        }
    }

    private static final List<String> VALID_EVENT_SEVERITIES = java.util.Arrays.asList(
            "Indeterminate", "Cleared", "Normal", "Warning", "Minor", "Major", "Critical");

    // The legacy wizard populated destination paths by dropdown; the API
    // equivalent is rejecting a reference to a path that does not exist, which
    // notifd would otherwise fail on silently at delivery time.
    private static void assertDestinationPathExists(final String pathName) throws Exception {
        if (getDestinationPathFactory().getPath(pathName) == null) {
            throw getException(Status.BAD_REQUEST, "destinationPath {} does not exist.", pathName);
        }
    }

    // Target names must resolve to a user, group, or on-call role (a name with
    // '@' is an email target, which notifd accepts without lookup), and every
    // command must exist in notificationCommands.xml — mirroring the dropdowns
    // the legacy wizard constrained these with.
    private static void validateDestinationPathReferences(final org.opennms.netmgt.config.destinationPaths.Path path) throws Exception {
        final java.util.Set<String> known = new java.util.HashSet<>();
        UserFactory.init();
        known.addAll(UserFactory.getInstance().getUserNames());
        known.addAll(getGroupManager().getGroupNames());
        known.addAll(java.util.Arrays.asList(getGroupManager().getRoleNames()));

        final List<org.opennms.netmgt.config.destinationPaths.Target> allTargets = new ArrayList<>(path.getTargets());
        for (final org.opennms.netmgt.config.destinationPaths.Escalate escalate : path.getEscalates()) {
            allTargets.addAll(escalate.getTargets());
        }
        for (final org.opennms.netmgt.config.destinationPaths.Target target : allTargets) {
            final String targetName = target.getName();
            if (!targetName.contains("@") && !known.contains(targetName)) {
                throw getException(Status.BAD_REQUEST, "Target {} is not a user, group, on-call role, or email address.", targetName);
            }
            for (final String command : target.getCommands()) {
                if (getNotificationCommandFactory().getCommand(command) == null) {
                    throw getException(Status.BAD_REQUEST, "Command {} is not defined in notificationCommands.xml.", command);
                }
            }
        }
    }

    private static void validateDestinationPath(final org.opennms.netmgt.config.destinationPaths.Path path) {
        if (path == null || StringUtils.isBlank(path.getName())) {
            throw getException(Status.BAD_REQUEST, "The destination path and its name are required");
        }
        if (path.getTargets().isEmpty()) {
            throw getException(Status.BAD_REQUEST, "The destination path requires at least one target");
        }
        validateDelay("initial-delay", path.getInitialDelay().orElse(null));
        validateTargets(path.getTargets());
        for (final org.opennms.netmgt.config.destinationPaths.Escalate escalate : path.getEscalates()) {
            if (StringUtils.isBlank(escalate.getDelay())) {
                throw getException(Status.BAD_REQUEST, "Every escalation requires a delay");
            }
            validateDelay("escalation delay", escalate.getDelay());
            if (escalate.getTargets().isEmpty()) {
                throw getException(Status.BAD_REQUEST, "Every escalation requires at least one target");
            }
            validateTargets(escalate.getTargets());
        }
    }

    // notifd parses these with TimeConverter.convertToMillis when the notice
    // fires — AFTER the notice row is inserted — so a bad value would persist
    // the notice, schedule nothing, and throw inside eventd's dispatch.
    // convertToMillis alone is too loose (Float.parseFloat takes signs, NaN
    // and Infinity), so the same grammar the UI enforces is applied first.
    private static final java.util.regex.Pattern NOTIFD_DURATION =
            java.util.regex.Pattern.compile("\\d+(\\.\\d+)?(us|ms|s|m|h|d)?", java.util.regex.Pattern.CASE_INSENSITIVE);

    private static void validateDelay(final String field, final String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        try {
            if (!NOTIFD_DURATION.matcher(value.trim()).matches()) {
                throw new NumberFormatException();
            }
            TimeConverter.convertToMillis(value);
        } catch (final NumberFormatException e) {
            throw getException(Status.BAD_REQUEST,
                    "Invalid {} '{}': use a number with an optional unit suffix us, ms, s, m, h or d (e.g. 30s, 15m, 1h).", field, value);
        }
    }

    private static void validateTargets(final List<org.opennms.netmgt.config.destinationPaths.Target> targets) {
        for (final org.opennms.netmgt.config.destinationPaths.Target target : targets) {
            if (StringUtils.isBlank(target.getName())) {
                throw getException(Status.BAD_REQUEST, "Every target requires a name");
            }
            validateDelay("target interval", target.getInterval().orElse(null));
            if (target.getCommands().isEmpty() || target.getCommands().stream().anyMatch(StringUtils::isBlank)) {
                throw getException(Status.BAD_REQUEST, "Every target requires at least one non-empty command");
            }
        }
    }

    private static DestinationPathFactory getDestinationPathFactory() throws Exception {
        DestinationPathFactory.init();
        return DestinationPathFactory.getInstance();
    }

    // addPath/replacePath/removePath mutate the in-memory map and only then
    // saveCurrent(); if the save throws (I/O error, or the marshalled XML fails
    // schema validation — e.g. removing the last path), memory is left ahead of
    // the file and the factory won't re-read until a restart. Reload from disk
    // on any mutation failure so memory matches what actually persisted.
    private static void reloadDestinationPathsQuietly() {
        try {
            getDestinationPathFactory().reload();
        } catch (final Exception e) {
            LOG.warn("Failed to reload destination paths after a save error; "
                    + "in-memory notification config may diverge from disk until restart", e);
        }
    }

    private static NotificationCommandFactory getNotificationCommandFactory() throws Exception {
        NotificationCommandFactory.init();
        return NotificationCommandFactory.getInstance();
    }

    private static GroupManager getGroupManager() throws Exception {
        GroupFactory.init();
        return GroupFactory.getInstance();
    }

    // ------------------------------------------------------------------
    // Path outages (critical paths). Storage is and stays the pathoutage
    // DB table; the logic below mirrors the legacy NotificationWizardServlet
    // (filter rule -> node set; per node delete + optional insert; blank
    // critical IP clears the path for the matching nodes).
    // ------------------------------------------------------------------

    private static final int PREVIEW_NODE_LIMIT = 200;

    public static class PathOutageDTO {
        private Integer nodeId;
        private String nodeLabel;
        private String criticalPathIp;
        private String criticalPathServiceName;

        public Integer getNodeId() {
            return nodeId;
        }

        public void setNodeId(final Integer nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeLabel() {
            return nodeLabel;
        }

        public void setNodeLabel(final String nodeLabel) {
            this.nodeLabel = nodeLabel;
        }

        public String getCriticalPathIp() {
            return criticalPathIp;
        }

        public void setCriticalPathIp(final String criticalPathIp) {
            this.criticalPathIp = criticalPathIp;
        }

        public String getCriticalPathServiceName() {
            return criticalPathServiceName;
        }

        public void setCriticalPathServiceName(final String criticalPathServiceName) {
            this.criticalPathServiceName = criticalPathServiceName;
        }
    }

    public static class PathOutagePreviewDTO {
        private int totalCount;
        private List<PathOutageDTO> nodes = new ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(final int totalCount) {
            this.totalCount = totalCount;
        }

        public List<PathOutageDTO> getNodes() {
            return nodes;
        }

        public void setNodes(final List<PathOutageDTO> nodes) {
            this.nodes = nodes;
        }
    }

    public static class PathOutageRequestDTO {
        private String rule;
        private String criticalIp;
        private String criticalSvc;

        public String getRule() {
            return rule;
        }

        public void setRule(final String rule) {
            this.rule = rule;
        }

        public String getCriticalIp() {
            return criticalIp;
        }

        public void setCriticalIp(final String criticalIp) {
            this.criticalIp = criticalIp;
        }

        public String getCriticalSvc() {
            return criticalSvc;
        }

        public void setCriticalSvc(final String criticalSvc) {
            this.criticalSvc = criticalSvc;
        }
    }

    @GET
    @Path("path-outages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PathOutageDTO> getPathOutages(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read path outages");
        return sessionUtils.withReadOnlyTransaction(() -> {
            final List<OnmsPathOutage> outages = pathOutageDao.findAll();
            // fetch the node labels in one query rather than a lazy select per
            // row on the @OneToOne node association
            final Set<Integer> nodeIds = new HashSet<>();
            for (final OnmsPathOutage po : outages) {
                nodeIds.add(po.getNodeId());
            }
            final Map<Integer, String> labelByNodeId = new HashMap<>();
            if (!nodeIds.isEmpty()) {
                for (final OnmsNode node : nodeDao.findMatching(new CriteriaBuilder(OnmsNode.class).in("id", nodeIds).toCriteria())) {
                    labelByNodeId.put(node.getId(), node.getLabel());
                }
            }
            final List<PathOutageDTO> result = new ArrayList<>();
            for (final OnmsPathOutage po : outages) {
                final PathOutageDTO dto = new PathOutageDTO();
                dto.setNodeId(po.getNodeId());
                dto.setNodeLabel(labelByNodeId.get(po.getNodeId()));
                dto.setCriticalPathIp(InetAddressUtils.str(po.getCriticalPathIp()));
                dto.setCriticalPathServiceName(po.getCriticalPathServiceName());
                result.add(dto);
            }
            // preserve the prior ordering: node label, then node id
            result.sort(Comparator
                    .comparing((final PathOutageDTO dto) -> dto.getNodeLabel() == null ? "" : dto.getNodeLabel(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(PathOutageDTO::getNodeId));
            return result;
        });
    }

    @GET
    @Path("path-outages/preview")
    @Produces(MediaType.APPLICATION_JSON)
    public PathOutagePreviewDTO previewPathOutageRule(@Context final SecurityContext securityContext, @javax.ws.rs.QueryParam("rule") final String rule) {
        assertAdmin(securityContext, "preview path outage rules");
        if (StringUtils.isBlank(rule)) {
            throw getException(Status.BAD_REQUEST, "A filter rule is required");
        }
        // database-only (filter evaluation); the config lock guards the XML
        // factories and has nothing to protect here
        final SortedMap<Integer, String> nodes = getMatchingNodes(rule);
        final PathOutagePreviewDTO preview = new PathOutagePreviewDTO();
        preview.setTotalCount(nodes.size());
        for (final Map.Entry<Integer, String> entry : nodes.entrySet()) {
            if (preview.getNodes().size() >= PREVIEW_NODE_LIMIT) {
                break;
            }
            final PathOutageDTO dto = new PathOutageDTO();
            dto.setNodeId(entry.getKey());
            dto.setNodeLabel(entry.getValue());
            preview.getNodes().add(dto);
        }
        return preview;
    }

    @POST
    @Path("path-outages")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response applyPathOutage(@Context final SecurityContext securityContext, final PathOutageRequestDTO request) {
        assertAdmin(securityContext, "configure path outages");
        if (request == null || StringUtils.isBlank(request.getRule())) {
            throw getException(Status.BAD_REQUEST, "A filter rule is required");
        }
        final boolean clearing = request.getCriticalIp() == null || request.getCriticalIp().trim().isEmpty();
        final String requestedSvc = StringUtils.isBlank(request.getCriticalSvc()) ? "ICMP" : request.getCriticalSvc().trim();
        if (!"ICMP".equalsIgnoreCase(requestedSvc)) {
            throw getException(Status.BAD_REQUEST, "Only ICMP is supported as a critical path service.");
        }
        // store the canonical service name: the path-outage joins compare
        // criticalPathServiceName to serviceType.name case-sensitively, so a
        // verbatim "icmp" would silently drop out of getNodesForPathOutage.
        final String criticalSvc = "ICMP";
        final InetAddress criticalIp;
        if (clearing) {
            criticalIp = null;
        } else {
            final String raw = request.getCriticalIp().trim();
            try {
                // forString only accepts IP literals; InetAddressUtils.addr is
                // getByName under the hood, which would resolve a hostname over
                // DNS in a blocking lookup with no timeout
                criticalIp = com.google.common.net.InetAddresses.forString(raw);
            } catch (final IllegalArgumentException e) {
                throw getException(Status.BAD_REQUEST, "Invalid critical path IP address: {}", raw);
            }
        }
        final SortedMap<Integer, String> nodes = getMatchingNodes(request.getRule());
        if (nodes.isEmpty()) {
            throw getException(Status.BAD_REQUEST, "The filter rule matches no nodes.");
        }
        try {
            // one transaction so a mid-loop failure can't leave some nodes
            // stripped of their old critical path with no new one written
            sessionUtils.withTransaction(() -> {
                int count = 0;
                for (final Integer nodeId : nodes.keySet()) {
                    final OnmsPathOutage existing = pathOutageDao.get(nodeId);
                    if (clearing) {
                        if (existing != null) {
                            pathOutageDao.delete(existing);
                        }
                    } else if (existing != null) {
                        // update in place rather than delete+insert (same primary key)
                        existing.setCriticalPathIp(criticalIp);
                        existing.setCriticalPathServiceName(criticalSvc);
                        pathOutageDao.update(existing);
                    } else {
                        // load() is a proxy (no query); the node came from the filter
                        // evaluation so it exists
                        pathOutageDao.save(new OnmsPathOutage(nodeDao.load(nodeId), criticalIp, criticalSvc));
                    }
                    if (++count % PATH_OUTAGE_BATCH == 0) {
                        pathOutageDao.flush();
                        pathOutageDao.clear();
                    }
                }
                return null;
            });
        } catch (final DataAccessException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Could not apply the path outage: {}", e.getMessage());
        }
        LOG.info("Path outage {} applied to {} nodes (rule '{}') by user {}",
                clearing ? "cleared" : "critical path " + InetAddressUtils.str(criticalIp) + "/" + criticalSvc,
                nodes.size(), request.getRule(), securityContext.getUserPrincipal().getName());
        return Response.noContent().build();
    }

    @DELETE
    @Path("path-outages/{nodeId}")
    public Response deletePathOutage(@Context final SecurityContext securityContext, @PathParam("nodeId") final Integer nodeId) {
        assertAdmin(securityContext, "remove path outages");
        final boolean removed;
        try {
            removed = sessionUtils.withTransaction(() -> {
                final OnmsPathOutage existing = pathOutageDao.get(nodeId);
                if (existing == null) {
                    return false;
                }
                pathOutageDao.delete(existing);
                return true;
            });
        } catch (final DataAccessException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't remove path outage for node {}: {}", String.valueOf(nodeId), e.getMessage());
        }
        if (!removed) {
            throw getException(Status.NOT_FOUND, "No critical path is configured for node {}.", String.valueOf(nodeId));
        }
        return Response.noContent().build();
    }

    private static SortedMap<Integer, String> getMatchingNodes(final String rule) {
        // getNodeMap already parses/evaluates the rule; a preceding validateRule
        // would be a second full evaluation, so rely on getNodeMap's own parse.
        try {
            return FilterDaoFactory.getInstance().getNodeMap(rule);
        } catch (final FilterParseException e) {
            throw getException(Status.BAD_REQUEST, "Invalid filter rule: {}", e.getMessage());
        } catch (final DataAccessException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Could not evaluate the filter rule: {}", e.getMessage());
        }
    }
}
