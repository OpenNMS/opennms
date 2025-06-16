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
