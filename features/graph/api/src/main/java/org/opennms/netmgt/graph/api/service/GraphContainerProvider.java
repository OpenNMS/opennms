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

import org.opennms.netmgt.graph.api.GraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;

// TODO MVR implement me
public interface GraphContainerProvider {
    // TODO MVR we have to implement this somehow
//    /**
//     * The provider may need to inform about graph changes.
//     * Whith this method the {@link GraphNotificationService} is passed to the provider.
//     * @param notificationService
//     */
//    void setNotificationService(GraphNotificationService notificationService);

    /**
     * Populates the whole container with all its graphs.
     * The provider should not initialize the container and does not need to cache it.
     * Invoking this call and also implement proper caching strategies is the {@link GraphService}'s responsibility.
     * May be slow.
     *
     * TODO MVR We may need to add eviction strategies or custom caching strategies, e.g. for bsm or vmware, etc.
     * @return The populated container
     */
    GraphContainer loadGraphContainer();

    /**
     * Invoking {@link #loadGraphContainer()} may take some time, so it is not feasible to invoke it, if only the meta data
     * of the container or its graph is requested. Therefore the {@link #getContainerInfo()} should return very quick
     * with the meta data of the container and its graphs
     *
     * @return The container's meta data
     */
    GraphContainerInfo getContainerInfo();
}
