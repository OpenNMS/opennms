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

package org.opennms.netmgt.collectd;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.dao.api.CollectorConfigDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.poller.mock.MockScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

public class CollectdTest extends TestCase {
    
    EasyMockUtils m_easyMockUtils = new EasyMockUtils();

    private Collectd m_collectd;

    private FilterDao m_filterDao;
    private EventIpcManager m_eventIpcManager;
    private CollectorConfigDao m_collectorConfigDao;
    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipIfDao;
    private ServiceCollector m_collector;

    private MockScheduler m_scheduler;
    
    private PlatformTransactionManager m_transactionManager;
    

    private CollectdPackage m_collectdPackage;


    @Override
    protected void setUp() throws Exception {

        MockLogAppender.setupLogging();

        Resource threshdResource = new ClassPathResource("/etc/thresholds.xml");
        File homeDir = threshdResource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        // Test setup
        m_eventIpcManager = m_easyMockUtils.createMock(EventIpcManager.class);
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        m_collectorConfigDao = m_easyMockUtils.createMock(CollectorConfigDao.class);
        m_nodeDao = m_easyMockUtils.createMock(NodeDao.class);
        m_ipIfDao = m_easyMockUtils.createMock(IpInterfaceDao.class);
        m_collector = m_easyMockUtils.createMock(ServiceCollector.class);
        m_scheduler = new MockScheduler();

        m_eventIpcManager.addEventListener(isA(EventListener.class));
        expectLastCall().anyTimes();
        m_eventIpcManager.addEventListener(isA(EventListener.class), isACollection(String.class));
        expectLastCall().anyTimes();
        m_eventIpcManager.addEventListener(isA(EventListener.class), isA(String.class));
        expectLastCall().anyTimes();
        m_eventIpcManager.removeEventListener(isA(EventListener.class));
        expectLastCall().anyTimes();

//        MockNetwork m_network = new MockNetwork();
//        m_network.setCriticalService("ICMP");
//        m_network.addNode(1, "Router");
//        m_network.addInterface("192.168.1.1");
//        m_network.addService("ICMP");
//        m_network.addService("SMTP");
//        m_network.addInterface("192.168.1.2");
//        m_network.addService("ICMP");
//        m_network.addService("SMTP");
//        m_network.addNode(2, "Server");
//        m_network.addInterface("192.168.1.3");
//        m_network.addService("ICMP");
//        m_network.addService("HTTP");
//        m_network.addNode(3, "Firewall");
//        m_network.addInterface("192.168.1.4");
//        m_network.addService("SMTP");
//        m_network.addService("HTTP");
//        m_network.addInterface("192.168.1.5");
//        m_network.addService("SMTP");
//        m_network.addService("HTTP");
//
//        MockDatabase m_db = new MockDatabase();
//        m_db.populate(m_network);
//
//        DataSourceFactory.setInstance(m_db);

        m_filterDao = EasyMock.createMock(FilterDao.class);
        List<InetAddress> allIps = new ArrayList<InetAddress>();
        allIps.add(addr("192.168.1.1"));
        allIps.add(addr("192.168.1.2"));
        allIps.add(addr("192.168.1.3"));
        allIps.add(addr("192.168.1.4"));
        allIps.add(addr("192.168.1.5"));
        expect(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE *.*.*.*")).andReturn(allIps).atLeastOnce();
        expect(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE 1.1.1.1")).andReturn(new ArrayList<InetAddress>(0)).atLeastOnce();
        EasyMock.replay(m_filterDao);
        FilterDaoFactory.setInstance(m_filterDao);

        // This call will also ensure that the poll-outages.xml file can parse IPv4
        // and IPv6 addresses.
        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        PollOutagesConfigFactory factory = new PollOutagesConfigFactory(resource);
        factory.afterPropertiesSet();
        PollOutagesConfigFactory.setInstance(factory);

        CollectdConfigFactory collectdConfig = new CollectdConfigFactory(ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/config/collectd-testdata.xml"), "nms1", false);
        CollectdConfigFactory.setInstance(collectdConfig);

        m_collectd = new Collectd();
        m_collectd.setEventIpcManager(getEventIpcManager());
        m_collectd.setCollectorConfigDao(getCollectorConfigDao());
        m_collectd.setNodeDao(getNodeDao());
        m_collectd.setIpInterfaceDao(getIpInterfaceDao());
        m_collectd.setScheduler(m_scheduler);

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

        m_collectdPackage = new CollectdPackage(pkg, "localhost", false);
        
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(ConfigurationTestUtils.getInputStreamForConfigFile("thresholds.xml")));
    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        // FIXME: we get a Threshd warning still if we enable this  :(
        // MockLogAppender.assertNoWarningsOrGreater();
        EasyMock.verify(m_filterDao);
    }
    

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    private ServiceCollector getCollector() {
        return m_collector;
    }
    
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }

    private IpInterfaceDao getIpInterfaceDao() {
        return m_ipIfDao;
    }

    private CollectorConfigDao getCollectorConfigDao() {
        return m_collectorConfigDao;
    }

    private EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    private OnmsIpInterface getInterface() {
        OnmsNode node = new OnmsNode();
        node.setId(1);
        OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
        iface.setId(1);
        return iface;
    }

    public void testCreate() throws CollectionInitializationException {
        
        setupTransactionManager();
        
        String svcName = "SNMP";
        setupCollector(svcName);
        setupTransactionManager();
        
        Scheduler m_scheduler = m_easyMockUtils.createMock(Scheduler.class);
        m_collectd.setScheduler(m_scheduler);
        
        m_scheduler.schedule(eq(0L), isA(ReadyRunnable.class));
        m_scheduler.start();
        m_scheduler.stop();

        m_easyMockUtils.replayAll();
        
        m_collectd.init();
        m_collectd.start();
        m_collectd.stop();

        m_easyMockUtils.verifyAll();
    }

    /**
     * Test override of read community string and max repetitions in Collectd configuration parameters
     */
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

    public void testNoMatchingSpecs() throws CollectionInitializationException {
        String svcName = "SNMP";

        setupCollector(svcName);
        expect(m_ipIfDao.findByServiceType(svcName)).andReturn(new ArrayList<OnmsIpInterface>(0));

        setupTransactionManager();

        m_easyMockUtils.replayAll();

        m_collectd.init();
        m_collectd.start();

        m_scheduler.next();

        assertEquals(0, m_scheduler.getEntryCount());

        m_collectd.stop();
        
        m_easyMockUtils.verifyAll();
    }

    public void testOneMatchingSpec() throws CollectionException, CollectionInitializationException {
        String svcName = "SNMP";
        OnmsIpInterface iface = getInterface();

        setupCollector(svcName);
        
        m_collector.initialize(isA(CollectionAgent.class), isAMap(String.class, Object.class));
        CollectionSet collectionSetResult=new CollectionSet() {
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
			public boolean ignorePersist() {
				return false;
			}
			
                @Override
			public Date getCollectionTimestamp() {
				return m_timestamp;
			}
        };      
        expect(m_collector.collect(isA(CollectionAgent.class), isA(EventProxy.class), isAMap(String.class, Object.class))).andReturn(collectionSetResult);
        setupInterface(iface);
        
        setupTransactionManager();
  
        expect(m_collectorConfigDao.getPackages()).andReturn(Collections.singleton(m_collectdPackage));
        
        m_easyMockUtils.replayAll();

        m_collectd.init();
        m_collectd.start();
        
        m_scheduler.next();

        assertEquals("scheduler entry count", 1, m_scheduler.getEntryCount());

        m_scheduler.next();

        m_collectd.stop();

        m_easyMockUtils.verifyAll();
    }

    @SuppressWarnings("unchecked")
    private <K> Collection<K> isACollection(Class<K> innerClass) {
        return isA(Collection.class);
    }

    /*
    @SuppressWarnings("unchecked")
    private <K> List<K> isAList(Class<K> innerClass) {
        return isA(List.class);
    }
    */

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> isAMap(Class<K> keyClass, Class<V> valueClass) {
        return isA(Map.class);
    }

    private void setupTransactionManager() {
        m_transactionManager = m_easyMockUtils.createMock(PlatformTransactionManager.class);
        TransactionTemplate transactionTemplate = new TransactionTemplate(m_transactionManager);
        m_collectd.setTransactionTemplate(transactionTemplate);
        
        expect(m_transactionManager.getTransaction(isA(TransactionDefinition.class))).andReturn(new SimpleTransactionStatus()).anyTimes();
        m_transactionManager.rollback(isA(TransactionStatus.class));
        expectLastCall().anyTimes();
        m_transactionManager.commit(isA(TransactionStatus.class)); //anyTimes();
        expectLastCall().anyTimes();
    }

    private void setupInterface(OnmsIpInterface iface) {
        expect(m_ipIfDao.findByServiceType("SNMP")).andReturn(Collections.singletonList(iface));
        expect(m_ipIfDao.load(iface.getId())).andReturn(iface).atLeastOnce();
    }

    private void setupCollector(String svcName) throws CollectionInitializationException {
        Collector collector = new Collector();
        collector.setService(svcName);
        collector.setClassName(MockServiceCollector.class.getName());
        
        MockServiceCollector.setDelegate(getCollector());
        
        EasyMockUtils m_mockUtils = new EasyMockUtils();
        m_collectd.setNodeDao(m_mockUtils.createMock(NodeDao.class));
        // Setup expectation
        Map<String,String> empty = Collections.emptyMap();
        m_collector.initialize(empty);

        
        expect(m_collectorConfigDao.getCollectors()).andReturn(Collections.singleton(collector));
    }

    
    public static class MockServiceCollector implements ServiceCollector {
        private static ServiceCollector s_delegate;

        public MockServiceCollector() {
            
        }
        
        public static void setDelegate(ServiceCollector delegate) {
            s_delegate = delegate;
        }
        
        @Override
        public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException {
            return s_delegate.collect(agent, eproxy, parameters);
        }

        @Override
        public void initialize(Map<String, String> parameters) throws CollectionInitializationException {
            s_delegate.initialize(parameters);
        }

        @Override
        public void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
            s_delegate.initialize(agent, parameters);
        }

        @Override
        public void release() {
            s_delegate.release();
        }

        @Override
        public void release(CollectionAgent agent) {
            s_delegate.release(agent);
        }

        @Override
        public RrdRepository getRrdRepository(String collectionName) {
            RrdRepository repo = new RrdRepository();
            ArrayList<String> rras=new ArrayList<String>();
            rras.add("RRA:AVERAGE:0.5:1:8928");
            repo.setRrdBaseDir(new File("/usr/local/opennms/share/rrd/snmp/"));
            repo.setRraList(rras);
            repo.setStep(300);
            repo.setHeartBeat(2 * 300);
            return repo;
        }
    }

}
