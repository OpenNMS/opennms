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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.rest.api.ConfigManagerRestService;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    /**
     * get or create a fake schema and return
     *
     * @param configName
     * @return
     */
    @Override
    public Response getRawSchema(String configName) {
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            if (schema.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(schema.get()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response getOpenApiSchema(String configName, String acceptType, HttpServletRequest request) {
        try {
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            if (schema.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
            String outStr = configSwaggerConverter.convertToString(schema.get().getConverter().getValidationSchema().getConfigItem(),
                    request.getContextPath() + BASE_PATH + schema.get().getName(), acceptType);
            return Response.ok(outStr).build();
        } catch (IOException | RuntimeException e) {
            LOG.error(e.getMessage());
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response getConfigIds(String configName) {
        try {
            Set<String> ids = configurationManagerService.getConfigIds(configName);
            return Response.ok(ids).build();
        } catch (IOException e) {
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response getConfig(String configName, String configId) {
        try {
            String jsonStr = configurationManagerService.getJSONStrConfiguration(configName, configId);
            return Response.ok(jsonStr).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response addConfig(String configName, String configId, String jsonStr) {
        try {
            configurationManagerService.registerConfiguration(configName, configId, new JSONObject(jsonStr));
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response updateConfig(String configName, String configId, String jsonStr) {
        try {
            configurationManagerService.updateConfiguration(configName, configId, new JSONObject(jsonStr));
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response deleteConfig(String configName, String configId) {
        try {
            configurationManagerService.unregisterConfiguration(configName, configId);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response listConfigs() {
        try {
            return Response.ok(configurationManagerService.getConfigNames()).build();
        } catch (IOException | RuntimeException e) {
            LOG.error("listConfigs: " + e.getMessage());
            return Response.serverError().build();
        }
    }

    /**
     * Generate simple error message response {"MESSAGE": "<ERROR MESSAGE>"}
     *
     * @param status
     * @param message
     * @return
     */
    private Response generateSimpleMessageResponse(Response.Status status, String message) {
        Map<String, String> messages = new HashMap<>();
        messages.put(MESSAGE_TAG, message);
        return Response.status(status).entity(messages).build();
    }
}
