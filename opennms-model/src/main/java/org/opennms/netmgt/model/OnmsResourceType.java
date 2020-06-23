/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
