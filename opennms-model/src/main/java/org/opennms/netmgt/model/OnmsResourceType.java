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
     * Checks whether this resource type is on a specific node.  If possible,
     * this should have less overhead than calling #getResourcesForNode(int).
     *
     * @param nodeId node ID to check
     * @return true if this resource type is on this node, false otherwise
     */
    public boolean isResourceTypeOnNode(int nodeId);
    
    /**
     * Gets a list of resources on a specific node.
     *
     * @param nodeId node ID for which to get resources
     * @return list of resources
     */
    public List<OnmsResource> getResourcesForNode(int nodeId);
    
    /**
     * Checks whether this resource type is on a specific domain.  If possible,
     * this should have less overhead than calling #getResourcesForDomain(String).
     *
     * @param domain domain to check
     * @return true if this resource type is on this domain, false otherwise
     */
    public boolean isResourceTypeOnDomain(String domain);

    /**
     * Gets a list of resources on a specific domain.
     *
     * @param domain domain for which to get resources
     * @return list of resources
     */
    public List<OnmsResource> getResourcesForDomain(String domain);

    /**
     * <p>getLinkForResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @return a {@link java.lang.String} object.
     */
    public String getLinkForResource(OnmsResource resource);
    
    public boolean isResourceTypeOnNodeSource(String nodeSource, int nodeId);
    
    public List<OnmsResource> getResourcesForNodeSource(String nodeSource, int nodeId);

    /**
     * Retrieves a child resource with the given name from the parent.
     *
     * @return null if no resource with the given name was found
     * @throws ObjectRetrievalFailureException If any exceptions are thrown while searching for the resource
     */
    public OnmsResource getChildByName(OnmsResource parent, String name);
}
