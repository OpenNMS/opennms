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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Comparator;
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

import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.filter.FilterDaoFactory;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
 * <li><b>GET/POST/DELETE /notification-config/path-outages</b> critical paths (pathoutage table)</li>
 * </ul>
 */
@Component("notificationConfigRestService")
@Path("notification-config")
@Tag(name = "Notification-config", description = "Notification Configuration API")
public class NotificationConfigRestService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationConfigRestService.class);

    @Autowired
    @Qualifier("eventProxy")
    protected EventProxy m_eventProxy;

    // Per-instance read/write/update lock serializing config mutations, carried
    // over from the base REST service this was split out of when it moved to v2.
    private final ReadWriteUpdateLock m_globalLock = new ReentrantReadWriteUpdateLock();
    private final Lock m_readLock = m_globalLock.updateLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    private void readLock() {
        m_readLock.lock();
    }

    private void readUnlock() {
        m_readLock.unlock();
    }

    private void writeLock() {
        m_writeLock.lock();
    }

    private void writeUnlock() {
        m_writeLock.unlock();
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
    private PathOutageDao m_pathOutageDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private SessionUtils m_sessionUtils;

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
        return m_sessionUtils.withReadOnlyTransaction(() -> {
            final List<OnmsPathOutage> outages = m_pathOutageDao.findAll();
            // fetch the node labels in one query rather than a lazy select per
            // row on the @OneToOne node association
            final Set<Integer> nodeIds = new HashSet<>();
            for (final OnmsPathOutage po : outages) {
                nodeIds.add(po.getNodeId());
            }
            final Map<Integer, String> labelByNodeId = new HashMap<>();
            if (!nodeIds.isEmpty()) {
                for (final OnmsNode node : m_nodeDao.findMatching(new CriteriaBuilder(OnmsNode.class).in("id", nodeIds).toCriteria())) {
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
        final boolean clearing = request.getCriticalIp() == null || request.getCriticalIp().trim().isEmpty();
        final String requestedSvc = request.getCriticalSvc() == null || request.getCriticalSvc().isBlank() ? "ICMP" : request.getCriticalSvc().trim();
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
                FilterDaoFactory.getInstance().validateRule("IPADDR IPLIKE " + raw);
                // addr already parses/canonicalizes; normalize would be a redundant round-trip
                criticalIp = InetAddressUtils.addr(raw);
            } catch (final FilterParseException | IllegalArgumentException e) {
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
            m_sessionUtils.withTransaction(() -> {
                int count = 0;
                for (final Integer nodeId : nodes.keySet()) {
                    final OnmsPathOutage existing = m_pathOutageDao.get(nodeId);
                    if (clearing) {
                        if (existing != null) {
                            m_pathOutageDao.delete(existing);
                        }
                    } else if (existing != null) {
                        // update in place rather than delete+insert (same primary key)
                        existing.setCriticalPathIp(criticalIp);
                        existing.setCriticalPathServiceName(criticalSvc);
                        m_pathOutageDao.update(existing);
                    } else {
                        // load() is a proxy (no query); the node came from the filter
                        // evaluation so it exists
                        m_pathOutageDao.save(new OnmsPathOutage(m_nodeDao.load(nodeId), criticalIp, criticalSvc));
                    }
                    if (++count % PATH_OUTAGE_BATCH == 0) {
                        m_pathOutageDao.flush();
                        m_pathOutageDao.clear();
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
            removed = m_sessionUtils.withTransaction(() -> {
                final OnmsPathOutage existing = m_pathOutageDao.get(nodeId);
                if (existing == null) {
                    return false;
                }
                m_pathOutageDao.delete(existing);
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
