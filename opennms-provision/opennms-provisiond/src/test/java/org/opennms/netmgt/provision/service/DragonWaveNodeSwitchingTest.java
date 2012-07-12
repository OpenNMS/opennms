/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.MockSnmpDataProvider;
import org.opennms.core.test.snmp.MockSnmpDataProviderAware;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
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
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml", 
        "classpath*:/META-INF/opennms/component-dao.xml", 
        "classpath*:/META-INF/opennms/detectors.xml", 
        "classpath:/importerServiceTest.xml" 
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DragonWaveNodeSwitchingTest implements InitializingBean, MockSnmpDataProviderAware {

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

	private MockSnmpDataProvider m_mockSnmpDataProvider;

	@Override
	public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);

		// Override the SnmpPeerFactory with an instance that directs all requests to the temporary JUnit SNMP agent
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);
	}

    @BeforeClass
    public static void setUpSnmpConfig() {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.opennms.netmgt.dao.castor.DataCollectionConfigParser", "WARN");
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "INFO");
        MockLogAppender.setupLogging(props);
    }

    @Before
    public void setUp() throws Exception {
    	final ForeignSource fs = new ForeignSource();
    	fs.setName("default");
    	fs.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));
    	final MockForeignSourceRepository mfsr = new MockForeignSourceRepository();
    	mfsr.putDefaultForeignSource(fs);
    	m_provisioner.getProvisionService().setForeignSourceRepository(mfsr);
        m_provisioner.start();
    }

    public void runScan(final NodeScan scan) throws InterruptedException, ExecutionException {
    	final Task t = scan.createTask();
        t.schedule();
        t.waitFor();
    }

    @Test
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="192.168.255.22", port=161, resource="classpath:/dw/walks/node1-walk.properties")
    })
    @JUnitTemporaryDatabase
    public void testInitialSetup() throws Exception {
        final InetAddress addr = InetAddressUtils.addr("192.168.255.22");
        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(1).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("192.168.255.22")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("192.168.255.22")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("192.168.255.22")).setService("ICMP").getEvent());

        importResource("classpath:/dw/import/dw_test_import.xml");

        anticipator.verifyAnticipated(200000, 0, 0, 0, 0);

        final OnmsNode onmsNode = m_nodeDao.findByForeignId("dw", "arthur");
        
        final String sysObjectId = onmsNode.getSysObjectId();
        assertEquals(".1.3.6.1.4.1.7262.2.3", sysObjectId);

		m_mockSnmpDataProvider.setDataForAddress(new SnmpAgentAddress(addr, 161), m_resourceLoader.getResource("classpath:/dw/walks/node3-walk.properties"));
        assertEquals(".1.3.6.1.4.1.7262.1", SnmpUtils.get(m_snmpPeerFactory.getAgentConfig(addr), SnmpObjId.get(".1.3.6.1.2.1.1.2.0")).toDisplayString());
        
        importResource("classpath:/dw/import/dw_test_import.xml");

        final NodeScan scan2 = m_provisioner.createNodeScan(onmsNode.getId(), onmsNode.getForeignSource(), onmsNode.getForeignId());
        runScan(scan2);

        m_nodeDao.flush();

        final OnmsNode node = m_nodeDao.findByForeignId("dw", "arthur");

        final String sysObjectId2 = node.getSysObjectId();
        assertEquals(".1.3.6.1.4.1.7262.1", sysObjectId2);
    }

    @Test
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="192.168.255.22", resource="classpath:/dw/walks/node3-walk.properties")
    })
    @JUnitTemporaryDatabase
    public void testASetup() throws Exception {

    	importResource("classpath:/dw/import/dw_test_import.xml");

        final OnmsNode onmsNode = m_nodeDao.findAll().get(0);
        String sysObjectId = onmsNode.getSysObjectId();

        assertEquals(".1.3.6.1.4.1.7262.1", sysObjectId);
    }

    private void importResource(final String location) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(location), true);
    }

	@Override
	public void setMockSnmpDataProvider(final MockSnmpDataProvider provider) {
		m_mockSnmpDataProvider = provider;
	}
}
