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
package org.opennms.netmgt.model;

import java.util.List;

/**
 * <p>OnmsResourceType interface.</p>
 */
public interface OnmsResourceType {
    /**
     * Provides a unique name for this resource type.
     *
     * @return unique name
     */
    public String getName();

    /**
     * Provides a human-friendly label for this resource type.  It is
     * particularly used in the webUI to describe this resource type.
     *
     * @return human-friendly label
     */
    public String getLabel();

    /**
     * Returns true if one on more instances of the resource type
     * are present on the parent resource.
     *
     * Top-level resource types should always return false.
     */
    public boolean isResourceTypeOnParent(OnmsResource parent);

    /**
     * Returns the set of resources that are available on the given
     * parent.
     *
     * If none are available, i.e. isResourceTypeOnParent() would
     * return false, then an empty list should be returned.
     *
     * Top-level resource types should return the set of top-level
     * resources when parent is null.
     */
    public List<OnmsResource> getResourcesForParent(OnmsResource parent);

    /**
     * Retrieves a child resource with the given name from the parent.
     *
     * @return null if no resource with the given name was found
     * @throws ObjectRetrievalFailureException If any exceptions are thrown while searching for the resource
     */
    public OnmsResource getChildByName(OnmsResource parent, String name);

    /**
     * <p>getLinkForResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @return a {@link java.lang.String} object.
     */
    public String getLinkForResource(OnmsResource resource);

}
