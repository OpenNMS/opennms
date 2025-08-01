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
package org.opennms.netmgt.flows.rest.classification;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("classifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ClassificationRestService {

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    Response getRules(@Context final UriInfo uriInfo);

    @GET
    @Path("{id}")
    Response getRule(@PathParam("id") int id);

    @POST
    Response saveRule(RuleDTO ruleDTO);

    @DELETE
    Response deleteRules(@Context final UriInfo uriInfo);

    @DELETE
    @Path("{id}")
    Response deleteRule(@PathParam("id") int id);

    @PUT
    @Path("{id}")
    Response updateRule(@PathParam("id") int id, RuleDTO newValue);

    @POST
    @Path("classify")
    Response classify(ClassificationRequestDTO classificationRequestDTO);

    @GET
    @Path("groups")
    Response getGroups(@Context final UriInfo uriInfo);

    @GET
    @Path("groups/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/comma-separated-values"})
    Response getGroup(@PathParam("id") int groupId,
                      @QueryParam("format") String format,
                      @QueryParam("filename") String requestedFilename,
                      @HeaderParam("Accept") String acceptHeader);

    @POST
    @Path("groups")
    Response saveGroup(GroupDTO groupDTO);

    @DELETE
    @Path("groups/{id}")
    Response deleteGroup(@PathParam("id") int groupId);

    @PUT
    @Path("groups/{id}")
    Response updateGroup(@PathParam("id") int id, GroupDTO newValue);

    @POST
    @Path("groups/{id}")
    @Consumes("text/comma-separated-values")
    Response importRules(@PathParam("id") int id, @Context UriInfo uriInfo, InputStream inputStream);

    @GET
    @Path("protocols")
    Response getProtocols();
}
