package org.opennms.features.poller.remote.gwt.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import de.novanic.eventservice.service.EventExecutorService;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations = {
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-remote-poller.xml",
        "classpath:/locationDataManagerTest.xml"
})
@JUnitTemporaryDatabase()
@Transactional
public class LocationDataManagerTest {
    @Autowired
    private LocationDataManager m_locationDataManager;

    private EasyMockUtils m_easyMockUtils = new EasyMockUtils();
    
    @Before
    public void setUp() {
    }
    
    @Test
    public void testStart() {
        EventExecutorService service = m_easyMockUtils.createMock(EventExecutorService.class);
        m_easyMockUtils.replayAll();
        m_locationDataManager.doInitialize(service);
        m_easyMockUtils.verifyAll();
        
    }
}
