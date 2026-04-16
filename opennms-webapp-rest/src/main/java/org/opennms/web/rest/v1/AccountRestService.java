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
package org.opennms.web.rest.v1;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONObject;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST service supporting the Vue WelcomeModal "change password" wizard step.
 *
 * <p>When the admin user logs in with the default credentials, the
 * {@code OpenNMSAuthSuccessHandler} sets a session attribute so the Vue UI can
 * show an in-app prompt rather than a full-page JSP redirect.
 */
@Component("accountRestService")
@Path("account")
@Tag(name = "Account", description = "Current-user account operations")
@SuppressWarnings("java:S2068")
public class AccountRestService {

    // Must match OpenNMSAuthSuccessHandler.REQUIRES_PASSWORD_CHANGE_SESSION_ATTR
    static final String REQUIRES_PASSWORD_CHANGE_ATTR = "requiresPasswordChange";

    // Same rules as AbstractBasePasswordChangeActionServlet
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&.*+\\-]).{12,128})");
    private static final Pattern SAME_CHAR_PATTERN =
        Pattern.compile("(.)\\1{5}");

    private static final Logger LOG = LoggerFactory.getLogger(AccountRestService.class);

    @Autowired
    private UserManager userManager;

    /**
     * Returns whether the current session requires a password change.
     * The Vue WelcomeModal polls this once after login.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requiresPasswordChange")
    public Response getRequiresPasswordChange(@Context HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        final boolean required = session != null &&
            Boolean.TRUE.equals(session.getAttribute(REQUIRES_PASSWORD_CHANGE_ATTR));
        return Response.ok(new JSONObject().put("requiresPasswordChange", required).toString()).build();
    }

    /**
     * Clears the "requires password change" flag without changing the password.
     * Called when the user clicks Skip in the WelcomeModal.
     */
    @DELETE
    @Path("/requiresPasswordChange")
    public Response dismissRequiresPasswordChange(@Context HttpServletRequest request) {
        clearFlag(request);
        return Response.noContent().build();
    }

    /**
     * Changes the current user's password.
     * Verifies the current password and enforces complexity requirements.
     * Clears the session flag on success.
     *
     * <p>Request body (JSON):
     * <pre>{ "currentPassword": "...", "newPassword": "..." }</pre>
     *
     * <p>Returns 204 on success, 400 on bad input, 500 on internal error.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/changePassword")
    public Response changePassword(
            @Context HttpServletRequest request,
            @Context SecurityContext securityContext,
            final String body) {

        final String username = securityContext.getUserPrincipal().getName();

        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (Exception e) {
            return errorResponse(Status.BAD_REQUEST, "Invalid JSON body");
        }

        final String currentPassword = json.optString("currentPassword", "");
        final String newPassword = json.optString("newPassword", "");

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            return errorResponse(Status.BAD_REQUEST, "currentPassword and newPassword are required");
        }

        // Verify current password
        try {
            if (!userManager.comparePasswords(username, currentPassword)) {
                return errorResponse(Status.BAD_REQUEST, "Current password is incorrect");
            }
        } catch (Exception e) {
            LOG.error("Error verifying password for user {}", username, e);
            return errorResponse(Status.INTERNAL_SERVER_ERROR, "Error verifying current password");
        }

        // Validate complexity: must match complexity pattern AND must not have 6+ identical chars in a row
        if (!PASSWORD_PATTERN.matcher(newPassword).matches() || SAME_CHAR_PATTERN.matcher(newPassword).find()) {
            return errorResponse(Status.BAD_REQUEST,
                "Password must be 12–128 characters and contain at least one digit, " +
                "one lowercase letter, one uppercase letter, and one special character (!@#$%&.*+-). " +
                "Sequences of 6 or more identical characters are not allowed.");
        }

        // Apply the change
        try {
            final OnmsUser user = userManager.getOnmsUser(username);
            if (user == null) {
                return errorResponse(Status.NOT_FOUND, "User not found: " + username);
            }
            user.setPassword(userManager.encryptedPassword(newPassword, true));
            user.setPasswordSalted(true);
            userManager.save(user);
        } catch (IOException e) {
            LOG.error("Error loading user {}", username, e);
            return errorResponse(Status.INTERNAL_SERVER_ERROR, "Error loading user account");
        } catch (Exception e) {
            LOG.error("Error saving new password for user {}", username, e);
            return errorResponse(Status.INTERNAL_SERVER_ERROR, "Error saving new password");
        }

        clearFlag(request);
        return Response.noContent().build();
    }

    private void clearFlag(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(REQUIRES_PASSWORD_CHANGE_ATTR);
        }
    }

    private Response errorResponse(Status status, String message) {
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(new JSONObject().put("error", message).toString())
            .build();
    }
}
