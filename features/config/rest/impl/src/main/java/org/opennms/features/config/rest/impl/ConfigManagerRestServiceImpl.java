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
package org.opennms.features.config.rest.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.ConfigSwaggerConverter;
import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.rest.api.ConfigManagerRestService;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * <b>Currently for testing OSGI integration</b>
 */
public class ConfigManagerRestServiceImpl implements ConfigManagerRestService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManagerRestServiceImpl.class);
    public static final String MESSAGE_TAG = "MESSAGE";

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    public void setConfigurationManagerService(ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = configurationManagerService;
    }

    @Override
    public Response getAllOpenApiSchema(String acceptType, HttpServletRequest request) {
        Map<String, ConfigDefinition> defs = configurationManagerService.getAllConfigDefinitions();
        Map<String, OpenAPI> apis = defs.entrySet().stream().filter(entry -> {
            OpenAPI api = entry.getValue().getSchema();
            return (api != null && api.getPaths() != null);
        }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getSchema()));
        ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
        OpenAPI allAPI = configSwaggerConverter.mergeAllPathsWithRemoteRef(apis, request.getContextPath() + ConfigurationManagerService.BASE_PATH);
        allAPI = ConfigSwaggerConverter.setupServers(allAPI, Arrays.asList(new String[]{request.getContextPath()}));
        String outStr = configSwaggerConverter.convertOpenAPIToString(allAPI, acceptType);
        return Response.ok(outStr).build();
    }

    @Override
    public Response getOpenApiSchema(String configName, String acceptType, HttpServletRequest request) {
        try {
            Optional<ConfigDefinition> def = configurationManagerService.getRegisteredConfigDefinition(configName);
            if (def.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ConfigSwaggerConverter configSwaggerConverter = new ConfigSwaggerConverter();
            OpenAPI openapi = def.get().getSchema();
            openapi = ConfigSwaggerConverter.setupServers(openapi, Arrays.asList(new String[]{request.getContextPath()}));
            String outStr = configSwaggerConverter.convertOpenAPIToString(openapi, acceptType);
            return Response.ok(outStr).build();
        } catch (ConfigRuntimeException e) {
            LOG.error(e.getMessage());
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response getConfigIds(String configName) {
        try {
            Set<String> ids = configurationManagerService.getConfigIds(configName);
            return Response.ok(ids).build();
        } catch (ConfigRuntimeException e) {
            LOG.error("Fail to getConfigIds for configName: {}, message: {}", configName, e.getMessage());
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response getConfig(String configName, String configId) {
        try {
            Optional<String> json = configurationManagerService.getJSONStrConfiguration(configName, configId);
            if (json.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(json.get()).build();
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
    public Response updateConfig(String configName, String configId, boolean isReplace, String jsonStr) {
        try {
            configurationManagerService.updateConfiguration(configName, configId, new JsonAsString(jsonStr), isReplace);
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
    public Response getConfigPart(String configName, String configId, String path) {
        try {
            Optional<String> json = configurationManagerService.getJSONStrConfiguration(configName, configId);
            if (json.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(JsonPathHelper.get(json.get(), path)).build();
        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public Response updateConfigPart(String configName, String configId, String path, String newPartContent) {
        try {
            Optional<String> json = configurationManagerService.getJSONStrConfiguration(configName, configId);
            if (json.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String newJson = JsonPathHelper.update(json.get(), path, newPartContent);

            configurationManagerService.updateConfiguration(configName, configId, new JsonAsString(newJson), true);

        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
        return Response.ok().build();
    }

    @Override
    public Response updateOrInsertConfigPart(String configName, String configId, String pathToParent, String nodeName,
                                             String newPartContent) {
        try {
            Optional<String> json = configurationManagerService.getJSONStrConfiguration(configName, configId);
            if (json.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String newJson = JsonPathHelper.insertOrUpdateNode(json.get(), pathToParent, nodeName, newPartContent);

            configurationManagerService.updateConfiguration(configName, configId, new JsonAsString(newJson), true);

        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
        return Response.ok().build();
    }

    @Override
    public Response appendToArrayInConfig(String configName, String configId, String path, String newElement) {
        try {
            Optional<String> json = configurationManagerService.getJSONStrConfiguration(configName, configId);
            if (json.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String newJson = JsonPathHelper.append(json.get(),path, newElement);

            configurationManagerService.updateConfiguration(configName, configId, new JsonAsString(newJson), true);

        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
        return Response.ok().build();
    }

    @Override
    public Response deleteConfigPart(String configName, String configId, String path) {
        try {
            Optional<String> json = configurationManagerService.getJSONStrConfiguration(configName, configId);
            if (json.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String newJson = JsonPathHelper.delete(json.get(),path);

            configurationManagerService.updateConfiguration(configName, configId, new JsonAsString(newJson), true);

        } catch (Exception e) {
            LOG.error("configName: {}, configId: {}", configName, configId, e);
            return this.generateSimpleMessageResponse(Response.Status.BAD_REQUEST, e.getMessage());
        }
        return Response.ok().build();
    }

    @Override
    public Response listConfigs() {
        try {
            return Response.ok(configurationManagerService.getConfigNames()).build();
        } catch (ConfigRuntimeException e) {
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
