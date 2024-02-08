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
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.rest.api.Converter;
import org.opennms.netmgt.graph.rest.impl.converter.JsonPropertyConverterService;
import org.osgi.framework.BundleContext;

public class GraphContainerConverter implements Converter<ImmutableGraphContainer<?>, JSONObject> {

    private final BundleContext bundleContext;

    public GraphContainerConverter(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public boolean canConvert(Class<ImmutableGraphContainer<?>> type) {
        return ImmutableGraphContainer.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(ImmutableGraphContainer<?> input) {
        final JSONObject jsonContainer = new JSONObject();
        final JSONArray jsonGraphArray = new JSONArray();
        jsonContainer.put("graphs", jsonGraphArray);

        final GenericGraphContainer genericGraphContainer = input.asGenericGraphContainer();
        final Map<String, Object> properties = genericGraphContainer.getProperties();
        final JSONObject convertedProperties = new JsonPropertyConverterService(bundleContext).convert(properties);
        convertedProperties.toMap().forEach(jsonContainer::put);

        genericGraphContainer.getProperties().forEach((key, value) -> jsonContainer.put(key, value));
        input.getGraphs()
                .stream()
                .sorted(Comparator.comparing(GraphInfo::getNamespace))
                .forEach(graph -> {
                            final JSONObject jsonGraph = new GraphConverter(bundleContext).convert(graph);
                            jsonGraphArray.put(jsonGraph);
                        }
                );
        return jsonContainer;
    }
}
