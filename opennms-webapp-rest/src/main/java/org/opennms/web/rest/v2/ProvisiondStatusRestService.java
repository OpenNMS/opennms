/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.provision.service.MonitorHolder;
import org.opennms.netmgt.provision.service.TimeTrackingMonitor;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Component
@Path("provisiond")
@Transactional
public class ProvisiondStatusRestService {
    private MonitorHolder getMonitorHolder() {
        return BeanUtils.getBean("provisiondContext", "monitorHolder", MonitorHolder.class);
    }

    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get all jobs status", description = "Get all recent provisiond jobs status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "all jobs current monitor object.", content = @Content(schema = @Schema(type = "Map<String, TimeTrackingMonitor>")))
    })
    public Response getAllJobStatus() {
        MonitorHolder monitorHolder = getMonitorHolder();
        return Response.ok(monitorHolder.getMonitors()).build();
    }

    @GET
    @Path("status/{jobId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get single job status", description = "Get single provisiond job status by jobId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The current job status.",
                    content = @Content(schema = @Schema(implementation = TimeTrackingMonitor.class))),
            @ApiResponse(responseCode = "404", description = "jobId not exist.",
                    content = @Content)
    })
    public Response getJobStatus(@PathParam("jobId") String jobId) {
        MonitorHolder monitorHolder = getMonitorHolder();
        ProvisionMonitor monitor = monitorHolder.getMonitors().get(jobId);
        if (monitor != null) {
            return Response.ok(monitor).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}