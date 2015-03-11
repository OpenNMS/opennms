/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xmlrpcd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.PollerConfigManager;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisioner.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class OpenNMSProvisionerTest {

    @Autowired
    private OpenNMSProvisioner m_provisioner;

    private TestPollerConfigManager m_pollerConfig;

    public static final String POLLER_CONFIG = "\n" +
        "<poller-configuration\n" + 
        "   threads=\"10\"\n" + 
        "   nextOutageId=\"SELECT nextval(\'outageNxtId\')\"\n" + 
        "   serviceUnresponsiveEnabled=\"false\">\n" + 
        "   <node-outage status=\"on\" pollAllIfNoCriticalServiceDefined=\"true\"></node-outage>\n" + 
        "   <package name=\"default\">\n" + 
        "       <filter>IPADDR IPLIKE *.*.*.*</filter>\n" + 
        "       <rrd step = \"300\">\n" + 
        "           <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" + 
        "           <rra>RRA:AVERAGE:0.5:12:4464</rra>\n" + 
        "           <rra>RRA:MIN:0.5:12:4464</rra>\n" + 
        "           <rra>RRA:MAX:0.5:12:4464</rra>\n" + 
        "       </rrd>\n" + 
        "       <service name=\"ICMP\" interval=\"300000\">\n" +
        "           <parameter key=\"retry\" value=\"2\" />\n" +
        "           <parameter key=\"timeout\" value=\"3000\"/>\n" + 
        "       </service>\n" + 
        "       <downtime begin=\"10000\" end=\"40000\" interval=\"300000\"/>\n" + 
        "       <downtime begin=\"40000\" interval=\"300000\"/>\n" + 
        "   </package>\n" + 
        "   <package name=\"MyTcp\">\n" + 
        "       <filter>IPADDR IPLIKE *.*.*.*</filter>\n" + 
        "       <rrd step = \"300\">\n" + 
        "           <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" + 
        "           <rra>RRA:AVERAGE:0.5:12:4464</rra>\n" + 
        "           <rra>RRA:MIN:0.5:12:4464</rra>\n" + 
        "           <rra>RRA:MAX:0.5:12:4464</rra>\n" + 
        "       </rrd>\n" + 
        "       <service name=\"MyTcp\" interval=\"1234\">\n" +
        "           <parameter key=\"retry\" value=\"3\" />\n" +
        "           <parameter key=\"timeout\" value=\"314159\"/>\n" +
        "           <parameter key=\"port\" value=\"1776\"/>\n" + 
        "           <parameter key=\"banner\" value=\"Right back at ya!\"/>\n" + 
        "       </service>\n" + 
        "       <downtime begin=\"0\" end=\"1492\" interval=\"17\"/>\n" + 
        "       <downtime begin=\"1492\" interval=\"1234\"/>\n" + 
        "   </package>\n" + 
        "   <monitor service=\"ICMP\" class-name=\"org.opennms.netmgt.poller.monitors.LdapMonitor\"/>\n" + 
        "   <monitor service=\"MyTcp\" class-name=\"org.opennms.netmgt.poller.monitors.LdapMonitor\"/>\n" + 
        "</poller-configuration>\n";

    private EasyMockUtils m_mocks = new EasyMockUtils();
    private RrdStrategy<?,?> m_strategy = m_mocks.createMock(RrdStrategy.class);

    @Autowired
    private MockEventIpcManager m_eventManager;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        MockDatabase db = new MockDatabase();
        DataSourceFactory.setInstance(db);

        RrdUtils.setStrategy(m_strategy);
        
        m_provisioner.setEventManager(m_eventManager);
        
        m_pollerConfig = new TestPollerConfigManager(POLLER_CONFIG, "localhost", false);
        PollerConfigFactory.setInstance(m_pollerConfig);
        
        m_provisioner.setPollerConfig(m_pollerConfig);

        InputStream configStream = ConfigurationTestUtils.getInputStreamForConfigFile("opennms-server.xml");
        OpennmsServerConfigFactory onmsSvrConfig = new OpennmsServerConfigFactory(configStream);
        configStream.close();
        OpennmsServerConfigFactory.setInstance(onmsSvrConfig);

        configStream = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(configStream));
        configStream.close();
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    static class TestPollerConfigManager extends PollerConfigManager {
        String m_xml;

        public TestPollerConfigManager(String xml, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
            super(new ByteArrayInputStream(xml.getBytes("UTF-8")), localServer, verifyServer);
            save();
        }

        @Override
        public void update() throws IOException {
            m_config = JaxbUtils.unmarshal(PollerConfiguration.class, m_xml);
            setUpInternalData();
        }

        @Override
        protected void saveXml(String xml) throws IOException {
            m_xml = xml;
        }

        @Override
        public List<InetAddress> getIpList(Package pkg) {
            return new ArrayList<InetAddress>(0);
        }

        public String getXml() {
            return m_xml;
        }


    }
    
    @Test
    public void testGetServiceConfiguration() throws Exception {
        checkServiceConfiguration("default", "ICMP", 2, 3000, 300000, 300000, 30000);
        checkTcpConfiguration("MyTcp", "MyTcp", 3, 314159, 1234, 17, 1492, 1776, "Right back at ya!");
    }

    private Map<String, Object> checkTcpConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, String banner) throws Exception {
        Map<String, Object> configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(Integer.valueOf(port), configParams.get("port"));
        assertEquals(banner, configParams.get("banner"));
        return configParams;
    }

    private Map<String, Object> checkServiceConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration) throws Exception {
        Map<String, Object> configParams = m_provisioner.getServiceConfiguration(pkgName, svcName);
        assertEquals(svcName, configParams.get("serviceid"));
        assertEquals(Integer.valueOf(interval), configParams.get("interval"));
        assertEquals(Integer.valueOf(downtimeInterval), configParams.get("downtime_interval"));
        assertEquals(Integer.valueOf(downtimeDuration), configParams.get("downtime_duration"));
        assertNull(configParams.get("downtime_interval1"));
        assertNull(configParams.get("downtime_duration1"));
        assertEquals(Integer.valueOf(retries), configParams.get("retries"));
        assertEquals(Integer.valueOf(timeout), configParams.get("timeout"));
        
        TestPollerConfigManager mgr = new TestPollerConfigManager(m_pollerConfig.getXml(), "localhost", false);
        
        Package pkg = mgr.getPackage(pkgName);
        assertNotNull(pkg);
        Service svc = mgr.getServiceInPackage(svcName, pkg);
        assertNotNull(svc);
        assertEquals(Long.valueOf(interval), svc.getInterval());
        assertNotNull("Unables to find monitor for svc "+svcName+" in origonal config", m_pollerConfig.getServiceMonitor(svcName));
        assertNotNull("Unable to find monitor for svc "+svcName, mgr.getServiceMonitor(svcName));

        return configParams;
    }

    @Test
    public void testGetServiceConfigNullPkgName() {
        try {
            m_provisioner.getServiceConfiguration(null, "ICMP");
            fail("Expected exception");
        } catch (NullPointerException e) {

        }
    }

    @Test
    public void testGetServiceConfigNullServiceId() {
        try {
            m_provisioner.getServiceConfiguration("default", null);
            fail("Expected exception");
        } catch (NullPointerException e) {

        }
    }

    @Test
    public void testGetServiceConfigInvalidPkg() {
        try {
            m_provisioner.getServiceConfiguration("invalid", "ICMP");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testGetServiceConfigInvalidServiceId() {
        try {
            m_provisioner.getServiceConfiguration("default", "invalid");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testAddServiceIcmp() throws Exception {

        m_provisioner.addServiceICMP("MyIcmp", 77, 1066, 36, 5, 1812);
        checkServiceConfiguration("MyIcmp", "MyIcmp", 77, 1066, 36, 5, 1812);
        
        TestPollerConfigManager mgr = new TestPollerConfigManager(m_pollerConfig.getXml(), "localhost", false);
        
        Package pkg = mgr.getPackage("MyIcmp");
        assertNotNull(pkg);
        assertNotNull(mgr.getServiceInPackage("MyIcmp", pkg));
        
        

    }

    // TODO: Add test for exception on save of XML file
    @Test
    public void testAddServiceDatabase() throws Exception {
        expectUpdateEvent();
        m_mocks.replayAll();

        m_provisioner.addServiceDatabase("MyDB", 13, 2001, 54321, 71, 23456, "dbuser", "dbPasswd", "org.mydb.MyDriver", "jdbc://mydbhost:2");
        checkDatabaseConfiguration("MyDB", "MyDB", 13, 2001, 54321, 71, 23456, "dbuser", "dbPasswd", "org.mydb.MyDriver", "jdbc://mydbhost:2");

        m_mocks.verifyAll();
        verifyEvents();
    }
    
    @Test
    public void testAddServiceDNS() throws Exception {
        expectUpdateEvent();
        m_mocks.replayAll();

        m_provisioner.addServiceDNS("MyDNS", 11, 1111, 11111, 111, 111111, 101, "www.opennms.org");
        checkDNSConfiguration("MyDNS", "MyDNS", 11, 1111, 11111, 111, 111111, 101, "www.opennms.org");
        
        m_mocks.verifyAll();
        verifyEvents();
    }

    @Test
    public void testAddServiceHTTP() throws Exception {
        expectUpdateEvent();
        m_mocks.replayAll();

        m_provisioner.addServiceHTTP("MyHTTP", 22, 2222, 22222, 222, 222222, "opennms.com", 212, "200-203", "Home", "/index.html", "user", "passwd", null);
        checkHTTPConfiguration("MyHTTP", "MyHTTP", 22, 2222, 22222, 222, 222222, "opennms.com", 212, "200-203", "Home", "/index.html", "user", "passwd", null);
        
        m_mocks.verifyAll();
        verifyEvents();
    }

    @Test
    public void testAddServiceHTTPNoResponseCode() throws Exception {
        expectUpdateEvent();
        m_mocks.replayAll();

        m_provisioner.addServiceHTTP("MyHTTP", 22, 2222, 22222, 222, 222222, "opennms.com", 212, "", "Home", "/index.html", "user", "pw", "");
        checkHTTPConfiguration("MyHTTP", "MyHTTP", 22, 2222, 22222, 222, 222222, "opennms.com", 212, null, "Home", "/index.html", "user", "pw", "");

        m_mocks.verifyAll();
        verifyEvents();
    }

    private Map<String, Object> checkHTTPConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, String hostName, int port, String responseCode, String contentCheck, String url, String user, String passwd, String agent) throws Exception {
        Map<String, Object> configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(hostName, configParams.get("hostname"));
        assertEquals(Integer.valueOf(port), configParams.get("port"));
        assertEquals(responseCode, configParams.get("response"));
        assertEquals(contentCheck, configParams.get("response_text"));
        assertEquals(user, configParams.get("user"));
        assertEquals(passwd, configParams.get("password"));
        assertEquals(agent, configParams.get("agent"));
        assertEquals(url, configParams.get("url"));
        return configParams;
    }

    @Test
    public void testAddServiceHTTPS() throws Exception {
        expectUpdateEvent();
        m_mocks.replayAll();

        m_provisioner.addServiceHTTPS("MyHTTPS", 33, 3333, 33333, 333, 333333, "opennms.com", 313, "303", "Secure", "/secure.html", "user", "pw", "");
        checkHTTPSConfiguration("MyHTTPS", "MyHTTPS", 33, 3333, 33333, 333, 333333, "opennms.com", 313, "303", "Secure", "/secure.html", "user", "pw", "");
        
        m_mocks.verifyAll();
        verifyEvents();
    }
    
    private Map<String, Object> checkHTTPSConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, String hostName, int port, String responseCode, String contentCheck, String url, String user, String passwd, String agent) throws Exception {
        return checkHTTPConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration, hostName, port, responseCode, contentCheck, url, user, passwd, agent);
    }
 
    @Test
    public void testAddServiceTCP() throws Exception {
        expectUpdateEvent();
        m_mocks.replayAll();

        m_provisioner.addServiceTCP("MyTCP", 4, 44, 444, 4444, 44444, 404, "HELO");
        checkTCPConfiguration("MyTCP", "MyTCP", 4, 44, 444, 4444, 44444, 404, "HELO");
        
        m_mocks.verifyAll();
        verifyEvents();
    }
    
    private void expectUpdateEvent() {
        m_eventManager.getEventAnticipator().anticipateEvent(MockEventUtil.createEventBuilder("Test", EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI).getEvent());
    }

    private void verifyEvents() {
        m_eventManager.getEventAnticipator().verifyAnticipated(1000, 0, 0, 0, 0);
    }

    @Test
    public void testReaddServiceTCP() throws Exception {
        testAddServiceTCP();
        expectUpdateEvent();
        m_provisioner.addServiceTCP("MyTCP", 5, 55, 555, 5555, 55555, 505, "AHOY");
        checkTCPConfiguration("MyTCP", "MyTCP", 5, 55, 555, 5555, 55555, 505, "AHOY");
        verifyEvents();
    }
    
    // TODO: If a service is not in capsd it gets deleted at startup.. test that
    // adding one adds it to casd as well
    
    // TODO: make sure we add a monitor to pollerConfig
    
    // TODO: make sure we add a plugin to capsdConfig

    // TODO: Test adding as well as updating a service

    // TODO: ensure the data gets saved to the config file

    private Map<String, Object> checkTCPConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, String contentCheck) throws Exception {
        Map<String, Object> configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(Integer.valueOf(port), configParams.get("port"));
        assertEquals(contentCheck, configParams.get("banner"));
        return configParams;
    }

    private Map<String, Object> checkDNSConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, String lookup) throws Exception {
        Map<String, Object> configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(Integer.valueOf(port), configParams.get("port"));
        assertEquals(lookup, configParams.get("lookup"));
        return configParams;
    }

    private Map<String, Object> checkDatabaseConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, String username, String password, String driver, String dbUrl) throws Exception {
        Map<String, Object> configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(username, configParams.get("user"));
        assertEquals(password, configParams.get("password"));
        assertEquals(driver, configParams.get("driver"));
        assertEquals(dbUrl, configParams.get("url"));
        return configParams;
    }

}
