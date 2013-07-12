package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml"
})
@JUnitConfigurationEnvironment
public class DaoEventdServiceManagerTest {
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    DaoEventdServiceManager m_eventdServiceManager;

    @Before
    public void setUp() throws Exception {
        m_eventdServiceManager = new DaoEventdServiceManager();
        m_eventdServiceManager.setServiceTypeDao(m_serviceTypeDao);
        m_eventdServiceManager.afterPropertiesSet();
    }

    @Test
    public void testSync() {
        m_eventdServiceManager.dataSourceSync();
        m_serviceTypeDao.save(new OnmsServiceType("ICMP"));
        assertTrue(m_eventdServiceManager.getServiceId("ICMP") > 0);
    }

}
