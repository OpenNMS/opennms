package org.opennms.features.poller.remote.gwt.server;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
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
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;



@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
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

	@Before
	public void setUp() {
		OnmsApplication app = new OnmsApplication();
		app.setName("RDU");
		m_applicationDao.saveOrUpdate(app);

		OnmsDistPoller dp = new OnmsDistPoller("localhost", "127.0.0.1");
		m_distPollerDao.saveOrUpdate(dp);
		
		OnmsNode node = new OnmsNode(dp);
		m_nodeDao.saveOrUpdate(node);
		
		OnmsIpInterface ipi = new OnmsIpInterface("127.0.0.1", node);
		m_ipInterfaceDao.saveOrUpdate(ipi);
		
		OnmsServiceType serviceType = new OnmsServiceType("HTTP");
		m_serviceTypeDao.saveOrUpdate(serviceType);

		OnmsMonitoredService service = new OnmsMonitoredService();
		service.setApplications(Collections.singleton(app));
		service.setIpInterface(ipi);
		service.setLastFail(new Date());
		service.setLastGood(new Date());
		service.setNotify("N");
		service.setServiceType(serviceType);
		service.setStatus("A");
		service.setSource("P");
		m_monitoredServiceDao.saveOrUpdate(service);
		
		Locations locations = new Locations();
		LocationDef locationDef = new LocationDef();
		locationDef.setLocationName("RDU");
		locationDef.setMonitoringArea("East Coast");
		locationDef.setCoordinates("35.715751,-79.16262");
		locationDef.setPollingPackageName("example1");
		locationDef.setPriority(1L);
		locations.addLocationDef(locationDef);
		m_monitoringLocationsConfiguration.setLocations(locations);

		OnmsLocationMonitor monitor = new OnmsLocationMonitor();
		monitor.setDefinitionName("RDU");
		monitor.setLastCheckInTime(new Date());
		monitor.setStatus(MonitorStatus.STARTED);
		m_locationMonitorDao.saveOrUpdate(monitor);

		m_applicationDao.flush();
		m_distPollerDao.flush();
		m_nodeDao.flush();
		m_ipInterfaceDao.flush();
		m_serviceTypeDao.flush();
		m_monitoredServiceDao.flush();
		m_locationMonitorDao.flush();
	}

	@Test
	public void testLocationInfo() throws Exception {
		LocationInfo li = m_locationDataService.getLocationInfo("RDU");
		assertEquals("RDU", li.getName());
		assertEquals(Status.UNKNOWN, li.getStatusDetails().getStatus());
	}

	@Test
	public void testLocationDetails() throws Exception {
		LocationDetails ld = m_locationDataService.getLocationDetails("RDU");
		assertEquals(Status.UNKNOWN, ld.getApplicationState().getStatusDetails().getStatus());
		assertEquals(Status.UNKNOWN, ld.getLocationMonitorState().getStatusDetails().getStatus());
	}
}
