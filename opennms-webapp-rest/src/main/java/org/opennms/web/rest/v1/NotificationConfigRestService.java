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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

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
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST access to the notification configuration that has historically only been
 * reachable through the admin JSP wizards. All operations delegate to the same
 * file-backed config factories the legacy servlets use, so the XML files
 * (notifications.xml, destinationPaths.xml, notificationCommands.xml,
 * notifd-configuration.xml) remain the system of record and stay fully
 * editable by hand.
 *
 * <ul>
 * <li><b>GET /notification-config/status</b> global notifd on/off status</li>
 * <li><b>PUT /notification-config/status</b> turn notifd on or off</li>
 * <li><b>GET /notification-config/destination-paths</b> all destination paths</li>
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
    public Response setStatus(@Context final SecurityContext securityContext, final NotificationStatus status) {
        assertAdmin(securityContext, "change the notification status");
        if (status == null || !("on".equals(status.getStatus()) || "off".equals(status.getStatus()))) {
            throw getException(Status.BAD_REQUEST, "Status must be 'on' or 'off'");
        }
        writeLock();
        try {
            LOG.info("Setting notifd status to {} for user {}", status.getStatus(), securityContext.getUserPrincipal().getName());
            if ("on".equals(status.getStatus())) {
                getNotifdConfigFactory().turnNotifdOn();
                sendStatusEvent("uei.opennms.org/internal/notificationsTurnedOn", securityContext);
            } else {
                getNotifdConfigFactory().turnNotifdOff();
                sendStatusEvent("uei.opennms.org/internal/notificationsTurnedOff", securityContext);
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

    private void sendStatusEvent(final String uei, final SecurityContext securityContext) {
        final EventBuilder bldr = new EventBuilder(uei, "ReST");
        bldr.addParam("remoteUser", securityContext.getUserPrincipal().getName());
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

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private static DestinationPathFactory getDestinationPathFactory() throws Exception {
        DestinationPathFactory.init();
        return DestinationPathFactory.getInstance();
    }

    // ------------------------------------------------------------------
    // Path outages (critical paths). Storage is and stays the pathoutage
    // DB table; the logic below mirrors the legacy NotificationWizardServlet
    // (filter rule -> node set; per node delete + optional insert; blank
    // critical IP clears the path for the matching nodes).
    // ------------------------------------------------------------------

    private static final String SQL_LIST_PATH_OUTAGES = "SELECT p.nodeid, n.nodelabel, p.criticalpathip, p.criticalpathservicename FROM pathoutage p LEFT JOIN node n ON n.nodeid = p.nodeid ORDER BY n.nodelabel, p.nodeid";
    private static final String SQL_SET_CRITICAL_PATH = "INSERT INTO pathoutage (nodeid, criticalpathip, criticalpathservicename) VALUES (?, ?, ?)";
    private static final String SQL_DELETE_CRITICAL_PATH = "DELETE FROM pathoutage WHERE nodeid=?";

    private static final int PREVIEW_NODE_LIMIT = 200;

    @XmlRootElement(name = "path-outage")
    public static class PathOutageDTO {
        private Integer m_nodeId;
        private String m_nodeLabel;
        private String m_criticalPathIp;
        private String m_criticalPathServiceName;

        public Integer getNodeId() {
            return m_nodeId;
        }

        public void setNodeId(final Integer nodeId) {
            m_nodeId = nodeId;
        }

        public String getNodeLabel() {
            return m_nodeLabel;
        }

        public void setNodeLabel(final String nodeLabel) {
            m_nodeLabel = nodeLabel;
        }

        public String getCriticalPathIp() {
            return m_criticalPathIp;
        }

        public void setCriticalPathIp(final String criticalPathIp) {
            m_criticalPathIp = criticalPathIp;
        }

        public String getCriticalPathServiceName() {
            return m_criticalPathServiceName;
        }

        public void setCriticalPathServiceName(final String criticalPathServiceName) {
            m_criticalPathServiceName = criticalPathServiceName;
        }
    }

    @XmlRootElement(name = "path-outage-preview")
    public static class PathOutagePreviewDTO {
        private int m_totalCount;
        private List<PathOutageDTO> m_nodes = new ArrayList<>();

        public int getTotalCount() {
            return m_totalCount;
        }

        public void setTotalCount(final int totalCount) {
            m_totalCount = totalCount;
        }

        public List<PathOutageDTO> getNodes() {
            return m_nodes;
        }

        public void setNodes(final List<PathOutageDTO> nodes) {
            m_nodes = nodes;
        }
    }

    @XmlRootElement(name = "path-outage-request")
    public static class PathOutageRequestDTO {
        private String m_rule;
        private String m_criticalIp;
        private String m_criticalSvc;

        public String getRule() {
            return m_rule;
        }

        public void setRule(final String rule) {
            m_rule = rule;
        }

        public String getCriticalIp() {
            return m_criticalIp;
        }

        public void setCriticalIp(final String criticalIp) {
            m_criticalIp = criticalIp;
        }

        public String getCriticalSvc() {
            return m_criticalSvc;
        }

        public void setCriticalSvc(final String criticalSvc) {
            m_criticalSvc = criticalSvc;
        }
    }

    @GET
    @Path("path-outages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PathOutageDTO> getPathOutages(@Context final SecurityContext securityContext) {
        assertAdmin(securityContext, "read path outages");
        readLock();
        try {
            final List<PathOutageDTO> result = new ArrayList<>();
            final Connection conn = DataSourceFactory.getInstance().getConnection();
            final DBUtils d = new DBUtils(getClass(), conn);
            try {
                final PreparedStatement stmt = conn.prepareStatement(SQL_LIST_PATH_OUTAGES);
                d.watch(stmt);
                final ResultSet rs = stmt.executeQuery();
                d.watch(rs);
                while (rs.next()) {
                    final PathOutageDTO dto = new PathOutageDTO();
                    dto.setNodeId(rs.getInt(1));
                    dto.setNodeLabel(rs.getString(2));
                    dto.setCriticalPathIp(rs.getString(3));
                    dto.setCriticalPathServiceName(rs.getString(4));
                    result.add(dto);
                }
            } finally {
                d.cleanUp();
            }
            return result;
        } catch (final SQLException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't read path outages: {}", e.getMessage());
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("path-outages/preview")
    @Produces(MediaType.APPLICATION_JSON)
    public PathOutagePreviewDTO previewPathOutageRule(@Context final SecurityContext securityContext, @javax.ws.rs.QueryParam("rule") final String rule) {
        assertAdmin(securityContext, "preview path outage rules");
        if (rule == null || rule.isBlank()) {
            throw getException(Status.BAD_REQUEST, "A filter rule is required");
        }
        readLock();
        try {
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
        } finally {
            readUnlock();
        }
    }

    @POST
    @Path("path-outages")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response applyPathOutage(@Context final SecurityContext securityContext, final PathOutageRequestDTO request) {
        assertAdmin(securityContext, "configure path outages");
        if (request == null || request.getRule() == null || request.getRule().isBlank()) {
            throw getException(Status.BAD_REQUEST, "A filter rule is required");
        }
        String criticalIp = request.getCriticalIp() == null ? "" : request.getCriticalIp().trim();
        final String criticalSvc = request.getCriticalSvc() == null || request.getCriticalSvc().isBlank() ? "ICMP" : request.getCriticalSvc().trim();
        if (!criticalIp.isEmpty()) {
            try {
                FilterDaoFactory.getInstance().validateRule("IPADDR IPLIKE " + criticalIp);
                // store the canonical form, matching the legacy wizard
                criticalIp = InetAddressUtils.normalize(criticalIp);
            } catch (final FilterParseException | IllegalArgumentException e) {
                throw getException(Status.BAD_REQUEST, "Invalid critical path IP address: {}", criticalIp);
            }
        }
        writeLock();
        try {
            final SortedMap<Integer, String> nodes = getMatchingNodes(request.getRule());
            final Connection conn = DataSourceFactory.getInstance().getConnection();
            final DBUtils d = new DBUtils(getClass(), conn);
            try {
                // one transaction so a mid-loop failure can't leave some nodes
                // stripped of their old critical path with no new one written
                conn.setAutoCommit(false);
                try (PreparedStatement delete = conn.prepareStatement(SQL_DELETE_CRITICAL_PATH);
                     PreparedStatement insert = conn.prepareStatement(SQL_SET_CRITICAL_PATH)) {
                    for (final Integer nodeId : nodes.keySet()) {
                        delete.setInt(1, nodeId);
                        delete.addBatch();
                        if (!criticalIp.isEmpty()) {
                            insert.setInt(1, nodeId);
                            insert.setString(2, criticalIp);
                            insert.setString(3, criticalSvc);
                            insert.addBatch();
                        }
                    }
                    delete.executeBatch();
                    if (!criticalIp.isEmpty()) {
                        insert.executeBatch();
                    }
                    conn.commit();
                } catch (final SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } finally {
                d.cleanUp();
            }
            LOG.info("Path outage {} applied to {} nodes (rule '{}') by user {}",
                    criticalIp.isEmpty() ? "cleared" : "critical path " + criticalIp + "/" + criticalSvc,
                    nodes.size(), request.getRule(), securityContext.getUserPrincipal().getName());
            return Response.noContent().build();
        } catch (final javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't apply path outage: {}", e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("path-outages/{nodeId}")
    public Response deletePathOutage(@Context final SecurityContext securityContext, @PathParam("nodeId") final Integer nodeId) {
        assertAdmin(securityContext, "remove path outages");
        writeLock();
        try {
            final Connection conn = DataSourceFactory.getInstance().getConnection();
            final DBUtils d = new DBUtils(getClass(), conn);
            try {
                deleteCriticalPath(nodeId, conn);
            } finally {
                d.cleanUp();
            }
            return Response.noContent().build();
        } catch (final SQLException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't remove path outage for node {}: {}", String.valueOf(nodeId), e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    private static SortedMap<Integer, String> getMatchingNodes(final String rule) {
        try {
            FilterDaoFactory.getInstance().validateRule(rule);
        } catch (final FilterParseException e) {
            throw getException(Status.BAD_REQUEST, "Invalid filter rule: {}", e.getMessage());
        }
        try {
            return FilterDaoFactory.getInstance().getNodeMap(rule);
        } catch (final FilterParseException e) {
            throw getException(Status.BAD_REQUEST, "Invalid filter rule: {}", e.getMessage());
        }
    }

    private static void deleteCriticalPath(final int nodeId, final Connection conn) throws SQLException {
        final DBUtils d = new DBUtils(NotificationConfigRestService.class);
        try {
            final PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }


}
