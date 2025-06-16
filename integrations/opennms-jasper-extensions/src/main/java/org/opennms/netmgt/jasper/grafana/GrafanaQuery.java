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
package org.opennms.netmgt.jasper.grafana;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GrafanaQuery {
    private final String dashboardUid;

    private final int width;
    private final int height;
    private final String theme;

    private final Date from;
    private final Date to;
    private String timezone;

    private final Map<String, String> variables;

    public GrafanaQuery(String queryString) {
        JsonParser parser = new JsonParser();
        JsonObject jo = (JsonObject)parser.parse(queryString);
        JsonObject dashboard = jo.getAsJsonObject("dashboard");
        dashboardUid = dashboard.getAsJsonPrimitive("uid").getAsString();

        JsonObject time = jo.getAsJsonObject("time");
        from = new Date(time.getAsJsonPrimitive("from").getAsLong());
        to = new Date(time.getAsJsonPrimitive("to").getAsLong());
        if (time.has("tz")) {
            timezone = time.getAsJsonPrimitive("tz").getAsString();
        }

        JsonObject render = jo.getAsJsonObject("render");
        width = render.getAsJsonPrimitive("width").getAsInt();
        height = render.getAsJsonPrimitive("height").getAsInt();
        theme = render.getAsJsonPrimitive("theme").getAsString();

        JsonObject vars = jo.getAsJsonObject("variables");
        if (vars != null) {
            variables = vars.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> vars.getAsJsonPrimitive(e.getKey()).getAsString()));
        } else {
            variables = Collections.emptyMap();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDashboardUid() {
        return dashboardUid;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public Date getFrom() {
        return from;
    }

    public String getTimezone() {
        return timezone;
    }

    public Date getTo() {
        return to;
    }

    public String getTheme() {
        return theme;
    }
}
