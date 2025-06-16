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
package org.opennms.web.rest.support;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.web.api.Authentication;

public class SecurityHelper {

    public static void assertUserReadCredentials(SecurityContext securityContext) {
        final String currentUser = securityContext.getUserPrincipal().getName();

        if (securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            // admin can do anything
            return;
        }
        if (securityContext.isUserInRole(Authentication.ROLE_REST) ||
                securityContext.isUserInRole(Authentication.ROLE_USER) ||
                securityContext.isUserInRole(Authentication.ROLE_MOBILE)) {
            return;
        }
        // otherwise
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is not allowed to read alarms.").type(MediaType.TEXT_PLAIN).build());
    }

    public static void assertUserEditCredentials(final SecurityContext securityContext, final String ackUser) {
        final String currentUser = securityContext.getUserPrincipal().getName();

        if (securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            // admin can do anything
            return;
        }
        if (securityContext.isUserInRole(Authentication.ROLE_READONLY)) {
            // read only is not allowed to edit
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is a read-only user!").type(MediaType.TEXT_PLAIN).build());
        }
        if (securityContext.isUserInRole(Authentication.ROLE_REST) ||
                securityContext.isUserInRole(Authentication.ROLE_USER) ||
                securityContext.isUserInRole(Authentication.ROLE_MOBILE)) {
            if (ackUser.equals(currentUser) || (!ackUser.equals(currentUser) && securityContext.isUserInRole(Authentication.ROLE_DELEGATE))) {
                // ROLE_REST and ROLE_MOBILE are allowed to modify things as long as it's as the
                // same user as they're logging in with, or if they also have ROLE_DELEGATE.
                return;
            }
        }
        // otherwise
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is not allowed to perform updates to alarms as user '" + ackUser + "'").type(MediaType.TEXT_PLAIN).build());
    }

}
