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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.web.rest.v2.model.GroupDto;
import org.opennms.web.rest.v2.model.GroupRenameRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Versioned group management API backed by the database. Admin-only
 * (enforced by Spring Security and in-code).
 */
@Path("groups")
@Tag(name = "Groups", description = "Group Management API")
public interface GroupsRestApi {

    String IF_MATCH_DESCRIPTION = "Optional. The `ETag` the group had when it was read; the write is refused with 412 if the group has changed since. A weak tag (`W/\"7\"`) or a comma-separated list of tags is accepted; `*` matches any existing group. If the group does not exist, the answer is 412 rather than 404.";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all groups", operationId = "listGroups")
    Response listGroups(@Context SecurityContext securityContext);

    @GET
    @Path("available-categories")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List the categories a group can be authorized for", operationId = "listAvailableCategories")
    Response listAvailableCategories(@Context SecurityContext securityContext);

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get one group", operationId = "getGroup")
    Response getGroup(@Context SecurityContext securityContext, @PathParam("name") String name);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a group", operationId = "createGroup")
    Response createGroup(@Context SecurityContext securityContext, GroupDto group);

    @PUT
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a group (fields not carried by the API are preserved; the user list order drives notification escalation)", operationId = "updateGroup")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response updateGroup(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name, GroupDto group);

    @POST
    @Path("{name}/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Rename a group (on-call roles referencing it follow the rename)", operationId = "renameGroup")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response renameGroup(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name, GroupRenameRequest body);

    @DELETE
    @Path("{name}")
    @Operation(summary = "Delete a group (rejected while on-call roles or destination paths reference it; the Admin group is protected)", operationId = "deleteGroup")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response deleteGroup(@Context SecurityContext securityContext, @Context HttpHeaders headers, @PathParam("name") String name);

    @GET
    @Path("item")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get one group by query parameter (for names a path cannot carry)", operationId = "getGroupByQuery")
    Response getGroupByQuery(@Context SecurityContext securityContext, @QueryParam("name") String name);

    @PUT
    @Path("item")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a group by query parameter (for names a path cannot carry)", operationId = "updateGroupByQuery")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response updateGroupByQuery(@Context SecurityContext securityContext, @Context HttpHeaders headers, @QueryParam("name") String name, GroupDto group);

    @POST
    @Path("item/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Rename a group by query parameter (for names a path cannot carry)", operationId = "renameGroupByQuery")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response renameGroupByQuery(@Context SecurityContext securityContext, @Context HttpHeaders headers, @QueryParam("name") String name, GroupRenameRequest body);

    @DELETE
    @Path("item")
    @Operation(summary = "Delete a group by query parameter (for names a path cannot carry)", operationId = "deleteGroupByQuery")
    @Parameters({
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, description = IF_MATCH_DESCRIPTION, schema = @Schema(type = "string"), example = "\"7\"")
    })
    Response deleteGroupByQuery(@Context SecurityContext securityContext, @Context HttpHeaders headers, @QueryParam("name") String name);
}
