/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.backup.service.api;

import org.opennms.features.backup.api.Config;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NetworkDeviceBackupManager {

    /**
     * Return a list of nodeIds already in backup
     * @return nodeIds
     */
    List<Integer> getBackupedNodeIds();

    /**
     * Return a list of kv store key for this nodeId
     * @param nodeId
     * @return keys
     */
    List<String> getConfigs(int nodeId);

    /**
     * Return the actual config from database
     * @param nodeId
     * @param version
     * @return
     */
    Config getConfig(int nodeId, String version);

    /**
     * Store the config
     * @param config
     * @throws Exception
     */
    void saveConfig(Config config) throws Exception;

    /**
     * Trigger backup for specific device
     * @param nodeId
     * @throws Exception
     */
    void backup(int nodeId) throws Exception;

    /**
     * Trigger all network device backup
     * @throws Exception
     */
    void backup() throws Exception;
}
