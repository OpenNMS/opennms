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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Basic Web Service using REST for {@link OnmsMonitoringSystem} entity.
 */
@Component
@Path("monitoringSystems")
@Transactional
@Tag(name = "MonitoringSystems", description = "Monitoring Systems API")
public class MonitoringSystemsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringSystemsRestService.class);

    @Autowired
    private MonitoringSystemDao dao;

    public static class MonitoringSystemResponseDTO {
        public String id;
        public String label;
        public String location;
        public String type;
    }

    @GET
    @Path("/main")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get main monitoring system", description = "Get main monitoring system",
            operationId = "MonitoringSystemsRestServiceGetMainMonitoringSystem")
    public Response getMainMonitoringSystem(final @Context HttpServletRequest request) {
        try {
            OnmsMonitoringSystem system = dao.getMainMonitoringSystem();

            if (system != null) {
                MonitoringSystemResponseDTO dto = new MonitoringSystemResponseDTO();
                dto.id = system.getId();
                dto.label = system.getLabel();
                dto.location = system.getLocation();
                dto.type = system.getType();

                return Response.ok(dto).build();
            }

            return Response.noContent().build();
        } catch (Exception e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN)
                            .entity("Error getting monitoring system.").build());
        }
    }
}
