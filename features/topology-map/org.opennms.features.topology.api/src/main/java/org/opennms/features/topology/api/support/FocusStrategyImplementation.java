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
package org.opennms.features.topology.api.support;

import java.util.List;

import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.BackendGraph;

/**
 * Interface to define the determination of the vertices in focus.
 *
 * @author mvrueden
 */
public interface FocusStrategyImplementation {
    /**
     * Determines the default focus for the given {@link BackendGraph}.
     * The optional vertexIdsWithoutNamespace parameter may be used to narrow down the selection to specific ids.
     *
     * @param graph
     * @param vertexIdsWithoutNamespace
     * @return A list of vertices in focus. The list may be empty, but should not be null.
     */
    List<VertexHopCriteria> determine(BackendGraph graph, String... vertexIdsWithoutNamespace);
}
