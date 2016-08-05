/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/emptyContext.xml"
})
@JUnitTemporaryDatabase
public class MonitoringLocationsMigratorOfflineIT {
    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        System.setProperty("opennms.home", "target/home");
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
    }

    /**
     * Test fixing the configuration file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMigrateLargeConfigToDatabase() throws Exception {
        MonitoringLocationsMigratorOffline migrator = new MonitoringLocationsMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();

        assertFalse(new File("target/home/etc/monitoring-locations.xml").exists());
        assertTrue(new File("target/home/etc/monitoring-locations.xml.zip").exists());
        assertTrue(new File("target/home/etc/monitoring-locations.xml.zip").isFile());

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceFactory.getInstance());

        // this first one has been a little persnickity for unknown reasons so we log some more information
        String first = jdbcTemplate.queryForObject("SELECT monitoringarea FROM monitoringlocations ORDER BY id LIMIT 1", String.class);
        String last = jdbcTemplate.queryForObject("SELECT monitoringarea FROM monitoringlocations ORDER BY id DESC LIMIT 1", String.class);
        // 2864 from our file, plus 1 that comes in create.sql for localhost
        assertEquals("count of monitoringlocations; first: " + first + "; last: " + last, 2864 + 1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM monitoringlocations", Integer.class).intValue());

        assertEquals("count of monitoringlocations WHERE tag = 'divisbileBy3'", 954, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM monitoringlocationstags WHERE tag = 'divisbileBy3'", Integer.class).intValue());
        assertEquals("count of monitoringlocations WHERE tag = 'divisibleBy5'", 572, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM monitoringlocationstags WHERE tag = 'divisibleBy5'", Integer.class).intValue());
        assertEquals("count of monitoringlocations WHERE tag = 'divisibleBy7'", 409, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM monitoringlocationstags WHERE tag = 'divisibleBy7'", Integer.class).intValue());
        assertEquals("count of monitoringlocations WHERE tag = 'odd'", 1432, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM monitoringlocationstags WHERE tag = 'odd'", Integer.class).intValue());
        assertEquals("count of monitoringlocations WHERE tag = 'even'", 1432, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM monitoringlocationstags WHERE tag = 'even'", Integer.class).intValue());
    }

    /**
     * Test fixing the configuration file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMissingConfigFile() throws Exception {
        FileUtils.deleteQuietly(new File("target/home/etc/monitoring-locations.xml"));
        MonitoringLocationsMigratorOffline migrator = new MonitoringLocationsMigratorOffline();
        migrator.execute();
    }
}
