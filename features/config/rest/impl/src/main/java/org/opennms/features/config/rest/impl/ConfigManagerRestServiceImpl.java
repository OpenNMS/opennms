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

package org.opennms.features.config.rest.impl;

import io.swagger.v3.oas.models.OpenAPI;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.rest.api.ConfigManagerRestService;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * <b>Currently for testing OSGI integration</b>
 */
public class ConfigManagerRestServiceImpl implements ConfigManagerRestService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManagerRestServiceImpl.class);
    private String BASE_PATH = "/rest/cm/";

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    public void setConfigurationManagerService(ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = configurationManagerService;
    }

    @Override
    public Response getOpenApiSchema(final String configName) {
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            SwaggerConverter swaggerConverter = new SwaggerConverter();
            OpenAPI openapi = swaggerConverter.convert(schema.get().getConverter().getValidationSchema().getConfigItem(),
                    BASE_PATH + schema.get().getName());
            return Response.ok(openapi)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization").build();
        } catch (IOException | RuntimeException e) {
            LOG.error(e.getMessage());
            return Response.serverError().build();
        }
    }

    @Override
    public Response getOpenApiSchemaStr(final String configName) {
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            SwaggerConverter swaggerConverter = new SwaggerConverter();
            String outStr = swaggerConverter.convertToString(schema.get().getConverter().getValidationSchema().getConfigItem(),
                    BASE_PATH + schema.get().getName());
            return Response.ok(outStr)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization").build();
        } catch (IOException | RuntimeException e) {
            LOG.error(e.getMessage());
            return Response.serverError().build();
        }
    }

    @Override
    public Response listConfigs() {
        try {
            return Response.ok(configurationManagerService.getConfigNames()).build();
        } catch (IOException | RuntimeException e) {
            LOG.error(e.getMessage());
            return Response.serverError().build();
        }
    }

    /**
     * get or create a fake schema and return
     *
     * @param serviceName
     * @return
     */
    @Override
    public ConfigSchema getSchema(String serviceName) {
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(serviceName);
            if (schema.isEmpty()) {
                configurationManagerService.registerSchema(serviceName, 29, 0, 0, ProvisiondConfiguration.class);
                schema = configurationManagerService.getRegisteredSchema(serviceName);
            }
            return schema.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
//            return Response.serverError().build();
        }
    }

    @Override
    public ConfigData getConfigFile(String serviceName, String filename) {
        return null;
    }

    @Override
    public ConfigData getView(String serviceName, String filename, Map<String, Object> inputParameters) {
        return null;
    }
}
