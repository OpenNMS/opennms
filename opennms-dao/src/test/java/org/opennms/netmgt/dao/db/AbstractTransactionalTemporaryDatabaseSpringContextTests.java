/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Feb 10: Organize imports. - dj@opennms.org
 * 2007 Apr 14: Call setDirty() at the end of runTest, not early on. - dj@opennms.org
 * 2007 Apr 07: Add docs; use ArrayList instead of LinkedList. - dj@opennms.org
 * 
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */
package org.opennms.netmgt.dao.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * Provides an extension to Spring's AbstractTransactionalDataSourceSpringContextTests
 * class where a temporary database is created for each test and populated
 * with the OpenNMS database schema.
 * 
 * @see AbstractTransactionalDataSourceSpringContextTests
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public abstract class AbstractTransactionalTemporaryDatabaseSpringContextTests extends AbstractTransactionalDataSourceSpringContextTests {
    /**
     * The location of our overridable application context.  This contains
     * beans that are required for this class to function properly, but can
     * be safely overridden by other beans.
     * 
     * In particular, this contains a
     * TransactionManager bean, which is often overridden, in particular
     * when applicationContext-dao.xml is loaded as well.  This context is
     * placed at the very front of the context locations list so that any
     * other loaded context files can override it.
     * 
     * @see #loadContextLocations(String[])
     */
    public static final String CONTEXT_OVERRIDABLE = "classpath:META-INF/opennms/applicationContext-AbstractTransactionalTemporaryDatabaseSpringContextTests-overridable.xml";
    
    /**
     * The location of our non-overridable application context.  This contains
     * beans that are required for this class to function properly that
     * *cannot* be safely overridden by other beans.
     * 
     * In particular, this
     * contains the DataSource that refers to our temporary database.  Because
     * we want to allow tests to make any changes to their database we do not
     * allow the DataSource to be overridden to prevent accidental access to
     * a live database.  This context is placed at the very end of the context
     * locations list so that no other loaded context files can override it.
     *
     * @see #loadContextLocations(String[])
     */
    public static final String CONTEXT_NON_OVERRIDABLE = "classpath:META-INF/opennms/applicationContext-AbstractTransactionalTemporaryDatabaseSpringContextTests.xml";
    
    private PopulatedTemporaryDatabaseTestCase m_populatedTempDb;

    /**
     * Load the context locations for this test if tests are enabled,
     * adding our own application context files to the list.  If tests are
     * not enabled, no beans will be loaded and no temporary database is
     * created.
     * 
     * This prepends
     * our overridable context file onto the front of the user-supplied list
     * and appends our non-overridable context file to the end of the list.
     * 
     * To enforce the ordering of context files and to ensure that our
     * non-overridable context is last, this method is marked final.
     * 
     * @see #CONTEXT_OVERRIDABLE
     * @see #CONTEXT_NON_OVERRIDABLE
     * @see org.springframework.test.AbstractSingleSpringContextTests#loadContextLocations(java.lang.String[])
     * @see PopulatedTemporaryDatabaseTestCase.isEnabled()
     */
    @Override
    protected final ConfigurableApplicationContext loadContextLocations(String[] locations) throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            /*
             * Disable dependency checking so we don't try to load any beans
             * and return an empty context list.  This ensures that no beans
             * are loaded, including the bean that creates our temporary
             * database.
             */
            setDependencyCheck(false);
            return super.loadContextLocations(new String[0]);
        }
        
        assertNotNull("config locations list cannot be null", locations);
        
        List<String> newLocations = new ArrayList<String>();
        newLocations.add(CONTEXT_OVERRIDABLE); 
        newLocations.addAll(Arrays.asList(locations));
        newLocations.add(CONTEXT_NON_OVERRIDABLE); 
        return super.loadContextLocations((String[]) newLocations.toArray(new String[0]));
    }
    
    /**
     * Set the populated temporary database test case object.
     * 
     * We use and abuse this TestCase to do the work of setting up,
     * populating, and tearing down our test database.
     * 
     * @param testCase object we will use to build and tear down test database
     */
    public void setPopulatedTemporaryDatabaseTestCase(PopulatedTemporaryDatabaseTestCase testCase) {
        assertNotNull("testCase should not be null", testCase);
        m_populatedTempDb = testCase;
    }
    
    /**
     * If tests are enabled, this calls our super's onSetUpInTransaction()
     * and onSetUpInTransactionIfEnabled().  Neither are performed if tests
     * are disabled.
     * 
     * This method is marked final to ensure that no set up actions occur
     * if tests are disabled.  Any set up actions that need to happen within
     * the transaction need to be done by overridding
     * onSetUpInTransactionIfEnabled().
     * 
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     * @see #onSetUpInTransactionIfEnabled()
     */
    @Override
    final protected void onSetUpInTransaction() throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            return;
        }
        
        super.onSetUpInTransaction();
        
        onSetUpInTransactionIfEnabled();
    }
    
    /**
     * Empty method that can be overridden to implement actions to occur
     * on set up inside of the transaction.  This will not be called if
     * tests are disabled.
     * 
     * @see #onSetUpInTransaction()
     */
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        // Empty by default
    }
    
    /**
     * Run the unit test if tests are enabled.
     * 
     * Note: this sets the Spring context as dirty at the end of each test
     * to ensure that each test gets a fresh context, and in particular,
     * a fresh temporary database.
     * 
     * @see junit.framework.TestCase#runTest()
     * @see setDirty()
     */
    @Override
    protected void runTest() throws Throwable {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            PopulatedTemporaryDatabaseTestCase.notifyTestDisabled(getName());
            return;
        }

        try {
            super.runTest();
        } finally {
            /*
             * Mark the context as dirty when we're all done with the test.
             * This causes the context to be destroyed immediately, so it
             * needs to happen at the very end when everything is done.
             */
            setDirty();
        }
    }

    /**
     * If tests are enabled, this calls our super's onTearDownInTransaction()
     * and onTearDownInTransactionIfEnabled().  Neither are performed if tests
     * are disabled.
     * 
     * This method is marked final to ensure that no tear down actions occur
     * if tests are disabled.  Any tear down actions that need to happen within
     * the transaction need to be done by overridding
     * onTearDownInTransactionIfEnabled().
     * 
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDownInTransaction()
     * @see #onTearDownInTransactionIfEnabled()
     */
    @Override
    final protected void onTearDownInTransaction() throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            return;
        }
        
        super.onTearDownInTransaction();
        
        onTearDownInTransactionIfEnabled();
    }
    
    /**
     * Empty method that can be overridden to implement actions to occur
     * on tear down inside of the transaction.  This will not be called if
     * tests are disabled.
     * 
     * @see #onTearDownInTransaction()
     */
    protected void onTearDownInTransactionIfEnabled() throws Exception {
        // Empty by default
    }
    
    /**
     * If tests are enabled, this removes our temporary database and calls
     * our super's onTearDownAfterTransaction().  Neither are performed if
     * tests are disabled.
     * 
     * If you override this method, make sure to still call it otherwise the
     * temporary database will not be removed.
     * 
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDownAfterTransaction()
     * @see PopulatedTemporaryDatabaseTestCase#tearDown()
     */
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        if (m_populatedTempDb != null) {
            m_populatedTempDb.tearDown();
        }
        
        super.onTearDownAfterTransaction();
    }

    /**
     * Get a handy JDBC template to make database queries.
     * 
     * @return JDBC template
     */
    public SimpleJdbcTemplate getSimpleJdbcTemplate() {
        return new SimpleJdbcTemplate(jdbcTemplate);
    }
}
