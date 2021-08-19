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

import io.swagger.v3.oas.models.OpenAPI;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;

@Path("/configManager")
@Produces("application/json")
@Consumes("application/json")
/**
 * <b>Currently for testing OSGI integration</b>
 */
public interface ConfigManagerRestService {
    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    Response listConfigs();

    @GET
    @Path("/schema/{configName}")
    Response getOpenApiSchema(@PathParam("configName") String config);

    @GET
    @Path("/schemaStr/{configName}")
    Response getOpenApiSchemaStr(@PathParam("configName") String config);

    @GET
    @Path("/{configName}")
    ConfigSchema getSchema(@PathParam("configName") String configName);

    @GET
    @Path("/{configName}/{configId}")
    ConfigData getConfigFile(@PathParam("configName") String configName, @PathParam("configId") String filename);

    @POST
    @Path("/{configName}/{configId}/attach")
    ConfigData getView(@PathParam("configName") String configName, @PathParam("configId") String filename, Map<String, Object> inputParameters);
}
