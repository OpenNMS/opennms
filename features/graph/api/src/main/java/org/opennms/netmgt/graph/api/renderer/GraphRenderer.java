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
package org.opennms.netmgt.graph.api.renderer;

import java.util.List;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;

public interface GraphRenderer {
    String getContentType();
    String render(int identation, List<GraphContainerInfo> containerInfos);
    String render(int identation, ImmutableGraphContainer<?> graphContainer);
    String render(int identation, ImmutableGraph<?, ?> graph);
    String render(int identation, Vertex vertex);

    default String render(List<GraphContainerInfo> containerInfos) {
        return render(0, containerInfos);
    }

    default String render(ImmutableGraphContainer<?> graphContainer) {
        return render(0, graphContainer);
    }

    default String render(ImmutableGraph<?, ?> graph) {
        return render(0, graph);
    }

    default String render(Vertex vertex) {
        return render(0, vertex);
    }
}
