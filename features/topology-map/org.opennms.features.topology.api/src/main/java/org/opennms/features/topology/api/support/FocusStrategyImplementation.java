/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import java.util.List;

import org.opennms.features.topology.api.topo.GraphProvider;

/**
 * Interface to define the determination of the vertices in focus.
 *
 * @author mvrueden
 */
public interface FocusStrategyImplementation {
    /**
     * Determines the default focus for the given {@link GraphProvider}.
     * The optional vertexIdsWithoutNamespace parameter may be used to narrow down the selection to specific ids.
     *
     * @param topologyProvider
     * @param vertexIdsWithoutNamespace
     * @return A list of vertices in focus. The list may be empty, but should not be null.
     */
    List<VertexHopGraphProvider.VertexHopCriteria> determine(GraphProvider topologyProvider, String... vertexIdsWithoutNamespace);
}
