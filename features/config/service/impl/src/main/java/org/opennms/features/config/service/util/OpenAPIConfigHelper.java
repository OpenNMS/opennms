/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
package org.opennms.features.config.service.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;

import java.util.Map;

public class OpenAPIConfigHelper {
    // hide public constructor
    private OpenAPIConfigHelper() {
    }

    /**
     * It will walk through all properties in schema and insert default value / null into json if property not found.
     *
     * @param openapi
     * @param topLevelElement
     * @param configJsonObj
     */
    public static void fillWithDefaultValue(OpenAPI openapi, String topLevelElement, final JSONObject configJsonObj) {
        if (openapi == null) {
            return;
        }
        Map<String, Schema> schemaMap = openapi.getComponents().getSchemas();
        Schema<?> rootSchema = schemaMap.get(topLevelElement);
        if (rootSchema == null) {
            return;
        }
        for (var entry : rootSchema.getProperties().entrySet()) {
            var key = entry.getKey();
            var schema = entry.getValue();
            if (!configJsonObj.has(key)) {
                fillSingleValue(key, configJsonObj, schema, openapi, true);
            } else {
                Object property = configJsonObj.get(key);
                if (property instanceof JSONObject) {
                    JSONObject object = (JSONObject) property;
                    fillSingleValue(key, object, schema, openapi, false);
                } else if (property instanceof JSONArray) {
                    for (var item : (JSONArray) property) {
                        if (item instanceof JSONObject && schema instanceof ArraySchema) {
                            String schemaName = ((ArraySchema) schema).getItems().get$ref().replaceAll("^" + OpenAPIBuilder.SCHEMA_REF_TAG, "");
                            fillWithDefaultValue(openapi, schemaName, (JSONObject) item);
                        }
                    }
                }
            }
        }
    }

    private static void fillSingleValue(String key, final JSONObject configJsonObj, Schema<?> propertySchema, OpenAPI openapi, boolean isNewObject) {
        if (propertySchema instanceof ArraySchema) {
            configJsonObj.put(key, new JSONArray());
        } else if (isSimpleDataType(propertySchema)) {
            // only fill with default value, give up fill null values. It may cause validation exception
            if(propertySchema.getDefault() != null) {
                configJsonObj.put(key, propertySchema.getDefault());
            }
        } else if (propertySchema instanceof Schema && propertySchema.get$ref() != null) {
            String schemaName = propertySchema.get$ref().replaceAll("^" + OpenAPIBuilder.SCHEMA_REF_TAG, "");
            if(isNewObject)
            {
                JSONObject children = new JSONObject();
                fillWithDefaultValue(openapi, schemaName, children);
                // only add if children is not empty
                if (children.length() > 0) {
                    configJsonObj.put(key, children);
                }
            } else {
                fillWithDefaultValue(openapi, schemaName, configJsonObj);
            }
        } else {
            configJsonObj.put(key, (Object) null);
        }
    }

    private static boolean isSimpleDataType(Schema schema){
        return (schema instanceof StringSchema || schema instanceof NumberSchema
                || schema instanceof IntegerSchema || schema instanceof DateSchema
                || schema instanceof DateTimeSchema || schema instanceof BooleanSchema);
    }
}
