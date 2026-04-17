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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("account")
@Tag(name = "Account", description = "Current-user account operations")
public interface AccountRestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requiresPasswordChange")
    @Operation(summary = "Check if a password change is required", operationId = "AccountGetRequiresPasswordChange", tags = {"Account"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content)
    })
    Response getRequiresPasswordChange(@Context HttpServletRequest request);

    @DELETE
    @Path("/requiresPasswordChange")
    @Operation(summary = "Dismiss the password change requirement", operationId = "AccountDismissRequiresPasswordChange", tags = {"Account"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Flag cleared successfully", content = @Content)
    })
    Response dismissRequiresPasswordChange(@Context HttpServletRequest request);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/changePassword")
    @Operation(summary = "Change the current user's password", operationId = "AccountChangePassword", tags = {"Account"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request – invalid input or password does not meet requirements", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    Response changePassword(@Context HttpServletRequest request, @Context SecurityContext securityContext, String body);
}
