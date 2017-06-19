/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.internal.MethodSorter;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.db.HikariCPConnectionFactory;
import org.opennms.core.db.XADataSourceFactory;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.Assert;

/**
 * This {@link TestExecutionListener} creates a temporary database and then
 * registers it as the default datasource inside {@link DataSourceFactory} by
 * using {@link DataSourceFactory#setInstance(DataSource)}.
 * 
 * To change the settings for the temporary database, use the 
 * {@link JUnitTemporaryDatabase} annotation on the test class or method.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class TemporaryDatabaseExecutionListener extends AbstractTestExecutionListener {

    private boolean m_createNewDatabases = false;
    private TemporaryDatabase m_database;
    private final Queue<TemporaryDatabase> m_databases = new ConcurrentLinkedQueue<TemporaryDatabase>();

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        Throwable closeThrowable = null;

        //System.err.println(String.format("TemporaryDatabaseExecutionListener.afterTestMethod(%s)", testContext));

        final JUnitTemporaryDatabase jtd = findAnnotation(testContext);
        if (jtd == null) {
            return;
        }

        // Close down the data sources that are referenced by the static DataSourceFactory helper classes
        try {
            DataSourceFactory.close();
            XADataSourceFactory.close();
        } catch (Throwable t) {
            closeThrowable = t;
        }

        try {
            if (m_createNewDatabases && m_database != null) {
                m_database.drop();
            }
        } finally {
            // We must mark the application context as dirty so that the DataSourceFactoryBean is
            // correctly pointed at the next temporary database.
            //
            // If the next database is the same as the current database, then do not rewire.
            // NOTE: This does not work because the Hibernate objects need to be reinjected or they
            // will reject database operations because they think that the database rows already
            // exist even if they were rolled back after a previous test execution.
            //
            if (jtd.dirtiesContext()) {
                testContext.markApplicationContextDirty(HierarchyMode.CURRENT_LEVEL);
                testContext.setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, Boolean.TRUE);
            } else {
                if (m_database != m_databases.peek()) {
                    testContext.markApplicationContextDirty(HierarchyMode.CURRENT_LEVEL);
                    testContext.setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, Boolean.TRUE);
                }
            }
        }

        if (closeThrowable != null) {
            throw new Exception("Caught a Throwable while closing database connections after test. Pickup after yourself! " + closeThrowable, closeThrowable);
        }
    }

    @Override
    public void afterTestClass(final TestContext testContext) throws Exception {
        //System.err.println(String.format("TemporaryDatabaseExecutionListener.afterTestClass(%s)", testContext));

        try {
            if (!m_createNewDatabases && m_database != null) {
                m_database.drop();
            }
        } catch (Throwable t) {
            throw new Exception("Caught an exception while dropping the database at the end of the test: " + t, t);
        } finally {
            testContext.markApplicationContextDirty(HierarchyMode.CURRENT_LEVEL);
            testContext.setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, Boolean.TRUE);
        }
    }

    private static JUnitTemporaryDatabase findAnnotation(final TestContext testContext) {
        JUnitTemporaryDatabase jtd = null;
        final Method testMethod = testContext.getTestMethod();
        if (testMethod != null) {
            jtd = testMethod.getAnnotation(JUnitTemporaryDatabase.class);
        }
        if (jtd == null) {
            final Class<?> testClass = testContext.getTestClass();
            jtd = testClass.getAnnotation(JUnitTemporaryDatabase.class);
        }
        return jtd;
    }

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        //System.err.println(String.format("TemporaryDatabaseExecutionListener.beforeTestMethod(%s)", testContext));

        // FIXME: Is there a better way to inject the instance into the test class?
        if (testContext.getTestInstance() instanceof TemporaryDatabaseAware<?>) {
            //System.err.println("injecting TemporaryDatabase into TemporaryDatabaseAware test: "
            //        + testContext.getTestInstance().getClass().getSimpleName() + "."
            //        + testContext.getTestMethod().getName());
            injectTemporaryDatabase(testContext);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void injectTemporaryDatabase(final TestContext testContext) {
        ((TemporaryDatabaseAware) testContext.getTestInstance()).setTemporaryDatabase(m_database);
    }

    public static List<Method> getOrderedTestMethods(Class<?> testClass) {
        final List<Method> methods = new LinkedList<>();
        getOrderedTestMethods(testClass, methods);
        return methods;
    }

    public static void getOrderedTestMethods(Class<?> testClass, List<Method> methods) {
        methods.addAll(Arrays.asList(MethodSorter.getDeclaredMethods(testClass)));
        final Class<?> testSuperClass = testClass.getSuperclass();
        if (testSuperClass != null) {
            getOrderedTestMethods(testSuperClass, methods);
        }
    }

    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        TemporaryDatabasePostgreSQL.failIfUnitTest();

        // Fire up a thread pool for each CPU to create test databases
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        final JUnitTemporaryDatabase classJtd = testContext.getTestClass().getAnnotation(JUnitTemporaryDatabase.class);

        final Future<TemporaryDatabase> classDs;
        if (classJtd != null) {
            classDs = pool.submit(new CreateNewDatabaseCallable(classJtd, testContext.getTestClass().getName(), null));
            if (classJtd.reuseDatabase() == false) {
                m_createNewDatabases = true;
            }
        } else {
            classDs = null;
        }

        List<Future<TemporaryDatabase>> futures = new ArrayList<Future<TemporaryDatabase>>();
        for (Method method : getOrderedTestMethods(testContext.getTestClass())) {
            if (method != null) {
                final JUnitTemporaryDatabase methodJtd = method.getAnnotation(JUnitTemporaryDatabase.class);
                boolean methodHasTest = method.getAnnotation(Test.class) != null;
                if (methodHasTest) {
                    // If there is a method-specific annotation, use it to create the temporary database
                    if (methodJtd != null) {
                        // Create a new database based on the method-specific annotation
                        Future<TemporaryDatabase> submit = pool.submit(new CreateNewDatabaseCallable(methodJtd, testContext.getTestClass().getName(), method.getName()));
                        Assert.notNull(submit, "pool.submit(new CreateNewDatabaseCallable(methodJtd = " + methodJtd + ")");
                        futures.add(submit);
                    } else if (classJtd != null) {
                        if (m_createNewDatabases) {
                            // Create a new database based on the test class' annotation
                            Future<TemporaryDatabase> submit = pool.submit(new CreateNewDatabaseCallable(classJtd, testContext.getTestClass().getName(), method.getName()));
                            Assert.notNull(submit, "pool.submit(new CreateNewDatabaseCallable(classJtd = " + classJtd + ")");
                            futures.add(submit);
                        } else {
                            // Reuse the database based on the test class' annotation
                            Assert.notNull(classDs, "classDs");
                            futures.add(classDs);
                        }
                    }
                }
            }
        }

        for (Future<TemporaryDatabase> db : futures) {
            m_databases.add(db.get());
        }
    }

    @Override
    public void prepareTestInstance(final TestContext testContext) throws Exception {
        //System.err.println(String.format("TemporaryDatabaseExecutionListener.prepareTestInstance(%s); details: %s", testContext.hashCode(), testContext));
        final JUnitTemporaryDatabase jtd = findAnnotation(testContext);

        if (jtd == null) {
            return;
        }

        m_database = m_databases.remove();

        // We should pool connections to simulate the behavior of OpenNMS,
        // but we also need to be able to shut down the connection pool reliably
        // after tests complete. Some connection pools aren't great at this
        // so make it configurable.
        //
        if (jtd.poolConnections()) {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setDatabaseName(m_database.getTestDatabase());
            ds.setUserName(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
            ds.setPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
            ds.setUrl(System.getProperty(TemporaryDatabase.URL_PROPERTY, TemporaryDatabase.DEFAULT_URL) + m_database.getTestDatabase());
            ds.setClassName(System.getProperty(TemporaryDatabase.DRIVER_PROPERTY, TemporaryDatabase.DEFAULT_DRIVER));

            HikariCPConnectionFactory pool = new HikariCPConnectionFactory(ds);
            // NMS-8911: Reduce the max connection lifetime so that HikariCP recycles 
            // connections more aggressively during tests
            pool.setMaxLifetime(500);

            DataSourceFactory.setInstance(pool);
        } else {
            DataSourceFactory.setInstance(m_database);
        }
        XADataSourceFactory.setInstance(m_database);

        //System.err.println(String.format("TemporaryDatabaseExecutionListener.prepareTestInstance(%s) prepared db %s; details: %s", testContext.hashCode(), m_database.toString(), testContext));
        //System.err.println("Temporary Database Name: " + m_database.getTestDatabase());
    }

    private static class CreateNewDatabaseCallable implements Callable<TemporaryDatabase> {
        private final JUnitTemporaryDatabase m_jtd;
        private final String m_className;
        private final String m_methodName;


        public CreateNewDatabaseCallable(JUnitTemporaryDatabase jtd, String className, String methodName) {
            m_jtd = jtd;
            m_className = className;
            m_methodName = methodName;
        }

        @Override
        public TemporaryDatabase call() throws Exception {
            return createNewDatabase(m_jtd, m_className, m_methodName);
        }

    }

    private static TemporaryDatabase createNewDatabase(JUnitTemporaryDatabase jtd, String className, String methodName) throws Exception {
        boolean useExisting = false;
        if (jtd.useExistingDatabase() != null) {
            useExisting = !jtd.useExistingDatabase().equals("");
        }

        final String dbName = useExisting ? jtd.useExistingDatabase() : null;

        final TemporaryDatabase retval = ((jtd.tempDbClass()).getConstructor(String.class, Boolean.TYPE).newInstance(dbName, useExisting));
        retval.setPopulateSchema(jtd.createSchema() && !useExisting);
        if (className != null) {
            retval.setClassName(className);
        }
        if (methodName != null) {
            retval.setMethodName(methodName);
        }
        StringBuffer b = new StringBuffer();
        if (jtd.useExistingDatabase() != null && !"".equals(jtd.useExistingDatabase())) {
            b.append("use existing database: " + jtd.useExistingDatabase() + " ");
        }
        b.append("reuse database: " + jtd.reuseDatabase());
        retval.setTestDetails(b.toString());
        retval.create();
        return retval;
    }
}
