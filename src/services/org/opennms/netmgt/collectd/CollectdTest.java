package org.opennms.netmgt.collectd;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.mock.MockScheduler;
import org.opennms.netmgt.scheduler.Scheduler;

public class CollectdTest extends MockObjectTestCase {
	
	private Collectd m_collectd;
	
	private Mock m_eventIpcManager;
	private Mock m_collectorConfigDao;
	private Mock m_ipIfDao;
	private Mock m_monSvcDao;
	private Mock m_collector;
	
	private MockScheduler m_scheduler;
	private CollectionSpecification m_spec;


	protected void setUp() throws Exception {
		
		m_eventIpcManager = mock(EventIpcManager.class);
		m_collectorConfigDao = mock(CollectorConfigDao.class);
		m_ipIfDao = mock(IpInterfaceDao.class);
		m_monSvcDao = mock(MonitoredServiceDao.class);
		
		m_collector = mock(ServiceCollector.class);

		m_scheduler = new MockScheduler();
		
		m_eventIpcManager.stubs();
		
		m_collectd = new Collectd();
		m_collectd.setEventIpcManager(getEventIpcManager());
		m_collectd.setCollectorConfigDao(getCollectorConfigDao());
		m_collectd.setIpInterfaceDao(getIpInterfaceDao());
		m_collectd.setMonitoredServiceDao(getMonitoredServiceDao());
		m_collectd.setScheduler(m_scheduler);
		
		Package pkg = new Package();
		pkg.setName("pkg");
		Service svc = new Service();
		pkg.addService(svc);
		svc.setName("SNMP");
		Parameter parm = new Parameter();
		parm.setKey("parm1");
		parm.setValue("value1");
		svc.addParameter(parm);
		
		CollectdPackage wpkg = new CollectdPackage(pkg, "localhost", false);
		
		m_spec = new CollectionSpecification(wpkg, "SNMP", null, getCollector());
	}
	
	private ServiceCollector getCollector() {
		return (ServiceCollector)m_collector.proxy();
	}

	private MonitoredServiceDao getMonitoredServiceDao() {
		return (MonitoredServiceDao)m_monSvcDao.proxy();
	}

	private IpInterfaceDao getIpInterfaceDao() {
		return (IpInterfaceDao)m_ipIfDao.proxy();
	}

	private CollectorConfigDao getCollectorConfigDao() {
		return (CollectorConfigDao)m_collectorConfigDao.proxy();
	}

	private EventIpcManager getEventIpcManager() {
		return (EventIpcManager)m_eventIpcManager.proxy();
	}
	
	private CollectionSpecification getCollectionSpecification() {
		return m_spec;
	}
	
	private OnmsIpInterface getInterface() {
		OnmsNode node = new OnmsNode();
		node.setId(new Integer(1));
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		return iface;
	}

	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	public void testCreate() {
		
		Mock m_scheduler = mock(Scheduler.class);
		m_collectd.setScheduler((Scheduler)m_scheduler.proxy());
		
		m_scheduler.expects(once()).method("schedule").with(eq(0L), ANYTHING);
		m_collectd.init();
		
		m_scheduler.expects(once()).method("start");
		m_collectd.start();

		m_scheduler.expects(once()).method("stop");
		m_collectd.stop();
	}
	
	public void testScheduling() {
		testNoMatchingSpecs();
	}

	public void testNoMatchingSpecs() {
		m_collectd.init();
		m_collectd.start();
		
		String svcName = "SNMP";
		OnmsIpInterface iface = getInterface();
		List specs = Collections.EMPTY_LIST;

		setupCollector(svcName);
		setupInterface(iface);
		setupSpecs(iface, svcName, specs);

		m_scheduler.next();
		
		assertEquals(0, m_scheduler.getEntryCount());
		
		m_collectd.stop();
		
	}

	public void testOneMatchingSpec() {
		m_collectd.init();
		m_collectd.start();
		
		String svcName = "SNMP";
		OnmsIpInterface iface = getInterface();
		List specs = Collections.singletonList(getCollectionSpecification());

		setupCollector(svcName);
		setupInterface(iface);
		setupSpecs(iface, svcName, specs);
		
		m_collector.expects(once()).method("initialize").with(isA(CollectableService.class), isA(Map.class));

		m_scheduler.next();
		
		assertEquals(1, m_scheduler.getEntryCount());
		
		m_scheduler.next();
		
		m_collectd.stop();
		
	}

	private void setupSpecs(OnmsIpInterface iface, String svcName, List specs) {
		m_collectorConfigDao.expects(once()).method("getSpecificationsForInterface").
			with(same(iface), eq(svcName)).
			will(returnValue(specs));
	}

	private void setupInterface(OnmsIpInterface iface) {
		m_ipIfDao.expects(once()).method("findByServiceType").
			with(eq("SNMP")).
			will(returnValue(Collections.singleton(iface)));
	}

	private void setupCollector(String svcName) {
		m_collectorConfigDao.expects(once()).method("getCollectorNames").
			will(returnValue(Collections.singleton(svcName)));
	}

}
