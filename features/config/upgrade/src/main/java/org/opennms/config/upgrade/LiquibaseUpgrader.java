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

import java.sql.Connection;

import org.opennms.features.config.service.api.ConfigurationManagerService;

import liquibase.Liquibase;
import liquibase.change.ChangeFactory;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.ext2.cm.change.ImportConfiguration;
import liquibase.ext2.cm.change.RegisterSchema;
import liquibase.ext2.cm.change.UpgradeSchema;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.executor.CmExecutor;
import liquibase.ext2.cm.sqlgenerator.CmSqlGenerator;
import liquibase.resource.ClassLoaderResourceAccessor;

public class LiquibaseUpgrader {

    public final static String TABLE_NAME_DATABASECHANGELOG = "cm_databasechangelog";
    public final static String TABLE_NAME_DATABASECHANGELOGLOCK = TABLE_NAME_DATABASECHANGELOG + "lock";

    final ConfigurationManagerService cm;

    public LiquibaseUpgrader(final ConfigurationManagerService cm) {
        this.cm = cm;
    }

    public void runChangelog(final String changelog, final Connection sqlConnection) throws LiquibaseException {

        // Database
        CmDatabase db = new CmDatabase(cm);
        DatabaseFactory.getInstance().register(db);
        db.setConnection(new JdbcConnection(sqlConnection));
        db.setDatabaseChangeLogTableName(TABLE_NAME_DATABASECHANGELOG);
        db.setDatabaseChangeLogLockTableName(TABLE_NAME_DATABASECHANGELOGLOCK);

        // Register our extensions.
        ChangeFactory.getInstance().register(RegisterSchema.class);
        ChangeFactory.getInstance().register(UpgradeSchema.class);
        ChangeFactory.getInstance().register(ImportConfiguration.class);
        // Liqui 4.4.3: Scope.getCurrentScope().getSingleton(liquibase.change.ChangeFactory.class).register(new RegisterSchema());
        ExecutorService.getInstance().clearExecutor(db);
        CmExecutor executor = new CmExecutor();
        executor.setDatabase(db);
        ExecutorService.getInstance().setExecutor(db, executor);
        // Liqui 4.4.3: Scope.getCurrentScope().getSingleton(liquibase.executor.ExecutorService.class).register(new CmExecutor());
        liquibase.sqlgenerator.SqlGeneratorFactory.getInstance().register(new CmSqlGenerator());

        // Liquibase
        Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(LiquibaseUpgrader.class.getClassLoader()), db);
        liquibase.update((String)null); // https://docs.liquibase.com/concepts/contexts.html
    }

}
