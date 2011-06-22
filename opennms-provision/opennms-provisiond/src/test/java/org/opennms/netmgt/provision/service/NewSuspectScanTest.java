/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.tasks.Task;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unit test for ModelImport application.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NewSuspectScanTest {
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private ProvisionService m_provisionService;
    
    @Autowired
    private PausibleScheduledThreadPoolExecutor m_pausibleExecutor;
    
    private ForeignSourceRepository m_foreignSourceRepository;
    
    private ForeignSource m_foreignSource;

    static private String s_initialDiscoveryEnabledValue;
    
    @BeforeClass
    public static void setUpSnmpConfig() {
        SnmpPeerFactory.setFile(new File("src/test/proxy-snmp-config.xml"));

        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
        
        //System.setProperty("mock.debug", "false");
        
    }

    @BeforeClass
    public static void setEnableDiscovery() {
        s_initialDiscoveryEnabledValue = System.getProperty("org.opennms.provisiond.enableDiscovery");
        System.setProperty("org.opennms.provisiond.enableDiscovery", "true");
        
    }
    
    @AfterClass
    public static void resetEnableDiscovery() {
        if (s_initialDiscoveryEnabledValue == null) {
            System.getProperties().remove("org.opennms.provisiond.enableDiscovery");
        } else {
            System.setProperty("org.opennms.provisiond.enableDiscovery", s_initialDiscoveryEnabledValue);
        }
    }
    
    @Before
    public void setUp() throws Exception {
        
        m_provisioner.start();
        
        m_foreignSource = new ForeignSource();
        m_foreignSource.setName("imported:");
        m_foreignSource.setScanInterval(Duration.standardDays(1));
        
        m_foreignSourceRepository = new MockForeignSourceRepository();
        m_foreignSourceRepository.save(m_foreignSource);
        
        m_provisionService.setForeignSourceRepository(m_foreignSourceRepository);
        
        m_pausibleExecutor.pause();

    }

    @Test(timeout=300000)
    @Transactional
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    @JUnitSnmpAgent(resource="classpath:snmpTestData3.properties")
    public void testScanNewSuspect() throws Exception {
        
        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());


        NewSuspectScan scan = m_provisioner.createNewSuspectScan(InetAddressUtils.addr("172.20.2.201"));
        runScan(scan);

        // wait for NodeScan triggered by NodeAdded to complete
        Thread.sleep(100000);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        StringBuffer errorMsg = new StringBuffer();
        //Verify ipinterface count
        for (OnmsIpInterface iface : getInterfaceDao().findAll()) {
            errorMsg.append(iface.toString());
        }
        assertEquals(errorMsg.toString(), 2, getInterfaceDao().countAll());

        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 2, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());

    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testScanNewSuspectNoSnmp() throws Exception {

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());
        
        
        NewSuspectScan scan = m_provisioner.createNewSuspectScan(InetAddressUtils.addr("172.20.2.201"));
        runScan(scan);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 0, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(0, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());
        
    }
    
    public void runScan(NewSuspectScan scan) throws InterruptedException, ExecutionException {
        Task t = scan.createTask();
        t.schedule();
        t.waitFor();
    }

    
    private DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }


    private NodeDao getNodeDao() {
        return m_nodeDao;
    }


    private IpInterfaceDao getInterfaceDao() {
        return m_ipInterfaceDao;
    }


    private SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }


    private MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }


    private ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }

}
