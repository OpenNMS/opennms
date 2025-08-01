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
