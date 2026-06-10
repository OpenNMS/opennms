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

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.ha.HaConfiguration;
import org.opennms.netmgt.ha.HaInstanceState;
import org.opennms.netmgt.ha.HaStartupCoordinator;
import org.opennms.netmgt.ha.rest.dto.HaInstanceStatusDto;
import org.opennms.netmgt.ha.rest.dto.HaStatusCollectionDto;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.File;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * REST resource exposing HA cluster status and configuration.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET  /rest/ha/status} — current state of all HA instances (all roles)</li>
 *   <li>{@code GET  /rest/ha/config} — current HA configuration for this instance</li>
 *   <li>{@code PUT  /rest/ha/config} — replace the on-disk configuration. The new
 *       configuration is applied immediately. Requires ROLE_ADMIN. Rejects changes
 *       to immutable fields ({@code enabled}, {@code instance-id}, {@code role})
 *       with 400 Bad Request.</li>
 *   <li>{@code POST /rest/ha/failover} — initiate graceful failover; only valid when
 *       this instance is currently ACTIVE. Requires ROLE_ADMIN.</li>
 * </ul>
 */
@Component("haResource")
@Path("ha")
@Tag(name = "HA", description = "High Availability management API")
public class HaResource extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(HaResource.class);

    @Autowired
    private DataSource dataSource;

    // -------------------------------------------------------------------------
    // GET /rest/ha/status
    // -------------------------------------------------------------------------

    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStatus() {
        // Failover threshold for staleness detection comes from the live local config.
        // If HA isn't enabled on this node, we fall back to the schema default.
        int failoverThresholdSeconds = new HaConfiguration().getFailoverThresholdSeconds();
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord != null) {
            failoverThresholdSeconds = coord.getConfig().getFailoverThresholdSeconds();
        }

        try (Connection conn = dataSource.getConnection();
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
            LOG.error("Failed to query HA instance status", e);
            return Response.serverError().entity("Failed to query HA status: " + e.getMessage()).build();
        }
    }

    // -------------------------------------------------------------------------
    // GET /rest/ha/config
    // -------------------------------------------------------------------------

    @GET
    @Path("config")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @PUT
    @Path("config")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateConfig(@Context SecurityContext securityContext, HaConfiguration newCfg) {
        if (!securityContext.isUserInRole(org.opennms.web.api.Authentication.ROLE_ADMIN)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("ROLE_ADMIN is required to modify HA configuration").build();
        }

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

        LOG.info("HA configuration updated via REST by user '{}'",
                securityContext.getUserPrincipal().getName());
        return Response.ok(coord.getConfig()).build();
    }

    // -------------------------------------------------------------------------
    // POST /rest/ha/failover
    // -------------------------------------------------------------------------

    @POST
    @Path("failover")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response initiateFailover(@Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(org.opennms.web.api.Authentication.ROLE_ADMIN)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("ROLE_ADMIN is required to initiate failover").build();
        }

        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }

        if (coord.getCurrentState() != HaInstanceState.ACTIVE) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("This instance is not currently ACTIVE; failover is not applicable").build();
        }

        HaConfiguration config = coord.getConfig();
        LOG.warn("HA failover initiated via REST by user '{}' on {} ({})",
                securityContext.getUserPrincipal().getName(), config.getInstanceId(), config.getRole());

        Thread failoverThread = new Thread(() -> {
            try {
                // Brief delay to allow this HTTP response to be delivered before services stop
                Thread.sleep(1000);

                // 1. Write STANDBY to DB and stop the heartbeat — partner monitor sees this immediately
                coord.initiateFailover();

                // 2. Stop all OpenNMS services via the in-process Manager MBean
                List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
                if (!servers.isEmpty()) {
                    servers.get(0).invoke(
                            ObjectName.getInstance("OpenNMS:Name=Manager"), "stop",
                            new Object[0], new String[0]);
                } else {
                    LOG.error("HA failover: no MBeanServer found; OpenNMS services may not stop cleanly");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.error("HA failover: error stopping services", e);
            }
        }, "ha-failover");
        failoverThread.setDaemon(false); // must not be daemon — must outlive the HTTP request
        failoverThread.start();

        return Response.accepted()
                .entity("Failover initiated. This instance will stop services and must be manually restarted to enter STANDBY. " +
                        "Monitor GET /rest/ha/status to confirm the partner activates.")
                .build();
    }
}
