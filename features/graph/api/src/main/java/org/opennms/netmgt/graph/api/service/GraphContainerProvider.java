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
