//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.db;

import java.util.Arrays;
import java.util.LinkedList;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public abstract class AbstractTransactionalTemporaryDatabaseSpringContextTests
    extends AbstractTransactionalDataSourceSpringContextTests {
    
    private PopulatedTemporaryDatabaseTestCase m_populatedTempDb;

    @Override
    protected final ConfigurableApplicationContext
            loadContextLocations(String[] locations) throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            setDependencyCheck(false);
            return super.loadContextLocations(new String[0]);
        }
        
        assertNotNull("config locations list cannot be null", locations);
        
        LinkedList<String> newLocations = new LinkedList<String>();
        newLocations.add("classpath:META-INF/opennms/applicationContext-AbstractTransactionalTemporaryDatabaseSpringContextTests-overridable.xml"); 
        newLocations.addAll(Arrays.asList(locations));
        newLocations.add("classpath:META-INF/opennms/applicationContext-AbstractTransactionalTemporaryDatabaseSpringContextTests.xml"); 
        return super.loadContextLocations((String[]) newLocations.toArray(new String[0]));
    }
    
    public void setPopulatedTemporaryDatabaseTestCase(PopulatedTemporaryDatabaseTestCase testCase) {
        m_populatedTempDb = testCase;
    }
    
    @Override
    final protected void onSetUpInTransaction() throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            return;
        }
        
        super.onSetUpInTransaction();
        
        onSetUpInTransactionIfEnabled();
    }
    
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        // Empty by default
    }
    
    @Override
    protected void runTest() throws Throwable {
        setDirty();

        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            PopulatedTemporaryDatabaseTestCase.notifyTestDisabled(getName());
            return;
        }

        super.runTest();
    }

    @Override
    final protected void onTearDownInTransaction() throws Exception {
        if (!PopulatedTemporaryDatabaseTestCase.isEnabled()) {
            return;
        }
        
        super.onTearDownInTransaction();
        
        onTearDownInTransactionIfEnabled();
    }
    
    protected void onTearDownInTransactionIfEnabled() throws Exception {
        // Empty by default
    }
    
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        if (m_populatedTempDb != null) {
            m_populatedTempDb.tearDown();
        }
    }

    protected SimpleJdbcTemplate getJdbcTemplate() {
        return new SimpleJdbcTemplate(jdbcTemplate);
    }
}
