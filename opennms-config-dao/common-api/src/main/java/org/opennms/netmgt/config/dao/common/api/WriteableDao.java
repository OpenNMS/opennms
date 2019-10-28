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
 * The writeable version of {@link ReadableDao}.
 */
public interface WriteableDao<T> extends ReadableDao<T> {
    /**
     * This method returns the mutable configuration object for which any changes made to it will be persisted and
     * reflected after a call to {@link #saveConfig()}.
     */
    T getWriteableConfig();

    /**
     * Save the current configuration held by the DAO.
     * <p>
     * The configuration held by the DAO may not necessarily be the same configuration reference as returned by
     * {@link #getWriteableConfig()} if the config has been reloaded in between calls. This implies any changes to the
     * reference
     * returned by {@link #getWriteableConfig()} will not be reflected when saved in this case.
     */
    void saveConfig();

    /**
     * Notify the DAO that the configuration it tracks has changed.
     * <p>
     * Call this method after updating the filesystem configuration directly or after providing a config extension.
     */
    void onConfigChanged();
}
