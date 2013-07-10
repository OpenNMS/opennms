/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-capsd.xml",
        // Override the capsd config with a stripped-down version
        "classpath:/META-INF/opennms/capsdTest.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath*:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@JUnitTemporaryDatabase
public class CapsdIntegrationTest implements TemporaryDatabaseAware<MockDatabase>, InitializingBean {

    private static final int FOREIGN_NODEID = 77;
    private static final String FOREIGN_NODE_IP_ADDRESS = "172.20.1.201";

    private MockNetwork m_mockNetwork = new MockNetwork();

    @Autowired
    private Capsd m_capsd;

    @Autowired
    private CapsdConfig m_capsdConfig;

    private MockDatabase m_db;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_db = database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        assertEquals(2, m_capsdConfig.getConfiguredProtocols().size());

        m_mockNetwork.createStandardNetwork();

        m_mockNetwork.addNode(FOREIGN_NODEID, "ForeignNode");
        m_mockNetwork.addInterface(FOREIGN_NODE_IP_ADDRESS);
        m_mockNetwork.addInterface("fe80:0000:0000:0000:ffff:eeee:dddd:cccc");
        m_mockNetwork.addService("ICMP");
        m_mockNetwork.addService("SNMP");

        m_db.populate(m_mockNetwork);
    }

    @Test
    @JUnitSnmpAgents(value={
    	    @JUnitSnmpAgent(host="172.20.1.201", resource="classpath:org/opennms/netmgt/snmp/snmpTestData1.properties"),
    	    @JUnitSnmpAgent(host="172.20.1.204", resource="classpath:org/opennms/netmgt/snmp/snmpTestData1.properties"),
    	    @JUnitSnmpAgent(host="172.20.1.205", resource="classpath:org/opennms/netmgt/snmp/snmpTestData1.properties")
    })
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public final void testRescan() throws Exception {

        assertEquals("Initally only 2 interfaces", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

        m_capsd.init();
        m_capsd.start();

        m_capsd.rescanInterfaceParent(FOREIGN_NODEID);

        Thread.sleep(15000);

        m_capsd.stop();

        assertEquals("after scanning should be 3 interfaces", 3, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));
    }

    /**
     * Refactored from org.opennms.netmgt.capsd.CapsdTest
     */
    @Test
    @JUnitSnmpAgents(value={
    	    @JUnitSnmpAgent(host="172.20.1.201", resource="classpath:org/opennms/netmgt/snmp/snmpTestData1.properties"),
    	    @JUnitSnmpAgent(host="172.20.1.204", resource="classpath:org/opennms/netmgt/snmp/snmpTestData1.properties"),
    	    @JUnitSnmpAgent(host="172.20.1.205", resource="classpath:org/opennms/netmgt/snmp/snmpTestData1.properties")
    })
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public final void testRescanOfForeignNode() throws Exception {

        m_db.getJdbcTemplate().update("update node set foreignSource='testSource', foreignId='123' where nodeid = ?", FOREIGN_NODEID);

        assertEquals("Initally only 2 interfaces", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

        m_capsd.init();
        m_capsd.start();

        m_capsd.rescanInterfaceParent(FOREIGN_NODEID);

        Thread.sleep(10000);

        m_capsd.stop();

        assertEquals("after scanning should still be 2 since its foreign", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));
    }

    /**
     * Refactored from org.opennms.netmgt.capsd.ScanSuspectTest
     * 
     * TODO: Add checks to this unit test to make sure that the suspect scan works correctly
     */
    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=FOREIGN_NODE_IP_ADDRESS, resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="149.134.45.45", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.16.201.2", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.17.1.230", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.1.1", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.1", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.9", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.17", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.25", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.33", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.41", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.49", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.57", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.65", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.31.3.73", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="172.100.10.1", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="203.19.73.1", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties"),
        @JUnitSnmpAgent(host="203.220.17.53", resource="classpath:org/opennms/netmgt/snmp/stonegate.properties")
    })
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public final void testStartStop() throws MarshalException, ValidationException, IOException {
        m_capsd.start();
        m_capsd.scanSuspectInterface(FOREIGN_NODE_IP_ADDRESS);
        m_capsd.stop();
    }

}
