package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;


public class RemotePollerAvailabilityRestServiceTest extends AbstractSpringJerseyRestTestCase {

    @Autowired
    ApplicationDao m_applicationDao;
    
    @Autowired
    LocationMonitorDao m_locationMonitorDao;
    
    @Autowired
    MonitoredServiceDao m_monServiceDao;
    
    @Autowired
    DatabasePopulator m_databasePopulator;
    
    public void beforeServletStart() throws IOException {
        String monitoringLocations = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                                     "<monitoring-locations-configuration xmlns='http://xmlns.opennms.org/xsd/config/monitoring-locations'>\n" +
                                     "<locations>\n" +
                                         "<location-def location-name='RDU' monitoring-area='raleigh'\n" +
                                             "polling-package-name='raleigh'\n" +
                                             "geolocation='35.7174,-79.1619'\n" +
                                             "coordinates='35.7174,-79.1619' priority='100'>\n" +
                                             "<ns1:tags xmlns:ns1='http://xmlns.opennms.org/xsd/config/tags'/>\n" +
                                          "</location-def>\n" +
                                     "</locations>\n" +
                                     "</monitoring-locations-configuration>";
        
        
        File locationDefs = new File("target/test/opennms-home/etc/monitoring-locations.xml");
        FileUtils.writeStringToFile(locationDefs, monitoringLocations);
    }
    
    public void afterServletStart() {
        MockLogAppender.setupLogging();
        
        m_databasePopulator = getBean("databasePopulator", DatabasePopulator.class);
        m_databasePopulator.populateDatabase();
        m_applicationDao = getBean("applicationDao", ApplicationDao.class);
        m_locationMonitorDao = getBean("locationMonitorDao", LocationMonitorDao.class);
        m_monServiceDao = getBean("monitoredServiceDao", MonitoredServiceDao.class);
        
        createLocationMonitor();
    }
    
    @Test
    public void testAvailability() throws Exception {
        String url = "/remotepoller/?startTime=1209614400000&endTime=1210046400000";
        
        String xml = sendRequest(GET, url, 200);
        
        assertEquals("total location size: 1", xml);
    }
    
    private void createLocationMonitor() {
        OnmsMonitoringLocationDefinition def = new OnmsMonitoringLocationDefinition("RDU", "package", "USA");
        def.setPriority(100L);
        m_locationMonitorDao.saveMonitoringLocationDefinition(def);
        
        OnmsLocationMonitor entity = new OnmsLocationMonitor();
        entity.setDefinitionName("Definition");
        entity.setLastCheckInTime(new Date());
        entity.setStatus(MonitorStatus.STARTED);
        m_locationMonitorDao.save(entity);
        m_locationMonitorDao.flush();
        
        OnmsApplication application = new OnmsApplication();
        application.setMonitoredServices(new HashSet<OnmsMonitoredService>(m_monServiceDao.findAll()));
        application.setName("IPv6");
        m_applicationDao.save(application);
        m_applicationDao.flush();
        
        OnmsLocationMonitor locMon = m_locationMonitorDao.findAll().get(0);
        OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
        statusChange.setLocationMonitor(locMon);
        statusChange.setPollResult(PollStatus.available());
        statusChange.setMonitoredService(m_monServiceDao.findAll().get(0));
        m_locationMonitorDao.saveStatusChange(statusChange);
        
    }
}
