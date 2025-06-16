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
package org.opennms.netmgt.bsm.service.model.graph.internal;

import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;

public class GraphEdgeImpl extends GraphElement implements GraphEdge {

    private final MapFunction m_mapFunction;
    private final int m_weight;
    private final String m_friendlyName;

    public GraphEdgeImpl(MapFunction mapFunction) {
        this(mapFunction, 1, null);
    }

    public GraphEdgeImpl(Edge edge) {
        this(edge.getMapFunction(), edge.getWeight(), edge.getFriendlyName());
    }

    private GraphEdgeImpl(MapFunction mapFunction, int weight, String friendlyName) {
        m_mapFunction = mapFunction;
        m_weight = weight;
        m_friendlyName = friendlyName;
    }

    public MapFunction getMapFunction() {
        return m_mapFunction;
    }

    public int getWeight() {
        return m_weight;
    }

    @Override
    public String getFriendlyName() {
        return m_friendlyName;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("mapFunction", m_mapFunction)
                .add("weight", m_weight)
                .toString();
    }
}
