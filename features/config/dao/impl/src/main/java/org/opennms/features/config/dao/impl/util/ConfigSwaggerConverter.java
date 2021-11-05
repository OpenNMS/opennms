/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.features.config.dao.impl.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.opennms.features.config.dao.api.ConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigSwaggerConverter {
    public static final String APPLICATION_JSON = "application/json";
    private final Logger LOG = LoggerFactory.getLogger(ConfigSwaggerConverter.class);

    private final Map<ConfigItem, Schema<?>> schemasByItem = new LinkedHashMap<>();
    private final Map<ConfigItem, String> pathsByItem = new LinkedHashMap<>();
    private final Map<String, PathItem> pathItemsByPath = new LinkedHashMap<>();

    private final OpenAPI openAPI = new OpenAPI();

    public String convertToString(ConfigItem item, String prefix, String acceptType) throws JsonProcessingException {
        OpenAPI openapi = convert(item, prefix);
        return convertOpenAPIToString(openapi, acceptType);
    }

    /**
     * convert open api object to specific string (default is yaml)
     *
     * @param openapi    schema
     * @param acceptType (json / yaml)
     * @return
     * @throws JsonProcessingException
     */
    public String convertOpenAPIToString(OpenAPI openapi, String acceptType) throws JsonProcessingException {
        ObjectMapper objectMapper;
        try {
            if (APPLICATION_JSON.equals(acceptType)) {
                objectMapper = Json.mapper();
            } else {
                objectMapper = Yaml.mapper();
            }
        } catch (Exception e) {
            LOG.warn("UNKNOWN MediaType: " + acceptType + " error: " + e.getMessage() + " using media type = yaml instead.");
            objectMapper = new ObjectMapper(new YAMLFactory());
        }

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        //TODO: dirty hack to remove exampleSetFlag
        if (objectMapper.getFactory() instanceof YAMLFactory) {
            final String intermediateJson = objectMapper.writeValueAsString(openapi);
            final String almostSwaggerJson = intermediateJson.replaceAll("[\\n\\r\\s]*exampleSetFlag.*,", "");
            return almostSwaggerJson.replaceAll(",?[\\n\\r\\s]*exampleSetFlag.*", "");
        } else {
            final String intermediateJson = objectMapper.writeValueAsString(openapi);
            final String almostSwaggerJson = intermediateJson.replaceAll("[\\n\\r\\s]*\"exampleSetFlag\".*,", "");
            return almostSwaggerJson.replaceAll(",?[\\n\\r\\s]*\"exampleSetFlag\".*", "");
        }
    }

    private Info genInfo() {
        // TODO: Freddy handle version properly
        Info info = new Info();
        info.setDescription("OpenNMS Data Model");
        info.setVersion("1.0.0");
        info.setTitle("OpenNMS Model");
        return info;
    }

    /**
     * replace servers part of openapi
     *
     * @param openapi
     * @param urls
     * @return
     */
    public OpenAPI setupServers(OpenAPI openapi, List<String> urls) {
        final List<Server> servers = new ArrayList<>(1);
        urls.forEach(url -> {
            Server server = new Server();
            server.setUrl(url);
            servers.add(server);
        });
        openapi.setServers(servers);
        return openapi;
    }

    /**
     * It will extract all API paths to generate a giant openapi with remote $ref schema
     *
     * @param openapiMap
     * @param prefix     (must include context path)
     * @return
     */
    public OpenAPI mergeAllPathsWithRemoteRef(Map<String, OpenAPI> openapiMap, String prefix) {
        OpenAPI allApi = new OpenAPI();
        allApi.setInfo(this.genInfo());
        allApi.setPaths(new Paths());

        openapiMap.forEach((configName, openapi) -> {
            Paths paths = openapi.getPaths();
            paths.forEach((name, path) -> {
                if (path.readOperations() == null)
                    return;
                path.readOperations().forEach((oper -> {
                    if (oper.getResponses() != null) {
                        oper.getResponses().forEach((resK, resV) -> {
                            if (resV.getContent() != null) {
                                resV.getContent().forEach((ck, cv) -> {
                                    if (cv.getSchema().get$ref() != null) {
                                        cv.getSchema().set$ref(prefix + "schema/" + configName + cv.getSchema().get$ref());
                                    }
                                });
                            }
                        });
                    }
                    if (oper.getRequestBody() != null && oper.getRequestBody().getContent() != null) {
                        oper.getRequestBody().getContent().forEach((ck, cv) -> {
                            if (cv.getSchema().get$ref() != null) {
                                cv.getSchema().set$ref(prefix + "schema/" + configName + cv.getSchema().get$ref());
                            }
                        });
                    }
                }));
                allApi.getPaths().putIfAbsent(name, path);
            });
        });
        return allApi;
    }

    public OpenAPI convert(ConfigItem item, String prefix) {
        return this.convert(item, prefix, false);
    }

    /**
     * Convert ConfigItem to OpenAPI
     *
     * @param item
     * @param prefix
     * @param isSingleConfig (it will disable post & delete API)
     * @return
     */
    public OpenAPI convert(ConfigItem item, String prefix, boolean isSingleConfig) {
        // Create an empty set of components
        Components components = new Components();
        openAPI.setComponents(components);

        // Create a basic info section
        Info info = this.genInfo();

        openAPI.setInfo(info);
        // Generate schemas for the items
        walk(null, item, this::generateSchemasForItems);
        schemasByItem.forEach((k, v) -> {
            if (ConfigItem.Type.OBJECT.equals(k.getType())) {
                components.addSchemas(v.getName(), v);
            }
        });

        if (prefix != null) {
            // Create an empty set of paths
            Paths paths = new Paths();
            openAPI.setPaths(paths);

            // Generate paths for the items
            this.generatePathsForItems(item,prefix , null, isSingleConfig);
            pathItemsByPath.forEach(paths::addPathItem);
        }

        return openAPI;
    }

//    public OpenAPI appendPaths(OpenAPI openapi, String prefix){
//        // Create an empty set of paths
//        Paths paths = new Paths();
//        openAPI.setPaths(paths);
//
//        // Generate paths for the items
//        this.generatePathsForItems(item, prefix, null);
//        pathItemsByPath.forEach(paths::addPathItem);
//    }

    /**
     * @param item
     * @param prefix
     * @param externalConfigName (use for generate external $ref)
     */
    private void generatePathsForItems(ConfigItem item, String prefix, String externalConfigName, boolean isSingleConfig) {
        String path = prefix;

        // Index the path for future reference
        pathsByItem.put(item, path);

        Schema schemaForCurrentItem = new Schema();
        schemaForCurrentItem.setName(item.getName());
        schemaForCurrentItem.set$ref(this.generate$ref(item, externalConfigName));

        PathItem configNamePathItem = new PathItem();
        PathItem configIdPathItem = new PathItem();

        String tagName = getTagName(path, prefix);
        Content jsonObjectContent = new Content();
        MediaType mediaType = new MediaType();
        mediaType.schema(schemaForCurrentItem);
        jsonObjectContent.addMediaType(APPLICATION_JSON, mediaType);

        // configId result content
        Content configIdContent = new Content();
        MediaType configIdMediaType = new MediaType();
        ArraySchema configIdParent = new ArraySchema();
        Schema configIdSchema = new StringSchema();
        configIdParent.setItems(configIdSchema);
        configIdMediaType.schema(configIdParent);
        configIdContent.addMediaType(APPLICATION_JSON, configIdMediaType);

        // configId path param
        List<Parameter> parameters = new ArrayList<>();
        Parameter configIdParam = new Parameter();
        configIdParam.setName("configId");
        configIdParam.setRequired(true);
        configIdParam.in("path");
        configIdParam.setSchema(new StringSchema());
        parameters.add(configIdParam);

        //============= POST =================
        if (!isSingleConfig) {
            Operation post = this.generateOperation(tagName, "Add " + item.getName() + " configuration", "empty",
                    parameters, jsonObjectContent, null);
            configIdPathItem.setPost(post);
        }

        //============== PUT =================
        Operation put = this.generateOperation(tagName, "Overwrite " + item.getName() + " configuration", "OK",
                isSingleConfig ? null : parameters, jsonObjectContent, null);
        configIdPathItem.setPut(put);

        //============== GET =================
        Operation get = this.generateOperation(tagName, "Get " + item.getName() + " configuration",
                item.getName() + " configuration", isSingleConfig ? null : parameters, null, jsonObjectContent);
        configIdPathItem.setGet(get);

        Operation getConfigIds = this.generateOperation(tagName, "Get " + item.getName() + " configIds",
                "configIds", null, null, configIdContent);
        configNamePathItem.setGet(getConfigIds);

        //============== DELETE =================
        if (!isSingleConfig) {
            Operation delete = this.generateOperation(tagName, "Delete " + item.getName() + " configuration",
                    item.getName() + " configuration", parameters, null, null);
            configIdPathItem.setDelete(delete);
        }

        // Save
        if(!isSingleConfig) {
            pathItemsByPath.put(path, configNamePathItem);
            pathItemsByPath.put(path + "/{configId}", configIdPathItem);
        } else {
            pathItemsByPath.put(path + "/default", configIdPathItem);
        }
    }

    private Operation generateOperation(String tagName, String summary, String description,
                                        List<Parameter> parameters,
                                        Content requestContent, Content responseContent) {
        Operation operation = new Operation();
        operation.tags(Arrays.asList(tagName));
        operation.summary(summary);

        if (parameters != null) {
            operation.parameters(parameters);
        }
        // Request body
        if (requestContent != null) {
            RequestBody requestBody = new RequestBody();
            requestBody.setContent(requestContent);
            operation.requestBody(requestBody);
        }

        // 200 OK
        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        if (responseContent != null) {
            apiResponse.setDescription(description);
            apiResponse.setContent(responseContent);
        } else {
            apiResponse.setDescription("empty");
        }
        apiResponses.addApiResponse("200", apiResponse);

        // 400 error
        Map<String, Schema> properties = new HashMap<>();
        Schema errorMessageSchema = new StringSchema();
        properties.put("message", errorMessageSchema);
        apiResponses.addApiResponse("400", getSimpleObjectResponse("Error message", properties));

        operation.responses(apiResponses);
        return operation;
    }

    private ApiResponse getSimpleObjectResponse(String description, Map<String, Schema> properties) {
        ApiResponse messageResponse = new ApiResponse();
        messageResponse.setDescription(description);
        Content messageContent = new Content();
        messageResponse.setContent(messageContent);
        MediaType mediaType = new MediaType();

        messageContent.addMediaType(APPLICATION_JSON.toString(), mediaType);

        ObjectSchema parentSchema = new ObjectSchema();

        mediaType.schema(parentSchema);
        properties.forEach(parentSchema::addProperties);
        return messageResponse;
    }

    private String getTagName(String path, String prefix) {
        String relevantPath = path.replace(prefix, "");

        if (relevantPath.isEmpty()) {
            // Top level config - use the last part of the prefix, should be the service name
            String[] prefixElements = path.split("/");
            return prefixElements[prefixElements.length - 1];
        }

        String[] pathElements = relevantPath.split("/");
        if (pathElements.length < 2) {
            return relevantPath;
        }
        return pathElements[1];
    }

    private List<String> buildUrlParamList(String path) {
        List<String> params = new ArrayList();
        Matcher m = Pattern.compile("\\{[\\w\\-\\.]+\\}").matcher(path);
        while (m.find()) {
            String urlItem = m.group();
            params.add(urlItem.substring(1, (urlItem.length() - 1)));
        }
        return params;
    }

    private void generateSchemasForItems(ConfigItem parent, ConfigItem item) {
        final Schema<?> schema;
        switch (item.getType()) {
            case OBJECT:
                schema = new ObjectSchema();
                break;
            case ARRAY:
                schema = new ArraySchema();
                break;
            case STRING:
                schema = new StringSchema();
                if (item.getPattern() != null) { // pattern only work on string
                    schema.setPattern(item.getPattern());
                }
                break;
            case NUMBER:
                schema = new NumberSchema();
                break;
            case INTEGER:
                schema = new IntegerSchema();
                break;
            case LONG:
                schema = new NumberSchema();
                schema.setFormat("int64");
                break;
            case BOOLEAN:
                schema = new BooleanSchema();
                break;
            case POSITIVE_INTEGER:
                schema = new IntegerSchema();
                schema.setMinimum(new BigDecimal(1));
                break;
            case NON_NEGATIVE_INTEGER:
                schema = new IntegerSchema();
                schema.setMinimum(new BigDecimal(0));
                break;
            case NEGATIVE_INTEGER:
                schema = new IntegerSchema();
                schema.setMaximum(new BigDecimal(-1));
                break;
            case DATE_TIME:
                schema = new DateTimeSchema();
                break;
            case DATE:
                schema = new DateSchema();
                break;
            default:
                throw new RuntimeException("Unsupported type " + item);
        }
        schema.setName(item.getName());
        if (item.getDocumentation() != null && !"".equals(item.getDocumentation().trim())) {
            schema.setDescription(item.getDocumentation());
        }
        if (item.getMultipleOf() != null) {
            if (schema instanceof NumberSchema || schema instanceof IntegerSchema)
                schema.setMultipleOf(BigDecimal.valueOf(item.getMultipleOf()));
        }
        if (item.getMin() != null) {
            if (schema instanceof StringSchema)
                schema.setMinLength(Math.toIntExact(item.getMin()));
            else if (schema instanceof ArraySchema)
                schema.setMinItems(Math.toIntExact(item.getMin()));
            else
                schema.setMinimum(BigDecimal.valueOf(item.getMin()));
        }
        if (item.getMax() != null) {
            if (schema instanceof StringSchema)
                schema.setMaxLength(Math.toIntExact(item.getMax()));
            else if (schema instanceof ArraySchema)
                schema.setMaxItems(Math.toIntExact(item.getMax()));
            else
                schema.setMaximum(BigDecimal.valueOf(item.getMax()));
        }

        if (item.getDefaultValue() != null) {
            schema.setDefault(item.getDefaultValue());
        }

        if (parent != null) {
            // Add the item to the parent
            Schema<?> schemaForParent = schemasByItem.get(parent);
            Schema<?> schemaForCurrentItem = schema;
            if (ConfigItem.Type.OBJECT.equals(item.getType())) {
                // Use a reference - these have no actual type set
                schemaForCurrentItem = new Schema();
                schemaForCurrentItem.setName(schema.getName());
                schemaForCurrentItem.set$ref(this.generate$ref(item));
            }

            if (ConfigItem.Type.ARRAY.equals(parent.getType())) {
                // If the parent is an array, then add the child as an item, and not a property
                ((ArraySchema) schemaForParent).setItems(schemaForCurrentItem);
            } else {
                schemaForParent.addProperties(schemaForCurrentItem.getName(), schemaForCurrentItem);
                if (item.isRequired()) {
                    schemaForParent.addRequiredItem(schemaForCurrentItem.getName());
                }
            }

        }

        // Index the schema for future reference
        schemasByItem.put(item, schema);
    }

    private String generate$ref(ConfigItem item) {
        return this.generate$ref(item, null);
    }

    /**
     * It help to generate $ref for openapi
     *
     * @param item
     * @param configName (It will generate external reference when it is not null)
     * @return
     */
    private String generate$ref(ConfigItem item, String configName) {
        if (configName != null) {
            return "/opennms/rest/cm/schema/" + configName + "#/components/schemas/" + item.getName();
        } else
            return "#/components/schemas/" + item.getName();
    }

    public void walk(ConfigItem parent, ConfigItem item, BiConsumer<ConfigItem, ConfigItem> consumer) {
        consumer.accept(parent, item);
        for (ConfigItem childItem : item.getChildren()) {
            walk(item, childItem, consumer);
        }
    }
}
