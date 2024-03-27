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
package org.opennms.netmgt.graph.api;

import java.util.List;

import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;

/**
 * Originally in OpenNMS there was the TopologyProvider (aka GraphProvider).
 * Its responsibility was mixed as it represented the graph and also the manager for the graph.
 *
 * From the graph's perspective it was not possible to determine the label or description, as the API was very limited.
 * It basically only supported calls like "getVertices()" or "getEdges()".
 *
 * At some point (around H18/H19) the need for multiple graphs provided by a TopologyProvider arose, as well
 * as the possibility to push multiple graphs into OpenNMS. Back then it was decided to use GraphML as an "exchange format".
 * Luckily GraphML already supported multiple GraphML graphs within one GraphML file.
 * In order to make this compatible with OpenNMS the MetaTopologyProvider was born.
 * It's responsibility was to provide multiple TopologyProviders, whereas to the user it looked like only one "Thing".
 * To accomplish this, the MetaTopologyProvider returned a "default" or "preferred" TopologyProvider to use as default.
 *
 * Besides that what was shown to the user (e.g. label and description) when building the menu was not part of the Model,
 * but enriched later on with OSGi service properties.
 *
 * When working on the "New Graph API", multiple graphs were considered from the start.
 * This is when the {@link ImmutableGraphContainer} was born.
 * Its purpose is similar to the GraphML definition:
 * A {@link ImmutableGraphContainer} can hold multiple {@link ImmutableGraph}s.
 * It can also just return a {@link ImmutableGraph} for a given namespace.
 *
 * Be aware, that a GraphContainer should always be fully populated (not enriched) when loaded by a provider.
 *
 * @author mvrueden
 */
public interface ImmutableGraphContainer<G extends ImmutableGraph<? extends Vertex, ? extends Edge>> extends GraphContainerInfo {
    /**
     * Returns the list of graphs provided by the container. The returned list, should never be null or empty.
     *
     * @return the provided graphs. Must never be null or empty
     */
    List<G> getGraphs();

    /**
     * Returns the graph with the requested namespace, or null if it does not exist.
     *
     * @param namespace the namespace of the graph to get
     * @return the graph with the requested namespace or null if it does not exist
     */
    G getGraph(String namespace);

    /**
     * Converts the {@link ImmutableGraphContainer} to its generic counter part.
     */
    GenericGraphContainer asGenericGraphContainer();
}
