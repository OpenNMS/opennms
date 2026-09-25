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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.web.rest.v2.model.OnCallRoleDto;
import org.opennms.web.rest.v2.model.OnCallRoleRenameRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Versioned on-call role management API backed by the roles section of
 * groups.xml. Admin-only (enforced by Spring Security and in-code).
 */
@Path("on-call-roles")
@Tag(name = "On-Call-Roles", description = "On-Call Role Management API")
public interface OnCallRolesRestApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all on-call roles", operationId = "listOnCallRoles")
    Response listRoles(@Context SecurityContext securityContext);

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get one on-call role including its schedules", operationId = "getOnCallRole")
    Response getRole(@Context SecurityContext securityContext, @PathParam("name") String name);

    @GET
    @Path("{name}/calendar")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Month view of who is on call, computed with the same resolution notifd uses (supervisor fills uncovered intervals)", operationId = "getOnCallCalendar")
    Response getCalendar(@Context SecurityContext securityContext, @PathParam("name") String name,
            @QueryParam("year") Integer year, @QueryParam("month") Integer month);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create an on-call role", operationId = "createOnCallRole")
    Response createRole(@Context SecurityContext securityContext, OnCallRoleDto role);

    @PUT
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update an on-call role (an omitted schedule list is preserved; a provided one replaces the schedules)", operationId = "updateOnCallRole")
    Response updateRole(@Context SecurityContext securityContext, @PathParam("name") String name, OnCallRoleDto role);

    @POST
    @Path("{name}/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Rename an on-call role", operationId = "renameOnCallRole")
    Response renameRole(@Context SecurityContext securityContext, @PathParam("name") String name, OnCallRoleRenameRequest request);

    @DELETE
    @Path("{name}")
    @Operation(summary = "Delete an on-call role", operationId = "deleteOnCallRole")
    Response deleteRole(@Context SecurityContext securityContext, @PathParam("name") String name);
}
