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

import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.rest.api.Converter;
import org.opennms.netmgt.graph.rest.impl.converter.JsonPropertyConverterService;
import org.osgi.framework.BundleContext;

public class VertexConverter implements Converter<Vertex, JSONObject> {

    private final BundleContext bundleContext;

    public VertexConverter(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public boolean canConvert(Class<Vertex> type) {
        return Vertex.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(Vertex input) {
        final GenericVertex genericVertex = input.asGenericVertex();
        final Map<String, Object> properties = genericVertex.getProperties();
        final JSONObject jsonVertex = new JsonPropertyConverterService(bundleContext).convert(properties);
        return jsonVertex;
    }
}
