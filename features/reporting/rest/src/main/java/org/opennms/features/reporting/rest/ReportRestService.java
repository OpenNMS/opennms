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
package org.opennms.features.reporting.rest;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/reports")
@Produces("application/json")
@Consumes("application/json")
public interface ReportRestService {

    @GET
    Response listReports();

    @GET
    @Path("/{id}")
    Response getReportDetails(@PathParam("id") String reportId, @QueryParam("userId") String userId);

    @POST
    @Path("/{id}")
    Response runReport(@PathParam("id") String reportId, Map<String, Object> inputParameters);

    @GET
    @Path("/persisted")
    Response listPersistedReports(@Context final UriInfo uriInfo);

    @DELETE
    @Path("/persisted")
    Response deletePersistedReports();

    @POST
    @Path("/persisted")
    Response deliverReport(Map<String, Object> parameters);

    @DELETE
    @Path("/persisted/{id}")
    Response deletePersistedReport(@PathParam("id") final int id);

    @GET
    @Path("/scheduled")
    Response listScheduledReports(@Context final UriInfo uriInfo);

    @GET
    @Path("/scheduled/{triggerName}")
    Response getSchedule(@PathParam("triggerName") final String triggerName);

    @PUT
    @Path("/scheduled/{triggerName}")
    Response updateSchedule(@PathParam("triggerName") final String triggerName, final Map<String, Object> parameters);

    @DELETE
    @Path("/scheduled")
    Response deleteScheduledReports();

    @POST
    @Path("/scheduled")
    Response scheduleReport(final Map<String, Object> parameters);

    @DELETE
    @Path("/scheduled/{id}")
    Response deleteScheduledReport(@PathParam("id") final String triggerName);

    @GET
    @Path("/download")
    Response downloadReport(@QueryParam("format") final String format, @QueryParam("locatorId") final String locatorId);
}
