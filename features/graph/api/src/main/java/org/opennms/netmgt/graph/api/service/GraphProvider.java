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
package org.opennms.netmgt.graph.api.service;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.info.GraphInfo;

/**
 * Convenient interface if a {@link GraphContainerProvider} only provides a single graph.
 *
 * Internally a {@link GraphProvider} will be converted to a {@link GraphContainerProvider} which provides a single graph.
 *
 * @author mvrueden
 */
public interface GraphProvider {

    /**
     * Loads the graph, this {@link GraphProvider} handles.
     * Loading may be performed very quickly, but also may take some time.
     *
     * @return The populated graph.
     */
    ImmutableGraph<?, ?> loadGraph();

    /**
     * The {@link GraphInfo} should be used to provide details of the graph's nature, e.g. the namespace, label or description
     * A {@link ImmutableGraph} should also embed this information. The difference is, that the info should always be available,
     * even if the graph is not yet loaded, and should also never change during the provider's live time, whereas the
     * graph itself may change (e.g. different vertices/edges and properties (besides the ones defining the info)).
     * @return the meta information of the graph
     */
    GraphInfo getGraphInfo();
}