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

package org.opennms.core.schema.migrator;

import liquibase.Liquibase;
import liquibase.change.ChangeFactory;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.ext.opennms.autoincrement.AddNamedAutoIncrementChange;
import liquibase.ext.opennms.createindex.CreateIndexWithWhereChange;
import liquibase.ext.opennms.createindex.CreateIndexWithWhereGenerator;
import liquibase.ext.opennms.dropconstraint.DropForeignKeyConstraintCascadeChange;
import liquibase.ext.opennms.dropconstraint.DropForeignKeyConstraintCascadeGenerator;
import liquibase.ext.opennms.setsequence.SetSequenceChange;
import liquibase.ext.opennms.setsequence.SetSequenceGenerator;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class ClassLoaderBasedLiquibaseExecutor implements MigratorLiquibaseExecutor {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(ClassLoaderBasedLiquibaseExecutor.class);

    private Logger log = DEFAULT_LOGGER;

    private final ClassLoader m_classLoader;

//========================================
// Constructors
//========================================

    /**
     * Use the class loader for this class to locate resources.
     */
    public ClassLoaderBasedLiquibaseExecutor() {
        this.m_classLoader = this.getClass().getClassLoader();
    }

    /**
     * Use the give class loader to locate resources.
     *
     * @param m_classLoader
     */
    public ClassLoaderBasedLiquibaseExecutor(ClassLoader m_classLoader) {
        this.m_classLoader = m_classLoader;
    }


//========================================
// Interface
//========================================

    @Override
    public void update(
            String changelog,
            String contexts,
            DataSource datasource,
            String schemaName,
            Map<String, String> changeLogParameters
    ) throws SQLException, LiquibaseException {

        Connection connection = datasource.getConnection();

        Liquibase liquibase = this.prepareLiquibase(connection, schemaName, changelog, changeLogParameters);

        liquibase.update(contexts);
    }

//========================================
// Internals
//========================================

    private Liquibase prepareLiquibase(Connection databaseConnection, String schemaName, String changelogUri, Map<String, String> changelogParameters) throws LiquibaseException {
        this.registerLiquibaseExtensions();

        DatabaseConnection liquibaseDatabaseConnection = new JdbcConnection(databaseConnection);

        PostgresDatabase postgresDatabase = new PostgresDatabase();
        postgresDatabase.setConnection(liquibaseDatabaseConnection);
        postgresDatabase.setDefaultSchemaName(schemaName);

        ClassLoaderResourceAccessor classLoaderResourceAccessor =
                new MyBundleClassLoaderResourceAccessor(this.m_classLoader);

        Liquibase liquibase = new Liquibase(changelogUri, classLoaderResourceAccessor, postgresDatabase);

        //
        // Populate Liquibase with the changelog parameters.
        //
        changelogParameters.forEach(liquibase::setChangeLogParameter);

        return liquibase;
    }

    private void registerLiquibaseExtensions() {
        SqlGeneratorFactory.getInstance().register(new DropForeignKeyConstraintCascadeGenerator());
        SqlGeneratorFactory.getInstance().register(new CreateIndexWithWhereGenerator());
        SqlGeneratorFactory.getInstance().register(new SetSequenceGenerator());

        ChangeFactory.getInstance().register(AddNamedAutoIncrementChange.class);
        ChangeFactory.getInstance().register(CreateIndexWithWhereChange.class);
        ChangeFactory.getInstance().register(DropForeignKeyConstraintCascadeChange.class);
        ChangeFactory.getInstance().register(SetSequenceChange.class);
    }

//========================================
// Internal Classes
//========================================

    /**
     * Resource Accessor that extracts the path from a resource URL for use with getResourceAsStream
     */
    private class MyBundleClassLoaderResourceAccessor extends ClassLoaderResourceAccessor {
        public MyBundleClassLoaderResourceAccessor(ClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
            if (path.startsWith("bundle://")) {
                String updPath = path.replaceFirst("bundle://[^/]*", "");

                ClassLoaderBasedLiquibaseExecutor.this.log.debug("MAPPED BUNDLE PATH {} => {}", path, updPath);

                path = updPath;
            }

            return super.getResourcesAsStream(path);
        }

        @Override
        public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
            if (path.startsWith("bundle://")) {
                String updPath = path.replaceFirst("bundle://[^/]*", "");

                ClassLoaderBasedLiquibaseExecutor.this.log.debug("MAPPED BUNDLE PATH {} => {}", path, updPath);

                path = updPath;
            }

            return super.list(relativeTo, path, includeFiles, includeDirectories, recursive);
        }
    }
}
