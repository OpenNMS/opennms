/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;

/**
 * This interface is used to abstract the I/O operations used
 * to walk the resource tree and retrieve or manipulate resource
 * details.
 *
 * @author jwhite
 * @see org.opennms.netmgt.model.ResourcePath
 */
public interface ResourceStorageDao {

    /**
     * Returns true if the path, or any of its children contain
     * one or more metrics.
     */
    public boolean exists(ResourcePath path);

    /**
     * Returns true if the path, or any of its children contain
     * one or more metrics within the given depth.
     */
    public boolean exists(ResourcePath path, int depth);

    /**
     * Retrieves the set of child paths one level bellow the given
     * path which contain one or more metrics.
     */
    public Set<ResourcePath> children(ResourcePath path);

    /**
     * Retrieves the set of child paths one level bellow the given
     * path which contain one or more metrics within the given depth.
     */
    public Set<ResourcePath> children(ResourcePath path, int depth);

    /**
     * Deletes all of the resources and metric bellow
     * the given path.
     *
     * Returns true if the operation succeeded.
     */
    public boolean delete(ResourcePath path);

    /**
     * Retrieves the set of attributes stored at the given path.
     */
    public Set<OnmsAttribute> getAttributes(ResourcePath path);

    /**
     * Retrieves the meta-data stored at the given path.
     */
    public Map<String,String> getMetaData(ResourcePath path);

}
