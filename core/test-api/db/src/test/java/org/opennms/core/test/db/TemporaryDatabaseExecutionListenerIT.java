/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Integration Test for {@link TemporaryDatabaseExecutionListener} with 
 * the default {@link TemporaryDatabasePostgreSQL} test database class.
 * 
 * @author dgregor
  */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/migratorTest.xml"
})
@JUnitTemporaryDatabase
public class TemporaryDatabaseExecutionListenerIT {
    
    @Autowired
    private DataSource m_dataSource;

    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testMethodAnnotationSchemaFalse() throws Exception {
        assertFalse(monitoringLocationsExists());
    }

    @Test
    @JUnitTemporaryDatabase(createSchema=true)
    public void testMethodAnnotationSchemaTrue() throws Exception {
        assertTrue(monitoringLocationsExists());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testMethodAnnotationPlain() throws Exception {
        assertTrue(monitoringLocationsExists());
    }
    
    @Test
    public void testMethodNoAnnotation() throws Exception {
        assertTrue(monitoringLocationsExists());
    }

    @Test
    public void testGetBlame() {
        assertEquals("blame string", getClass().getName() + ".?: reuse database: true", new JdbcTemplate(m_dataSource).queryForObject("SELECT blame FROM blame", String.class));
    }

    protected boolean monitoringLocationsExists() {
        try {
            new JdbcTemplate(m_dataSource).queryForObject("SELECT count(id) FROM monitoringlocations", Integer.class);
            return true;
        } catch (BadSqlGrammarException e) {
            return false;
        }
    }
}
