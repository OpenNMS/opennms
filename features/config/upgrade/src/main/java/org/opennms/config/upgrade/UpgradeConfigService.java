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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.opennms.config.upgrade.datacollection.SnmpDataCollectionDefaultsUpdate;
import org.opennms.config.upgrade.datacollection.SnmpDataCollectionMigration;
import org.opennms.core.logging.Logging;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(UpgradeConfigService.class);

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
            // Pin upgrade-time logging to manager.log so messages don't leak into
            // whichever subsystem's MDC prefix happens to be active on Main.
            try (Logging.MDCCloseable ignored = Logging.withPrefixCloseable("manager")) {
                new LiquibaseUpgrader(cm).runChangelog("changelog-cm/changelog-cm.xml", dataSource.getConnection());
                migrateSnmpDataCollection();
                // Must run after the migration so first-boot imports are seen
                // (single-boot convergence for every install type).
                applySnmpDataCollectionDefaultUpdates();
            }
        }
    }

    /**
     * Migrate SNMP data collection XML configs to database.
     * Idempotent — skips if data already exists.
     */
    private void migrateSnmpDataCollection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            final SnmpDataCollectionMigration migration = new SnmpDataCollectionMigration();
            final boolean imported = migration.execute(connection);
            connection.commit();
            // Archive only if data was actually imported
            if (imported) {
                migration.archiveFiles();
            }
        } catch (final Exception e) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) { }
            }
            LOG.error("SNMP data collection XML-to-DB migration failed: {}", e.getMessage(), e);
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) { }
            }
        }
    }

    /**
     * Apply shipped-default updates to the DB-resident SNMP data collection
     * config. Ledger-backed (snmp_collection_defaults_log) — each update runs
     * at most once per database.
     */
    private void applySnmpDataCollectionDefaultUpdates() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            final SnmpDataCollectionDefaultsUpdate defaultsUpdate = new SnmpDataCollectionDefaultsUpdate();
            final boolean applied = defaultsUpdate.execute(connection);
            connection.commit();
            // Mirror applied fragments into etc_archive only after commit,
            // matching the migration's archive-after-commit pattern.
            if (applied) {
                defaultsUpdate.archiveAppliedFragments();
            }
        } catch (final Exception e) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) { }
            }
            LOG.error("SNMP data collection default updates failed: {}", e.getMessage(), e);
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) { }
            }
        }
    }
}
