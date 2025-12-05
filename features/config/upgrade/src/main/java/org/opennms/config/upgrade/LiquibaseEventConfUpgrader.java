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

import liquibase.Liquibase;
import liquibase.change.ChangeFactory;
import liquibase.core.schema.change.EventConfChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Standalone upgrader for EventConf changes using Liquibase.
 * Runs the existing changelog file for eventconf_events table.
 */
public class LiquibaseEventConfUpgrader {

    private final String changelogPath;

    public LiquibaseEventConfUpgrader(String changelogPath) {
        this.changelogPath = Objects.requireNonNull(changelogPath, "Changelog path must not be null");
    }

    /**
     * Runs the Liquibase changelog on the provided JDBC connection.
     */
    public void runChangelog(Connection sqlConnection) throws LiquibaseException {
        try {
            Database database = new PostgresDatabase();
            DatabaseFactory.getInstance().register(database);
            database.setConnection(new JdbcConnection(sqlConnection));
            ChangeFactory.getInstance().register(EventConfChange.class);
            Liquibase liquibase = new Liquibase(
                    changelogPath,
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update((String) null);
        } catch (LiquibaseException e) {
            throw new LiquibaseException("Failed to run EventConf changelog: " + changelogPath, e);
        }
    }
}
