/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.web.rest.AvailCalculator.UptimeCalculator;
import org.opennms.web.rest.support.TimeChunker;
import org.opennms.web.rest.support.TimeChunker.TimeChunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

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
    
    public static final boolean USE_EXISTING = false;
    
    @Before
    @Override
    public void setUp() throws Throwable {
        beforeServletStart();

        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
        
        
        if(USE_EXISTING) {
            TemporaryDatabase db = new TemporaryDatabase("opennms", true);
            db.setPopulateSchema(false);
            db.create();
            DataSourceFactory.setInstance(db);
        }else {
            MockDatabase db = new MockDatabase(true);
            DataSourceFactory.setInstance(db);
        }
        
                
        setServletContext(new MockServletContext("file:src/main/webapp"));

        getServletContext().addInitParameter("contextConfigLocation", 
                "classpath:/org/opennms/web/rest/applicationContext-test.xml " +
                "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml " +
                "classpath*:/META-INF/opennms/component-service.xml " +
                "classpath*:/META-INF/opennms/component-dao.xml " +
                "classpath:/META-INF/opennms/applicationContext-reportingCore.xml " +
                "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml " +
                "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml " +
                "classpath:/org/opennms/web/rest/applicationContext-mockEventProxy.xml " +
                "classpath:/META-INF/opennms/applicationContext-reporting.xml " +
                "/WEB-INF/applicationContext-spring-security.xml " +
                "/WEB-INF/applicationContext-jersey.xml");
        
        getServletContext().addInitParameter("parentContextKey", "daoContext");
                
        ServletContextEvent e = new ServletContextEvent(getServletContext());
        setContextListener(new ContextLoaderListener());
        getContextListener().contextInitialized(e);

        getServletContext().setContextPath(contextPath);
        setServletConfig(new MockServletConfig(getServletContext(), "dispatcher"));    
        getServletConfig().addInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        getServletConfig().addInitParameter("com.sun.jersey.config.property.packages", "org.opennms.web.rest");
        
        try {

            MockFilterConfig filterConfig = new MockFilterConfig(getServletContext(), "openSessionInViewFilter");
            setFilter(new OpenSessionInViewFilter());        
            getFilter().init(filterConfig);

            setDispatcher(new SpringServlet());
            getDispatcher().init(getServletConfig());

        } catch (ServletException se) {
            throw se.getRootCause();
        }
        
        setWebAppContext(WebApplicationContextUtils.getWebApplicationContext(getServletContext()));
        
        
        afterServletStart();
        
        System.err.println("------------------------------------------------------------------------------");
    }
    
    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging();
        
        m_databasePopulator = getBean("databasePopulator", DatabasePopulator.class);
        
        m_applicationDao = getBean("applicationDao", ApplicationDao.class);
        m_locationMonitorDao = getBean("locationMonitorDao", LocationMonitorDao.class);
        m_monServiceDao = getBean("monitoredServiceDao", MonitoredServiceDao.class);
        
        if(!USE_EXISTING) {
            m_databasePopulator.populateDatabase();
        
            try {
                createLocationMonitors();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @Test
    public void testGetAvailability() {
        
        assertFalse("Don't use existing database", USE_EXISTING);
        
        TransactionTemplate txTemplate = getBean("transactionTemplate", TransactionTemplate.class);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                long startMillis = System.currentTimeMillis() - 12000;
                long totalTime = new Date().getTime() - startMillis;
                TimeChunker timeChunker = new TimeChunker((int)totalTime, new Date(System.currentTimeMillis() - 12000), new Date());
                @SuppressWarnings("unused") // increment the time segment
                final TimeChunk timeChunk = timeChunker.getNextSegment();
                Collection<OnmsLocationSpecificStatus> allStatusChanges = m_locationMonitorDao.getStatusChangesForApplicationBetween(new Date(startMillis), new Date(), "IPv6");
                
                final AvailCalculator calc = new AvailCalculator(timeChunker);
                
                for(OnmsLocationSpecificStatus statusChange : allStatusChanges) {
                    calc.onStatusChange(statusChange);
                }
                
                Collection<OnmsMonitoredService> svcs = m_monServiceDao.findByApplication(m_applicationDao.findByName("IPv6"));
                double avail = calc.getAvailabilityFor(svcs, 0);
                assertEquals(0.8333, avail, 0.03);
            }
        });
        
    }
    
    
    @Test
    public void testGetLocations() throws Exception {
        String url = "/remotelocations";
        String responseString = sendRequest(GET, url, 200);
        
        assertTrue(responseString != null);
    }
    
    @Test
    public void testGetParticipants() throws Exception {
        String url = "/remotelocations/participants";
        String responseString = sendRequest(GET, url, 200);
        
        assertTrue(responseString != null);
    }
    
    @Test
    public void testRemotePollerAvailability() throws Exception {
        long startTime = System.currentTimeMillis();
        String url = BASE_REST_URL;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("resolution", "minute");
        //addStartTime(parameters);
        //addEndTime(parameters);
        
        String responseString = sendRequest(GET, url, parameters, 200);
        
        
        
        if(USE_EXISTING) {
            assertTrue(responseString.contains("HTTP-v6"));
            assertTrue(responseString.contains("HTTP-v4"));
        } else {
            assertTrue(responseString.contains("IPv6"));
            assertTrue(responseString.contains("IPv4"));
        }
        System.err.println("total time taken: " + (System.currentTimeMillis() - startTime) + "UptimeCalculator.count = " + UptimeCalculator.count);
        
        Thread.sleep(360000);
        
        startTime = System.currentTimeMillis();
        responseString = sendRequest(GET, url, parameters, 200);
        
        
        
        if(USE_EXISTING) {
            assertTrue(responseString.contains("HTTP-v6"));
            assertTrue(responseString.contains("HTTP-v4"));
        } else {
            assertTrue(responseString.contains("IPv6"));
            assertTrue(responseString.contains("IPv4"));
        }
        
        System.err.println("total time taken for cache: " + (System.currentTimeMillis() - startTime) + "UptimeCalculator.count = " + UptimeCalculator.count);
    }
    
    @Test
    public void testRemotePollerAvailabilitySingleLocation() throws Exception {
        long startTime = System.currentTimeMillis();
        String url = BASE_REST_URL + "/RDU";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("resolution", "minute");
        addStartTime(parameters);
        addEndTime(parameters);
        
        String responseString = sendRequest(GET, url, parameters, 200);
        
        if(USE_EXISTING) {
            assertTrue(responseString.contains("HTTP-v6"));
            assertTrue(responseString.contains("HTTP-v4"));
        } else {
            assertTrue(responseString.contains("IPv6"));
            assertTrue(responseString.contains("IPv4"));
        }
        System.err.println("total time taken: " + (System.currentTimeMillis() - startTime));
    }
    
    private void addEndTime(Map<String, String> parameters) {
        if(USE_EXISTING) {
            parameters.put("endTime", "" + 1307101853449L);
        } else {
            parameters.put("endTime", "" + System.currentTimeMillis());
        }
    }

    private void addStartTime(Map<String, String> parameters) {
        if(USE_EXISTING) {
            parameters.put("startTime", "" + 1306943136422L);
        }else {
            parameters.put("startTime", "" + (System.currentTimeMillis() - 300001));
        }
    }

    @Test
    public void testRemotePollerAvailabilityFiveMinutes() throws Exception {
        String url = BASE_REST_URL;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("startTime", "" + (new Date().getTime() - (86400000 * 2)));
        parameters.put("endTime", "" + (new Date().getTime() - 86400000));
        parameters.put("resolution", "minute");
        
        String responseString = sendRequest(GET, url, parameters, 200);
        
        assertTrue(responseString.contains("IPv6"));
        assertTrue(responseString.contains("IPv4"));
        
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
                
                OnmsApplication ipv6App = new OnmsApplication();
                ipv6App.setName("IPv6");
                m_applicationDao.saveOrUpdate(ipv6App);
                
                OnmsApplication ipv4App = new OnmsApplication();
                ipv4App.setName("IPv4");
                m_applicationDao.saveOrUpdate(ipv4App);
                
                OnmsMonitoredService service2 = m_monServiceDao.findByType("HTTP").get(1);
                service2.addApplication(ipv4App);
                ipv4App.addMonitoredService(service2);
                m_monServiceDao.saveOrUpdate(service2);
                m_applicationDao.saveOrUpdate(ipv4App);
                
                List<OnmsMonitoredService> services = m_monServiceDao.findByType("HTTP");
                for(OnmsMonitoredService service : services) {
                    
                    service = m_monServiceDao.findByType("HTTP").get(0);
                    service.addApplication(ipv6App);
                    ipv6App.addMonitoredService(service);
                    m_monServiceDao.saveOrUpdate(service);
                    m_applicationDao.saveOrUpdate(ipv6App);
                    
                    OnmsLocationMonitor locMon = m_locationMonitorDao.findAll().get(0);
                    OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
                    statusChange.setLocationMonitor(locMon);
                    statusChange.setPollResult(PollStatus.available());
                    statusChange.setMonitoredService(service);
                    m_locationMonitorDao.saveStatusChange(statusChange);
                }
                
                System.err.println("======= End createLocationMonitors() ======");
                
            }
        });
        
        Thread.sleep(10000L);
        
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<OnmsMonitoredService> services = m_monServiceDao.findByType("HTTP");
                for(OnmsMonitoredService service : services) {
                
                    OnmsLocationMonitor locMon = m_locationMonitorDao.findAll().get(0);
                    OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
                    statusChange.setLocationMonitor(locMon);
                    statusChange.setPollResult(PollStatus.unavailable());
                    statusChange.setMonitoredService(service);
                
                    m_locationMonitorDao.saveStatusChange(statusChange);
                }
            }
        });
        
        Thread.sleep(2000L);
        
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<OnmsMonitoredService> services = m_monServiceDao.findByType("HTTP");
                for(OnmsMonitoredService service : services) {
                
                    OnmsLocationMonitor locMon = m_locationMonitorDao.findAll().get(0);
                    OnmsLocationSpecificStatus statusChange = new OnmsLocationSpecificStatus();
                    statusChange.setLocationMonitor(locMon);
                    statusChange.setPollResult(PollStatus.available());
                    statusChange.setMonitoredService(service);
                
                    m_locationMonitorDao.saveStatusChange(statusChange);
                }
            }
        });
        
    }
}
