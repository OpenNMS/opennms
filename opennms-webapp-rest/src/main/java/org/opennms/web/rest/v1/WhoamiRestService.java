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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Whoami", description = "Whoami API")
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
