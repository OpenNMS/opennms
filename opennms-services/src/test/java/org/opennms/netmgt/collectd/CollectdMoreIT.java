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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.NetworkBuilder.NodeBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

/**
 * CollectdMoreIT
 *
 * @author brozow
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment
public class CollectdMoreIT {
    
    protected static final String TEST_KEY_PARM_NAME = "key";

    private static Map<String, CollectdMoreIT> m_tests = new HashMap<String, CollectdMoreIT>();

    private EventIpcManager m_eventIpcManager;
    private Collectd m_collectd;
    private CollectdConfigFactory m_collectdConfigFactory;
    private String m_key;
    private MockServiceCollector m_serviceCollector;

    private IpInterfaceDao m_ifaceDao;

    private NodeDao m_nodeDao;

    private FilterDao m_filterDao;

    @Rule
    public TestName m_testName = new TestName();
    
    @Autowired
    private OverrideablePollOutagesDao m_pollOutagesDao;

    @Before
    public void setUp() throws Exception {

        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        
        m_filterDao = mock(FilterDao.class);
        FilterDaoFactory.setInstance(m_filterDao);
        

        // This call will also ensure that the poll-outages.xml file can parse IPv4
        // and IPv6 addresses.
        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        m_pollOutagesDao.overrideConfig(resource.getInputStream());

        File homeDir = resource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        // set up test using a string key
        m_key = m_testName.getMethodName()+System.nanoTime();
        m_tests.put(m_key, this);

        //create a collector definition
        Collector collector = new Collector();
        collector.setService("SNMP");
        collector.setClassName(MockServiceCollector.class.getName());
        
        // pass the key to the collector definition so it can look up the associated test
        Parameter param = new Parameter();
        param.setKey(TEST_KEY_PARM_NAME);
        param.setValue(m_key);
        collector.addParameter(param);
        
        m_collectdConfigFactory = mock(CollectdConfigFactory.class);
        when(m_collectdConfigFactory.getCollectors()).thenReturn(Collections.singletonList(collector));
        when(m_collectdConfigFactory.getThreads()).thenReturn(1);
        
        m_ifaceDao = mock(IpInterfaceDao.class);
        m_nodeDao = mock(NodeDao.class);
        
        m_collectd = new Collectd() {

            @Override
            protected void handleInsufficientInfo(InsufficientInformationException e) {
                fail("Invalid event received: "+e.getMessage());
            }
            
        };
        
        m_collectd.setPollOutagesDao(m_pollOutagesDao);
        m_collectd.setEntityScopeProvider(new MockEntityScopeProvider());

        ThresholdingService mockThresholdingService = mock(ThresholdingService.class);
        ThresholdingSession mockThresholdingSession = mock(ThresholdingSession.class);
        when(mockThresholdingService.createSession(anyInt(), anyString(), anyString(), any(ServiceParameters.class))).thenReturn(mockThresholdingSession);
        m_collectd.setThresholdingService(mockThresholdingService);

        mockThresholdingSession.accept(any(CollectionSet.class));

        OnmsServiceType snmp = new OnmsServiceType("SNMP");
        NetworkBuilder netBuilder = new NetworkBuilder();
        NodeBuilder nodeBuilder = netBuilder.addNode("node1").setId(1);
        InterfaceBuilder ifaceBlder = 
            netBuilder.addInterface("192.168.1.1")
            .setId(2)
            .setIsSnmpPrimary("P");
        ifaceBlder.addSnmpInterface(1);
        OnmsMonitoredService svc = netBuilder.addService(snmp);
        
        List<OnmsIpInterface> initialIfs = Collections.emptyList();
        when(m_ifaceDao.findByServiceType(snmp.getName())).thenReturn(initialIfs);
        
        m_filterDao.flushActiveIpAddressListCache();
        verify(m_filterDao, atLeastOnce()).flushActiveIpAddressListCache();

        when(m_nodeDao.load(1)).thenReturn(nodeBuilder.getNode());
        
        createGetPackagesExpectation(svc);
        
        when(m_ifaceDao.load(2)).thenReturn(ifaceBlder.getInterface());

        final MockTransactionTemplate transTemplate = new MockTransactionTemplate();
        transTemplate.afterPropertiesSet();

        m_collectd.setCollectdConfigFactory(m_collectdConfigFactory);
        m_collectd.setEventIpcManager(m_eventIpcManager);
        m_collectd.setTransactionTemplate(transTemplate);
        m_collectd.setIpInterfaceDao(m_ifaceDao);
        m_collectd.setNodeDao(m_nodeDao);
        m_collectd.setFilterDao(m_filterDao);
        m_collectd.setPersisterFactory(new MockPersisterFactory());
        m_collectd.setServiceCollectorRegistry(new DefaultServiceCollectorRegistry());
        m_collectd.setLocationAwareCollectorClient(CollectorTestUtils.createLocationAwareCollectorClient());

        // Inits the class
        m_collectd.afterPropertiesSet();
        //assertNotNull(m_serviceCollector);
    }
    
    public static void setServiceCollectorInTest(String testKey, MockServiceCollector collector) {
        CollectdMoreIT test = m_tests.get(testKey);
        assertNotNull(test);
        test.setServiceCollector(collector);
    }

    private void setServiceCollector(MockServiceCollector collector) {
        m_serviceCollector = collector;
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(m_filterDao);
        verifyNoMoreInteractions(m_collectdConfigFactory);
        verifyNoMoreInteractions(m_ifaceDao);
        verifyNoMoreInteractions(m_nodeDao);

        m_tests.remove(m_key);
    }

    @Test
    public void testIt() throws InterruptedException {

        m_collectd.start();
        
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Test");
        bldr.setNodeid(1);
        bldr.setInterface(addr("192.168.1.1"));
        bldr.setService("SNMP");

        m_collectd.onEvent(ImmutableMapper.fromMutableEvent(bldr.getEvent()));
        
        Thread.sleep(2000);
        
        assertNotNull(m_serviceCollector);
        assertEquals(1, m_serviceCollector.getCollectCount());

        verify(m_filterDao, atLeastOnce()).flushActiveIpAddressListCache();
        verify(m_collectdConfigFactory, atLeastOnce()).getCollectors();
        verify(m_collectdConfigFactory, atLeastOnce()).getPackages();
        verify(m_collectdConfigFactory, atLeastOnce()).interfaceInPackage(any(OnmsIpInterface.class), any(Package.class));
        verify(m_collectdConfigFactory, atLeastOnce()).getCollectors();
        verify(m_collectdConfigFactory, atLeastOnce()).getPackages();
        verify(m_collectdConfigFactory, atLeastOnce()).getThreads();
        verify(m_ifaceDao, atLeastOnce()).findByServiceType("SNMP");
        verify(m_ifaceDao, atLeastOnce()).load(anyInt());
        verify(m_nodeDao, atLeastOnce()).load(anyInt());
    }

    private void createGetPackagesExpectation(OnmsMonitoredService svc) {
        String rule = "ipaddr = '"+ str(svc.getIpAddress())+"'";
        
        final Package pkg = new Package();
        pkg.setName("testPackage");
        Filter filter = new Filter();
        filter.setContent(rule);
        pkg.setFilter(filter);
        
        final Service collector = new Service();
        collector.setName("SNMP");
        collector.setStatus("on");
        collector.setInterval(3000l);
        
        Parameter parm = new Parameter();
        parm.setKey(TEST_KEY_PARM_NAME);
        parm.setValue(m_key);
        
        collector.setParameters(Collections.singletonList(parm));
        
        pkg.addService(collector);
        
        when(m_collectdConfigFactory.getPackages()).thenReturn(Collections.singletonList(pkg));
        when(m_collectdConfigFactory.interfaceInPackage(any(OnmsIpInterface.class), eq(pkg))).thenReturn(true);
    }

}
