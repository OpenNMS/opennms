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
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.mock.snmp.MockSnmpAgentAware;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.support.ProxySnmpAgentConfigFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
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
public class DragonWaveNodeSwitchingTest implements MockSnmpAgentAware {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    private MockSnmpAgent m_snmpAgent;

    @BeforeClass
    public static void setUpSnmpConfig() {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        MockLogAppender.setupLogging(props);
    }

    @Before
    public void setUp() throws Exception {
        // Override the SnmpPeerFactory with an instance that directs all requests to
        // the temporary JUnit SNMP agent
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);
        m_provisioner.start();
    }

    public void runScan(NodeScan scan) throws InterruptedException, ExecutionException {
        Task t = scan.createTask();
        t.schedule();
        t.waitFor();
    }

    @Test
    @JUnitSnmpAgent(resource = "classpath:/dw/walks/node1-walk.properties")
    public void testInitialSetup() throws Exception {
        
        final CountDownLatch eventRecieved = anticipateEvents(EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI );

        assertEquals(".1.3.6.1.4.1.7262.2.3", getSnmpValue("192.168.255.22", ".1.3.6.1.2.1.1.2.0").toDisplayString());

        importResource("classpath:/dw/import/dw_test_import.xml");

        OnmsNode onmsNode = m_nodeDao.findByForeignId("dw", "arthur");
        
	//NodeScan scan = m_provisioner.createNodeScan(onmsNode.getId(), onmsNode.getForeignSource(), onmsNode.getForeignId());
        //runScan(scan);

        eventRecieved.await();

        String sysObjectId = onmsNode.getSysObjectId();
        assertEquals(".1.3.6.1.4.1.7262.2.3", sysObjectId);

        m_snmpAgent.updateValuesFromResource(m_resourceLoader.getResource("classpath:/dw/walks/node3-walk.properties"));

        // Make sure agent reports the proper OID
        assertEquals(".1.3.6.1.4.1.7262.1", getSnmpValue("192.168.255.22", ".1.3.6.1.2.1.1.2.0").toDisplayString());

        NodeScan scan2 = m_provisioner.createNodeScan(onmsNode.getId(), onmsNode.getForeignSource(), onmsNode.getForeignId());
        runScan(scan2);

        m_nodeDao.flush();

        OnmsNode node = m_nodeDao.findByForeignId("dw", "arthur");

        String sysObjectId2 = node.getSysObjectId();
        assertEquals(".1.3.6.1.4.1.7262.1", sysObjectId2);
    }

    private CountDownLatch anticipateEvents(String... ueis) {
        final CountDownLatch eventRecieved = new CountDownLatch(1);
        m_eventSubscriber.addEventListener(new EventListener() {

            public void onEvent(Event e) {
                eventRecieved.countDown();
            }

            public String getName() {
                return "Test Initial Setup";
            }
        }, Arrays.asList(ueis));
        return eventRecieved;
    }

    private SnmpValue getSnmpValue(String host, String oid) throws UnknownHostException {
        return SnmpUtils.get(m_snmpPeerFactory.getAgentConfig(InetAddressUtils.addr(host)), SnmpObjId.get(oid));
    }

    @Test
    @JUnitSnmpAgent(resource = "classpath:/dw/walks/node3-walk.properties")
    public void testASetup() throws Exception {

        importResource("classpath:/dw/import/dw_test_import.xml");

        OnmsNode onmsNode = m_nodeDao.get(1);
        String sysObjectId = onmsNode.getSysObjectId();

        assertEquals(".1.3.6.1.4.1.7262.1", sysObjectId);
    }

    private void importResource(String location) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(location));
    }

    public void setMockSnmpAgent(MockSnmpAgent agent) {
        m_snmpAgent = agent;

    }
}
