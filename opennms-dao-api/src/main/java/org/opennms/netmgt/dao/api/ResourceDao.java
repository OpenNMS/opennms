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
package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;

/**
 * <p>ResourceDao interface.</p>
 */
public interface ResourceDao {

    /**
     * <p>getResourceTypes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsResourceType> getResourceTypes();

    /**
     * <p>findTopLevelResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findTopLevelResources();

    /**
     * <p>getResourceById</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceById(ResourceId id);

    /**
     * <p>getResourceForNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceForNode(OnmsNode node);

    /**
     * Deletes the resource identified by the given resource ID.
     *
     * @param resourceId the ID of the resource to delete
     *
     * @return {@code true} iff, the resource was found and deleted
     */
    public boolean deleteResourceById(final ResourceId resourceId);


    public ResourceId getResourceId(CollectionResource resource, long nodeId);
}
