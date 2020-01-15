/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.rest.impl.converter.json;

import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.rest.api.Converter;

public class GraphContainerInfoConverter implements Converter<GraphContainerInfo, JSONObject> {

    @Override
    public boolean canConvert(Class<GraphContainerInfo> type) {
        return GraphContainerInfo.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(GraphContainerInfo input) {
        final JSONObject jsonGraphContainerInfoObject = new JSONObject();
        jsonGraphContainerInfoObject.put("id", input.getId());
        jsonGraphContainerInfoObject.put("label", input.getLabel());
        jsonGraphContainerInfoObject.put("description", input.getDescription());

        final JSONArray graphInfoArray = new JSONArray();
        input.getGraphInfos().stream()
                .sorted(Comparator.comparing(GraphInfo::getNamespace))
                .forEach(graphInfo -> {
                    final JSONObject jsonGraphInfoObject = new JSONObject();
                    jsonGraphInfoObject.put("namespace", graphInfo.getNamespace());
                    jsonGraphInfoObject.put("label", graphInfo.getLabel());
                    jsonGraphInfoObject.put("description", graphInfo.getDescription());
                    graphInfoArray.put(jsonGraphInfoObject);
                });
        jsonGraphContainerInfoObject.put("graphs", graphInfoArray);
        return jsonGraphContainerInfoObject;
    }
}
