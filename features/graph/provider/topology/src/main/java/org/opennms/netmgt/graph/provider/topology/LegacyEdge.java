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
package org.opennms.netmgt.graph.provider.topology;

import java.util.Map;
import java.util.Objects;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.integration.api.v1.graph.Properties;
import org.opennms.netmgt.graph.api.generic.GenericEdge;

public class LegacyEdge extends AbstractEdge {

    private final Map<String, Object> properties;

    public LegacyEdge(GenericEdge edge) {
        super(Objects.requireNonNull(edge).getNamespace(), edge.getId(), createVertexRef(edge.getSource()), createVertexRef(edge.getTarget()));
        this.properties = edge.getProperties();
        String tooltip = edge.getProperty(Properties.Edge.TOOLTIP_TEXT, edge.getLabel());
        setTooltipText(tooltip);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    private static VertexRef createVertexRef(org.opennms.netmgt.graph.api.VertexRef input) {
        Objects.requireNonNull(input);
        final VertexRef output = new DefaultVertexRef(input.getNamespace(), input.getId());
        return output;
    }
}
