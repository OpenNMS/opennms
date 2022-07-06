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

import static javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Component
@Path(ConfigurationManagerService.BASE_PATH + "/provisiond/" + ConfigDefinition.DEFAULT_CONFIG_ID)
@Transactional
public class ProvisiondRestService {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisiondRestService.class);

    @Autowired
    @Qualifier("provisiondConfigDao")
    private ProvisiondConfigurationDao m_configDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @GET
    @Path("requisition-def/import-name/{importName}/cron-schedule")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get specified cron-schedule of a requisition definition", description = "Get specified cron-schedule of a specified requisition definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cron schedule if specified on the requisition definition or empty string otherwise"),
            @ApiResponse(responseCode = "403", description = "User have no access rights"),
            @ApiResponse(responseCode = "404", description = "Definition not found"),
            @ApiResponse(responseCode = "500", description = "Internal error")})
    public Response getCronSchedule(@Context SecurityContext securityContext, @PathParam("importName") String importName) {

        SecurityHelper.assertUserReadCredentials(securityContext);

        ProvisiondConfiguration config;
        try {
            config = m_configDao.getConfig();
        } catch (IOException e) {
            LOG.error("Error on getting data from ProvisiondConfiguration",e);
            throw new InternalServerErrorException("Cannot get configuration");
        }

        final List<RequisitionDef> requisitionDefs = config.getRequisitionDefs();
        for(RequisitionDef requisitionDef:requisitionDefs) {
            if (requisitionDef.getImportName().orElse("").equals(importName)) {
                return Response.ok(requisitionDef.getCronSchedule().orElse("")).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("requisition-def/import-name/{importName}/cron-schedule")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Get specified cron-schedule of a requisition definition", description = "Get specified cron-schedule of a specified requisition definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully updated/rescheduled or unchanged"),
            @ApiResponse(responseCode = "400", description = "Parameter has wrong format"),
            @ApiResponse(responseCode = "404", description = "Definition not found"),
            @ApiResponse(responseCode = "500", description = "Internal error")})
    public Response updateCronSchedule(@Context final SecurityContext securityContext, @PathParam("importName") String importName, MultivaluedMapImpl params) {

        SecurityHelper.assertUserEditCredentials(securityContext);

        final String cronSchedule = params.getFirst("cronSchedule");

        if (!CronExpression.isValidExpression(cronSchedule)) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        final ProvisiondConfiguration config = getProvisiondConfiguration();

        final List<RequisitionDef> requisitionDefs = config.getRequisitionDefs();
        for(RequisitionDef requisitionDef:requisitionDefs) {
            if (requisitionDef.getImportName().orElse("").equals(importName)) {
                final String existingSchedule = requisitionDef.getCronSchedule().orElse("");
                if (!existingSchedule.equals(cronSchedule)) {
                    requisitionDef.setCronSchedule(cronSchedule);
                    sendConfigChangedEvent();
                }

                return Response.noContent().build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("requisition-def/import-name/{importName}/cron-schedule")
    @Operation(summary = "Delete specified cron-schedule of a requisition definition", description = "Delete specified cron-schedule of a specified requisition definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Definition not found"),
            @ApiResponse(responseCode = "500", description = "Internal error")})
    public Response clearCronSchedule(@Context final SecurityContext securityContext, @PathParam("importName") String importName) {

        SecurityHelper.assertUserEditCredentials(securityContext);

        final ProvisiondConfiguration config = getProvisiondConfiguration();

        final List<RequisitionDef> requisitionDefs = config.getRequisitionDefs();
        for(RequisitionDef requisitionDef:requisitionDefs) {
            if (requisitionDef.getImportName().orElse("").equals(importName)) {
                final String existingSchedule = requisitionDef.getCronSchedule().orElse("");
                if (!existingSchedule.isEmpty()) {
                    sendConfigChangedEvent();
                }

                return Response.noContent().build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private ProvisiondConfiguration getProvisiondConfiguration() {
        final ProvisiondConfiguration config;
        try {
            config = m_configDao.getConfig();
        } catch (IOException e) {
            LOG.error("Error on getting data from ProvisiondConfiguration",e);
            throw new InternalServerErrorException("Cannot get configuration");
        }
        return config;
    }

    private void sendConfigChangedEvent() {
       EventBuilder builder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "ReST");
       try {
           m_eventProxy.send(builder.getEvent());
       } catch (Throwable e) {
           throw new WebApplicationException(
                   Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(e.getMessage()).build());
       }
    }

}