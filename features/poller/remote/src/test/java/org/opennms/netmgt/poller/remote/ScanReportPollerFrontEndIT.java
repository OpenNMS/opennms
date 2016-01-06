/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.remote.support.ScanReportPollerFrontEnd;
import org.opennms.test.FileAnticipator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
        "classpath:/META-INF/opennms/applicationContext-pollerFrontEnd-scanReport.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
    "opennms.pollerBackend.monitorCheckInterval=500",
    "opennms.pollerBackend.disconnectedTimeout=3000"
})
@JUnitTemporaryDatabase
public class ScanReportPollerFrontEndIT implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ScanReportPollerFrontEndIT.class);

    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private PollerFrontEnd m_frontEnd;

    private static FileAnticipator m_fileAnticipator;

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_populator.populateDatabase();
    }

    @Test
    public void testRegister() throws Exception {
        // Check preconditions
        assertFalse(m_frontEnd.isRegistered());
        assertEquals(1, m_jdbcTemplate.queryForInt("select count(*) from monitoringsystems"));
        assertEquals(0, m_jdbcTemplate.queryForInt("select count(*) from monitoringsystemsproperties"));
        assertTrue("There were unexpected poll results", 0 == m_jdbcTemplate.queryForInt("select count(*) from location_specific_status_changes"));

        m_frontEnd.initialize();

        // Initialization shouldn't change anything since we're unregistered
        assertFalse(m_frontEnd.isRegistered());
        assertEquals(1, m_jdbcTemplate.queryForInt("select count(*) from monitoringsystems"));
        assertEquals(0, m_jdbcTemplate.queryForInt("select count(*) from monitoringsystemsproperties"));
        assertTrue("There were unexpected poll results", 0 == m_jdbcTemplate.queryForInt("select count(*) from location_specific_status_changes"));

        // Start up the remote poller
        m_frontEnd.register("RDU");
        assertTrue(m_frontEnd.isStarted());
        String monitorId = m_frontEnd.getMonitoringSystemId();

        assertTrue(m_frontEnd.isRegistered());
        for (Map.Entry entry : ((ScanReportPollerFrontEnd)m_frontEnd).getDetails().entrySet()) {
            LOG.info("Front end detail: " + entry.getKey() + " -> " + entry.getValue());
        }
        // Make sure there is a total of one remote poller
        assertEquals(2, m_jdbcTemplate.queryForInt("select count(*) from monitoringsystems"));
        assertEquals(5, m_jdbcTemplate.queryForInt("select count(*) from monitoringsystemsproperties where monitoringsystemid = ?", monitorId));
        // Make sure there is a total of one remote poller with the expected ID
        assertEquals(1, getMonitorCount(monitorId));

        assertEquals(System.getProperty("os.arch"), m_jdbcTemplate.queryForObject("select propertyValue from monitoringsystemsproperties where monitoringsystemid = ? and property = ?", String.class, monitorId, "os.arch"));
        assertEquals(System.getProperty("os.name"), m_jdbcTemplate.queryForObject("select propertyValue from monitoringsystemsproperties where monitoringsystemid = ? and property = ?", String.class, monitorId, "os.name"));
        assertEquals(System.getProperty("os.version"), m_jdbcTemplate.queryForObject("select propertyValue from monitoringsystemsproperties where monitoringsystemid = ? and property = ?", String.class, monitorId, "os.version"));

        long wait = 60000L;
        while (wait > 0) {
            Thread.sleep(1000L);
            wait -= 1000L;
            LOG.debug("wait = {}", wait);

            // If the monitor disconnects, break
            if (
              getMonitorCount(monitorId) == 1 &&
              getDisconnectedCount(monitorId) == 1
            ) break;
        }

        assertEquals(1, getMonitorCount(monitorId));
        assertEquals(1, getDisconnectedCount(monitorId));

        m_frontEnd.stop();
    }

    protected int getSpecificChangesCount(String monitorId) {
        return m_jdbcTemplate.queryForInt("select count(*) from location_specific_status_changes where systemid = ?", monitorId);
    }

    protected int getDisconnectedCount(String monitorId) {
        return m_jdbcTemplate.queryForInt("select count(*) from monitoringsystems where status=? and id=?", MonitorStatus.DISCONNECTED.toString(), monitorId);
    }

    protected int getMonitorCount(String monitorId) {
        return m_jdbcTemplate.queryForInt("select count(*) from monitoringsystems where id=?", monitorId);
    }
}
