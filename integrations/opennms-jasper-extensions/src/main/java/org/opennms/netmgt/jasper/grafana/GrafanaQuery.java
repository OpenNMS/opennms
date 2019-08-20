/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
