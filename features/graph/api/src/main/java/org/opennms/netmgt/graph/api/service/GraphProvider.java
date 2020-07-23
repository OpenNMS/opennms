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