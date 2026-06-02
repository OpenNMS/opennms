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
import org.opennms.netmgt.ha.HaRole;
import org.opennms.netmgt.ha.HaStartupCoordinator;
import org.opennms.netmgt.ha.rest.dto.HaInstanceStatusDto;
import org.opennms.netmgt.ha.rest.dto.HaStatusCollectionDto;
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
 *   <li>{@code GET  /rest/ha/config} — current on-disk HA configuration for this instance</li>
 *   <li>{@code POST /rest/ha/failback} — initiate graceful failback; only valid when this
 *       instance is configured-SECONDARY and currently ACTIVE. Requires ROLE_ADMIN.</li>
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
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT instance_id, configured_role, current_state, last_heartbeat, hostname " +
                     "FROM ha_instance_status ORDER BY configured_role")) {

            List<HaInstanceStatusDto> instances = new ArrayList<>();
            while (rs.next()) {
                HaInstanceStatusDto dto = new HaInstanceStatusDto();
                dto.setInstanceId(rs.getString("instance_id"));
                dto.setConfiguredRole(rs.getString("configured_role"));
                dto.setCurrentState(rs.getString("current_state"));
                Timestamp ts = rs.getTimestamp("last_heartbeat");
                dto.setLastHeartbeat(ts != null ? ts.toInstant().toString() : null);
                dto.setHostname(rs.getString("hostname"));
                dto.setDegraded("SECONDARY".equals(dto.getConfiguredRole()) && "ACTIVE".equals(dto.getCurrentState()));
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
    // POST /rest/ha/failback
    // -------------------------------------------------------------------------

    @POST
    @Path("failback")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response initiateFailback(@Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(org.opennms.web.api.Authentication.ROLE_ADMIN)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("ROLE_ADMIN is required to initiate failback").build();
        }

        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("HA is not enabled on this instance").build();
        }

        HaConfiguration config = coord.getConfig();
        if (config.getRole() != HaRole.SECONDARY) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Failback can only be initiated on a configured-SECONDARY instance").build();
        }

        if (coord.getCurrentState() != HaInstanceState.ACTIVE) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("This SECONDARY instance is not currently ACTIVE; failback is not applicable").build();
        }

        LOG.warn("HA failback initiated via REST by user '{}'", securityContext.getUserPrincipal().getName());

        // Shutdown is asynchronous — the coordinator updates DB state and signals the process.
        // The operator should monitor GET /rest/ha/status to confirm the partner PRIMARY activates.
        Thread failbackThread = new Thread(() -> {
            try {
                // Brief delay to allow this HTTP response to be sent before services begin stopping
                Thread.sleep(2000);
                HaStartupCoordinator.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ha-failback");
        failbackThread.setDaemon(true);
        failbackThread.start();

        return Response.accepted()
                .entity("Failback initiated. This instance will stop services and enter STANDBY. " +
                        "Monitor GET /rest/ha/status on the PRIMARY instance to confirm it activates.")
                .build();
    }
}
