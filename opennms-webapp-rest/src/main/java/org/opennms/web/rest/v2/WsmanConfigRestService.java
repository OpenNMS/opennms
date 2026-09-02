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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.WsmanConfigDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Read access to wsman-config.xml (agent defaults and per-address
 * definitions) for the Manage WS-Man page. The file carries agent
 * credentials, so every method is admin-only and passwords are never
 * returned.
 */
@Component
@Path("wsman-config")
@Tag(name = "WsmanConfig", description = "WS-Man agent configuration API")
public class WsmanConfigRestService {

    @Autowired
    private WSManConfigDao wsManConfigDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the WS-Man agent configuration", description = "Agent defaults and definitions from wsman-config.xml; passwords are reported as present or absent only", operationId = "WsmanConfigRestServiceGetConfig")
    public Response getConfig(@Context final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(WsmanConfigDto.from(wsManConfigDao.getConfig())).build();
    }
}
