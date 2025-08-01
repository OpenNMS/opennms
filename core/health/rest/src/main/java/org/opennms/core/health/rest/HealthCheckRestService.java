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
package org.opennms.core.health.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/health")
public interface HealthCheckRestService {

    @GET
    @Path("probe")
    @Produces(MediaType.TEXT_PLAIN)
    Response probeHealth(@QueryParam("t") @DefaultValue("5000") int timeoutInMs, @QueryParam("maxAgeMs") @DefaultValue("90000") int maxAgeMs, @Context final UriInfo uriInfo);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getHealth(@QueryParam("t") @DefaultValue("5000") int timeoutInMs, @QueryParam("maxAgeMs") @DefaultValue("90000") int maxAgeMs, @Context final UriInfo uriInfo);
}
