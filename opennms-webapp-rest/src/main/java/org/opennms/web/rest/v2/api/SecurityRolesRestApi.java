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

import org.opennms.web.rest.v2.model.SecurityRoleDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Versioned security role management API, on the database. Admin-only
 * (enforced by Spring Security and in-code).
 */
@Path("security-roles")
@Tag(name = "Security Roles", description = "Security Role Management API")
public interface SecurityRolesRestApi {

    String IF_MATCH_DESCRIPTION = "Optional. The `ETag` the role had when it was read; the write is refused with 412 if the role has changed since. A weak tag (`W/\"7\"`) or a comma-separated list of tags is accepted; `*` matches any existing role. If the role does not exist, the answer is 412 rather than 404.";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all security roles with the number of users that hold each (built-in roles first, then custom roles, each by name)", operationId = "listSecurityRoles")
    Response listSecurityRoles(@Context SecurityContext securityContext);

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get one security role (without the user count, which the list has)", operationId = "getSecurityRole")
    Response getSecurityRole(@Context SecurityContext securityContext, @Context Request request, @PathParam("name") String name);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a custom security role (a name without the ROLE_ prefix gets it; the name is upper-cased and limited to A-Z, 0-9 and _)", operationId = "createSecurityRole")
    Response createSecurityRole(@Context SecurityContext securityContext, SecurityRoleDto role);

    @PUT
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the description of a custom security role (an omitted description is preserved, an empty one clears it; built-in descriptions are read-only; roles cannot be renamed)", operationId = "updateSecurityRole")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response updateSecurityRole(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name, SecurityRoleDto role);

    @DELETE
    @Path("{name}")
    @Operation(summary = "Delete a custom security role (rejected while users hold it, and for built-in roles)", operationId = "deleteSecurityRole")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response deleteSecurityRole(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name);
}
