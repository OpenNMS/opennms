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
package org.opennms.features.topology.plugins.topo.graphml;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.topo.AbstractEdge;

import com.google.common.collect.Maps;

public class GraphMLEdge extends AbstractEdge {
    private Map<String, Object> properties = Maps.newHashMap();

    public GraphMLEdge(String namespace, org.opennms.features.graphml.model.GraphMLEdge graphMLEdge, GraphMLVertex source, GraphMLVertex target) {
        super(namespace, graphMLEdge.getId(), source, target);

        setTooltipText(graphMLEdge.getProperty(GraphMLProperties.TOOLTIP_TEXT));
        setProperties(graphMLEdge.getProperties());
    }

    /**
     * Clone constructor.
     * It is required because each edge (whatever type) is cloned in the UI.
     * The resulting object is of type AbstractEdge.
     * This may be okay for edges which have the same fields. However, if a certain implementation needs Edge
     * specific properties (e.g. a VertexStatusProviderType) there is no way to retrieve those.
     * In order to make them accessible (without knowing the actual implementation), the clone constructor is used.
     *
     * @param edgeToClone The edge to clone
     */
    private GraphMLEdge(GraphMLEdge edgeToClone) {
        super(edgeToClone);
        properties = new HashMap<>(edgeToClone.getProperties());
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public AbstractEdge clone() {
        return new GraphMLEdge(this);
    }
}
