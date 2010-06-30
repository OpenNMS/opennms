//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 05: Remove getRelativePathAttribute. - dj@opennms.org
//
// Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.util.List;

/**
 * <p>OnmsResourceType interface.</p>
 *
 * @author ranger
 * @version $Id: $
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
}
