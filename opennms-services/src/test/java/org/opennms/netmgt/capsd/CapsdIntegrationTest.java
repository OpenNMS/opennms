/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseAware;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-capsd.xml",
        // Override the capsd config with a stripped-down version
        "classpath:/META-INF/opennms/capsdTest.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/smallEventConfDao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
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
        Assert.assertNotNull(m_capsd);
    }

    @Before
    public void setUp() throws Exception {
        /*
        InputStream configStream = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/capsd/capsd-configuration.xml");
        DefaultCapsdConfigManager capsdConfig = new DefaultCapsdConfigManager(configStream);
        configStream.close();
        CapsdConfigFactory.setInstance(capsdConfig);
        */

        assertEquals(2, m_capsdConfig.getConfiguredProtocols().size());

        m_mockNetwork.createStandardNetwork();

        m_mockNetwork.addNode(FOREIGN_NODEID, "ForeignNode");
        m_mockNetwork.addInterface(FOREIGN_NODE_IP_ADDRESS);
        m_mockNetwork.addService("ICMP");
        m_mockNetwork.addService("SNMP");

        m_db.populate(m_mockNetwork);
    }

    /*
    @Override
    protected String preprocessConfigContents(File srcFile, String contents) {
        if (srcFile.getName().matches("snmp-config.xml")) {
            return getSnmpConfig();
        } else if (srcFile.getName().matches("capsd-configuration.xml")) {
            String updatedContents = contents.replaceAll("initial-sleep-time=\"30000\"", "initial-sleep-time=\"300\"");
            updatedContents = updatedContents.replaceAll("scan=\"on\"", "scan=\"off\"");
            updatedContents = updatedContents.replaceAll("SnmpPlugin\" scan=\"off\"", "SnmpPlugin\" scan=\"on\"");
            return updatedContents;
        } else {
            return contents;
        }
    }
     */

    /*
    public String getSnmpConfig() {
        return "<?xml version=\"1.0\"?>\n" + 
                "<snmp-config "+ 
                " retry=\"3\" timeout=\"3000\"\n" + 
                " read-community=\"public\"" +
                " write-community=\"private\"\n" + 
                " port=\"161\"\n" +
                " version=\"v1\">\n" +
                "   <definition version=\"v2c\" port=\"9161\" read-community=\"public\" proxy-host=\""+InetAddressUtils.getLocalHostAddressAsString()+"\">\n" + 
                "      <specific>172.20.1.201</specific>\n" +
                "      <specific>172.20.1.204</specific>\n" +
                "   </definition>\n" + 
                "</snmp-config>\n";
    }
     */

    @Test
    @JUnitSnmpAgent(host=FOREIGN_NODE_IP_ADDRESS, resource="classpath:org/opennms/netmgt/snmp/snmpTestData1.properties")
    public final void testRescan() throws Exception {

        assertEquals("Initially only 1 interface", 1, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

        m_capsd.start();

        m_capsd.rescanInterfaceParent(FOREIGN_NODEID);

        Thread.sleep(10000);

        m_capsd.stop();

        assertEquals("After scanning should be 2 interfaces", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));
    }
}
