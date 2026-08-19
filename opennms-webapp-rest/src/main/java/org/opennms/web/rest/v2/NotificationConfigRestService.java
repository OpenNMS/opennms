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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotificationCommandFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
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
 * <li><b>GET /notification-config/destination-paths</b> all destination paths</li>
 * <li><b>GET /notification-config/destination-paths/{name}</b> one destination path</li>
 * <li><b>POST/PUT/DELETE /notification-config/destination-paths[/{name}]</b> destination path mutations (destinationPaths.xml)</li>
 * <li><b>GET /notification-config/commands</b> notification commands (notificationCommands.xml, read-only)</li>
 * <li><b>GET /notification-config/on-call-roles</b> on-call role names (groups.xml, read-only)</li>
 * </ul>
 */
@Component("notificationConfigRestService")
@Path("notification-config")
@Tag(name = "Notification-config", description = "Notification Configuration API")
public class NotificationConfigRestService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationConfigRestService.class);

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

    private static void validateDestinationPath(final org.opennms.netmgt.config.destinationPaths.Path path) {
        if (path == null || StringUtils.isBlank(path.getName())) {
            throw getException(Status.BAD_REQUEST, "The destination path and its name are required");
        }
        if (path.getTargets().isEmpty()) {
            throw getException(Status.BAD_REQUEST, "The destination path requires at least one target");
        }
        validateTargets(path.getTargets());
        for (final org.opennms.netmgt.config.destinationPaths.Escalate escalate : path.getEscalates()) {
            if (StringUtils.isBlank(escalate.getDelay())) {
                throw getException(Status.BAD_REQUEST, "Every escalation requires a delay");
            }
            if (escalate.getTargets().isEmpty()) {
                throw getException(Status.BAD_REQUEST, "Every escalation requires at least one target");
            }
            validateTargets(escalate.getTargets());
        }
    }

    private static void validateTargets(final List<org.opennms.netmgt.config.destinationPaths.Target> targets) {
        for (final org.opennms.netmgt.config.destinationPaths.Target target : targets) {
            if (StringUtils.isBlank(target.getName())) {
                throw getException(Status.BAD_REQUEST, "Every target requires a name");
            }
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
}
