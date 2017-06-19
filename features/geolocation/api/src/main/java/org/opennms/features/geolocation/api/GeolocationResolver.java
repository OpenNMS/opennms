/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.geolocation.api;

import java.util.Collection;
import java.util.Map;

/**
 * Interface to resolve the address string of nodes.
 *
 * @author mvrueden
 */
public interface GeolocationResolver {

    /**
     * Resolve the address string to coordinates for all nodes in nodeIds.
     *
     * @param nodeIds The ids to resolve the address string to coordinates.
     * @return A Map (nodeId -> Coordinate) for all nodes in nodeIds which have a address defined.
     */
    Map<Integer, Coordinates> resolve(Collection<Integer> nodeIds);

    /**
     * Resolves each entry's address to its coordinate.
     *
     * @param nodeIdAddressMap Key: nodeId, Value: Address
     * @return A Map (nodeId -> Coordinates) for all nodes which id matches the keys of the map and have an address defined and
     */
    Map<Integer, Coordinates> resolve(Map<Integer, String> nodeIdAddressMap);

    /**
     * Resolves the given addressString.
     *
     * @param addressString The address to resolve to coordinates.
     * @return The resolved coordinates.
     */
    Coordinates resolve(String addressString);
}
