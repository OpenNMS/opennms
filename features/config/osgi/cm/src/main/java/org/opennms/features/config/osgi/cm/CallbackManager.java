/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
