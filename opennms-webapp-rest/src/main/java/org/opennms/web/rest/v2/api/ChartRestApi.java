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
package org.opennms.web.rest.v2.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("charts")
@Tag(name = "Charts", description = "Bar charts configured in chart-configuration.xml, as data. API V2")
public interface ChartRestApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List the configured charts.",
            description = "Returns the name, title and axis labels of every bar chart in chart-configuration.xml.",
            operationId = "getCharts"
    )
    Response getCharts();

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get a configured chart's data.",
            description = "Runs the chart's series queries and returns the categories and one value series per series definition, or 404 for an unknown chart name.",
            operationId = "getChart"
    )
    Response getChart(@PathParam("name") String name);
}
