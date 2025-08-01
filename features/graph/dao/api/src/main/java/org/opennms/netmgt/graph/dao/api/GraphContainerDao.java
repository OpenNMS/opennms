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
package org.opennms.netmgt.graph.dao.api;

import org.opennms.netmgt.graph.GraphContainerEntity;

public interface GraphContainerDao {
    /**
     * Saves or Updates the entity.
     *
     * @param graphContainerEntity The entity to save or update
     */
    void save(GraphContainerEntity graphContainerEntity);

    /**
     * Returns the GraphContainerEntity identified by the containerId.
     * It is not fully populated.
     *
     * @param containerId The containerId of the entity
     * @return The container or null
     */
    GraphContainerEntity findContainerById(String containerId);

    /**
     * Returns the GraphContainerEntity, but only populating the properties.
     * Vertices and Edges are not initialized.
     *
     * @param containerId  The container id of the entity
     * @return
     */
    GraphContainerEntity findContainerInfoById(String containerId);

    /**
     * Looks up the given entity by its container id and afterwards deletes that entity.
     *
     * @param containerId the container id of the entity.
     */
    void delete(String containerId);

    /**
     * Updates the given entity
     * @param graphContainerEntity
     */
    void update(GraphContainerEntity graphContainerEntity);
}
