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
package org.opennms.netmgt.ha.rest.impl;

import org.opennms.netmgt.ha.DbConnectionFactory;
import org.opennms.netmgt.ha.HaConfiguration;
import org.opennms.netmgt.ha.HaInstanceState;
import org.opennms.netmgt.ha.HaMode;
import org.opennms.netmgt.ha.HaStartupCoordinator;
import org.opennms.netmgt.ha.HaSyncFiles;
import org.opennms.netmgt.ha.rest.HaRestService;
import org.opennms.netmgt.ha.rest.dto.HaInstanceStatusDto;
import org.opennms.netmgt.ha.rest.dto.HaStatusCollectionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Whiteboard implementation of the HA REST API. Reaches the in-JVM
 * {@link HaStartupCoordinator} singleton through the system-bundle export of
 * {@code org.opennms.netmgt.ha} — the same mechanism the Karaf shell commands
 * use, so all surfaces observe identical state.
 *
 * <p>No programmatic role checks here: authorization is container-side
 * (Spring Security intercept-urls). The principal is used for audit logging
 * only.
 */
public class HaRestServiceImpl implements HaRestService {

    private static final Logger LOG = LoggerFactory.getLogger(HaRestServiceImpl.class);

    private volatile DbConnectionFactory fallbackDbFactory;

    // -------------------------------------------------------------------------
    // GET /rest/ha/status
    // -------------------------------------------------------------------------

    @Override
    public Response getStatus() {
        // Failover threshold for staleness detection comes from the live local config.
        // If HA isn't enabled on this node, fall back to the schema default.
        int failoverThresholdSeconds = new HaConfiguration().getFailoverThresholdSeconds();
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord != null) {
            failoverThresholdSeconds = coord.getConfig().getFailoverThresholdSeconds();
        }

        DbConnectionFactory dbFactory = dbFactory(coord);
        if (dbFactory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }

        try (Connection conn = dbFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT instance_id, configured_role, current_state, last_heartbeat, active_since, " +
                     "EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds, hostname " +
                     "FROM ha_instance_status ORDER BY configured_role")) {

            List<HaInstanceStatusDto> instances = new ArrayList<>();
            while (rs.next()) {
                HaInstanceStatusDto dto = new HaInstanceStatusDto();
                dto.setInstanceId(rs.getString("instance_id"));
                dto.setConfiguredRole(rs.getString("configured_role"));
                dto.setCurrentState(rs.getString("current_state"));
                Timestamp ts = rs.getTimestamp("last_heartbeat");
                dto.setLastHeartbeat(ts != null ? ts.toInstant().toString() : null);
                Timestamp activeSinceTs = rs.getTimestamp("active_since");
                dto.setActiveSince(activeSinceTs != null ? activeSinceTs.toInstant().toString() : null);
                dto.setHostname(rs.getString("hostname"));

                long ageSeconds = rs.getLong("age_seconds");
                boolean heartbeatStale = !rs.wasNull() && ageSeconds > failoverThresholdSeconds;
                boolean stateBasedDegraded =
                        ("SECONDARY".equals(dto.getConfiguredRole()) && "ACTIVE".equals(dto.getCurrentState()))
                        || "DEGRADED".equals(dto.getCurrentState());
                dto.setDegraded(stateBasedDegraded || heartbeatStale);

                instances.add(dto);
            }

            HaStatusCollectionDto collection = new HaStatusCollectionDto();
            collection.setInstances(instances);
            return Response.ok(collection).build();

        } catch (Exception e) {
            if (isUndefinedTable(e)) {
                // The table is created when HA first starts enabled; its
                // absence simply means HA has never been enabled here.
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("HA is not enabled on this instance").build();
            }
            LOG.error("Failed to query HA instance status", e);
            return Response.serverError().entity("Failed to query HA status: " + e.getMessage()).build();
        }
    }

    /** True if the exception chain contains Postgres undefined_table (42P01). */
    private static boolean isUndefinedTable(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof java.sql.SQLException sql && "42P01".equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // GET /rest/ha/config
    // -------------------------------------------------------------------------

    @Override
    public Response getConfig() {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }
        return Response.ok(coord.getConfig()).build();
    }

    // -------------------------------------------------------------------------
    // PUT /rest/ha/config — replace the on-disk configuration and reload
    // -------------------------------------------------------------------------

    @Override
    public Response updateConfig(SecurityContext securityContext, HaConfiguration newCfg) {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }

        if (newCfg == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body must contain an HaConfiguration document").build();
        }

        try {
            coord.writeConfig(newCfg); // writes to disk and reloads in one step
        } catch (IllegalArgumentException e) {
            LOG.warn("HA config update rejected: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IOException e) {
            LOG.error("HA config update failed to write to disk", e);
            return Response.serverError()
                    .entity("Failed to write configuration: " + e.getMessage()).build();
        }

        LOG.info("HA configuration updated via REST by user '{}'", principal(securityContext));
        return Response.ok(coord.getConfig()).build();
    }

    // -------------------------------------------------------------------------
    // POST /rest/ha/failover
    // -------------------------------------------------------------------------

    @Override
    public Response initiateFailover(SecurityContext securityContext) {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }

        if (coord.getConfig().getMode() == HaMode.HEARTBEAT_ONLY) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("HA is in heartbeat-only mode; failover is controlled by the external HA agent")
                    .build();
        }

        if (coord.getCurrentState() != HaInstanceState.ACTIVE) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("This instance is not currently ACTIVE; failover is not applicable").build();
        }

        HaConfiguration config = coord.getConfig();
        LOG.warn("HA failover initiated via REST by user '{}' on {} ({})",
                principal(securityContext), config.getInstanceId(), config.getRole());

        Thread failoverThread = new Thread(() -> {
            try {
                // Brief delay to allow this HTTP response to be delivered before services stop.
                // An interrupt here is safe: nothing has been advertised yet.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            // 1. Stop the heartbeat and mark the step-down. STANDBY is published only
            // after Manager.stop() below finishes draining the services, so the
            // partner never promotes alongside a node that is still processing.
            coord.initiateFailover();

            // 2. Stop all OpenNMS services via the in-process Manager MBean; its completion
            // publishes STANDBY. The heartbeat is already silenced, so if the stop cannot
            // run, this node would keep serving as an ACTIVE row with a dead heartbeat until
            // the partner staleness-promotes next to it — halting is the only safe outcome.
            try {
                List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
                if (servers.isEmpty()) {
                    LOG.error("HA failover: no MBeanServer found; halting to honor the step-down");
                    Runtime.getRuntime().halt(70);
                }
                servers.get(0).invoke(
                        ObjectName.getInstance("OpenNMS:Name=Manager"), "stop",
                        new Object[0], new String[0]);
            } catch (Exception e) {
                LOG.error("HA failover: failed to stop services after stepping down; halting to prevent an undetectable active-active pair", e);
                Runtime.getRuntime().halt(70);
            }
        }, "ha-failover");
        failoverThread.setDaemon(false); // must not be daemon — must outlive the HTTP request
        failoverThread.start();

        return Response.accepted()
                .entity("Failover initiated. This instance will stop services and must be manually restarted to enter STANDBY. " +
                        "Monitor GET /rest/ha/status to confirm the partner activates.")
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /rest/ha/sync/manifest + /rest/ha/sync/file — served by the ACTIVE
    // node for the standby's HaConfigSyncer (binary-safe, manifest-based)
    // -------------------------------------------------------------------------

    @Override
    public Response getSyncManifest() {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }
        try {
            List<String> excludes = coord.getConfig().getSyncExcludes();
            List<HaSyncFiles.Entry> manifest =
                    HaSyncFiles.buildManifest(HaSyncFiles.etcRoot(), excludes);
            return Response.ok(HaSyncFiles.toManifestText(manifest, excludes)).build();
        } catch (Exception e) {
            LOG.error("HA sync: failed to build manifest", e);
            return Response.serverError().entity("Failed to build manifest: " + e.getMessage()).build();
        }
    }

    @Override
    public Response getSyncFile(String relativePath) {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }
        if (relativePath == null || relativePath.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("query parameter 'f' is required").build();
        }
        if (HaSyncFiles.isExcluded(relativePath, coord.getConfig().getSyncExcludes())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("file is excluded from sync: " + relativePath).build();
        }
        try {
            Path file = HaSyncFiles.resolveSafe(HaSyncFiles.etcRoot(), relativePath);
            if (!Files.isRegularFile(file)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("no such file: " + relativePath).build();
            }
            return Response.ok(Files.readAllBytes(file)).build();
        } catch (IOException e) {
            // resolveSafe rejects traversal with IOException — treat as client error
            LOG.warn("HA sync: rejected file request '{}': {}", relativePath, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // -------------------------------------------------------------------------

    /** DB access: prefer the coordinator's factory; fall back to parsing
     * opennms-datasources.xml directly (status can then still be served when
     * the local coordinator failed to initialise). */
    private DbConnectionFactory dbFactory(HaStartupCoordinator coord) {
        if (coord != null) {
            return coord.getDbFactory();
        }
        DbConnectionFactory cached = fallbackDbFactory;
        if (cached != null) {
            return cached;
        }
        try {
            cached = DbConnectionFactory.fromDatasourcesXml();
            fallbackDbFactory = cached;
            return cached;
        } catch (Exception e) {
            LOG.debug("HA: could not create fallback DB connection factory", e);
            return null;
        }
    }

    private static String principal(SecurityContext ctx) {
        return ctx != null && ctx.getUserPrincipal() != null
                ? ctx.getUserPrincipal().getName() : "<unknown>";
    }
}
