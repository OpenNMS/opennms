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

package org.opennms.features.config.rest.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.opennms.features.config.dao.api.ConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwaggerConverter {
    private static final Logger LOG = LoggerFactory.getLogger(SwaggerConverter.class);

    private final Map<ConfigItem, Schema<?>> schemasByItem = new LinkedHashMap<>();
    private final Map<ConfigItem, String> pathsByItem = new LinkedHashMap<>();
    private final Map<String, PathItem> pathItemsByPath = new LinkedHashMap<>();

    private final OpenAPI openAPI = new OpenAPI();

    private String prefix = "/";

    public String convertToString(ConfigItem item, String prefix, String type) {
        OpenAPI openapi = convert(item, prefix);

        try {
            return convertOpenAPIToString(openapi, type);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * convert open api object to specific string (default is yaml)
     *
     * @param openapi
     * @param type
     * @return
     * @throws JsonProcessingException
     */
    static public String convertOpenAPIToString(OpenAPI openapi, String type) throws JsonProcessingException {
        ObjectMapper objectMapper;
        try {
            org.springframework.http.MediaType mediaType = org.springframework.http.MediaType.valueOf(type);
            if (org.springframework.http.MediaType.APPLICATION_JSON.equals(mediaType) && mediaType != null) {
                objectMapper = new ObjectMapper();
            } else {
                objectMapper = new ObjectMapper(new YAMLFactory());
            }
        } catch (Exception e) {
            LOG.warn("UNKNOWN MideaType: " + type + " error: " + e.getMessage());
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

    public OpenAPI convert(ConfigItem item, String prefix) {
        this.prefix = prefix;

        // Create an empty set of components
        Components components = new Components();
        openAPI.setComponents(components);

        // Create a basic info section
        Info info = new Info();
        info.setDescription("OpenNMS Data Model");
        info.setVersion("1.0.0");
        info.setTitle("OpenNMS Model");

        openAPI.setInfo(info);
        // Generate schemas for the items
        walk(null, item, this::generateSchemasForItems);
        schemasByItem.forEach((k, v) -> {
            if (ConfigItem.Type.OBJECT.equals(k.getType())) {
                components.addSchemas(v.getName(), v);
            }
        });

        // Create an empty set of paths
        Paths paths = new Paths();
        openAPI.setPaths(paths);

        // Generate paths for the items
        walk(null, item, this::generatePathsForItems);
        pathItemsByPath.forEach(paths::addPathItem);

        return openAPI;
    }

    private void generatePathsForItems(ConfigItem parent, ConfigItem item) {
// TODO: check is that correct !!!
// Skip simple types - they can be set on the parent object and have no children
//        if (item.getType().isSimple()) {
//            return;
//        }
        // we only generate path for first level item (compare with the last checking, it looks better)
        if (parent != null) {
            return;
        }

        // Build the path to this element
        String path;
        if (parent != null) {
            path = pathsByItem.get(parent);
            if (ConfigItem.Type.ARRAY.equals(parent.getType())) {
                path += "/{" + item.getName() + "Index}";
            } else {
                path += "/" + item.getName();
            }
        } else {
            path = prefix;
        }

        // Index the path for future reference
        pathsByItem.put(item, path);

        Schema schemaForCurrentItem = new Schema();
        schemaForCurrentItem.setName(item.getName());
        schemaForCurrentItem.set$ref("#/components/schemas/" + item.getName());

        PathItem pathItem = new PathItem();

        List<String> urlParameters = buildUrlParamList(path);

        for (Iterator<String> iterator = urlParameters.iterator(); iterator.hasNext(); ) {
            String param = iterator.next();
            Parameter parameter = new Parameter();
            parameter.setName(param);
            parameter.setIn("path");
            parameter.setSchema(new IntegerSchema());
            parameter.setRequired(true);
            parameter.setDescription("Index of item in the array");
            pathItem.addParametersItem(parameter);
        }

        String tagName = getTagName(path);
        Content jsonObjectContent = new Content();
        MediaType mediaType = new MediaType();
        mediaType.schema(schemaForCurrentItem);
        jsonObjectContent.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON.toString(), mediaType);
        //============= POST =================
        Operation post = this.generateOperation(tagName, "Add Configure " + item.getName(), "OK",
                jsonObjectContent, true, false);
        pathItem.setPost(post);

        //============== PUT =================
        Operation put = this.generateOperation(tagName, "Overwrite " + item.getName(), "OK",
                jsonObjectContent, true, false);
        pathItem.setPut(put);

        //============== PATCH =================
        Operation patch = this.generateOperation(tagName, "Overwrite " + item.getName(), "OK",
                jsonObjectContent, true, false);
        pathItem.setPatch(patch);

        //============== GET =================
        Operation get = this.generateOperation(tagName, "Get " + item.getName() + " configuration",
                item.getName() + " configuration", jsonObjectContent, false, true);
        pathItem.setGet(get);

        //============== DELETE =================
        Operation delete = this.generateOperation(tagName, "Delete " + item.getName() + " configuration",
                item.getName() + " configuration", jsonObjectContent, false, false);
        pathItem.setDelete(delete);

        // Save
        pathItemsByPath.put(path, pathItem);
    }

    private Operation generateOperation(String tagName, String summary, String description,
                                        Content jsonObjectContent, boolean isGenRequestBody, boolean isGenResponseContent) {
        Operation operation = new Operation();
        operation.tags(Arrays.asList(tagName));
        operation.summary(summary);

        // Request body
        if (isGenRequestBody) {
            RequestBody requestBody = new RequestBody();
            requestBody.setContent(jsonObjectContent);
            operation.requestBody(requestBody);
        }

        // 200 OK
        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription(description);
        if (isGenResponseContent) {
            apiResponse.setContent(jsonObjectContent);
        }
        apiResponses.addApiResponse("200", apiResponse);

        // 400 error
        apiResponses.addApiResponse("400", getSimpleMessageResponse("Error message", "message"));

        //operation.requestBody(getSimpleRequest("fileId", "fileId"));
        operation.responses(apiResponses);
        return operation;
    }
/*
    private RequestBody getSimpleRequest(String description, String keyName) {
        RequestBody requestBody = new RequestBody();
        requestBody.setDescription(description);
        Content messageContent = new Content();
        requestBody.setContent(messageContent);
        MediaType mediaType = new MediaType();
        messageContent.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON.toString(), mediaType);

        Schema parentSchema = new ObjectSchema();
        Schema messageSchema = new StringSchema();

        mediaType.schema(parentSchema);
        parentSchema.addProperties(keyName, messageSchema);
        return requestBody;
    }*/

    private ApiResponse getSimpleMessageResponse(String description, String keyName) {
        ApiResponse messageResponse = new ApiResponse();
        messageResponse.setDescription(description);
        Content messageContent = new Content();
        messageResponse.setContent(messageContent);
        MediaType mediaType = new MediaType();
        messageContent.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON.toString(), mediaType);

        Schema parentSchema = new ObjectSchema();
        Schema messageSchema = new StringSchema();

        mediaType.schema(parentSchema);
        parentSchema.addProperties(keyName, messageSchema);
        return messageResponse;
    }

    private String getTagName(String path) {
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

        if (item.isMinSet()) {
            schema.setMinimum(BigDecimal.valueOf(item.getMin()));
        }
        if (item.isMaxSet()) {
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
                schemaForCurrentItem.set$ref("#/components/schemas/" + schema.getName());
            }

            if (ConfigItem.Type.ARRAY.equals(parent.getType())) {
                // If the parent is an array, then add the the child as an item, and not a property
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

    public static void walk(ConfigItem parent, ConfigItem item, BiConsumer<ConfigItem, ConfigItem> consumer) {
        consumer.accept(parent, item);
        for (ConfigItem childItem : item.getChildren()) {
            walk(item, childItem, consumer);
        }
    }

}