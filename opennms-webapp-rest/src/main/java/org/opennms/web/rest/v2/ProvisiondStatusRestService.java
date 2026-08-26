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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "Get all jobs status",
            description = """
        Every provisioning job monitor provisiond still holds, keyed by job id. The set is a bounded,
        expiring cache rather than a history: old entries fall out, so an id that worked earlier can
        stop resolving.

        Each value is a Dropwizard-metrics snapshot: nine timers (`loadingTimer`, `auditTimer`,
        `importTimer`, `schedulingTimer`, `relateTimer`, `scanEventTimer`, `scanningTimer`,
        `persistingTimer`, `eventTimer`), each with a percentile snapshot and one/five/fifteen-minute
        and mean rates, plus `nodeCount`, `name`, `startTime`, `endTime` and `currentNodes`. Timer
        values are nanoseconds; `startTime` and `endTime` are epoch milliseconds, with `endTime` unset
        until the job finishes.

        The keys are metric names built from the requisition URL plus a timestamp, so they contain `/`
        and `:` characters.""",
            operationId = "ProvisiondStatusRestServiceGETStatusOfJobs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job monitors keyed by job id, empty when provisiond has run nothing recently.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            // The handler returns a Map, so the document can only
                            // carry a free-form object plus this example.
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(value = """
                    {
                      "file:/opt/opennms/etc/imports/pending/Routers.xml.20260826025715": {
                        "loadingTimer": {
                          "snapshot": {
                            "values": [9353170],
                            "mean": 9353170.0,
                            "stdDev": 0.0,
                            "min": 9353170,
                            "max": 9353170,
                            "median": 9353170.0,
                            "75thPercentile": 9353170.0,
                            "95thPercentile": 9353170.0,
                            "98thPercentile": 9353170.0,
                            "99thPercentile": 9353170.0,
                            "999thPercentile": 9353170.0
                          },
                          "fifteenMinuteRate": 0.1945208954232697,
                          "fiveMinuteRate": 0.18400888292586465,
                          "meanRate": 0.031371987477271615,
                          "oneMinuteRate": 0.1318481260400888,
                          "count": 1
                        },
                        "nodeCount": 3,
                        "name": "file:/opt/opennms/etc/imports/pending/Routers.xml.20260826025715",
                        "startTime": 1787727435506,
                        "endTime": 1787727442797,
                        "currentNodes": {}
                      }
                    }""")))
    })
    public Response getAllJobStatus() {
        MonitorHolder monitorHolder = getMonitorHolder();
        return Response.ok(monitorHolder.getMonitors()).build();
    }

    @GET
    @Path("status/{jobId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get single job status",
            description = """
        One job's monitor, looked up by the same key `GET /provisiond/status` uses.

        Real job ids are metric names derived from the requisition URL and so contain `/` characters.
        A JAX-RS path template does not match across `/`, and percent-encoding the separator as `%2F`
        does not reach the handler either, so such an id cannot be addressed here.""",
            operationId = "ProvisiondStatusRestServiceGETStatusOfJobByJobId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The job monitor. Same shape as one value of the all-jobs map.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TimeTrackingMonitor.class))),
            @ApiResponse(responseCode = "404", description = """
                    No monitor is held under that id, either because it never existed or because it has
                    expired out of the cache. The body is empty.""")
    })
    public Response getJobStatus(@Parameter(description = """
                    A key from `GET /provisiond/status`. Ids containing `/` cannot be expressed in this
                    path segment.""",
                    required = true, example = "Routers.xml.20260826025715")
                                 @PathParam("jobId") String jobId) {
        MonitorHolder monitorHolder = getMonitorHolder();
        ProvisionMonitor monitor = monitorHolder.getMonitors().get(jobId);
        if (monitor != null) {
            return Response.ok(monitor).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}