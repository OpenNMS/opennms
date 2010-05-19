package org.opennms.features.poller.remote.gwt.server;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.utils.Interval;
import org.opennms.features.poller.remote.gwt.client.utils.IntervalUtils;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.Locations;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

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
        "classpath:/locationDataServiceTest.xml"
})
    @JUnitTemporaryDatabase()
public class LocationDataServiceTest {
    @Autowired
    private LocationDataService m_locationDataService;

    @Autowired
    private LocationMonitorDao m_locationMonitorDao;

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
    private MonitoringLocationsConfiguration m_monitoringLocationsConfiguration;

    @Autowired
    private PollerBackEnd m_pollerBackEnd;

    private OnmsLocationMonitor m_rduMonitor1;
    private OnmsLocationMonitor m_rduMonitor2;
    private OnmsMonitoredService m_localhostHttpService;
    private OnmsMonitoredService m_googleHttpService;
    // private Date m_pollingEnd = getMidnight();
    private Date m_pollingEnd = new Date();
    private Date m_pollingStart = new Date(m_pollingEnd.getTime() - (1000 * 60 * 60 * 24));

    @Before
    public void setUp() throws Exception {
        Locations locations = new Locations();
        LocationDef locationDef = new LocationDef();
        locationDef.setLocationName("RDU");
        locationDef.setMonitoringArea("East Coast");
        locationDef.setCoordinates("35.715751,-79.16262");
        locationDef.setPollingPackageName("example1");
        locationDef.setPriority(1L);
        locations.addLocationDef(locationDef);
        m_monitoringLocationsConfiguration.setLocations(locations);

        OnmsApplication app = new OnmsApplication();
        app.setName("TestApp1");
        m_applicationDao.saveOrUpdate(app);

        OnmsDistPoller dp = new OnmsDistPoller("localhost", "127.0.0.1");
        m_distPollerDao.saveOrUpdate(dp);

        OnmsNode localhostNode = new OnmsNode(dp);
        m_nodeDao.saveOrUpdate(localhostNode);
        OnmsNode googleNode = new OnmsNode(dp);
        m_nodeDao.saveOrUpdate(googleNode);

        OnmsIpInterface localhostIpInterface = new OnmsIpInterface("127.0.0.1", localhostNode);
        m_ipInterfaceDao.saveOrUpdate(localhostIpInterface);
        OnmsIpInterface googleIpInterface = new OnmsIpInterface("66.249.80.104", googleNode);
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

        m_pollingEnd = new Date();
        m_pollingStart = new Date(m_pollingEnd.getTime() - (1000 * 60 * 60 * 24));
}

    private OnmsMonitoredService createService(OnmsApplication app, OnmsIpInterface localhostIpInterface, OnmsServiceType httpServiceType) {
        OnmsMonitoredService service = new OnmsMonitoredService();
        service.setApplications(Collections.singleton(app));
        service.setIpInterface(localhostIpInterface);
        service.setLastFail(m_pollingEnd);
        service.setLastGood(m_pollingEnd);
        service.setNotify("N");
        service.setServiceType(httpServiceType);
        service.setStatus("A");
        service.setSource("P");
        m_monitoredServiceDao.saveOrUpdate(service);
        return service;
    }

    @Test
    @Transactional
    public void testLocationInfo() throws Exception {
        LocationInfo li = m_locationDataService.getLocationInfo("RDU");
        assertEquals("RDU", li.getName());
        assertEquals(Status.UNKNOWN, li.getStatusDetails().getStatus());
    }

    @Test
    @Transactional
    public void testLocationDetails() throws Exception {
        LocationDetails ld = m_locationDataService.getLocationDetails("RDU");
        assertEquals(Status.UNKNOWN, ld.getApplicationState().getStatusDetails().getStatus());
        assertEquals(Status.UNKNOWN, ld.getLocationMonitorState().getStatusDetails().getStatus());
    }

    @Test
    @Transactional
    public void testApplicationInfo() throws Exception {
        ApplicationInfo ai = m_locationDataService.getApplicationInfo("TestApp1");
        assertEquals("TestApp1", ai.getName());
        assertEquals(Status.UNKNOWN, ai.getStatusDetails().getStatus());
    }

    @Test
    @Transactional
    public void testApplicationDetailsFullyAvailableOneMonitor() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getAvailable(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getAvailable(m_pollingStart));
        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(100), ad.getAvailability());
        assertEquals(StatusDetails.up(), ad.getStatusDetails());
    }

    @Test
    @Transactional
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
    @Transactional
    public void testApplicationDetailsDownOneMonitor() throws Exception {
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_localhostHttpService.getId(), getDown(m_pollingStart));
        m_pollerBackEnd.reportResult(m_rduMonitor1.getId(), m_googleHttpService.getId(), getDown(m_pollingStart));
        ApplicationDetails ad = m_locationDataService.getApplicationDetails("TestApp1");
        assertEquals("TestApp1", ad.getApplicationName());
        assertEquals(Double.valueOf(0), ad.getAvailability());
        assertEquals("down for 24 hours", StatusDetails.down("foo"), ad.getStatusDetails());
    }

    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
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
}
