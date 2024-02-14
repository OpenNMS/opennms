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
package org.opennms.netmgt.endpoints.grafana.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/endpoints/grafana")
public interface GrafanaEndpointRestService {

    @GET
    Response listEndpoints();

    @DELETE
    Response deleteAllEndpoints();

    @POST
    @Path("/verify")
    Response verifyEndpoint(final GrafanaEndpoint grafanaEndpoint);

    @GET
    @Path("/{id}")
    Response getEndpoint(@PathParam("id") final Long endpointId);

    @PUT
    @Path("/{id}")
    Response updateEndpoint(final GrafanaEndpoint grafanaEndpoint);

    @POST
    Response createEndpoint(final GrafanaEndpoint newGrafanaEndpoint);

    @DELETE
    @Path("/{id}")
    Response deleteEndpoint(@PathParam("id") final Long endpointId);

    @Path("/{uid}/dashboards")
    @GET
    Response listDashboards(@PathParam("uid") final String uid);

    @Path("/{uid}/dashboards/{dashboardId}")
    @GET
    Response getDashboard(@PathParam("uid") final String uid, @PathParam("dashboardId") final String dashboardId);
}
