package org.opennms.features.poller.remote.gwt.server;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
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
@JUnitTemporaryDatabase(useExistingDatabase="opennmspj")
@Transactional
public class LocationDataManagerTest {
    
    @Autowired
    private LocationMonitorDao m_locationMonitorDao;
    
    @Autowired
    private LocationDataManager m_locationDataManager;
    
    @Autowired
    private LocationDataService m_locationDataService;

    private EasyMockUtils m_easyMockUtils = new EasyMockUtils();
    
    @Before
    public void setUp() {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.hibernate", "INFO");
        p.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        MockLogAppender.setupLogging(p);
    }
    
    @Test
    public void testHandleAllMonitoringLocationDefinitions() {
        LocationDefHandler handler = m_easyMockUtils.createMock(LocationDefHandler.class);
        handler.start(2864);
        handler.handle(isA(OnmsMonitoringLocationDefinition.class));
        expectLastCall().times(2864);
        handler.finish();
        
        m_easyMockUtils.replayAll();
        
        m_locationDataService.handleAllMonitoringLocationDefinitions(Collections.singleton(handler));
        
        m_easyMockUtils.verifyAll();
    }


    @Test
    public void testGetInfoForAllLocations() {
        List<LocationInfo> locations = m_locationDataService.getInfoForAllLocations();
        
        assertEquals(2864, locations.size());
    }
    
    @Test
    public void testGetSatusDetails() {
        
        OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition("00002");
        
        m_locationDataService.getStatusDetails(def);
    }
  
    @Test
    public void testStart() {
        EventExecutorService service = m_easyMockUtils.createMock(EventExecutorService.class);
        service.addEventUserSpecific(isA(LocationUpdatedRemoteEvent.class));
        expectLastCall().times(2864);
        service.addEventUserSpecific(isA(ApplicationUpdatedRemoteEvent.class));
        expectLastCall().times(14);
        service.addEventUserSpecific(isA(UpdateCompleteRemoteEvent.class));
        m_easyMockUtils.replayAll();
        m_locationDataManager.doInitialize(service);
        m_easyMockUtils.verifyAll();
        
    }
    
    
}
