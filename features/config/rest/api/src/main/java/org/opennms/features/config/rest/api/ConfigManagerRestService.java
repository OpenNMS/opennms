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

package org.opennms.features.config.rest.api;

<<<<<<< HEAD
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/configManager")
=======
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cm")
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
@Produces("application/json")
@Consumes("application/json")
/**
 * <b>Currently for testing OSGI integration</b>
 */
public interface ConfigManagerRestService {
<<<<<<< HEAD
    @GET
    @Produces(value={MediaType.APPLICATION_JSON})
    Set<String> listConfigNames();

    @GET
    @Path("/{configName}")
    ConfigSchema getSchema(@PathParam("configName") String configName);

    @GET
    @Path("/{configName}/{configId}")
    ConfigData getConfigFile(@PathParam("configName") String configName, @PathParam("configId") String filename);

    @POST
    @Path("/{configName}/{configId}/attach")
    ConfigData getView(@PathParam("configName") String configName, @PathParam("configId") String filename, Map<String, Object> inputParameters);
=======
    /**
     * list registered configNames
     * @return
     */
    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    Response listConfigs();

    /**
     * get raw OpenApi schema (for debug use, <b>SHOULD REMOVE BEFORE PRODUCTION</b>)
     * @param configName
     * @return
     */
    @GET
    @Path("/schema/raw/{configName}")
    Response getRawSchema(@PathParam("configName") String configName);

    @GET
    @Path("/schema/all")
    Response getAllOpenApiSchema(@HeaderParam("accept") String acceptType, @Context HttpServletRequest request) throws JsonProcessingException;

    /**
     * get filtered OpenApi schema
     * @param configName
     * @param acceptType
     * @return
     */
    @GET
    @Path("/schema/{configName}")
    Response getOpenApiSchema(@PathParam("configName") String configName, @HeaderParam("accept") String acceptType, @Context HttpServletRequest request);

    /**
     * get configIds
     * @param configName
     * @return
     */
    @GET
    @Path("/{configName}")
    Response getConfigIds(@PathParam("configName") String configName);

    /**
     * get config by configName and configId
     * @param configName
     * @param configId
     * @return
     */
    @GET
    @Path("/{configName}/{configId}")
    Response getConfig(@PathParam("configName") String configName, @PathParam("configId") String configId);

    /**
     * add new config by
     * @param configName
     * @param configId
     * @param jsonStr
     * @return
     */
    @POST
    @Path("/{configName}/{configId}")
    Response addConfig(@PathParam("configName") String configName, @PathParam("configId") String configId, String jsonStr);

    /**
     *
     * @param configName
     * @param configId
     * @param jsonStr
     * @return
     */
    @PUT
    @Path("/{configName}/{configId}")
    Response updateConfig(@PathParam("configName") String configName, @PathParam("configId") String configId, String jsonStr);


    /**
     * delete config by configName and configId
     * @param configName
     * @param configId
     * @return
     */
    @DELETE
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/{configName}/{configId}")
    Response deleteConfig(@PathParam("configName") String configName, @PathParam("configId") String configId);
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
}
