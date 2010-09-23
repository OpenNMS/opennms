/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockTransactionTemplate;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.NetworkBuilder.NodeBuilder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
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


    private MockEventIpcManager m_eventIpcManager;
    private Collectd m_collectd;
    private EasyMockUtils m_mockUtils;
    private CollectorConfigDao m_collectorConfigDao;
    private String m_key;
    private MockServiceCollector m_serviceCollector;

    private IpInterfaceDao m_ifaceDao;

    private NodeDao m_nodeDao;

    private FilterDao m_filterDao;

    protected void setUp() throws Exception {

        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        
        m_mockUtils = new EasyMockUtils();
        
        m_filterDao = m_mockUtils.createMock(FilterDao.class);
        FilterDaoFactory.setInstance(m_filterDao);
        
        Resource resource = new ClassPathResource("etc/poll-outages.xml"); 
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(resource));

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
        
        MockTransactionTemplate transTemplate = new MockTransactionTemplate();
        
        m_collectorConfigDao = m_mockUtils.createMock(CollectorConfigDao.class);
        EasyMock.expect(m_collectorConfigDao.getCollectors()).andReturn(Collections.singleton(collector));
        EasyMock.expect(m_collectorConfigDao.getSchedulerThreads()).andReturn(1);

        m_ifaceDao = m_mockUtils.createMock(IpInterfaceDao.class);
        m_nodeDao = m_mockUtils.createMock(NodeDao.class);
        
        m_collectd = new Collectd() {

            @Override
            protected void handleInsufficientInfo(InsufficientInformationException e) {
                fail("Invalid event received: "+e.getMessage());
            }
            
        };
        m_collectd.setCollectorConfigDao(m_collectorConfigDao);
        m_collectd.setEventIpcManager(m_eventIpcManager);
        m_collectd.setTransactionTemplate(transTemplate);
        m_collectd.setIpInterfaceDao(m_ifaceDao);
        m_collectd.setNodeDao(m_nodeDao);
    }
    
    public static void setServiceCollectorInTest(String testKey, MockServiceCollector collector) {
        CollectdIntegrationTest test = m_tests.get(testKey);
        assertNotNull(test);
        test.setServiceCollector(collector);
    }

    private void setServiceCollector(MockServiceCollector collector) {
        m_serviceCollector = collector;
    }

    protected void tearDown() throws Exception {
        m_tests.remove(m_key);
    }
    
    
    
    public void testIt() throws InterruptedException {

        OnmsServiceType snmp = new OnmsServiceType("SNMP");
        NetworkBuilder netBuilder = new NetworkBuilder("localhost", "127.0.0.1");
        NodeBuilder nodeBuilder = netBuilder.addNode("node1").setId(1);
        InterfaceBuilder ifaceBlder = 
            netBuilder.addInterface("192.168.1.1")
            .setId(2)
            .setIsSnmpPrimary("P");
        ifaceBlder.addSnmpInterface("192.168.1.1", 1);
        OnmsMonitoredService svc = netBuilder.addService(snmp);
        
        Set<OnmsIpInterface> initialIfs = Collections.emptySet();
        EasyMock.expect(m_ifaceDao.findByServiceType(snmp.getName())).andReturn(initialIfs);
        
        
        m_collectorConfigDao.rebuildPackageIpListMap();
        
        EasyMock.expect(m_nodeDao.load(1)).andReturn(nodeBuilder.getNode()).anyTimes();
        
        createGetPackagesExpectation(svc);
        
        EasyMock.expect(m_ifaceDao.load(2)).andReturn(ifaceBlder.getInterface()).anyTimes();
        
        m_mockUtils.replayAll();
        
        m_collectd.init();
        //assertNotNull(m_serviceCollector);
        
        m_collectd.start();
        
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Test");
        bldr.setNodeid(1);
        bldr.setInterface("192.168.1.1");
        bldr.setService("SNMP");

        m_collectd.onEvent(bldr.getEvent());
        
        Thread.sleep(2000);
        
        assertNotNull(m_serviceCollector);
        assertEquals(1, m_serviceCollector.getCollectCount());
        
        
        m_mockUtils.verifyAll();
    }

    private void createGetPackagesExpectation(OnmsMonitoredService svc) {
        String rule = "ipaddr = '"+svc.getIpAddress()+"'";
        
        EasyMock.expect(m_filterDao.getIPList(rule)).andReturn(Collections.singletonList(svc.getIpAddress()));
        
        final Package pkg = new Package();
        pkg.setName("testPackage");
        Filter filter = new Filter();
        filter.setContent(rule);
        pkg.setFilter(filter);
        
        Service collector = new Service();
        collector.setName("SNMP");
        collector.setStatus("on");
        collector.setInterval(3000);
        
        Parameter parm = new Parameter();
        parm.setKey(TEST_KEY_PARM_NAME);
        parm.setValue(m_key);
        
        collector.setParameter(Collections.singletonList(parm));
        
        pkg.addService(collector);
        
        
        
        EasyMock.expect(m_collectorConfigDao.getPackages()).andAnswer(new IAnswer<Collection<CollectdPackage>>() {

            public Collection<CollectdPackage> answer() throws Throwable {
                CollectdPackage cPkg = new CollectdPackage(pkg, "localhost", false);
                return Collections.singleton(cPkg);
            }
            
        });
        
    }

    public static class MockServiceCollector implements ServiceCollector {
        
        int m_collectCount = 0;

        public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) {
            m_collectCount++;
            CollectionSet collectionSetResult=new CollectionSet() {

                public int getStatus() {
                    return ServiceCollector.COLLECTION_SUCCEEDED;
                }

                public void visit(CollectionSetVisitor visitor) {
                    visitor.visitCollectionSet(this);   
                    visitor.completeCollectionSet(this);
                }

				public boolean ignorePersist() {
					return false;
				}
            }; 
            return collectionSetResult;
        }

        public Object getCollectCount() {
            return m_collectCount;
        }

        @SuppressWarnings("unchecked")
        public void initialize(Map parameters) {
            // This fails because collectd does NOT actually passed in configured monitor parameters
            // since no collectors actually use them (except this one)
//            String testKey = (String)parameters.get(TEST_KEY_PARM_NAME);
//            assertNotNull(testKey);
//            CollectdIntegrationTest.setServiceCollectorInTest(testKey, this);
        }

        @SuppressWarnings("unchecked")
        public void initialize(CollectionAgent agent, Map parameters) {
            String testKey = (String)parameters.get(TEST_KEY_PARM_NAME);
            assertNotNull(testKey);
            CollectdIntegrationTest.setServiceCollectorInTest(testKey, this);
        }

        public void release() {
            throw new UnsupportedOperationException("MockServiceCollector.release is not yet implemented");
        }

        public void release(CollectionAgent agent) {
            throw new UnsupportedOperationException("MockServiceCollector.release is not yet implemented");
        }
        
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
