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
import java.util.Objects;

import liquibase.ext2.cm.change.EventConfChange;
import org.opennms.features.config.service.api.ConfigurationManagerService;

import liquibase.Liquibase;
import liquibase.change.ChangeFactory;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.ext2.cm.change.ImportConfiguration;
import liquibase.ext2.cm.change.ChangeSchema;
import liquibase.ext2.cm.change.ImportConfigurations;
import liquibase.ext2.cm.change.RegisterSchema;
import liquibase.ext2.cm.change.UpgradeSchema;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.executor.CmExecutor;
import liquibase.ext2.cm.sqlgenerator.CmSqlGenerator;
import liquibase.resource.ClassLoaderResourceAccessor;

public class LiquibaseUpgrader {

    public final static String TABLE_NAME_DATABASECHANGELOG = "cm_databasechangelog";
    public final static String TABLE_NAME_DATABASECHANGELOGLOCK = TABLE_NAME_DATABASECHANGELOG + "lock";

    final private ConfigurationManagerService cm;

    public LiquibaseUpgrader(final ConfigurationManagerService cm) {
        this.cm = Objects.requireNonNull(cm);
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
        ChangeFactory.getInstance().register(ImportConfigurations.class);
        ChangeFactory.getInstance().register(ChangeSchema.class);
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
