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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
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
public class IfIndexNullTest implements InitializingBean {
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        final MockForeignSourceRepository mfsr = new MockForeignSourceRepository();
        final ForeignSource fs = new ForeignSource();
        fs.setName("default");
        fs.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));
        mfsr.putDefaultForeignSource(fs);
        m_provisioner.getProvisionService().setForeignSourceRepository(mfsr);
    }

    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host="172.20.1.201", port=161, resource="classpath:snmpTestData-null.properties"),
        @JUnitSnmpAgent(host="172.20.1.204", port=161, resource="classpath:snmpTestData-null.properties")
    })
    public void testNullIfIndex() throws Exception {
        final CountDownLatch eventRecieved = anticipateEvents(EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI );

        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/tec_dump.xml"), true);
        
        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);
        
        eventRecieved.await();
        
        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
        
        //Verify ipinterface count
        assertEquals(2, getInterfaceDao().countAll());
        
    }
    
    public void runScan(final NodeScan scan) throws InterruptedException, ExecutionException {
    	final Task t = scan.createTask();
        t.schedule();
        t.waitFor();
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
    
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }

    private IpInterfaceDao getInterfaceDao() {
        return m_ipInterfaceDao;
    }
}
