/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.netmgt.distributed.datasource;

import org.opennms.core.schema.MigratorAdminInitialize;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * INIT DB - create the opennms DB and other admin
 *
 * Perform the Database setup that only requires the Admin connection, so that it can safely execute prior to the
 *  existence of the "opennms" database.
 *
 * Separating this step from the rest of the migration allows the opennms DB source to still fail-fast in case of
 *  problems accessing the database at startup time, even when database initialization is disabled.
 */
public class BootTimeDatabaseInit {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(BootTimeDatabaseInit.class);

    private Logger log = DEFAULT_LOGGER;

    private boolean m_enabled;

    private DataSource m_adminDatasource;
    private JdbcDataSource m_adminDataSourceConfig;
    private String m_databaseName;
    private String m_databaseUsername;
    private String m_databasePassword;

    private boolean m_validateDatabase;

//========================================
// Getters and Setters
//========================================

    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        this.m_enabled = enabled;
    }

    public DataSource getAdminDatasource() {
        return m_adminDatasource;
    }

    public void setAdminDatasource(DataSource adminDatasource) {
        this.m_adminDatasource = adminDatasource;
    }

    public JdbcDataSource getAdminDataSourceConfig() {
        return m_adminDataSourceConfig;
    }

    public void setAdminDataSourceConfig(JdbcDataSource adminDataSourceConfig) {
        this.m_adminDataSourceConfig = adminDataSourceConfig;
    }

    public String getDatabaseName() {
        return m_databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.m_databaseName = databaseName;
    }

    public String getDatabaseUsername() {
        return m_databaseUsername;
    }

    public void setDatabaseUsername(String databaseUsername) {
        this.m_databaseUsername = databaseUsername;
    }

    public String getDatabasePassword() {
        return m_databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.m_databasePassword = databasePassword;
    }

    public boolean isValidateDatabase() {
        return m_validateDatabase;
    }

    public void setValidateDatabase(boolean validateDatabase) {
        this.m_validateDatabase = validateDatabase;
    }

//========================================
// Lifecycle
//========================================

    public void init() throws Exception {
        if (this.m_enabled) {
            MigratorAdminInitialize migratorAdminInitialize = new MigratorAdminInitialize();

            migratorAdminInitialize.setAdminDataSource(this.m_adminDatasource);

            migratorAdminInitialize.setAdminUser(this.m_adminDataSourceConfig.getUserName());
            migratorAdminInitialize.setAdminPassword(this.m_adminDataSourceConfig.getPassword());
            migratorAdminInitialize.setDatabaseName(this.m_databaseName);
            migratorAdminInitialize.setDatabaseUser(this.m_databaseUsername);
            migratorAdminInitialize.setDatabasePassword(this.m_databasePassword);
            migratorAdminInitialize.setValidateDatabaseVersion(this.m_validateDatabase);

            this.log.info("INITILIAZE DATABASE");

            migratorAdminInitialize.initializeDatabase(true, false);
        } else {
            this.log.info("SKIPPING DATABASE INITIALIZATION");
        }
    }
}
