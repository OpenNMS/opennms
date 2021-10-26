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

<<<<<<< HEAD
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.rest.api.ConfigManagerRestService;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

=======
import java.io.IOException;
import java.util.HashMap;
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
import java.util.Map;
import java.util.Optional;
import java.util.Set;

<<<<<<< HEAD
=======
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.converter.SwaggerConverter;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.rest.api.ConfigManagerRestService;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
/**
 * <b>Currently for testing OSGI integration</b>
 */
public class ConfigManagerRestServiceImpl implements ConfigManagerRestService {
<<<<<<< HEAD

    @Autowired
    private ConfigStoreDao configStoreDao;
=======
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManagerRestServiceImpl.class);
    private String BASE_PATH = "/rest/cm/";
    public static final String MESSAGE_TAG = "MESSAGE";
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63

    @Autowired
    private ConfigurationManagerService configurationManagerService;

<<<<<<< HEAD
    public void setConfigStoreDao(ConfigStoreDao configStoreDao) {
        this.configStoreDao = configStoreDao;
    }

=======
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
    public void setConfigurationManagerService(ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = configurationManagerService;
    }

<<<<<<< HEAD
    @Override
    public Set<String> listConfigNames() {
       return (Set<String>) configStoreDao.getConfigNames().get();
    }

    /**
     * get or create a fake schema and return
=======
    /**
     * get or create a fake schema and return
     *
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
     * @param configName
     * @return
     */
    @Override
<<<<<<< HEAD
    public ConfigSchema getSchema(String configName){
        try{
            Optional<ConfigSchema<?>> schema = configurationManagerService.getRegisteredSchema(configName);
            if(schema.isEmpty()){
                configurationManagerService.registerSchema(configName, 29,0,0,ProvisiondConfiguration.class);
                schema = configurationManagerService.getRegisteredSchema(configName);
            }
            return schema.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
//            return Response.serverError().build();
=======
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
    public Response getAllOpenApiSchema(String acceptType, HttpServletRequest request) throws JsonProcessingException {
        Map<String, ConfigSchema<?>> schemas = configurationManagerService.getAllConfigSchema();
        Map<String, ConfigItem> items = new HashMap<>();
        schemas.forEach((key, schema) -> {
            items.put(key, schema.getConverter().getValidationSchema().getConfigItem());
        });
        ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
        OpenAPI openapi = configSwaggerConverter.convert(request.getContextPath() + BASE_PATH, items);
        return Response.ok(configSwaggerConverter.convertOpenAPIToString(openapi, acceptType)).build();
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
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
        }
    }

    @Override
<<<<<<< HEAD
    public ConfigData getConfigFile(String configName, String filename) {
        return null;
    }

    @Override
    public ConfigData getView(String configName, String filename, Map<String, Object> inputParameters) {
        return null;
=======
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
            configurationManagerService.registerConfiguration(configName, configId, new JsonAsString(jsonStr));
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response updateConfig(String configName, String configId, String jsonStr) {
        try {
            configurationManagerService.updateConfiguration(configName, configId, new JsonAsString(jsonStr));
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
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
    }
}
