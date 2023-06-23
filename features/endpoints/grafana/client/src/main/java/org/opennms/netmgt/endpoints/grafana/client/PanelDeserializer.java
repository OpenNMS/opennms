/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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
 *******************************************************************************/

package org.opennms.netmgt.endpoints.grafana.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.opennms.netmgt.endpoints.grafana.api.Panel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The PanelDeserializer is a custom JSON deserializer that handles the difference between
 * Grafana 7 and Grafana 8.  In 7, the dashboard panel's <code>datasource</code> property
 * is a JSON primitive string containing just the UID, but in Grafana 8 it is now an object
 * with <code>uid</code> and <code>type</code> properties.
 *
 * This deserializer should behave identical to the default deserializer in Gson
 * <i>except</i> for the special-case handling of <code>datasource</code>.
 */
public class PanelDeserializer implements JsonDeserializer<Panel> {
    private Gson gson;

    @Override
    public Panel deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final Panel p = new Panel();

        final JsonObject obj = jsonElement.getAsJsonObject();

        JsonElement prop;

        if (obj.has("id")) {
            prop = obj.get("id");
            p.setId(prop.getAsJsonPrimitive().getAsInt());
        }

        if (obj.has("title")) {
            prop = obj.get("title");
            p.setTitle(prop.getAsJsonPrimitive().getAsString());
        }

        if (obj.has("type")) {
            prop = obj.get("type");
            p.setType(prop.getAsJsonPrimitive().getAsString());
        }

        if (obj.has("datasource")) {
            prop = obj.get("datasource");
            if (prop.isJsonPrimitive()) {
                // older Grafana versions just had the datasource be a JSON string of the UID
                p.setDatasource(prop.getAsJsonPrimitive().getAsString());
            } else if (prop.isJsonObject()) {
                // newer contain an object with { uid: ..., type: ... }
                p.setDatasource(prop.getAsJsonObject().get("uid").getAsJsonPrimitive().getAsString());
            } else if (p.getType().equals("row") && prop.isJsonNull()) {
                // do nothing, let the datasource be null, since rows are skipped from the report
            } else {
                throw new JsonParseException("JSON element 'datasource' was expected to be either a uid string, or an object containing a uid string and optional type string, but instead was: " + jsonElement);
            }
        }

        if (obj.has("description")) {
            prop = obj.get("description");
            p.setDescription(prop.getAsJsonPrimitive().getAsString());
        }

        if (obj.has("panels")) {
            final List<Panel> panels = new ArrayList<>();
            final JsonArray arr = obj.get("panels").getAsJsonArray();
            for (int i=0; i < arr.size(); i++) {
                panels.add(this.getGson().fromJson(arr.get(i), Panel.class));
            }
            p.setPanels(panels);
        }
        return p;
    }

    private Gson getGson() {
        if (this.gson == null) {
            this.gson = new GsonBuilder().registerTypeAdapter(Panel.class, this).create();
        }
        return this.gson;
    }
}
