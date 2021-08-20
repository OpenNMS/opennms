/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
// TODO : Patrick: this is the wrong copyright header but I copied it in here so our checks will work...

package liquibase.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * Controls the database connections for running tests.
 * For times we aren't wanting to run the database-hitting tests, set the "test.databases" system property
 * to be a comma-separated list of the databses we want to test against.  The string is checked against the database
 * url.
 */
public class TestContext {
    private static TestContext instance = new TestContext();

    private Set<Database> allDatabases;
    private ResourceAccessor resourceAccessor;

    public static TestContext getInstance() {
        return instance;
    }

    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>();

            allDatabases.addAll(DatabaseFactory.getInstance().getImplementedDatabases());

            List<Database> toRemove = new ArrayList<Database>();
            for (Database database : allDatabases) {
                if ((database instanceof SQLiteDatabase) //todo: re-enable sqlite testing
                        || (database instanceof MockDatabase)) {
                    toRemove.add(database);
                }
                database.setCanCacheLiquibaseTableInfo(false);
            }
            allDatabases.removeAll(toRemove);
        }
        return allDatabases;
    }

    public File findCoreJvmProjectRoot() throws URISyntaxException {
        return new File(findCoreProjectRoot().getParentFile(), "liquibase-core");
    }

    public File findIntegrationTestProjectRoot() throws URISyntaxException {
        return new File(findCoreProjectRoot().getParentFile(), "liquibase-integration-tests");
    }

    public File findCoreProjectRoot() throws URISyntaxException {
        URI uri = new URI(this.getClass().getClassLoader().getResource("liquibase/test/TestContext.class").toExternalForm());
        if(!uri.isOpaque()) {
            File thisClassFile = new File(uri);
            return thisClassFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        }
        uri = new URI(this.getClass().getClassLoader().getResource("liquibase/integration/commandline/Main.class").toExternalForm());
        if(!uri.isOpaque()) {
            File thisClassFile = new File(uri);
            return new File(thisClassFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile(), "liquibase-core");
        }
        uri = new URI(this.getClass().getClassLoader().getResource("liquibase/test/DatabaseTest.class").toExternalForm());
        if(!uri.isOpaque()) {
            File thisClassFile = new File(uri);
            return new File(thisClassFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile(), "liquibase-core");
        }
        throw new IllegalStateException("Cannot find liquibase-core project root");
    }

    public ResourceAccessor getTestResourceAccessor() throws URISyntaxException, MalformedURLException {
        if (resourceAccessor == null) {

            resourceAccessor = new ClassLoaderResourceAccessor(new URLClassLoader(new URL[]{
                    new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/target/classes").toURI().toURL(),
                    new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/target/test-classes").toURI()
                            .toURL(),
                    new File(TestContext.getInstance().findCoreProjectRoot(), "/target/classes").toURI().toURL(),
                    new File(TestContext.getInstance().findCoreProjectRoot(), "/target/test-classes").toURI().toURL()
            }));
        }

        return resourceAccessor;
    }


}
