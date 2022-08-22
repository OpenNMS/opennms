/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.PathVariable;

@Path("/cm")
@Produces("application/json")
@Consumes("application/json")
/**
 * <b>Currently for testing OSGI integration</b>
 */
public interface ConfigManagerRestService {
    /**
     * list registered configNames
     *
     * @return
     */
    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    Response listConfigs();

    @GET
    @Path("/schema")
    Response getAllOpenApiSchema(@HeaderParam("accept") String acceptType, @Context HttpServletRequest request);

    /**
     * get filtered OpenApi schema
     *
     * @param configName
     * @param acceptType
     * @return
     */
    @GET
    @Path("/schema/{configName}")
    Response getOpenApiSchema(@PathParam("configName") String configName, @HeaderParam("accept") String acceptType, @Context HttpServletRequest request);

    /**
     * get configIds
     *
     * @param configName
     * @return
     */
    @GET
    @Path("/{configName}")
    Response getConfigIds(@PathParam("configName") String configName);

    /**
     * get config by configName and configId
     *
     * @param configName
     * @param configId
     * @return
     */
    @GET
    @Path("/{configName}/{configId}")
    Response getConfig(@PathParam("configName") String configName, @PathParam("configId") String configId);

    /**
     * add new config by
     *
     * @param configName
     * @param configId
     * @param jsonStr
     * @return
     */
    @POST
    @Path("/{configName}/{configId}")
    Response addConfig(@PathParam("configName") String configName, @PathParam("configId") String configId, String jsonStr);

    /**
     * @param configName
     * @param configId
     * @param jsonStr
     * @param isReplace
     * @return
     */
    @PUT
    @Path("/{configName}/{configId}")
    Response updateConfig(@PathParam("configName") String configName, @PathParam("configId") String configId, @QueryParam("replace") boolean isReplace, String jsonStr);

    /**
     * delete config by configName and configId
     *
     * @param configName
     * @param configId
     * @return
     */
    @DELETE
    @Path("/{configName}/{configId}")
    Response deleteConfig(@PathParam("configName") String configName, @PathParam("configId") String configId);

    /**
     * Get config part specified by configName, configId and path to the part
     *
     * @param configName configuration name
     * @param configId configuration id
     * @param path path to the part
     * @return requested part of configuration
     */
    @GET
    @Path(value="/{configName}/{configId}/{path:.*}")
    Response getConfigPart(@PathVariable("configName") String configName, @PathVariable("configId") String configId,
                           @PathVariable("path") String path);

    /**
     * Update config part specified by configName, configId and path to the part
     *
     * @param configName configuration name
     * @param configId configuration id
     * @param path path to the part
     * @return empty response
     */
    @PUT
    @Path(value="/{configName}/{configId}/{path:.+}")
    Response updateConfigPart(@PathVariable("configName") String configName, @PathVariable("configId") String configId,
                              @PathVariable("path") String path, String newContent);

    /**
     * Add an array element to a config specified by configName, configId and path to the array
     *
     * @param configName configuration name
     * @param configId configuration id
     * @param path path to the array
     * @param newElement a new element to add to array in configuration
     * @return empty response
     */
    @POST
    @Path(value="/{configName}/{configId}/{path:.+}")
    Response appendToArrayInConfig(@PathVariable("configName") String configName, @PathVariable("configId") String configId,
                                   @PathVariable("path") String path, String newElement);

    /**
     * Delete config part specified by configName, configId and path to the part
     *
     * @param configName configuration name
     * @param configId configuration id
     * @param path path to the part
     * @return empty response
     */
    @DELETE
    @Path(value="/{configName}/{configId}/{path:.+}")
    Response deleteConfigPart(@PathVariable("configName") String configName, @PathVariable("configId") String configId,
                              @PathVariable("path") String path);

}
