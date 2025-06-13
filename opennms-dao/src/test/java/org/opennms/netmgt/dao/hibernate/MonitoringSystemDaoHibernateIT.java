package org.opennms.netmgt.dao.hibernate;

import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MonitoringSystemDaoHibernateIT implements InitializingBean {
    public static final String DEFAULT_SYSTEM_LABEL = "localhost";
    public static final String DEFAULT_SYSTEM_LOCATION = "Default";
    private String testSystemId;

    @Autowired
    private MonitoringSystemDao m_monitoringSystemDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Before
    public void setUp() {
        testSystemId = UUID.randomUUID().toString().toLowerCase();

        // force test database to have our main monitoring system (DistPoller is actually a Monitoring System)
        m_databasePopulator.resetDatabase(true);
        m_databasePopulator.populateMainDistPoller(testSystemId, DEFAULT_SYSTEM_LABEL, DEFAULT_SYSTEM_LOCATION);
        m_databasePopulator.populateDatabase();
    }

    @After
    public void tearDown() {
        m_databasePopulator.resetDatabase(true);
    }

    @Test
    @Transactional
    public void testGetNumMonitoringSystems() {
        long result = m_monitoringSystemDao.getNumMonitoringSystems(OnmsMonitoringSystem.TYPE_OPENNMS);
        assertEquals(1, result);

        result = m_monitoringSystemDao.getNumMonitoringSystems(OnmsMonitoringSystem.TYPE_MINION);
        assertEquals(0, result);

        result = m_monitoringSystemDao.getNumMonitoringSystems(OnmsMonitoringSystem.TYPE_SENTINEL);
        assertEquals(0, result);
    }

    @Test
    @Transactional
    public void testGetMainSystem() {
        OnmsMonitoringSystem system = m_monitoringSystemDao.getMainMonitoringSystem();

        assertNotNull(system);
        assertEquals(testSystemId, system.getId());
        assertEquals(DEFAULT_SYSTEM_LABEL, system.getLabel());
        assertEquals(DEFAULT_SYSTEM_LOCATION, system.getLocation());
    }
}
