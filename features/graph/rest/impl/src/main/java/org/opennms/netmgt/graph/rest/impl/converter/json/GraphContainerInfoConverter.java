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
