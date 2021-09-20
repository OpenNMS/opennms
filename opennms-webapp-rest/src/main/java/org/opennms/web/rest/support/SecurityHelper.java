/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
