/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.dao.common.api;

/**
 * A config DAO that supports only reading and reloading.
 *
 * @param <T> the entity type this DAO manages
 */
public interface ReadableDao<T> {
    /**
     * Get the current configuration. Whether or not this configuration is in sync with the backing data source is up to
     * the implementation, see {@link #reload()}.
     * 
     * The value returned by this method may be the result of merging multiple objects together. As a result, mutations
     * to this object may not be visible to other readers and are not permitted.
     * 
     * For cases where mutation and/or persistence of changes is required see {@link WriteableDao#getWriteableConfig()}.
     */
    T getReadOnlyConfig();

    /**
     * Instructs the DAO to reload the configuration from the backing data source immediately. Whether or not the config
     * actually gets reloaded is up to the implementation as it may already be up to date. However invoking this method
     * should guarantee that a subsequent call to {@link #getReadOnlyConfig()} yields the up to date configuration.
     */
    void reload();
}
