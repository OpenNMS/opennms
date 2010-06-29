package org.opennms.features.poller.remote.gwt.server;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
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
    
    private static final DateFormat s_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    
    @Autowired
    private LocationMonitorDao m_locationMonitorDao;
    
    @Autowired
    private LocationDataManager m_locationDataManager;
    
    @Autowired
    private LocationDataService m_locationDataService;
    
    @Autowired
    private ApplicationDao m_applicationDao;

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
        long count = 10;
        long start = System.currentTimeMillis();
        
        for(int i = 0; i < count; i++ ) {
            List<LocationInfo> locations = m_locationDataService.getInfoForAllLocations();
            assertEquals(2864, locations.size());
        }
        
        System.err.printf("Avg getInfoForAllLocations: %d\n", (System.currentTimeMillis() - start)/count);
    }
    
    @Test
    public void testGetInfoForAllApplications() {
        long count = 10;
        long start = System.currentTimeMillis();
        
        for(int i = 0; i < count; i++ ) {
            List<ApplicationInfo> locations = m_locationDataService.getInfoForAllApplications();
            assertEquals(14, locations.size());
        }

        System.err.printf("Avg getInfoForAllApplications: %d\n", (System.currentTimeMillis() - start)/count);
    }
    
    @Test
    public void testGetSatusDetailsForLocation() {
        
        OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition("00002");
        
        m_locationDataService.getStatusDetailsForLocation(def);
    }
    
    @Test
    public void testGetSatusDetailsForApplication() {
        String appName = "Domain Controllers";

        int count = 100;
        long start = System.currentTimeMillis();

        for(int i = 0; i < count; i++) {

            OnmsApplication app = m_applicationDao.findByName(appName);

            //System.err.println("TEST testGetSatusDetailsForApplication: calling getStatusDetailsForApplication");

            StatusDetails details = m_locationDataService.getStatusDetailsForApplication(app);
            assertEquals(Status.UP, details.getStatus());

        }

        System.err.println(String.format("Avg getStatusDetailsForApplication: %d\n", (System.currentTimeMillis() - start)/count));
    }
    
    @Test
    public void testGetApplicationInfo() {
        String appName = "Domain Controllers";
        
        
        
        OnmsApplication app = m_applicationDao.findByName(appName);  
        
        System.err.println("TEST testGetApplicationInfo: calling getApplicationInfo");
        
        m_locationDataService.getApplicationInfo(app, new StatusDetails());
    }
    
    @Test
    public void testLocationMonitorDaoFindByApplication() {
        
        OnmsApplication app = m_applicationDao.findByName("Domain Controllers");
        
        Collection<OnmsLocationMonitor> monitors = m_locationMonitorDao.findByApplication(app);
        
        assertEquals(18, monitors.size());

    }

    @Test
    public void testGetAllStatusChangesAt() {
        
        Collection<OnmsLocationSpecificStatus> changes = m_locationMonitorDao.getAllStatusChangesAt(new Date());
        

        assertEquals(450, changes.size());

    }

    @Test
    public void testGetStatusChangesForApplicationBetween() throws ParseException {
        
        Collection<OnmsLocationSpecificStatus> changes = m_locationMonitorDao.getStatusChangesForApplicationBetween(june(7, 2010), june(8, 2010), "Domain Controllers");
       assertEquals(54, changes.size());

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
    
    @Test
    public void testJune() throws ParseException {
        Date d= june(1, 2009);
        
        assertEquals("2009-06-01 00:00:00,000", s_format.format(d));
        
    }
    
    Date june(int day, int year) {
        Calendar cal = new GregorianCalendar(year, Calendar.JUNE, day);
        return cal.getTime();
    }
    
    
}
