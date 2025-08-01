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
