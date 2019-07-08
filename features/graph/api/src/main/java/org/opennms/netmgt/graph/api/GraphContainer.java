/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.graph.api;

import java.util.List;

import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;

/**
 * In OpenNMS (H24 was developed on and H23 already released when writing this) there was only the TopologyProvider also called
 * GraphProvider. Its responsibility was kinda mixed as it represented the graph and also the manager for the graph.
 * However from just the graph it was not possible to determine the label or description, as the API was very poor.
 * Basically it only contained "getVertices()" and "getEdges()".
 *
 * At some point (probably around H18/H19) the need for multiple graphs provided by a TopologyProvider arose, as well
 * as the possibility to push multiple graphs into OpenNMS. Back then it was decided to use GraphML as a "exchange format".
 * Luckily GraphML already supports multiple GraphML graphs within one GraphML file.
 * In order to make this compatible with OpenNMS the MetaTopologyProvider was born.
 * It's responsibility was to provide multiple TopologyProviders, whereas to the user it looked like only one "Thing".
 * To accomplish this, the MetaTopologyProvider returned a "default" or "preferred" TopologyProvider, which was the default one.
 *
 * Besides that what was shown to the user (e.g. label and description) when building the menu was not part of the Model,
 * but enriched later on with OSGi service properties.
 *
 * When working on this Graph POC multiple graphs were also considered and the {@link GraphContainer} was born.
 * Its purpose is similar to the GraphML definition. A {@link GraphContainer} can hold multiple {@link ImmutableGraph}s.
 * It can also just return a {@link ImmutableGraph} for a given namespace.
 *
 * Be aware, that a GraphContainer should always be fully populated (not enriched) when loaded by a provider.
 */
// TODO MVR rework the javadoc
public interface GraphContainer<V extends Vertex, E extends Edge, G extends ImmutableGraph<V, E>> extends GraphContainerInfo {
    List<G> getGraphs();
    G getGraph(String namespace);
    void addGraph(G graph);
    void removeGraph(String namespace);
    GenericGraphContainer asGenericGraphContainer();
}
