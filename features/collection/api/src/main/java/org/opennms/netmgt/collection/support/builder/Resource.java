/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support.builder;

import java.nio.file.Path;

import org.opennms.netmgt.collection.api.CollectionResource;

public interface Resource {

    public Resource getParent();

    /**
     * Returns the type name associated with the resource. Used for thresholding.
     *
     * @return type name
     */
    public String getTypeName();

    /**
     * Returns a unique name for the instance of this resource.
     * Used by the {@link org.opennms.netmgt.collection.support.IndexStorageStrategy}
     *
     * @return instance name
     */
    public String getInstance();

    /**
     * Returns a unique label for the instance of this resource.
     * This label is used by threshd to generate a unique id on a per resource basis,
     * grouped by node.
     *
     * See {@link org.opennms.netmgt.threshd.CollectionResourceWrapper}
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel(CollectionResource resource);

    /**
     * Retrieves the path of the resource, relative to the repository root.
     *
     * @param resource Used by the {@link GenericTypeResource} in order to determine the instance name.
     * @return relative path
     */
    public Path getPath(CollectionResource resource);

}
