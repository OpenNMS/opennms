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

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;

/**
 * A {@link GraphContainerProvider} is responsible for providing an {@link ImmutableGraphContainer}
 * as well as the meta information of that container.
 *
 * If possible the implementators should not load the full container when {@link #getContainerInfo()} is invoked.
 */
public interface GraphContainerProvider {

    /**
     * Returns a fully loaded {@link ImmutableGraphContainer} object, containing ALL vertices and edges.
     * May be slow.
     *
     * @return The populated container
     */
    ImmutableGraphContainer loadGraphContainer();

    /**
     * Invoking {@link #loadGraphContainer()} may take some time, so it is not feasible to invoke it,
     * if only the meta data of the container is requested.
     * Therefore the {@link #getContainerInfo()} should return very quickly with the meta data of
     * the container and its graphs.
     *
     * @return The container's meta data
     */
    GraphContainerInfo getContainerInfo();
}
