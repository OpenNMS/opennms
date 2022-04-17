/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.config.osgi.cm;

import static org.opennms.features.config.service.api.ConfigUpdateInfo.WILDCARD_ID;

import java.io.IOException;

import org.opennms.features.config.osgi.del.MigratedServices;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the communication CM to Osgi.
 * It will inform Osgi of changes that came into CM from the rest api.
 */
class CallbackManager {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackManager.class);

    void registerCallbacks(ConfigurationAdmin configurationAdmin,
                           ConfigurationManagerService cm,
                           CmPersistenceManager cmPersistenceManager) {

        // single instance services:
        for (String pid : MigratedServices.PIDS_SINGLE_INSTANCE) {
            ConfigUpdateInfo key = new ConfigUpdateInfo(pid);
            cm.registerEventHandler(EventType.UPDATE, key, k -> updateConfig(configurationAdmin, cmPersistenceManager, k));
            // CREATE and DELETE doesn't apply for single instance services
        }

        // multi instance services:
        for (String configName : MigratedServices.PIDS_MULTI_INSTANCE) {
            ConfigUpdateInfo key = new ConfigUpdateInfo(configName, WILDCARD_ID);
            cm.registerEventHandler(EventType.CREATE, key, k -> createConfig(configurationAdmin, cmPersistenceManager, k));
            cm.registerEventHandler(EventType.UPDATE, key, k -> updateConfig(configurationAdmin, cmPersistenceManager, k));
            cm.registerEventHandler(EventType.DELETE, key, k -> deleteConfig(configurationAdmin, cmPersistenceManager, k));
        }
    }

    private void createConfig(ConfigurationAdmin configurationAdmin,
                              CmPersistenceManager cmPersistenceManager,
                              ConfigUpdateInfo key) {
        String cmPid = CmIdentifierUtil.cmIdentifierToPid(key);

        try {
            Configuration config = configurationAdmin.createFactoryConfiguration(key.getConfigName(), "?");
            cmPersistenceManager.setPidMapping(config.getPid(), cmPid);
            config.update();
        } catch (IOException e) {
            LOG.error("Cannot create configuration for pid=" + cmPid, e);
        }
    }

    private void updateConfig(ConfigurationAdmin configurationAdmin,
                              CmPersistenceManager cmPersistenceManager,
                              ConfigUpdateInfo identifier) {
        String osgiPid = cmPersistenceManager.getOsgiPid(identifier);
        try {
            configurationAdmin
                    .getConfiguration(osgiPid)
                    .update();
        } catch (IOException e) {
            LOG.error("Cannot update configuration for identifier=" + identifier, e);
        }
    }

    private void deleteConfig(ConfigurationAdmin configurationAdmin,
                              CmPersistenceManager cmPersistenceManager,
                              ConfigUpdateInfo identifier) {
        String osgiPid = cmPersistenceManager.getOsgiPid(identifier);
        try {
            configurationAdmin
                    .getConfiguration(osgiPid)
                    .delete();
        } catch (IOException e) {
            LOG.error("Cannot delete configuration for identifier=" + identifier, e);
        }
    }
}
