/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.JUnitServer;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

//import com.simontuffs.onejar.Boot;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		//"classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
		//"classpath:/META-INF/opennms/applicationContext-mockEventProxy.xml",
		//"classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
		//"classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd-http.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml"
})
// Override the monitor check intervals to check very frequently for disconnects
@JUnitConfigurationEnvironment(systemProperties={
		"opennms.pollerBackend.monitorCheckInterval=500",
		"opennms.pollerBackend.disconnectedTimeout=3000"
})
@JUnitTemporaryDatabase
public class PollerBackEnd18IntegrationTest implements InitializingBean {

	private static final int REMOTING_WEBAPP_PORT = 9162;

	/*
	@Resource(name="daemon")
	PollerBackEnd m_backEnd;

	@Autowired
	SessionFactory m_sessionFactory;

	@Autowired
	JdbcTemplate m_jdbcTemplate;

	@Autowired
	DistPollerDao m_distPollerDao;

	@Autowired
	NodeDao m_nodeDao;

	@Autowired
	ServiceTypeDao m_serviceTypeDao;

	@Autowired
	LocationMonitorDao m_locationMonitorDao;
	 */

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

	private JUnitServer server = null;

	@Before
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();

		// Register the MockEventIpcManager as an implementation of the event classes that
		// the remotePollerBackEnd context requires.
		m_registry.register(m_eventIpcManager, new Class<?>[] { EventProxy.class, EventSubscriptionService.class });

		// Create a new JUnitHttpServer running on port {@link #REMOTING_WEBAPP_PORT}
		server = new JUnitServer(new JUnitHttpServer() {

			@Override
			public Class<? extends Annotation> annotationType() { return JUnitHttpServer.class; }

			@Override
			public Webapp[] webapps() {
				return new Webapp[] {
						new Webapp() {

							@Override
							public Class<? extends Annotation> annotationType() { return Webapp.class; }

							@Override
							public String context() { return "/"; }

							@Override
							public String path() {
								String path = System.getProperty("httpRemotingWebAppPath");
								//String path = System.getProperty("org.opennms.assemblies:org.opennms.assemblies.http-remoting:war");
								Assert.notNull(path);
								return path;
							}
						}
				};
			}

			@Override
			public String[] vhosts() { return new String[0]; }

			@Override
			public String resource() { return ""; }

			@Override
			public int port() { return REMOTING_WEBAPP_PORT; }

			@Override
			public String keystorePassword() { return null; }

			@Override
			public String keystore() { return null; }

			@Override
			public String keyPassword() { return null; }

			@Override
			public boolean https() { return false; }

			@Override
			public String basicAuthFile() { return null; }

			@Override
			public boolean basicAuth() { return false; }
		});

		// Start the HTTP server
		server.start();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		BeanUtils.assertAutowiring(this);
	}

	@After
	public void tearDown() throws Exception {
		// Stop the HTTP server
		server.stop();
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
	//@JUnitHttpServer(webapps=@Webapp(context="/opennms", path=System.getProperty("httpRemotingWebAppPath")))
	public void runRemotePoller() throws Exception {
		//Boot.main(new String[] {

		// Use a separate process to execute the remote poller so that the OneJar classloader
		// doesn't unintentionally load any code from the JUnit classloader
		Process remotePoller = Runtime.getRuntime().exec(new String[] {
				"java",
				// Set the user home directory to the target directory
				"-Duser.home=target",
				"-jar",
				"src/test/resources/org.opennms.assemblies.remote-poller-onejar-1.8.17.one-jar.jar",
				"-u",
				"http://127.0.0.1:9162/",
				"-l",
				"my_remote_poller",
				"-n",
				"admin",
				"-p",
				"admin",
				"-d"
		});
		IOUtils.copy(remotePoller.getInputStream(), System.out);
		IOUtils.copy(remotePoller.getErrorStream(), System.err);

		Thread.sleep(20000);

		try {
			remotePoller.exitValue();
			fail("Remote poller process has terminated unexpectedly");
		} catch (IllegalThreadStateException e) {
			// This exception should be thrown if the process is still executing
		}
		remotePoller.destroy();
	}
}
