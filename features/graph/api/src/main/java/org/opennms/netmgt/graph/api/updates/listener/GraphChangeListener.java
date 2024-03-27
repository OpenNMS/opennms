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
package org.opennms.netmgt.graph.api.updates.listener;

import java.util.List;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public interface GraphChangeListener<V extends Vertex, E extends Edge> {
    void handleVerticesAdded(List<V> verticesAdded);

    void handleVerticesRemoved(List<V> verticesRemoved);

    void handleVerticesUpdated(List<V> verticesUpdated);

    void handleEdgesAdded(List<E> edgesAdded);

    void handleEdgesUpdated(List<E> edgesUpdated);

    void handleEdgesRemoved(List<E> edgesRemoved);

    void handleGraphInfoChanged(GraphInfo currentGraphInfo);

    void handleFocusChanged(Focus currentFocus);
}
