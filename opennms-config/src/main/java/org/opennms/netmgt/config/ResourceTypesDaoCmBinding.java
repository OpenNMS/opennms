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
package org.opennms.netmgt.config;

import java.util.Objects;

import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.springframework.beans.factory.InitializingBean;

/**
 * Binds the {@link ResourceTypesDaoCm} of the common context to the configuration manager and the JSON store of the
 * DAO context, see {@link ResourceTypesDaoCm#bind(ConfigurationManagerService, JsonStore)}.
 *
 * <p>The JSON store is optional (wire this bean with {@code autowire="byName"}): some test contexts don't have one,
 * and then nothing is published for Sentinel.</p>
 */
public class ResourceTypesDaoCmBinding implements InitializingBean {

    private ResourceTypesDao resourceTypesDao;
    private ConfigurationManagerService configurationManagerService;
    private JsonStore jsonStore;

    public void setResourceTypesDao(final ResourceTypesDao resourceTypesDao) {
        this.resourceTypesDao = resourceTypesDao;
    }

    public void setConfigurationManagerService(final ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = configurationManagerService;
    }

    public void setJsonStore(final JsonStore jsonStore) {
        this.jsonStore = jsonStore;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(configurationManagerService, "configurationManagerService");
        // a test context may replace the DAO with a mock, which has nothing to bind
        if (resourceTypesDao instanceof ResourceTypesDaoCm) {
            ((ResourceTypesDaoCm) resourceTypesDao).bind(configurationManagerService, jsonStore);
        }
    }
}
