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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <b>Currently for testing OSGI integration</b>
 */
public class ConfigManagerRestServiceImpl implements ConfigManagerRestService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManagerRestServiceImpl.class);
    private String BASE_PATH = "/rest/cm/";
    public static final String MESSAGE_TAG = "MESSAGE";

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    public void setConfigurationManagerService(ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = configurationManagerService;
    }

    @Override
    public Response getRawOpenApiSchema(final String configName) {
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            if(schema.isEmpty()){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
            OpenAPI openapi = configSwaggerConverter.convert(schema.get().getConverter().getValidationSchema().getConfigItem(),
                    BASE_PATH + schema.get().getName());
            return Response.ok(openapi).build();
        } catch (IOException | RuntimeException e) {
            LOG.error(e.getMessage());
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response getOpenApiSchema(String configName, String acceptType){
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            if(schema.isEmpty()){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
            String outStr = configSwaggerConverter.convertToString(schema.get().getConverter().getValidationSchema().getConfigItem(),
                    BASE_PATH + schema.get().getName(), acceptType);
            return Response.ok(outStr).build();
        } catch (IOException | RuntimeException e) {
            LOG.error(e.getMessage());
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
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
     * @param configName
     * @return
     */
    @Override
    public ConfigSchema getSchema(String configName) {
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            if (schema.isEmpty()) {
                // TODO: Freddy remove in PE-54
                configurationManagerService.registerSchema(configName, 29, 0, 0, ProvisiondConfiguration.class);
                schema = configurationManagerService.getRegisteredSchema(configName);
            }
            return schema.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
//            return Response.serverError().build();
        }
    }

    @Override
    public ConfigData getConfigFile(String configName, String filename) {
        return null;
    }

    @Override
    public ConfigData getView(String configName, String filename, Map<String, Object> inputParameters) {
        return null;
    }


    /**
     * Generate simple error message response {"MESSAGE": "<ERROR MESSAGE>"}
     * @param status
     * @param message
     * @return
     */
    private Response generateSimpleMessageResponse(Response.Status status, String message){
        Map<String,String> messages = new HashMap<>();
        messages.put(MESSAGE_TAG, message);
        return Response.status(status).entity(messages).build();
    }
}
