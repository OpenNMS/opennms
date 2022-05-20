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

package org.opennms.config.upgrade;

import java.util.Objects;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.opennms.features.config.service.api.ConfigurationManagerService;
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

    @Inject
    public UpgradeConfigService(final ConfigurationManagerService cm,
                                final DataSource dataSource,
                                @Value( "${skipConfigUpgrades:false}" )
                                final boolean skipConfigUpgrades) {
        this.cm = Objects.requireNonNull(cm);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.skipConfigUpgrades = skipConfigUpgrades;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!skipConfigUpgrades) {
            new LiquibaseUpgrader(cm).runChangelog("changelog-cm/changelog-cm.xml", dataSource.getConnection());
        }
    }
}
