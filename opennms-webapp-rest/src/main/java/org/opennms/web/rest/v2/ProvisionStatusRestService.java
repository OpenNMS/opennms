/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.provision.service.ImportScheduler;
import org.opennms.web.rest.model.v2.EnlinkdDTO;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("provisiond")
@Transactional
public class ProvisionStatusRestService {

    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get a node's all types of links", description = "Get all types of links for a specific node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = @Content(schema = @Schema(implementation = EnlinkdDTO.class))),
            @ApiResponse(responseCode = "500", description = "Fail to get info.",
                    content = @Content)
    })
    public Response applicationStatus() {
        ImportScheduler importScheduler = BeanUtils.getBean("provisiondContext", "provisiondImportSchedule", ImportScheduler.class);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(importScheduler.getMonitors());
            return Response.ok(json).build();
        } catch (SchedulerException | JsonProcessingException e) {
            return Response.serverError().build();
        }
    }
}
