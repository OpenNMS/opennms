/*
 * This file is part of the OpenNMS(R) Application.
 * 
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * Modifications:
 *
 * 2007 Jun 30: Make tests work again. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 */

package org.opennms.netmgt.collectd;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.mock.MockScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.test.mock.MockLogAppender;
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
    private IpInterfaceDao m_ipIfDao;
    private MonitoredServiceDao m_monSvcDao;
    private ServiceCollector m_collector;

    private MockScheduler m_scheduler;
    private CollectionSpecification m_spec;

    private PlatformTransactionManager m_transactionManager;

    @Override
    protected void setUp() throws Exception {

        MockLogAppender.setupLogging();

        // Test setup
        m_eventIpcManager = m_easyMockUtils.createMock(EventIpcManager.class);
        m_collectorConfigDao = m_easyMockUtils.createMock(CollectorConfigDao.class);
        m_ipIfDao = m_easyMockUtils.createMock(IpInterfaceDao.class);
        m_monSvcDao = m_easyMockUtils.createMock(MonitoredServiceDao.class);
        m_collector = m_easyMockUtils.createMock(ServiceCollector.class);
        m_scheduler = new MockScheduler();

        m_eventIpcManager.addEventListener(isA(EventListener.class));
        expectLastCall().anyTimes();
        m_eventIpcManager.addEventListener(isA(EventListener.class), isA(List.class));
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
        List<String> allIps = new ArrayList<String>();
        allIps.add("192.168.1.1");
        allIps.add("192.168.1.2");
        allIps.add("192.168.1.3");
        allIps.add("192.168.1.4");
        allIps.add("192.168.1.5");
        expect(m_filterDao.getIPList("IPADDR IPLIKE *.*.*.*")).andReturn(allIps).atLeastOnce();
        expect(m_filterDao.getIPList("IPADDR IPLIKE 1.1.1.1")).andReturn(new ArrayList<String>(0)).atLeastOnce();
        EasyMock.replay(m_filterDao);
        FilterDaoFactory.setInstance(m_filterDao);

        Resource resource = new ClassPathResource("etc/poll-outages.xml"); 
        InputStreamReader pollOutagesRdr = new InputStreamReader(resource.getInputStream());
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(pollOutagesRdr));
        pollOutagesRdr.close();

        Reader collectdReader = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/collectd-testdata.xml");
        CollectdConfigFactory collectdConfig = new CollectdConfigFactory(collectdReader, "nms1", false);
        CollectdConfigFactory.setInstance(collectdConfig);

        m_collectd = new Collectd();
        m_collectd.setEventIpcManager(getEventIpcManager());
        m_collectd.setCollectorConfigDao(getCollectorConfigDao());
        m_collectd.setIpInterfaceDao(getIpInterfaceDao());
        m_collectd.setMonitoredServiceDao(getMonitoredServiceDao());
        m_collectd.setScheduler(m_scheduler);

        Package pkg = new Package();
        pkg.setName("pkg");
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
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

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
        EasyMock.verify(m_filterDao);
    }
    

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    private ServiceCollector getCollector() {
        return m_collector;
    }

    private MonitoredServiceDao getMonitoredServiceDao() {
        return m_monSvcDao;
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

    private CollectionSpecification getCollectionSpecification() {
        return m_spec;
    }

    private OnmsIpInterface getInterface() {
        OnmsNode node = new OnmsNode();
        node.setId(new Integer(1));
        OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
        iface.setId(1);
        return iface;
    }

    public void testCreate() {
        String svcName = "SNMP";
        setupCollector(svcName);

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

    public void testNoMatchingSpecs() {
        String svcName = "SNMP";
        OnmsIpInterface iface = getInterface();
        List<CollectionSpecification> specs = new ArrayList<CollectionSpecification>(0);

        setupCollector(svcName);
        expect(m_ipIfDao.findByServiceType("SNMP")).andReturn(new ArrayList<OnmsIpInterface>(0));
        setupSpecs(iface, svcName, specs);
        setupTransactionManager();

        m_easyMockUtils.replayAll();

        m_collectd.init();
        m_collectd.start();

        m_scheduler.next();

        assertEquals(0, m_scheduler.getEntryCount());

        m_collectd.stop();
        
        m_easyMockUtils.verifyAll();
    }

    public void testOneMatchingSpec() {
        String svcName = "SNMP";
        OnmsIpInterface iface = getInterface();
        List<CollectionSpecification> specs = Collections.singletonList(getCollectionSpecification());

        setupCollector(svcName);
        
        m_collector.initialize(isA(CollectionAgent.class), isA(Map.class));
        expect(m_collector.collect(isA(CollectionAgent.class), isA(EventProxy.class), isAMap(String.class, String.class))).andReturn(ServiceCollector.COLLECTION_SUCCEEDED);
        
        setupInterface(iface);
        setupSpecs(iface, svcName, specs);
        
        setupTransactionManager();
        
        m_easyMockUtils.replayAll();

        m_collectd.init();
        m_collectd.start();
        
        m_scheduler.next();

        assertEquals(1, m_scheduler.getEntryCount());

        m_scheduler.next();

        m_collectd.stop();

        m_easyMockUtils.verifyAll();
    }

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

    private void setupSpecs(OnmsIpInterface iface, String svcName, List<CollectionSpecification> specs) {
//        expect(m_collectorConfigDao.getSpecificationsForInterface(iface, svcName)).andReturn(Collections.singleton(collector));

        /*
        m_collectorConfigDao.expects(once()).method("getSpecificationsForInterface").
        with(same(iface), eq(svcName)).
        will(returnValue(specs));
        */
//        m_collectorConfigDao.
    }

    private void setupInterface(OnmsIpInterface iface) {
        expect(m_ipIfDao.findByServiceType("SNMP")).andReturn(Collections.singleton(iface));
        expect(m_ipIfDao.get(iface.getId())).andReturn(iface).atLeastOnce();
    }

    private void setupCollector(String svcName) {
        Collector collector = new Collector();
        collector.setService(svcName);
        collector.setClassName(MockServiceCollector.class.getName());
        
        MockServiceCollector.setDelegate(getCollector());
        
        // Setup expectation
        m_collector.initialize(null);

        expect(m_collectorConfigDao.getCollectors()).andReturn(Collections.singleton(collector));
    }

    
    public static class MockServiceCollector implements ServiceCollector {
        private static ServiceCollector s_delegate;

        public MockServiceCollector() {
            
        }
        
        public static void setDelegate(ServiceCollector delegate) {
            s_delegate = delegate;
        }
        
        public int collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) {
            return s_delegate.collect(agent, eproxy, parameters);
        }

        public void initialize(Map parameters) {
            s_delegate.initialize(parameters);
        }

        public void initialize(CollectionAgent agent, Map parameters) {
            s_delegate.initialize(agent, parameters);
        }

        public void release() {
            s_delegate.release();
        }

        public void release(CollectionAgent agent) {
            s_delegate.release(agent);
        }
    }

}
