package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import org.opennms.netmgt.model.PollStatus;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


@Transactional
public class RemotePollerAvailabilityRestServiceTest extends AbstractSpringJerseyRestTestCase {

    @Autowired
    ApplicationDao m_applicationDao;
    
    @Autowired
    LocationMonitorDao m_locationMonitorDao;
    
    @Autowired
    MonitoredServiceDao m_monServiceDao;
    
    @Autowired
    DatabasePopulator m_databasePopulator;
    
    public static final String BASE_REST_URL = "/remotelocations/availability";
    
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
                                          "<location-def location-name='CLT' monitoring-area='charlotte'\n" +
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
        
        
        try {
            createLocationMonitors();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    @Ignore
    public void testGetLocations() throws Exception {
        String url = "/remotelocations";
        String responseString = sendRequest(GET, url, 200);
        
        assertTrue(responseString != null);
    }
    
    @Test
    @Ignore
    public void testGetParticipants() throws Exception {
        String url = "/remotelocations/participants";
        String responseString = sendRequest(GET, url, 200);
        
        assertTrue(responseString != null);
    }
    
    @Test
    @Ignore
    public void testRemotePollerAvailability() throws Exception {
        String url = BASE_REST_URL;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("startTime", "" + (new Date().getTime() - 900000));
        parameters.put("endTime", "" + new Date().getTime());
        parameters.put("resolution", "minute");
        
        String responseString = sendRequest(GET, url, parameters, 200);
        
        assertTrue(responseString.contains("IPv6"));
        assertTrue(responseString.contains("IPv4"));
        
        //Get Specific Location
        String rduURL = BASE_REST_URL + "/RDU";
        String rduResponse = sendRequest(GET, rduURL, parameters, 200);
        assertTrue(rduResponse.contains("IPv6") && rduResponse.contains("IPv4"));
        //assertTrue(rduResponse.contains("\"availability\":\"3.343\""));
        
        //Get Specific Location and Host
        parameters.put("host", "node1");
        rduURL = BASE_REST_URL + "/RDU";
        rduResponse = sendRequest(GET, rduURL, parameters, 200);
        assertTrue(rduResponse.contains("IPv6") && rduResponse.contains("IPv4"));
        //assertTrue(rduResponse.contains("\"availability\":\"3.342\""));
        
        //Get All Locations and Specific Host
        
        responseString = sendRequest(GET, url, parameters, 200);
        assertTrue(responseString.contains("IPv6") && rduResponse.contains("IPv4"));
        //assertTrue(responseString.contains("\"availability\":\"3.342\""));
    }
    
    @Test
    @Ignore
    public void testLocationSpecificAvailability() throws Exception {
        String url = BASE_REST_URL + "/CLT";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("startTime", "" + (new Date().getTime() - 300000));
        parameters.put("endTime", "" + new Date().getTime());
        parameters.put("resolution", "minute");
        
        String responseString = sendRequest(GET, url, parameters, 200);
        
        System.err.println("server response: " + responseString);
        assertTrue(responseString.contains("IPv6"));
        assertTrue(responseString.contains("\"availability\":\"0.000\""));
        
        String rduURL = BASE_REST_URL + "/RDU";
        String rduResponse = sendRequest(GET, rduURL, parameters, 200);
        assertTrue(rduResponse.contains("IPv6") && rduResponse.contains("IPv4"));
        assertTrue(rduResponse.contains("\"availability\":\"3.344\""));
    }
    
    @Test
    @Ignore
    public void testLocationAndHostSpecificAvailability() throws Exception {
        String url = BASE_REST_URL + "/CLT";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("startTime", "" + (new Date().getTime() - 300000));
        parameters.put("endTime", "" + new Date().getTime());
        parameters.put("resolution", "minute");
        parameters.put("host", "node1");
        
        String responseString = sendRequest(GET, url, parameters, 200);
        
        assertTrue(responseString.contains("IPv6"));
        assertTrue(responseString.contains("\"availability\":\"0.000\""));
        
        String rduURL = BASE_REST_URL + "/RDU";
        String rduResponse = sendRequest(GET, rduURL, parameters, 200);
        assertTrue(rduResponse.contains("IPv6") && rduResponse.contains("IPv4"));
        assertTrue(rduResponse.contains("\"availability\":\"3.342\""));
    }
    
    
    private void createLocationMonitors() throws InterruptedException {
        TransactionTemplate txTemplate = getBean("transactionTemplate", TransactionTemplate.class);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                
                System.err.println("======= Starting createLocationMonitors() ======");
                OnmsLocationMonitor locMon1 = new OnmsLocationMonitor();
                locMon1.setDefinitionName("RDU");
                locMon1.setLastCheckInTime(new Date());
                locMon1.setStatus(MonitorStatus.STARTED);
                m_locationMonitorDao.save(locMon1);
                
                OnmsLocationMonitor locMon2 = new OnmsLocationMonitor();
                locMon2.setDefinitionName("CLT");
                locMon2.setLastCheckInTime(new Date());
                locMon2.setStatus(MonitorStatus.STARTED);
                m_locationMonitorDao.save(locMon2);
                
                OnmsApplication ipv6App = new OnmsApplication();
                ipv6App.setName("IPv6");
                m_applicationDao.saveOrUpdate(ipv6App);
                
                OnmsApplication ipv4App = new OnmsApplication();
                ipv4App.setName("IPv4");
                m_applicationDao.saveOrUpdate(ipv4App);
                
                OnmsMonitoredService service = m_monServiceDao.findByType("HTTP").get(0);
                service.addApplication(ipv6App);
                ipv6App.addMonitoredService(service);
                m_monServiceDao.saveOrUpdate(service);
                m_applicationDao.saveOrUpdate(ipv6App);
                
                OnmsMonitoredService service2 = m_monServiceDao.findByType("HTTP").get(1);
                service2.addApplication(ipv4App);
                ipv4App.addMonitoredService(service2);
                m_monServiceDao.saveOrUpdate(service2);
                m_applicationDao.saveOrUpdate(ipv4App);
                
                
                OnmsLocationMonitor locMon = m_locationMonitorDao.findAll().get(0);
                OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
                statusChange.setLocationMonitor(locMon);
                statusChange.setPollResult(PollStatus.available());
                statusChange.setMonitoredService(service);
                
                m_locationMonitorDao.saveStatusChange(statusChange);
                
                System.err.println("======= End createLocationMonitors() ======");
                
            }
        });
        
        Thread.sleep(10000L);
        
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                OnmsMonitoredService service = m_monServiceDao.findByType("HTTP").get(0);
                
                OnmsLocationMonitor locMon = m_locationMonitorDao.findAll().get(0);
                OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
                statusChange.setLocationMonitor(locMon);
                statusChange.setPollResult(PollStatus.unavailable());
                statusChange.setMonitoredService(service);
                
                m_locationMonitorDao.saveStatusChange(statusChange);
            }
        });
        
        Thread.sleep(2000L);
        
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                OnmsMonitoredService service = m_monServiceDao.findByType("HTTP").get(0);
                
                OnmsLocationMonitor locMon = m_locationMonitorDao.findAll().get(0);
                OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
                statusChange.setLocationMonitor(locMon);
                statusChange.setPollResult(PollStatus.available());
                statusChange.setMonitoredService(service);
                
                m_locationMonitorDao.saveStatusChange(statusChange);
            }
        });
        
    }
}
