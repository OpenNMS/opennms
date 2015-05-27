/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.server;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.reportMatcher;
import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import de.novanic.eventservice.service.EventExecutorService;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-remote-poller.xml",
        "classpath:/locationDataManagerTest.xml",
        "classpath:META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(useExistingDatabase="opennms")
@Transactional
@Ignore("requires custom database")
public class LocationDataManagerTest implements InitializingBean {
    
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
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

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
        handler.start(2880);
        handler.handle(isA(OnmsMonitoringLocationDefinition.class));
        expectLastCall().times(2880);
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
            assertEquals(2880, locations.size());
        }
        
        System.err.printf("Avg getInfoForAllLocations: %d\n", (System.currentTimeMillis() - start)/count);
    }
    
    @Test
    public void testGetStatusDetailsForAllLocations() {
        Map<String, StatusDetails> statusDetails = m_locationDataService.getStatusDetailsForAllLocations();
        assertEquals(2880, statusDetails.size());

        assertEquals(2880-376, countStatus(Status.UNKNOWN, statusDetails));
        assertEquals(3, countStatus(Status.DISCONNECTED, statusDetails));
        assertEquals(4, countStatus(Status.STOPPED, statusDetails));
        assertEquals(0, countStatus(Status.MARGINAL, statusDetails));

        assertEquals(12, countStatus(Status.DOWN, statusDetails));
        assertEquals(357, countStatus(Status.UP, statusDetails));
        
    }

    private int countStatus(Status status, Map<String, StatusDetails> statusDetails) {
        int count = 0;
        for(Entry<String, StatusDetails> entry : statusDetails.entrySet()) {
            if (status.equals(entry.getValue().getStatus())) {
                count++;
            }
        }
        return count;
    }
    
    @Test
    public void testGetInfoForAllApplications() {
        long count = 10;
        long start = System.currentTimeMillis();
        
        for(int i = 0; i < count; i++ ) {
            List<ApplicationInfo> applications = m_locationDataService.getInfoForAllApplications();
            assertEquals(12, applications.size());
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
        
        assertEquals(376, monitors.size());

    }

    @Test
    public void testGetAllStatusChangesAt() {
        
        Collection<OnmsLocationSpecificStatus> changes = m_locationMonitorDao.getAllStatusChangesAt(new Date());
        

        assertEquals(4888, changes.size());

    }

    @Test
    @Ignore
    public void testGetStatusChangesForApplicationBetween() throws ParseException {
        
        Collection<OnmsLocationSpecificStatus> changes = m_locationMonitorDao.getStatusChangesForApplicationBetween(june(7, 2010), june(8, 2010), "Domain Controllers");
       assertEquals(54, changes.size());

    }

    @Test
    public void testStart() {
        EventExecutorService service = m_easyMockUtils.createMock(EventExecutorService.class);
        
        service.addEventUserSpecific(hasStatus(Status.DOWN));
        expectLastCall().times(12);
        service.addEventUserSpecific(hasStatus(Status.UP));
        expectLastCall().times(357);


        service.addEventUserSpecific(hasStatus(Status.DISCONNECTED));
        expectLastCall().times(3);

        service.addEventUserSpecific(hasStatus(Status.STOPPED));
        expectLastCall().times(4);
        
        service.addEventUserSpecific(hasStatus(Status.UNKNOWN));
        expectLastCall().times(2880-376);
        
        service.addEventUserSpecific(isA(ApplicationUpdatedRemoteEvent.class));
        expectLastCall().times(12);
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
    

    public static LocationUpdatedRemoteEvent hasStatus(final Status status) {
        reportMatcher(new IArgumentMatcher() {

            @Override
            public void appendTo(StringBuffer buffer) {
                buffer.append("hasStatus(\"" + status + "\")");
            }

            @Override
            public boolean matches(Object argument) {
                if (argument instanceof LocationUpdatedRemoteEvent) {
                    LocationUpdatedRemoteEvent e = (LocationUpdatedRemoteEvent)argument;
                    return status.equals(e.getLocationInfo().getStatus());
                } else {
                    return false;
                }
            }
            
        });
        return null;
    }
    
}
