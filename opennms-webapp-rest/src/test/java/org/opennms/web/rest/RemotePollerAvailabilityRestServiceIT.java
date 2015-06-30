/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.AvailCalculator.UptimeCalculator;
import org.opennms.web.rest.support.TimeChunker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml",

        "classpath:/org/opennms/web/rest/applicationContext-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class RemotePollerAvailabilityRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    ApplicationDao m_applicationDao;

    @Autowired
    LocationMonitorDao m_locationMonitorDao;

    @Autowired
    MonitoredServiceDao m_monServiceDao;

    @Autowired
    DatabasePopulator m_databasePopulator;

    @Autowired
    TransactionTemplate m_transactionTemplate;

    public static final String BASE_REST_URL = "/remotelocations/availability";

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging();

        m_databasePopulator.populateDatabase();

        try {
            createLocationMonitors();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testGetAvailability() {
        final long endMillis = System.currentTimeMillis();
        final long startMillis = endMillis - 12000;
        final long totalTime = endMillis - startMillis;

        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final TimeChunker timeChunker = new TimeChunker(totalTime, new Date(startMillis), new Date(endMillis));
                // increment the time segment
                timeChunker.getNextSegment();
                final Collection<OnmsLocationSpecificStatus> allStatusChanges = m_locationMonitorDao.getStatusChangesForApplicationBetween(new Date(startMillis), new Date(endMillis), "IPv6");
                final AvailCalculator calc = new AvailCalculator(timeChunker);

                for(final OnmsLocationSpecificStatus statusChange : allStatusChanges) {
                    calc.onStatusChange(statusChange);
                }

                final Collection<OnmsMonitoredService> svcs = m_monServiceDao.findByApplication(m_applicationDao.findByName("IPv6"));
                final double avail = calc.getAvailabilityFor(svcs, 0);
                assertEquals(0.8333, avail, 0.0333);
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

        String responseString = sendRequest(GET, url, parameters, 200);

        assertTrue(responseString.contains("IPv6"));
        assertTrue(responseString.contains("IPv4"));

        System.err.println("total time taken: " + (System.currentTimeMillis() - startTime) + "UptimeCalculator.count = " + UptimeCalculator.count);

        Thread.sleep(2000);

        startTime = System.currentTimeMillis();
        responseString = sendRequest(GET, url, parameters, 200);

        assertTrue(responseString.contains("IPv6"));
        assertTrue(responseString.contains("IPv4"));

        System.err.println("total time taken for cache: " + (System.currentTimeMillis() - startTime) + "UptimeCalculator.count = " + UptimeCalculator.count);
    }

    @Test
    public void testRemotePollerAvailabilitySingleLocation() throws Exception {
        final long startTime = System.currentTimeMillis();
        final String url = BASE_REST_URL + "/RDU";
        final Map<String, String> parameters = new HashMap<String, String>();
        addStartTime(parameters);
        addEndTime(parameters);

        for (String resolution : new String[] {"minute", "hourly", "daily", "Minute", "hOURly", "daiLY"}){
            parameters.put("resolution", resolution);
            final String responseString = sendRequest(GET, url, parameters, 200);
            assertTrue(responseString.contains("IPv6"));
            assertTrue(responseString.contains("IPv4"));
        }

        System.err.println("total time taken: " + (System.currentTimeMillis() - startTime));
    }

    private void addEndTime(final Map<String, String> parameters) {
        parameters.put("endTime", "" + System.currentTimeMillis());
    }

    private void addStartTime(final Map<String, String> parameters) {
        parameters.put("startTime", "" + (System.currentTimeMillis() - 300001));
    }

    @Test
    public void testRemotePollerAvailabilityTimePeriods() throws Exception {
        String url = BASE_REST_URL;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("startTime", "" + (new Date().getTime() - (86400000 * 2)));
        parameters.put("endTime", "" + (new Date().getTime() - 86400000));

        for (String resolution : new String[] {"minute", "hourly", "daily", "Minute", "hOURly", "daiLY"}){
            parameters.put("resolution", resolution);
            String responseString = sendRequest(GET, url, parameters, 200);
            assertTrue(responseString.contains("IPv6"));
            assertTrue(responseString.contains("IPv4"));
        }
    }


    private void createLocationMonitors() throws InterruptedException {
        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {

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

        Thread.sleep(2000L);

        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {

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

        m_transactionTemplate.execute(new TransactionCallbackWithoutResult() {

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
