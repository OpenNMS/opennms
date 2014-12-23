/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.core.config.api;

/**
 * A generic API for accessing configuration resources.
 * 
 * @param <T> The type of resource to be accessed.
 */
public interface ConfigurationResource<T> {
    /**
     * Get the configuration resource.
     * 
     * @return the resource
     * @throws ConfigurationResourceException
     */
    public T get() throws ConfigurationResourceException;

    /**
     * Replace the stored version of the resource with the provided version.
     * 
     * @param config The new copy of the resource.
     * @throws ConfigurationResourceException
     */
    public void put(T config) throws ConfigurationResourceException;
}
