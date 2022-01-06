/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.netmgt.distributed.datasource;

import org.opennms.core.schema.ClassLoaderBasedMigrator;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class BootTimeDatabaseMigration {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(BootTimeDatabaseMigration.class);

    private Logger log = DEFAULT_LOGGER;

    private boolean m_enabled;
    private DataSource m_datasource;

    private boolean m_validateDatabase;
    private JdbcDataSource m_dataSourceConfig;

//========================================
// Getters and Setters
//========================================

    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        this.m_enabled = enabled;
    }

    public DataSource getDatasource() {
        return m_datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.m_datasource = datasource;
    }

    public boolean isValidateDatabase() {
        return m_validateDatabase;
    }

    public void setValidateDatabase(boolean validateDatabase) {
        this.m_validateDatabase = validateDatabase;
    }

    public JdbcDataSource getDataSourceConfig() {
        return m_dataSourceConfig;
    }

    public void setDataSourceConfig(JdbcDataSource dataSourceConfig) {
        this.m_dataSourceConfig = dataSourceConfig;
    }

//========================================
// Lifecycle
//========================================

    public void init() throws Exception {
        if (this.m_enabled) {
            ClassLoaderBasedMigrator migrator = new ClassLoaderBasedMigrator(this.getClass().getClassLoader());

            migrator.setDataSource(this.m_datasource);
            migrator.setAdminDataSource(this.m_datasource); // TODO: what is the difference between ADMIN and non-admin datasource?
            migrator.setValidateDatabaseVersion(this.m_validateDatabase);

            // TODO: admin username + password vs db username + password
            migrator.setDatabaseName(this.m_dataSourceConfig.getDatabaseName());
            migrator.setSchemaName(this.m_dataSourceConfig.getSchemaName());
            migrator.setAdminUser(this.m_dataSourceConfig.getUserName());
            migrator.setAdminPassword(this.m_dataSourceConfig.getPassword());
            migrator.setDatabaseUser(this.m_dataSourceConfig.getUserName());
            migrator.setDatabasePassword(this.m_dataSourceConfig.getPassword());

            this.log.info("STARTING MIGRATOR");

            migrator.setupDatabase(true, false, false, true, false);
        } else {
            this.log.info("SKIPPING MIGRATOR");
        }
    }
}
