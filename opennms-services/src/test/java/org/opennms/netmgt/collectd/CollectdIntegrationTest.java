/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.support.AbstractCollectionSet;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.NetworkBuilder.NodeBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * CollectdIntegrationTest
 *
 * @author brozow
 */
public class CollectdIntegrationTest extends TestCase {
    
    private static final String TEST_KEY_PARM_NAME = "key";

    private static Map<String, CollectdIntegrationTest> m_tests = new HashMap<String, CollectdIntegrationTest>();

    private EventIpcManager m_eventIpcManager;
    private Collectd m_collectd;
    private EasyMockUtils m_mockUtils;
    private CollectdConfigFactory m_collectdConfigFactory;
    private CollectdConfiguration m_collectdConfiguration;
    private String m_key;
    private MockServiceCollector m_serviceCollector;

    private IpInterfaceDao m_ifaceDao;

    private NodeDao m_nodeDao;

    private FilterDao m_filterDao;

    @Override
    protected void setUp() throws Exception {

        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        
        m_mockUtils = new EasyMockUtils();
        
        m_filterDao = m_mockUtils.createMock(FilterDao.class);
        FilterDaoFactory.setInstance(m_filterDao);
        

        // This call will also ensure that the poll-outages.xml file can parse IPv4
        // and IPv6 addresses.
        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        PollOutagesConfigFactory factory = new PollOutagesConfigFactory(resource);
        factory.afterPropertiesSet();
        PollOutagesConfigFactory.setInstance(factory);

        File homeDir = resource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        resource = new ClassPathResource("/test-thresholds.xml");
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(resource.getInputStream()));

        // set up test using a string key
        m_key = getName()+System.nanoTime();
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
        
        m_collectdConfigFactory = m_mockUtils.createMock(CollectdConfigFactory.class);
        m_collectdConfiguration = m_mockUtils.createMock(CollectdConfiguration.class);
        EasyMock.expect(m_collectdConfigFactory.getCollectdConfig()).andReturn(m_collectdConfiguration).anyTimes();
        EasyMock.expect(m_collectdConfiguration.getCollectors()).andReturn(Collections.singletonList(collector)).anyTimes();
        EasyMock.expect(m_collectdConfiguration.getThreads()).andReturn(1).anyTimes();
        
        m_ifaceDao = m_mockUtils.createMock(IpInterfaceDao.class);
        m_nodeDao = m_mockUtils.createMock(NodeDao.class);
        
        m_collectd = new Collectd() {

            @Override
            protected void handleInsufficientInfo(InsufficientInformationException e) {
                fail("Invalid event received: "+e.getMessage());
            }
            
        };

        OnmsServiceType snmp = new OnmsServiceType("SNMP");
        NetworkBuilder netBuilder = new NetworkBuilder("localhost", "127.0.0.1");
        NodeBuilder nodeBuilder = netBuilder.addNode("node1").setId(1);
        InterfaceBuilder ifaceBlder = 
            netBuilder.addInterface("192.168.1.1")
            .setId(2)
            .setIsSnmpPrimary("P");
        ifaceBlder.addSnmpInterface(1);
        OnmsMonitoredService svc = netBuilder.addService(snmp);
        
        List<OnmsIpInterface> initialIfs = Collections.emptyList();
        EasyMock.expect(m_ifaceDao.findByServiceType(snmp.getName())).andReturn(initialIfs).anyTimes();
        
        m_filterDao.flushActiveIpAddressListCache();
        
        EasyMock.expect(m_nodeDao.load(1)).andReturn(nodeBuilder.getNode()).anyTimes();
        
        createGetPackagesExpectation(svc);
        
        EasyMock.expect(m_ifaceDao.load(2)).andReturn(ifaceBlder.getInterface()).anyTimes();
        
        m_mockUtils.replayAll();

        final MockTransactionTemplate transTemplate = new MockTransactionTemplate();
        transTemplate.afterPropertiesSet();

        m_collectd.setCollectdConfigFactory(m_collectdConfigFactory);
        m_collectd.setEventIpcManager(m_eventIpcManager);
        m_collectd.setTransactionTemplate(transTemplate);
        m_collectd.setIpInterfaceDao(m_ifaceDao);
        m_collectd.setNodeDao(m_nodeDao);
        m_collectd.setFilterDao(m_filterDao);
        
        // Inits the class
        m_collectd.afterPropertiesSet();
        //assertNotNull(m_serviceCollector);
    }
    
    public static void setServiceCollectorInTest(String testKey, MockServiceCollector collector) {
        CollectdIntegrationTest test = m_tests.get(testKey);
        assertNotNull(test);
        test.setServiceCollector(collector);
    }

    private void setServiceCollector(MockServiceCollector collector) {
        m_serviceCollector = collector;
    }

    @Override
    protected void tearDown() throws Exception {
        m_tests.remove(m_key);
    }
    
    public void testIt() throws InterruptedException {

        m_collectd.start();
        
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Test");
        bldr.setNodeid(1);
        bldr.setInterface(addr("192.168.1.1"));
        bldr.setService("SNMP");

        m_collectd.onEvent(bldr.getEvent());
        
        Thread.sleep(2000);
        
        assertNotNull(m_serviceCollector);
        assertEquals(1, m_serviceCollector.getCollectCount());
        
        m_mockUtils.verifyAll();
    }

    private void createGetPackagesExpectation(OnmsMonitoredService svc) {
        String rule = "ipaddr = '"+ str(svc.getIpAddress())+"'";
        
        //EasyMock.expect(m_filterDao.getActiveIPAddressList(rule)).andReturn(Collections.singletonList(svc.getIpAddress()));
        
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
        
        EasyMock.expect(m_collectdConfiguration.getPackages()).andReturn(Collections.singletonList(pkg));
        EasyMock.expect(m_collectdConfigFactory.interfaceInPackage(anyObject(OnmsIpInterface.class), eq(pkg))).andReturn(true);
    }

    public static class MockServiceCollector implements ServiceCollector {
        
        int m_collectCount = 0;
        
        public MockServiceCollector() {
            System.err.println("Created a MockServiceCollector");
        }

        @Override
        public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) {
            m_collectCount++;
            CollectionSet collectionSetResult=new AbstractCollectionSet() {
                private Date m_timestamp = new Date();

                @Override
                public int getStatus() {
                    return ServiceCollector.COLLECTION_SUCCEEDED;
                }

                @Override
                public void visit(CollectionSetVisitor visitor) {
                    visitor.visitCollectionSet(this);   
                    visitor.completeCollectionSet(this);
                }

                @Override
                public Date getCollectionTimestamp() {
                    return m_timestamp;
                }
            }; 
            return collectionSetResult;
        }

        public Object getCollectCount() {
            return m_collectCount;
        }

        @Override
        public void initialize(Map<String, String> parameters) {
            // This fails because collectd does NOT actually passed in configured monitor parameters
            // since no collectors actually use them (except this one)
//            String testKey = (String)parameters.get(TEST_KEY_PARM_NAME);
//            assertNotNull(testKey);
//            CollectdIntegrationTest.setServiceCollectorInTest(testKey, this);
        }

        @Override
        public void initialize(CollectionAgent agent, Map<String, Object> parameters) {
            String testKey = (String)parameters.get(TEST_KEY_PARM_NAME);
            assertNotNull(testKey);
            CollectdIntegrationTest.setServiceCollectorInTest(testKey, this);
        }

        @Override
        public void release() {
            throw new UnsupportedOperationException("MockServiceCollector.release() is not yet implemented");
        }

        @Override
        public void release(CollectionAgent agent) {
            throw new UnsupportedOperationException("MockServiceCollector.release() is not yet implemented");
        }
        
        @Override
        public RrdRepository getRrdRepository(String collectionName) {
            RrdRepository repo = new RrdRepository();
            repo.setRrdBaseDir(new File("/usr/local/opennms/share/rrd/snmp/"));
            repo.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:8928"));
            repo.setStep(300);
            repo.setHeartBeat(2 * 300);
            return repo;
        }
        
    }
    

}
