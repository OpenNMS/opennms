/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.web.api.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component("whoamiRestService")
@Path("whoami")
public class WhoamiRestService {

    @Autowired
    private UserManager userManager;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/")
    public Response whoami(@Context final SecurityContext securityContext) {
        final String userName = securityContext.getUserPrincipal().getName();
        final JSONArray userRoles = new JSONArray();
        for (String eachRole : Authentication.getAvailableRoles()) {
            if (securityContext.isUserInRole(eachRole)) {
                userRoles.put(eachRole);
            }
        }

        final JSONObject userInfo = new JSONObject();
        userInfo.put("id", userName);
        userInfo.put("roles", userRoles);
        userInfo.put("internal", false);

        // Check if the user exists in users.xml, if so it is an internal
        // user and email, full name, etc. can be populated
        try {
            final OnmsUser onmsUser = userManager.getOnmsUser(userName);
            if (onmsUser != null) {
                userInfo.put("internal", true);
                if (!Strings.isNullOrEmpty(onmsUser.getEmail())) {
                    userInfo.put("email", onmsUser.getEmail());
                }
                if (!Strings.isNullOrEmpty(onmsUser.getFullName())) {
                    userInfo.put("fullName", onmsUser.getFullName());
                }
            }
        } catch (IOException ex) {
            // ignore
        }
        return Response.ok().entity(userInfo.toString()).build();
    }
}
