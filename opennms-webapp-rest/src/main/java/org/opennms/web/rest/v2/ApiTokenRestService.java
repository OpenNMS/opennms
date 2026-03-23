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
package org.opennms.web.rest.v2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.features.apitokens.ApiTokenCreateRequest;
import org.opennms.features.apitokens.ApiTokenCreateResponse;
import org.opennms.features.apitokens.ApiTokenService;
import org.opennms.features.apitokens.ApiToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("apiTokens")
@Transactional
@Tag(name = "API Tokens", description = "API Token Management")
public class ApiTokenRestService {

    @Autowired(required = false)
    private ApiTokenService apiTokenService;

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createToken(@Context SecurityContext securityContext,
                                @Context UriInfo uriInfo,
                                @QueryParam("username") String targetUsername,
                                ApiTokenCreateRequest request) {
        requireService();
        String callerUsername = securityContext.getUserPrincipal().getName();
        String effectiveUsername = callerUsername;

        if (targetUsername != null && !targetUsername.equals(callerUsername)) {
            requireAdmin(securityContext);
            effectiveUsername = targetUsername;
        }

        try {
            ApiTokenCreateResponse response = apiTokenService.createToken(
                effectiveUsername,
                request != null ? request.getDescription() : null,
                request != null ? request.getExpiresInDays() : null
            );
            Map<String, Object> entity = new LinkedHashMap<>();
            entity.put("id", response.getId());
            entity.put("token", response.getToken());
            entity.put("description", response.getDescription());
            entity.put("createdAt", formatDate(response.getCreatedAt()));
            entity.put("expiresAt", formatDate(response.getExpiresAt()));
            return Response.created(uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(response.getId())).build())
                    .entity(entity)
                    .build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Map<String, Object>> listTokens(@Context SecurityContext securityContext,
                                     @QueryParam("username") String targetUsername) {
        requireService();
        String callerUsername = securityContext.getUserPrincipal().getName();
        String effectiveUsername = callerUsername;

        if (targetUsername != null && !targetUsername.equals(callerUsername)) {
            requireAdmin(securityContext);
            effectiveUsername = targetUsername;
        }

        List<ApiToken> tokens = apiTokenService.listTokens(effectiveUsername);
        // Map to safe representation — exclude tokenHash to prevent leaking
        return tokens.stream().map(this::toSafeMap).collect(Collectors.toList());
    }

    private Map<String, Object> toSafeMap(ApiToken t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("description", t.getDescription());
        m.put("createdAt", formatDate(t.getCreatedAt()));
        m.put("expiresAt", formatDate(t.getExpiresAt()));
        m.put("lastUsedAt", formatDate(t.getLastUsedAt()));
        return m;
    }

    private static String formatDate(Date d) {
        if (d == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(d);
    }

    @DELETE
    @Path("{id}")
    public Response revokeToken(@Context SecurityContext securityContext,
                                @PathParam("id") Integer id) {
        requireService();
        String callerUsername = securityContext.getUserPrincipal().getName();
        String tokenOwner = apiTokenService.getTokenOwner(id);

        if (tokenOwner == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!tokenOwner.equals(callerUsername) && !securityContext.isUserInRole("ROLE_ADMIN")) {
            // Return 404 to avoid leaking token existence
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        apiTokenService.revokeToken(id);
        return Response.noContent().build();
    }

    @DELETE
    public Response revokeAllTokens(@Context SecurityContext securityContext,
                                    @QueryParam("username") String targetUsername) {
        requireService();
        if (targetUsername == null) {
            throw new WebApplicationException("username parameter required", Response.Status.BAD_REQUEST);
        }
        String callerUsername = securityContext.getUserPrincipal().getName();
        if (!targetUsername.equals(callerUsername)) {
            requireAdmin(securityContext);
        }
        apiTokenService.revokeAllTokens(targetUsername);
        return Response.noContent().build();
    }

    private void requireAdmin(SecurityContext securityContext) {
        if (!securityContext.isUserInRole("ROLE_ADMIN")) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private void requireService() {
        if (apiTokenService == null) {
            throw new WebApplicationException("API token service not available",
                    Response.Status.SERVICE_UNAVAILABLE);
        }
    }
}
