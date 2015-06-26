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

import static org.opennms.core.utils.InetAddressUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.utils.Interval;
import org.opennms.features.poller.remote.gwt.client.utils.IntervalUtils;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.hibernate.LocationMonitorDaoHibernate;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-remote-poller.xml",
        "classpath:/locationDataServiceTest.xml",
        "classpath:META-INF/opennms/applicationContext-minimal-conf.xml"
        
})
@JUnitConfigurationEnvironment(systemProperties={
    "opennms.pollerBackend.monitorCheckInterval=500",
    "opennms.pollerBackend.disconnectedTimeout=3000"
})
@JUnitTemporaryDatabase
@Transactional
public class LocationDataServiceTest implements TemporaryDatabaseAware<TemporaryDatabase>, InitializingBean {
    @Autowired
    private LocationDataService m_locationDataService;

    @Autowired
    private LocationMonitorDaoHibernate m_locationMonitorDao;

    @Autowired
    private ApplicationDao m_applicationDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private PollerBackEnd m_pollerBackEnd;

    private OnmsLocationMonitor m_rduMonitor1;
    private OnmsLocationMonitor m_rduMonitor2;
    private OnmsMonitoredService m_localhostHttpService;
    private OnmsMonitoredService m_googleHttpService;
    // private Date m_pollingEnd = getMidnight();
    private Date m_pollingEnd = new Date();
    private Date m_pollingStart = new Date(m_pollingEnd.getTime() - (1000 * 60 * 60 * 24));

    @Override
    public void afterPropertiesSet() throws Exception {
    	BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.hibernate", "INFO");
        p.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        MockLogAppender.setupLogging(p);
        
        OnmsApplication app = new OnmsApplication();
        app.setName("TestApp1");
        m_applicationDao.saveOrUpdate(app);

        OnmsDistPoller dp = new OnmsDistPoller("localhost", "127.0.0.1");
        m_distPollerDao.saveOrUpdate(dp);

        OnmsNode localhostNode = new OnmsNode(dp);
        localhostNode.setLabel("localhost");
        m_nodeDao.saveOrUpdate(localhostNode);
        OnmsNode googleNode = new OnmsNode(dp);
        googleNode.setLabel("google");
        m_nodeDao.saveOrUpdate(googleNode);

        OnmsIpInterface localhostIpInterface = new OnmsIpInterface(addr("127.0.0.1"), localhostNode);
        m_ipInterfaceDao.saveOrUpdate(localhostIpInterface);
        OnmsIpInterface googleIpInterface = new OnmsIpInterface(addr("66.249.80.104"), googleNode);
        m_ipInterfaceDao.saveOrUpdate(googleIpInterface);

        OnmsServiceType httpServiceType = new OnmsServiceType("HTTP");
        m_serviceTypeDao.saveOrUpdate(httpServiceType);

        m_localhostHttpService = createService(app, localhostIpInterface, httpServiceType);
        m_googleHttpService = createService(app, googleIpInterface, httpServiceType);

        m_rduMonitor1 = new OnmsLocationMonitor();
        m_rduMonitor1.setDefinitionName("RDU");
        m_rduMonitor1.setLastCheckInTime(m_pollingEnd);
        m_rduMonitor1.setStatus(MonitorStatus.STARTED);
        m_locationMonitorDao.saveOrUpdate(m_rduMonitor1);

        m_rduMonitor2 = new OnmsLocationMonitor();
        m_rduMonitor2.setDefinitionName("RDU");
        m_rduMonitor2.setLastCheckInTime(m_pollingEnd);
        m_rduMonitor2.setStatus(MonitorStatus.STARTED);
        m_locationMonitorDao.saveOrUpdate(m_rduMonitor2);
        
        m_applicationDao.flush();
        m_distPollerDao.flush();
        m_nodeDao.flush();
        m_ipInterfaceDao.flush();
        m_serviceTypeDao.flush();
        m_monitoredServiceDao.flush();
        m_locationMonitorDao.flush();

        OnmsApplication onmsApp = m_applicationDao.findByName("TestApp1");
        assertTrue(onmsApp.equals(app));

        assertEquals("Count of applications associated with services is wrong", 1, m_localhostHttpService.getApplications().size());
        assertEquals("Count of applications associated with services is wrong", 1, m_googleHttpService.getApplications().size());
        assertEquals("Count of services associated with application is wrong", 2, app.getMonitoredServices().size());
        m_pollingEnd = new Date();
        m_pollingStart = new Date(m_pollingEnd.getTime() - (1000 * 60 * 60 * 24));
    }

    private long days(int numDays) {
        return 86400000 * numDays;
    }
    
    private long hours(int numHours) {
        return 3600000 * numHours;
    }
    
    @SuppressWarnings("unused")
    private long minutes(int numMinutes) {
        return 60000 * numMinutes;
    }

    private long now() {
        return System.currentTimeMillis();
    }
    
    private OnmsMonitoredService createService(OnmsApplication app, OnmsIpInterface localhostIpInterface, OnmsServiceType httpServiceType) {
        OnmsMonitoredService service = new OnmsMonitoredService();
        service.addApplication(app);
        app.addMonitoredService(service);
        service.setIpInterface(localhostIpInterface);
        service.setLastFail(m_pollingEnd);
        service.setLastGood(m_pollingEnd);
        service.setNotify("N");
        service.setServiceType(httpServiceType);
        service.setStatus("A");
        service.setSource("P");
        m_monitoredServiceDao.saveOrUpdate(service);
        m_applicationDao.saveOrUpdate(app);
        return service;
    }

    @Test
    public void testLocationInfo() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - days(20) - hours(3))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getDown(new Date(now() - days(20) - hours(2))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - days(20) - hours(1))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(now() - days(20) - hours(4))));
        
        LocationInfo li = m_locationDataService.getLocationInfo("RDU");
        assertEquals("RDU", li.getName());
        // Down because one of the services is down.
        assertEquals(Status.DOWN, li.getStatusDetails().getStatus());
    }

    @Test
    public void testLocationDetails() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - days(20) - hours(3))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getDown(new Date(now() - days(20) - hours(2))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - days(20) - hours(1))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(now() - days(20) - hours(4))));
        
        LocationDetails ld = m_locationDataService.getLocationDetails("RDU");
        assertEquals(Status.UNKNOWN, ld.getApplicationState().getStatusDetails().getStatus());
        assertEquals(Status.DOWN, ld.getLocationMonitorState().getStatusDetails().getStatus());
    }
    
    @Test
    public void testLocationMonitorState() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - days(20) - hours(3))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getDown(new Date(now() - days(20) - hours(2))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - days(20) - hours(1))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(now() - days(20) - hours(4))));
        
        LocationDetails ld = m_locationDataService.getLocationDetails("RDU");
        LocationMonitorState lms = ld.getLocationMonitorState();
        assertEquals(Status.DOWN, lms.getStatusDetails().getStatus());
        assertEquals(2, lms.getServices().size());
        assertEquals(1, lms.getServicesDown().size());
        assertEquals(1, lms.getMonitorsWithServicesDown().size());
        assertEquals(2, lms.getMonitorsStarted());
        assertEquals(0, lms.getMonitorsStopped());
        assertEquals(0, lms.getMonitorsDisconnected());
        
        
    }

    @Test
    public void testApplicationInfo() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - hours(3))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getDown(new Date(now() - hours(2))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(new Date(now() - hours(1))));
        
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(now() - hours(4))));
        
        ApplicationInfo ai = m_locationDataService.getApplicationInfo("TestApp1");
        assertEquals("TestApp1", ai.getName());
        assertEquals(Status.DOWN, ai.getStatusDetails().getStatus());
    }

    @Test
    public void testApplicationDetailsFullyAvailableOneMonitor() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));
        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(100), ad.getAvailability());
        assertEquals(StatusDetails.up(), ad.getStatusDetails());
    }

    @Test
    public void testApplicationDetailsHalfAvailableOneMonitor() throws Exception {
        // first, everything's up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));

        // bring it down for 12 hours
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 6))));
        
        // bring it back up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 18))));

        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(50), ad.getAvailability());
        assertEquals("currently available", StatusDetails.up(), ad.getStatusDetails());
    }

    @Test
    public void testApplicationDetailsDownOneMonitor() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getDown(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(m_pollingStart));
        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(0), ad.getAvailability());
        assertEquals("down for 24 hours", StatusDetails.down("foo"), ad.getStatusDetails());
    }

    @Test
    public void testApplicationDetailsTwoMonitorsMarginal() throws Exception {
        // first, everything's up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));

        // bring one down for 12 hours
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 6))));
        // and back up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 18))));

        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(100), ad.getAvailability());
        assertEquals("currently available", StatusDetails.up(), ad.getStatusDetails());
    }

    @Test
    public void testApplicationDetailsTwoMonitorsOutageContainedInOther() throws Exception {
        // first, everything's up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));

        // bring one down for 12 hours
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 6))));
        // and back up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 18))));

        // bring the other down for 4 hours, overlapping
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_googleHttpService.getId(), getDown(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 6))));
        // and back up
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_googleHttpService.getId(), getAvailable(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 10))));

        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(20D/24D*100), ad.getAvailability());
        assertEquals("currently available", StatusDetails.up(), ad.getStatusDetails());
    }

    @Test
    public void testApplicationDetailsTwoMonitorsOutagesOverlap() throws Exception {
        // first, everything's up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));

        // bring one down for 12 hours
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 6))));
        // and back up
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 18))));

        // bring the other down for 12 hours, overlapping by 8
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_googleHttpService.getId(), getDown(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 10))));
        // and back up
        m_pollerBackEnd.reportResult(m_rduMonitor2.getId(), m_googleHttpService.getId(), getAvailable(new Date(m_pollingStart.getTime() + (1000 * 60 * 60 * 22))));

        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(16D/24D*100), ad.getAvailability());
        assertEquals("currently available", StatusDetails.up(), ad.getStatusDetails());
        final String detailString = ad.getDetailsAsString();
        System.err.println(detailString);
        assertTrue(detailString.contains(""));
    }
    
    @Test
    public void testIntervalManipulation() {
        Set<Interval> intervals = IntervalUtils.getIntervalSet();
        intervals.add(new Interval(0, 1000));
        intervals.add(new Interval(1200, 2000));
        Set<Interval> inverted = IntervalUtils.invert(new Date(0), new Date(3000), intervals);
        Set<Interval> invertedMatch = IntervalUtils.getIntervalSet();
        invertedMatch.add(new Interval(1000, 1200));
        invertedMatch.add(new Interval(2000, 3000));
        assertEquals(invertedMatch, inverted);

        intervals = IntervalUtils.getIntervalSet();
        intervals.add(new Interval(100, 1000));
        intervals.add(new Interval(1500, 2000));
        inverted = IntervalUtils.invert(new Date(0), new Date(2000), intervals);
        invertedMatch = IntervalUtils.getIntervalSet();
        invertedMatch.add(new Interval(0, 100));
        invertedMatch.add(new Interval(1000, 1500));
        assertEquals(invertedMatch, inverted);

        intervals = IntervalUtils.getIntervalSet();
        intervals.add(new Interval(0, 1500));
        intervals.add(new Interval(1000, 3000));
        intervals.add(new Interval(4000, 5000));
        Set<Interval> normalized = IntervalUtils.normalize(intervals);
        Set<Interval> normalizedMatch = IntervalUtils.getIntervalSet();
        normalizedMatch.add(new Interval(0, 3000));
        normalizedMatch.add(new Interval(4000, 5000));
        assertEquals(normalizedMatch, normalized);

        intervals = IntervalUtils.getIntervalSet();
        intervals.add(new Interval(0, 1500));
        intervals.add(new Interval(1000, 3000));
        intervals.add(new Interval(4000, 5000));
        intervals.add(new Interval(2000, 4500));
        intervals.add(new Interval(3500, 4400));
        intervals.add(new Interval(2800, 5600));
        normalized = IntervalUtils.normalize(intervals);
        normalizedMatch = IntervalUtils.getIntervalSet();
        normalizedMatch.add(new Interval(0, 5600));
        assertEquals(normalizedMatch, normalized);
    }

    private PollStatus getDown(final Date date) {
        final PollStatus ps = PollStatus.down("butt itches");
        ps.setTimestamp(date);
        return ps;
    }

    private PollStatus getAvailable(final Date date) {
        final PollStatus ps = PollStatus.available();
        ps.setTimestamp(date);
        return ps;
    }

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        FilterDaoFactory.setInstance(null);
    }
}
