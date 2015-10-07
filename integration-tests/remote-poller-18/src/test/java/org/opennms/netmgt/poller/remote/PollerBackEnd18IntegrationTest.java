/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.fail;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

//import com.simontuffs.onejar.Boot;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",

		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		// This overrides poller config and location monitor config with files from 
		// the org.opennms:opennms-services:test-jar artifact
		"classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml"
})
// Override the monitor check intervals to check very frequently for disconnects
@JUnitConfigurationEnvironment(systemProperties={
		"opennms.pollerBackend.monitorCheckInterval=500",
		"opennms.pollerBackend.disconnectedTimeout=3000"
})
@JUnitTemporaryDatabase
public class PollerBackEnd18IntegrationTest implements InitializingBean {

	@Autowired
	LocationMonitorDao m_locationMonitorDao;

	@Autowired
	MonitoredServiceDao m_monitoredServiceDao;

	@Autowired
	private MockEventIpcManager m_eventIpcManager;

	@Autowired
	private ServiceRegistry m_registry;

	/**
	 * Start an RMI registry since this is required by the remotePollerBackEnd contexts.
	 */
	@BeforeClass
	public static void beforeTest() throws Exception {
		LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
	}

	@Before
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();

		// Deregister any existing Eventd classes and DAOs
		m_registry.unregisterAll(EventProxy.class);
		m_registry.unregisterAll(EventSubscriptionService.class);
		m_registry.unregisterAll(EventIpcManager.class);
		m_registry.unregisterAll(MonitoredServiceDao.class);
		m_registry.unregisterAll(LocationMonitorDao.class);

		// Register the MockEventIpcManager as an implementation of the event classes that
		// the remotePollerBackEnd context requires.
		m_registry.register(m_eventIpcManager, new Class<?>[] { EventIpcManager.class, EventProxy.class, EventSubscriptionService.class });

		// Register our versions of the DAOs
		m_registry.register(m_monitoredServiceDao, new Class<?>[] { MonitoredServiceDao.class });
		m_registry.register(m_locationMonitorDao, new Class<?>[] { LocationMonitorDao.class });
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		BeanUtils.assertAutowiring(this);
	}

	/*
    @Test
    @Transactional
    public void testRegister() {

        final Collection<OnmsMonitoringLocationDefinition> locations = m_backEnd.getMonitoringLocations();
        assertNotNull("locations list should not be null", locations);
        assertFalse("locations list should not be empty", locations.isEmpty());

        final int initialCount = m_locationMonitorDao.findAll().size();

        for (final OnmsMonitoringLocationDefinition location : locations) {
            final int locationMonitorId = m_backEnd.registerLocationMonitor(location.getName());
            assertTrue(locationMonitorId > 0);
            assertEquals(MonitorStatus.REGISTERED, m_locationMonitorDao.get(locationMonitorId).getStatus());
        }

        assertEquals(initialCount + locations.size(), m_locationMonitorDao.findAll().size());
    }

    @Test
    @Transactional
    public void testPollingStarted() {
        final int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");

        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());

        final OnmsLocationMonitor monitor = m_locationMonitorDao.get(locationMonitorId);
        assertNotNull(monitor);
        final Map<String, String> details = monitor.getDetails();
        assertNotNull(details);
        assertEquals(MonitorStatus.STARTED, monitor.getStatus());
        assertEquals(2, details.keySet().size());
        assertEquals("WonkaOS", details.get("os.name"));
    }

    @Test
    @Transactional
    public void testPollingStopped() {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");

        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());

        assertEquals(MonitorStatus.STARTED, m_locationMonitorDao.get(locationMonitorId).getStatus());

        m_backEnd.pollerStopping(locationMonitorId);

        assertEquals(MonitorStatus.STOPPED, m_locationMonitorDao.get(locationMonitorId).getStatus());
    }

    @Test
    @Transactional
    public void testPollerDisconnected() throws Exception {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");

        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());

        assertEquals(MonitorStatus.STARTED, m_locationMonitorDao.get(locationMonitorId).getStatus());

        Thread.sleep(1500);

        m_backEnd.checkForDisconnectedMonitors();

        assertEquals(MonitorStatus.STARTED, m_locationMonitorDao.get(locationMonitorId).getStatus());

        Thread.sleep(2000);

        m_backEnd.checkForDisconnectedMonitors();

        assertEquals(MonitorStatus.DISCONNECTED, m_locationMonitorDao.get(locationMonitorId).getStatus());

    }

    @Test
    @Transactional
    public void testGetServiceMonitorLocators() {

        Collection<ServiceMonitorLocator> results = m_backEnd.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR);

        assertNotNull(results);

        assertTrue(results.size() > 0);
    }

    @Test
    @Transactional
    public void testReportResults() throws InterruptedException {
        final OnmsNode node = new OnmsNode(m_distPollerDao.findAll().get(0), "foo");
        final OnmsIpInterface iface = new OnmsIpInterface(InetAddressUtils.addr("192.168.1.1"), node);
        OnmsServiceType serviceType = m_serviceTypeDao.findByName("HTTP");
        if (serviceType == null) {
            serviceType = new OnmsServiceType("HTTP");
            m_serviceTypeDao.save(serviceType);
            m_serviceTypeDao.flush();
        }
        final OnmsMonitoredService service = new OnmsMonitoredService(iface, serviceType);
        iface.setMonitoredServices(Collections.singleton(service));
        m_nodeDao.save(node);
        m_nodeDao.flush();

        final int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        final int serviceId = service.getId();

        // make sure there is no rrd data
        final File rrdFile = new File("target/test-data/distributed/"+locationMonitorId+"/"+ InetAddressUtils.str(iface.getIpAddress()) +"/http" + RrdUtils.getExtension());
        if (rrdFile.exists()) {
            rrdFile.delete();
        }

        assertFalse(rrdFile.exists());

        m_backEnd.reportResult(locationMonitorId, serviceId, PollStatus.available(1234.0));
        Thread.sleep(1000);
        m_backEnd.reportResult(locationMonitorId, serviceId, PollStatus.unavailable());

        final Collection<OnmsLocationSpecificStatus> statuses = m_locationMonitorDao.getStatusChangesForLocationBetween(new Date(0L), new Date(), "RDU");
        assertEquals(2, statuses.size());

        final Iterator<OnmsLocationSpecificStatus> statusIterator = statuses.iterator();
        final OnmsLocationSpecificStatus status1 = statusIterator.next();
        final OnmsLocationSpecificStatus status2 = statusIterator.next();

        assertEquals(Double.valueOf(1234D), status1.getPollResult().getResponseTime());
        assertNull(status2.getPollResult().getResponseTime());

        assertTrue("rrd file doesn't exist at " + rrdFile.getAbsolutePath(), rrdFile.exists());
    }

    public Map<String, String> getPollerDetails() {
        final Map<String, String> pollerDetails = new HashMap<String, String>();
        pollerDetails.put("os.name", "WonkaOS");
        pollerDetails.put("os.version", "1.2.3");
        return pollerDetails;
    }
	 */

	@Test
	// The httpRemotingWebAppPath property is set in the POM to the path to the http-remoting WAR file
	@JUnitHttpServer(webapps=@Webapp(context="/", pathSystemProperty="httpRemotingWebAppPath"))
	public void runRemotePoller() throws Exception {
		//Boot.main(new String[] {

		// Use a separate process to execute the remote poller so that the OneJar classloader
		// doesn't unintentionally load any code from the JUnit classloader
		Process remotePoller = Runtime.getRuntime().exec(new String[] {
				"java",
				// Set the user home directory to the target directory so that the remote poller
				// will log to that directory
				"-Duser.home=target",
				// Execute the 1.8.17 version of the remote poller
				"-jar",
				"src/test/resources/org.opennms.assemblies.remote-poller-onejar-1.8.17.one-jar.jar",
				// Connect to the JUnitHttpServer
				"-u",
				"http://127.0.0.1:9162/",
				// Use RDU as the ID of this remote poller
				"-l",
				"RDU",
				// Login with admin:admin credentials
				"-n",
				"admin",
				"-p",
				"admin",
				// Debug logging
				"-d"
		});

		Thread.sleep(10000);

		try {
			remotePoller.exitValue();
			fail("Remote poller process has terminated unexpectedly");
		} catch (IllegalThreadStateException e) {
			// This exception should be thrown if the process is still executing
		}
		remotePoller.destroy();
	}
}
