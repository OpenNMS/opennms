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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.web.rest.v2.model.OnCallRoleDto;
import org.opennms.web.rest.v2.model.OnCallRoleRenameRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Versioned on-call role (notification rota) management API, on the database. Admin-only
 * (enforced by Spring Security and in-code).
 */
@Path("oncall-roles")
@Tag(name = "On-Call Roles", description = "On-Call Role Management API")
public interface OnCallRolesRestApi {

    String IF_MATCH_DESCRIPTION = "Optional. The `ETag` the on-call role had when it was read; the write is refused with 412 if the role has changed since. A weak tag (`W/\"7\"`) or a comma-separated list of tags is accepted; `*` matches any existing role. If the role does not exist, the answer is 412 rather than 404.";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all on-call roles, with their schedules, by name", operationId = "listOnCallRoles")
    Response listOnCallRoles(@Context SecurityContext securityContext);

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get one on-call role", operationId = "getOnCallRole")
    Response getOnCallRole(@Context SecurityContext securityContext, @Context Request request, @PathParam("name") String name);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create an on-call role; the membership group and supervisor must exist, and a schedule needs at least one time", operationId = "createOnCallRole")
    Response createOnCallRole(@Context SecurityContext securityContext, OnCallRoleDto role);

    @PUT
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update an on-call role. Omitted fields are preserved (an empty description clears it); schedules, when sent, replace the stored ones", operationId = "updateOnCallRole")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response updateOnCallRole(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name, OnCallRoleDto role);

    @POST
    @Path("{name}/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Rename an on-call role in place (destination paths that name the role are not updated)", operationId = "renameOnCallRole")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response renameOnCallRole(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name, OnCallRoleRenameRequest body);

    @DELETE
    @Path("{name}")
    @Operation(summary = "Delete an on-call role", operationId = "deleteOnCallRole")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response deleteOnCallRole(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name);
}
