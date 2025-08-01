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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Provisiond", description = "Provisiond API")
public class ProvisiondStatusRestService {
    private MonitorHolder getMonitorHolder() {
        return BeanUtils.getBean("provisiondContext", "monitorHolder", MonitorHolder.class);
    }

    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get all jobs status", description = "Get all recent provisiond jobs status.", operationId = "ProvisiondStatusRestServiceGETStatusOfJobs")
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
    @Operation(summary = "Get single job status", description = "Get single provisiond job status by jobId", operationId = "ProvisiondStatusRestServiceGETStatusOfJobByJobId")
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