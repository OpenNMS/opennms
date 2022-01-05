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
 ******************************************************************************/
package org.opennms.features.config.service.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;

import java.util.ArrayList;
import java.util.Map;

public class OpenAPIConfigHelper {
    // hide public constructor
    private OpenAPIConfigHelper(){}

    /**
     * It will walk through all properties in schema and insert default value / null into json if property not found.
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
        (rootSchema.getProperties()).forEach((key, schema) -> {
            if (!configJsonObj.has(key)) {
                fillSingleValue(key, configJsonObj, schema, openapi);
            } else {
                Object property = configJsonObj.get(key);
                if(property instanceof JSONObject){
                    JSONObject object = (JSONObject) property;
                    fillSingleValue(key, object, schema, openapi);
                } else if (property instanceof JSONArray){
                    JSONArray array = (JSONArray) property;
                    if(array.length() > 0) {
                        array.forEach(item -> {
                            if (item instanceof JSONObject && schema instanceof ArraySchema) {
                                String schemaName = ((ArraySchema) schema).getItems().get$ref().replaceAll("^" + OpenAPIBuilder.SCHEMA_REF_TAG, "");
                                fillWithDefaultValue(openapi, schemaName, (JSONObject) item);
                            }
                        });
                    }
                }
            }
        });
    }

    private static void fillSingleValue(String key, final JSONObject configJsonObj, Schema<?> propertySchema, OpenAPI openapi) {
        if (propertySchema instanceof ArraySchema) {
            configJsonObj.put(key, new ArrayList<>(0));
        } else if (propertySchema instanceof StringSchema || propertySchema instanceof NumberSchema
                || propertySchema instanceof IntegerSchema || propertySchema instanceof DateSchema
                || propertySchema instanceof DateTimeSchema || propertySchema instanceof BooleanSchema) {
            configJsonObj.put(key, propertySchema.getDefault() == null ? JSONObject.NULL : propertySchema.getDefault());
        } else if (propertySchema instanceof Schema && propertySchema.get$ref() != null) {
            String schemaName = propertySchema.get$ref().replaceAll("^" + OpenAPIBuilder.SCHEMA_REF_TAG, "");

            JSONObject newObject = new JSONObject();
            configJsonObj.put(key, newObject);
            fillWithDefaultValue(openapi, schemaName, newObject);
        } else {
            configJsonObj.put(key, (Object) null);
        }
    }
}
