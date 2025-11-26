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
package org.opennms.config.upgrade;

import java.util.Objects;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Checks for config upgrades and executes them at startup of OpenNMS.
 * Runs at every start of the application.
 * Uses liquibase as underlying technology.
 */
@Component
public class UpgradeConfigService implements InitializingBean {

    private final ConfigurationManagerService cm;
    private final DataSource dataSource;
    private final boolean skipConfigUpgrades;
    private final EventConfEventDao eventConfEventDao;
    @Inject
    public UpgradeConfigService(final ConfigurationManagerService cm,
                                final DataSource dataSource,
                                @Value( "${skipConfigUpgrades:false}" )
                                final boolean skipConfigUpgrades, EventConfEventDao eventConfEventDao) {
        this.cm = Objects.requireNonNull(cm);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.skipConfigUpgrades = skipConfigUpgrades;
        this.eventConfEventDao = Objects.requireNonNull(eventConfEventDao);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!skipConfigUpgrades) {
            new LiquibaseUpgrader(cm).runChangelog("changelog-cm/changelog-cm.xml", dataSource.getConnection());
            new EventConfUpgrader(eventConfEventDao).runContentUpgrade();
        }
    }
}
