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

package org.opennms.netmgt.config.api;

import java.io.IOException;

public interface ThreshdConfigModifiable extends ThreshdConfig {

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException
     *             if any.
     */
    void saveCurrent() throws IOException;

    /**
     * Reload the config from the default config file
     *
     * @throws java.io.IOException
     *             if the specified config file cannot be read/loaded
     */
    void reload() throws IOException;
    
    /**
     * This nethod is used to rebuild the package agaist iplist mapping when needed. When a node gained service event occurs, threshd has to determine which package the ip/service
     * combination is in, but if the interface is a newly added one, the package iplist should be rebuilt so that threshd could know which package this ip/service pair is in.
     */
    void rebuildPackageIpListMap();

}
