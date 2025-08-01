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
package org.opennms.netmgt.graph.rest.impl.converter;

import java.util.Objects;

import org.json.JSONObject;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.IpInfo;
import org.opennms.netmgt.graph.rest.impl.converter.json.GraphContainerConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.GraphContainerInfoConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.GraphConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.IpInfoConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.VertexConverter;
import org.osgi.framework.BundleContext;

public class JsonConverterService {

    private final BundleContext bundleContext;

    public JsonConverterService(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    public JSONObject convert(GraphContainerInfo containerInfo) {
        return new GraphContainerInfoConverter().convert(containerInfo);
    }

    public JSONObject convert(ImmutableGraphContainer<?> graphContainer) {
        return new GraphContainerConverter(bundleContext).convert(graphContainer);
    }

    public JSONObject convert(ImmutableGraph<?, ?> graph) {
        return new GraphConverter(bundleContext).convert(graph);
    }

    public JSONObject convert(Vertex vertex) {
        return new VertexConverter(bundleContext).convert(vertex);
    }

    public JSONObject convert(IpInfo ipInfo) {
        return new IpInfoConverter().convert(ipInfo);
    }
}
