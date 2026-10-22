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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.web.rest.v2.model.UserDto;
import org.opennms.web.rest.v2.model.UserPasswordRequest;
import org.opennms.web.rest.v2.model.UserRenameRequest;
import org.opennms.web.rest.v2.model.UserWriteRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Versioned user management API backed by users.xml. Password hashes are
 * never returned; admin-only (enforced by Spring Security and in-code).
 */
@Path("users")
@Tag(name = "Users", description = "User Management API")
public interface UsersRestApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all users", operationId = "listUsers")
    Response listUsers(@Context SecurityContext securityContext);

    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get one user", operationId = "getUser")
    Response getUser(@Context SecurityContext securityContext, @PathParam("userId") String userId);

    @GET
    @Path("available-roles")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List the assignable security roles", operationId = "listAvailableRoles")
    Response listAvailableRoles(@Context SecurityContext securityContext);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a user", operationId = "createUser")
    Response createUser(@Context SecurityContext securityContext, UserWriteRequest request);

    @PUT
    @Path("{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a user's details (contact types and password not covered here are preserved)", operationId = "updateUser")
    Response updateUser(@Context SecurityContext securityContext, @PathParam("userId") String userId, UserDto user);

    @PUT
    @Path("{userId}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Set a user's password (stored salted)", operationId = "setUserPassword")
    Response setPassword(@Context SecurityContext securityContext, @PathParam("userId") String userId, UserPasswordRequest request);

    @POST
    @Path("{userId}/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Rename a user (also updates group memberships)", operationId = "renameUser")
    Response renameUser(@Context SecurityContext securityContext, @PathParam("userId") String userId, UserRenameRequest request);

    @DELETE
    @Path("{userId}")
    @Operation(summary = "Delete a user (also removes group memberships; admin and rtc are protected)", operationId = "deleteUser")
    Response deleteUser(@Context SecurityContext securityContext, @PathParam("userId") String userId);
}
