/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.template;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Merges an existing elastic template with provided (optional) settings.
 */
public class TemplateMerger {

    private static final String SETTINGS_KEY = "settings";
    private static final String INDEX_KEY = "index";

    public String merge(final String template, final IndexSettings indexSettings) {
        final JsonElement json = new JsonParser().parse(template);
        if (!json.isJsonObject()) {
            throw new IllegalArgumentException("Provided template is not a valid json object");
        }
        JsonObject object = merge(json.getAsJsonObject(), indexSettings);
        return new Gson().toJson(object);
    }

    public JsonObject merge(final JsonObject template, final IndexSettings indexSettings) {
        if (indexSettings != null && !indexSettings.isEmpty()) {
            addMissingProperties(template);

            final JsonObject indexObject = template.get(SETTINGS_KEY).getAsJsonObject().get(INDEX_KEY).getAsJsonObject();
            if (indexSettings.getNumberOfShards() != null) {
                indexObject.addProperty("number_of_shards", indexSettings.getNumberOfShards());
            }
            if (indexSettings.getNumberOfReplicas() != null) {
                indexObject.addProperty("number_of_replicas", indexSettings.getNumberOfReplicas());
            }
            if (indexSettings.getRefreshInterval() != null) {
                indexObject.addProperty("refresh_interval", indexSettings.getRefreshInterval());
            }
            if (indexSettings.getRoutingPartitionSize() != null) {
                indexObject.addProperty("routing_partition_size", indexSettings.getRoutingPartitionSize());
            }
        }
        return template;
    }

    private void addMissingProperties(final JsonObject template) {
        final JsonObject settings = addMissingProperty(template, SETTINGS_KEY);
        addMissingProperty(settings, INDEX_KEY);
    }

    private JsonObject addMissingProperty(JsonObject root, String property) {
        if (root.get(property) == null) {
            root.add(property, new JsonObject());
        }
        if (!root.get(property).isJsonObject()) {
            throw new IllegalArgumentException("Provided template contains property '" + property + "' must be of type JsonObject");
        }
        return root.get(property).getAsJsonObject();
    }


}
