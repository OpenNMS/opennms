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
