/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 * This interface allows extensions to provide configuration objects of a given type.
 *
 * See {@link ConfigReloadContainer}
 *
 * @author jwhite
 */
public interface ConfigurationProvider {

    /**
     * Retrieve a class reference to the type of object returned by {@link #getObject()}.
     *
     * This is used instead of generics to be OSGi friendly.
     *
     * @return the type of object returned by {@link #getObject()}
     */
    Class<?> getType();

    /**
     * Retrieve the actual configuration bean.
     *
     * @return the configuration bean, must be non-null
     */
    Object getObject();

    /**
     * @return the last time (in ms) at which the configuration bean was updated
     */
    long getLastUpdate();

}
