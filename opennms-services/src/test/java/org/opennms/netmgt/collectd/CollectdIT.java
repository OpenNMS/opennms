/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThresholdingDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.scheduler.mock.MockScheduler;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment
public class CollectdIT {
    private IpInterfaceDao m_ipIfDao;
    private FilterDao m_filterDao;
    private Collectd m_collectd;
    private MockScheduler m_scheduler;
    private CollectdConfigFactory m_collectdConfigFactory;
    
    private EventIpcManager m_eventIpcManager;

    @Autowired
    private OverrideableThresholdingDao m_thresholdingDao;
    
    @Autowired
    private OverrideablePollOutagesDao m_pollOutagesDao;

    @Before
    public void setUp() throws Exception {
        NodeDao m_nodeDao;

        MockLogAppender.setupLogging();

        Resource threshdResource = new ClassPathResource("/etc/thresholds.xml");
        File homeDir = threshdResource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        // Test setup
        m_eventIpcManager = mock(EventIpcManager.class);
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        m_nodeDao = mock(NodeDao.class);
        m_ipIfDao = mock(IpInterfaceDao.class);
        m_scheduler = new MockScheduler();

        m_eventIpcManager.addEventListener(isA(EventListener.class));
        verify(m_eventIpcManager, times(1)).addEventListener(null);
        m_eventIpcManager.addEventListener(isA(EventListener.class), isACollection(String.class));
        verify(m_eventIpcManager, times(1)).addEventListener(null, (Collection<String>)null);
        m_eventIpcManager.addEventListener(isA(EventListener.class), isA(String.class));
        verify(m_eventIpcManager, times(1)).addEventListener(null, (String)null);
        m_eventIpcManager.removeEventListener(isA(EventListener.class));
        verify(m_eventIpcManager, times(1)).removeEventListener(null);

        m_filterDao = mock(FilterDao.class);
        List<InetAddress> allIps = new ArrayList<>();
        allIps.add(addr("192.168.1.1"));
        allIps.add(addr("192.168.1.2"));
        allIps.add(addr("192.168.1.3"));
        allIps.add(addr("192.168.1.4"));
        allIps.add(addr("192.168.1.5"));
        when(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE *.*.*.*")).thenReturn(allIps);
        when(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE 1.1.1.1")).thenReturn(new ArrayList<InetAddress>(0));
        FilterDaoFactory.setInstance(m_filterDao);

        // This call will also ensure that the poll-outages.xml file can parse IPv4
        // and IPv6 addresses.
        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        m_pollOutagesDao.overrideConfig(resource.getInputStream());

        final MockTransactionTemplate transTemplate = new MockTransactionTemplate();
        transTemplate.afterPropertiesSet();

        m_collectd = new Collectd();
        m_collectd.setEventIpcManager(m_eventIpcManager);
        m_collectd.setNodeDao(m_nodeDao);
        m_collectd.setIpInterfaceDao(m_ipIfDao);
        m_collectd.setFilterDao(m_filterDao);
        m_collectd.setScheduler(m_scheduler);
        m_collectd.setTransactionTemplate(transTemplate);
        m_collectd.setPersisterFactory(new MockPersisterFactory());
        m_collectd.setServiceCollectorRegistry(new DefaultServiceCollectorRegistry());
        m_collectd.setLocationAwareCollectorClient(CollectorTestUtils.createLocationAwareCollectorClient());
        m_collectd.setPollOutagesDao(m_pollOutagesDao);
        m_collectd.setEntityScopeProvider(new MockEntityScopeProvider());

        m_thresholdingDao.overrideConfig(ConfigurationTestUtils.getInputStreamForConfigFile("thresholds.xml"));
    }

    private static Package getCollectionPackageThatMatchesSNMP() {
        Package pkg = new Package();
        pkg.setName("pkg");
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service svc = new Service();
        pkg.addService(svc);
        svc.setName("SNMP");
        svc.setStatus("on");
        Parameter parm = new Parameter();
        parm.setKey("collection");
        parm.setValue("default");
        svc.addParameter(parm);
        parm = new Parameter();
        parm.setKey("thresholding-enabled");
        parm.setValue("true");
        svc.addParameter(parm);
        svc.setStatus("on");

        return pkg;
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();

        verifyNoMoreInteractions(m_eventIpcManager);
        verifyNoMoreInteractions(m_filterDao);
    }

    private static OnmsIpInterface getInterface() {
        OnmsNode node = new OnmsNode();
        node.setId(1);
        OnmsIpInterface iface = new OnmsIpInterface(InetAddressUtils.getInetAddress("192.168.1.1"), node);
        iface.setId(1);
        return iface;
    }

    @Test
    public void testCreate() throws Exception {

        setupCollector("SNMP");
        setupTransactionManager();

        // Use a mock scheduler to track calls to the Collectd scheduler
        Scheduler m_scheduler = mock(Scheduler.class);
        m_collectd.setScheduler(m_scheduler);

        // Expect one task to be scheduled
        m_scheduler.schedule(eq(0L), isA(ReadyRunnable.class));

        // Expect the scheduler to be started and stopped during Collectd
        // start() and stop()
        m_scheduler.start();
        m_scheduler.stop();

        // Initialize Collectd
        m_collectd.afterPropertiesSet();

        // Start and stop collectd
        m_collectd.start();
        m_collectd.stop();

        verify(m_eventIpcManager, times(1)).addEventListener(eq(m_collectd), (Collection<String>)isA(Collection.class));
        verify(m_eventIpcManager, times(1)).removeEventListener(m_collectd);
    }

    /**
     * Test override of read community string and max repetitions in Collectd configuration parameters
     */
    @Test
    public void testOverrides() {
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("max-repetitions", "11");
    	map.put("read-community", "notPublic");
		ServiceParameters params = new ServiceParameters(map);
		
		int reps = params.getSnmpMaxRepetitions(6);
		assertEquals("Overriding max repetitions failed.", 11, reps);
		params = new ServiceParameters(map);
		map.remove("max-repetitions");
		map.put("maxRepetitions", "11");
		assertEquals("Overriding max repetitions failed.", 11, reps);
		
		String s = params.getSnmpReadCommunity("public");
		assertEquals("Overriding read community failed.", "notPublic", s);
		map.remove("read-community");
		map.put("readCommunity", "notPublic");
		params = new ServiceParameters(map);
		s = params.getSnmpReadCommunity("public");
		assertEquals("Overriding read community failed.", "notPublic", s);
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testNoMatchingSpecs() throws Exception {

        setupCollector("SNMP");
        when(m_ipIfDao.findByServiceType("SNMP")).thenReturn(new ArrayList<OnmsIpInterface>(0));
        setupTransactionManager();

        m_collectd.afterPropertiesSet();

        m_collectd.start();

        m_scheduler.next();

        assertEquals(0, m_scheduler.getEntryCount());

        m_collectd.stop();

        verify(m_eventIpcManager, times(1)).addEventListener(eq(m_collectd), (Collection<String>)isA(Collection.class));
        verify(m_eventIpcManager, times(1)).removeEventListener(m_collectd);
    }

    @Test
    public void testOneMatchingSpec() throws Exception {
        OnmsIpInterface iface = getInterface();

        setupCollector("SNMP");
        setupInterface(iface);
        setupTransactionManager();
  
        when(m_collectdConfigFactory.getPackages()).thenReturn(Collections.singletonList(getCollectionPackageThatMatchesSNMP()));
        when(m_collectdConfigFactory.interfaceInPackage(iface, getCollectionPackageThatMatchesSNMP())).thenReturn(true);

        // Mock Thresholding
        ThresholdingService mockThresholdingService = mock(ThresholdingService.class);
        ThresholdingSession mockThresholdingSession = mock(ThresholdingSession.class);
        when(mockThresholdingService.createSession(anyInt(), anyString(), anyString(), isA(ServiceParameters.class))
             ).thenReturn(mockThresholdingSession);
        mockThresholdingSession.accept(isA(CollectionSet.class));

        m_collectd.setThresholdingService(mockThresholdingService);

        assertEquals("scheduler entry count", 0, m_scheduler.getEntryCount());

        m_collectd.afterPropertiesSet();

        m_collectd.start();
        
        m_scheduler.next();

        assertEquals("scheduler entry count", 1, m_scheduler.getEntryCount());

        m_scheduler.next();

        m_collectd.stop();

        verify(m_eventIpcManager, times(1)).addEventListener(eq(m_collectd), (Collection<String>)isA(Collection.class));
        verify(m_eventIpcManager, times(1)).removeEventListener(m_collectd);
    }

    /**
     * NMS-9413: Verifies that collectd does not schedule interfaces when the
     * {@link ServiceCollector} throws a {@link CollectionInitializationException}
     * while validating the agent.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testInterfaceIsNotScheduledWhenValidationFails() throws Exception {
        ServiceCollector svcCollector = mock(ServiceCollector.class);
        svcCollector.initialize();
        svcCollector.validateAgent(isA(CollectionAgent.class), isA(Map.class));

        doThrow(new CollectionInitializationException("No!")).when(svcCollector).validateAgent(isA(CollectionAgent.class), isA(Map.class));

        setupCollector("SNMP", svcCollector);

        OnmsIpInterface iface = getInterface();

        setupInterface(iface);
        setupTransactionManager();

        when(m_collectdConfigFactory.getPackages()).thenReturn(Collections.singletonList(getCollectionPackageThatMatchesSNMP()));
        when(m_collectdConfigFactory.interfaceInPackage(iface, getCollectionPackageThatMatchesSNMP())).thenReturn(true);

        assertEquals("scheduler entry count", 0, m_scheduler.getEntryCount());

        m_collectd.afterPropertiesSet();

        m_collectd.start();

        m_scheduler.next();

        assertEquals("scheduler entry count", 0, m_scheduler.getEntryCount());

        m_collectd.stop();

        verify(m_eventIpcManager, times(1)).addEventListener(eq(m_collectd), (Collection<String>)isA(Collection.class));
        verify(m_eventIpcManager, times(1)).removeEventListener(m_collectd);
    }

    @SuppressWarnings("unchecked")
    private static <K> Collection<K> isACollection(Class<K> innerClass) {
        return isA(Collection.class);
    }

    /**
     * Add a dummy transaction manager that has mock calls to commit() and rollback()
     */
    private void setupTransactionManager() {
        PlatformTransactionManager m_transactionManager = mock(PlatformTransactionManager.class);
        TransactionTemplate transactionTemplate = new TransactionTemplate(m_transactionManager);
        m_collectd.setTransactionTemplate(transactionTemplate);
        
        when(m_transactionManager.getTransaction(isA(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
        m_transactionManager.rollback(isA(TransactionStatus.class));
        verify(m_transactionManager, times(1)).rollback(null);
        m_transactionManager.commit(isA(TransactionStatus.class));
        verify(m_transactionManager, times(1)).commit(null);
    }

    private void setupInterface(OnmsIpInterface iface) {
        when(m_ipIfDao.findByServiceType("SNMP")).thenReturn(Collections.singletonList(iface));
        when(m_ipIfDao.load(iface.getId())).thenReturn(iface);
    }

    @SuppressWarnings("unchecked")
    private void setupCollector(String svcName) throws CollectionInitializationException {
        ServiceCollector svcCollector = mock(ServiceCollector.class);
        svcCollector.initialize();
        svcCollector.validateAgent(isA(CollectionAgent.class), isA(Map.class));

        when(svcCollector.getEffectiveLocation(anyString())).thenReturn(LocationUtils.DEFAULT_LOCATION_NAME);
        when(svcCollector.getRuntimeAttributes(isA(CollectionAgent.class),isA(Map.class))).thenReturn(Collections.emptyMap());
        when(svcCollector.collect(isA(CollectionAgent.class),isA(Map.class))).thenAnswer(new Answer<CollectionSet>() {
            @Override
            public CollectionSet answer(final InvocationOnMock invocation) throws Throwable {
                CollectionAgent agent = (CollectionAgent) invocation.getArgument(0);
                return new CollectionSetBuilder(agent).build();
            }
            
        });
        setupCollector(svcName, svcCollector);
    }

    private void setupCollector(String svcName, ServiceCollector svcCollector) throws CollectionInitializationException {
        MockServiceCollector.setDelegate(svcCollector);

        // Tell the config to use the MockServiceCollector for the specified service
        Collector collector = new Collector();
        collector.setService(svcName);
        collector.setClassName(MockServiceCollector.class.getName());

        m_collectdConfigFactory = mock(CollectdConfigFactory.class);
        when(m_collectdConfigFactory.getCollectors()).thenReturn(Collections.singletonList(collector));
        when(m_collectdConfigFactory.getThreads()).thenReturn(1);

        m_collectd.setCollectdConfigFactory(m_collectdConfigFactory);
    }
}
