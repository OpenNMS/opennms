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

import java.util.Collection;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.support.ApplicationStatusUtil;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.perspectivepolling.ApplicationServiceStatus;
import org.opennms.netmgt.model.perspectivepolling.ApplicationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("perspectivepoller")
@Transactional
@Tag(name = "PerspectivePoller", description = "Perspective Poller API")
public class ApplicationStatusRestService {

    @Autowired
    private OutageDao outageDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    private static final String WINDOW_NOTE = """
            `start` and `end` are epoch milliseconds. Omitting `end` uses now; omitting `start` uses
            `end` minus 24 hours. The window is not validated, so a `start` after `end` is accepted and
            simply matches nothing.""";

    @GET
    @Path("{applicationId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a Status of a specified application",
            description = """
        Perspective-poller availability for one application over a time window, aggregated overall and
        per monitoring location. `overallStatus` and `aggregated-status` are percentages of the window
        during which the application was up as seen from that perspective.

        """ + WINDOW_NOTE + """


        In XML, `applicationId`, `start` and `end` are attributes on `application-status`, and the
        per-location value is the hyphenated element `aggregated-status`. The JSON keeps the hyphenated
        spelling for `aggregated-status`.""",
            operationId = "ApplicationStatusRestServiceGetStatusByApplicationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability for the application over the window.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = ApplicationStatus.class),
                                    examples = @ExampleObject(value = """
                    {
                      "applicationId": 1,
                      "start": 1787641143996,
                      "end": 1787727543996,
                      "overallStatus": 100.0,
                      "location": [
                        {"name": "Default", "aggregated-status": 100.0}
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = ApplicationStatus.class),
                                    examples = @ExampleObject(value = """
                    <application-status applicationId="1" end="1787727557946" start="1787641157946">
                      <location name="Default">
                        <aggregated-status>100.0</aggregated-status>
                      </location>
                      <overallStatus>100.0</overallStatus>
                    </application-status>"""))
                    }),
            @ApiResponse(responseCode = "404", description = """
                    No application with that id. A non-numeric `applicationId` also lands here, from the
                    parameter conversion. The body is empty either way.""")
    })
    public Response applicationStatus(@Parameter(description = "Numeric id of the application, as returned by `/applications`.",
                                              required = true, example = "1")
                                      @PathParam("applicationId") final Integer applicationId,
                                      @Parameter(description = "Window start, epoch milliseconds. Defaults to `end` minus 24 hours.",
                                              example = "1787641143996")
                                      @QueryParam("start") Long start,
                                      @Parameter(description = "Window end, epoch milliseconds. Defaults to now.",
                                              example = "1787727543996")
                                      @QueryParam("end") Long end) {
        if (end == null) {
            end = new Date().getTime();
        }

        if (start == null) {
            start = end - 86400000;
        }

        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsOutage> statusChanges = outageDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(ApplicationStatusUtil.buildApplicationStatus(onmsApplication, statusChanges, start, end)).build();
    }

    @GET
    @Path("{applicationId}/{monitoredServiceId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(summary = "Get a Status of a specified application by monitoringServiceId",
            description = """
        Perspective-poller availability for one monitored service inside an application, per monitoring
        location. Each location also reports `response-resource-id`, the resource id under which that
        perspective's response-time data is stored.

        """ + WINDOW_NOTE + """


        `monitoredServiceId` is an `ifServiceId`, and it has to be one the application actually
        contains: an id that is not a member fails with a 500 rather than a 404.""",
            operationId = "ApplicationStatusRestServiceGetStatusByMonitoringServiceId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability for the service over the window, per location.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApplicationServiceStatus.class),
                            examples = @ExampleObject(value = """
                    {
                      "applicationId": 1,
                      "monitoredServiceId": 1022,
                      "start": 1787641157901,
                      "end": 1787727557901,
                      "location": [
                        {
                          "name": "Default",
                          "response-resource-id": "127.0.0.4[HTTP-8080]@Default",
                          "aggregated-status": 100.0
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No application with that id. The body is empty."),
            @ApiResponse(responseCode = "500", description = """
                    `monitoredServiceId` does not resolve to a service in this application, so building
                    the per-location resource id dereferences null.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.model.OnmsMonitoredService.getIpAddress()\" because \"onmsMonitoredService\" is null")))
    })
    public Response applicationServiceStatus(@Parameter(description = "Numeric id of the application, as returned by `/applications`.",
                                                     required = true, example = "1")
                                             @PathParam("applicationId") final Integer applicationId,
                                             @Parameter(description = "`ifServiceId` of a monitored service that belongs to the application.",
                                                     required = true, example = "1022")
                                             @PathParam("monitoredServiceId") final Integer monitoredServiceId,
                                             @Parameter(description = "Window start, epoch milliseconds. Defaults to `end` minus 24 hours.",
                                                     example = "1787641157901")
                                             @QueryParam("start") Long start,
                                             @Parameter(description = "Window end, epoch milliseconds. Defaults to now.",
                                                     example = "1787727557901")
                                             @QueryParam("end") Long end) {
        if (end == null) {
            end = new Date().getTime();
        }

        if (start == null) {
            start = end - 86400000;
        }

        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsOutage> statusChanges = outageDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(ApplicationStatusUtil.buildApplicationServiceStatus(monitoredServiceDao, onmsApplication, monitoredServiceId, statusChanges, start, end)).build();
    }
}
