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
package org.opennms.features.config.service.util;

import io.swagger.v3.oas.models.media.*;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigDefinition;

import java.util.ArrayList;
import java.util.Map;

public class OpenAPIConfigHelper {
    public static void fillWithDefaultValue(ConfigDefinition configDefinition, final JSONObject configJsonObj) {
        if (configDefinition == null || configDefinition.getSchema() == null) {
            return;
        }
        Map<String, Schema> schemaMap = configDefinition.getSchema().getComponents().getSchemas();
        Schema rootSchema = schemaMap.get(configDefinition.getMetaValue(ConfigDefinition.TOP_LEVEL_ELEMENT_NAME_TAG));
        ((Map<String, Schema>) rootSchema.getProperties()).forEach((key, schema) -> {
            if (!configJsonObj.has(key)) {
                fillSingleValue(key, configJsonObj, schema);
            }
        });
    }

    public static void fillSingleValue(String key, final JSONObject configJsonObj, Schema propertySchema) {
        if (propertySchema instanceof ArraySchema) {
            configJsonObj.put(key, new ArrayList<>(0));
        } else if (propertySchema instanceof StringSchema || propertySchema instanceof NumberSchema || propertySchema instanceof IntegerSchema) {
            configJsonObj.put(key, propertySchema.getDefault());
        } else if (propertySchema.get$ref() != null) {
            //TODO: handle properly
        }
    }
}
