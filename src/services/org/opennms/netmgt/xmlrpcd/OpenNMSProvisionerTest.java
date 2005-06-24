//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.xmlrpcd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.opennms.netmgt.config.CapsdConfigManager;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.PollerConfigManager;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;

public class OpenNMSProvisionerTest extends MockObjectTestCase {


    private OpenNMSProvisioner m_provisioner;

    private TestCapsdConfigManager m_capsdConfig;

    private TestPollerConfigManager m_pollerConfig;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(OpenNMSProvisionerTest.class);
    }

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
        "   </package>\n" + 
        "   <monitor service=\"ICMP\" class-name=\"org.opennms.netmgt.poller.monitors.LdapMonitor\"/>\n" + 
        "   <monitor service=\"MyTcp\" class-name=\"org.opennms.netmgt.poller.monitors.LdapMonitor\"/>\n" + 
        "</poller-configuration>\n";

    private static final String CAPSD_CONFIG = "\n" +
            "<capsd-configuration max-suspect-thread-pool-size=\"2\" max-rescan-thread-pool-size=\"3\"\n" +
            "   delete-propagation-enabled=\"true\">\n" +
            "   <protocol-plugin protocol=\"ICMP\" class-name=\"org.opennms.netmgt.capsd.plugins.LdapPlugin\"/>\n" +
            "   <protocol-plugin protocol=\"MyTcp\" class-name=\"org.opennms.netmgt.capsd.plugins.LdapPlugin\"/>\n" +
            "</capsd-configuration>\n";

    private Mock m_strategy;

    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.setupLogging();
        MockUtil.resetLogLevel();
        MockDatabase db = new MockDatabase();
        DatabaseConnectionFactory.setInstance(db);
        
        Properties properties = new Properties();
        RrdConfig.setProperties(properties);

        m_strategy = mock(RrdStrategy.class);
        RrdUtils.setStrategy((RrdStrategy)m_strategy.proxy());
        
        m_provisioner = new OpenNMSProvisioner();
        
        m_capsdConfig = new TestCapsdConfigManager(CAPSD_CONFIG);
        m_capsdConfig.setNextSvcIdSql(db.getNextServiceIdStatement());
        
        Connection conn = db.getConnection();
        try {
            m_capsdConfig.syncServices(conn);
        } finally {
            conn.close();
        }
        
        m_pollerConfig = new TestPollerConfigManager(POLLER_CONFIG, "localhost", false);
        
        m_provisioner.setCapsdConfig(m_capsdConfig);
        m_provisioner.setPollerConfig(m_pollerConfig);

    }

    private void expectRrdInitialize() {
        m_strategy.expects(atLeastOnce()).method("initialize");
    }

    protected void tearDown() throws Exception {
        
        DatabaseConnectionFactory.setInstance(null);
        super.tearDown();
        assertTrue("Warnings in Log!", MockUtil.noWarningsOrHigherLogged());
    }

    static class TestPollerConfigManager extends PollerConfigManager {
        String m_xml;

        private String m_localServer;

        private boolean m_verifyServer;

        public TestPollerConfigManager(String xml, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
            super(new StringReader(xml), localServer, verifyServer);
            m_localServer = localServer;
            m_verifyServer = verifyServer;
            save();
        }

        public void update() throws IOException, MarshalException, ValidationException {
            reloadXML(new StringReader(m_xml));
        }

        protected void saveXml(String xml) throws IOException {
            m_xml = xml;
        }

        protected List getIpList(Package pkg) {
            return Collections.EMPTY_LIST;
        }

        public String getXml() {
            return m_xml;
        }

    }
    
    static class TestCapsdConfigManager extends CapsdConfigManager {
        private String m_xml;

        public TestCapsdConfigManager(String xml) throws MarshalException, ValidationException, IOException {
            super(new StringReader(xml));
            save();
        }

        protected void saveXml(String xml) throws IOException {
            m_xml = xml;
        }

        protected void update() throws IOException, FileNotFoundException, MarshalException, ValidationException {
            loadXml(new StringReader(m_xml));
        }
        
        public String getXml() {
            return m_xml;
        }

        
    }

    public void testGetServiceConfiguration() throws Exception {
        checkServiceConfiguration("default", "ICMP", 2, 3000, 300000, 300000, 30000);
        checkTcpConfiguration("MyTcp", "MyTcp", 3, 314159, 1234, 17, 1492, 1776, "Right back at ya!");
    }

    private Map checkTcpConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, String banner) throws Exception {
        Map configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(new Integer(port), configParams.get("port"));
        assertEquals(banner, configParams.get("banner"));
        return configParams;
    }

    private Map checkServiceConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration) throws Exception {
        Map configParams = m_provisioner.getServiceConfiguration(pkgName, svcName);
        assertEquals(svcName, configParams.get("serviceid"));
        assertEquals(new Integer(interval), configParams.get("interval"));
        assertEquals(new Integer(downtimeInterval), configParams.get("downtime_interval"));
        assertEquals(new Integer(downtimeDuration), configParams.get("downtime_duration"));
        assertEquals(new Integer(retries), configParams.get("retries"));
        assertEquals(new Integer(timeout), configParams.get("timeout"));
        
        TestPollerConfigManager mgr = new TestPollerConfigManager(m_pollerConfig.getXml(), "localhost", false);
        
        Package pkg = mgr.getPackage(pkgName);
        assertNotNull(pkg);
        Service svc = mgr.getServiceInPackage(svcName, pkg);
        assertNotNull(svc);
        assertEquals(interval, svc.getInterval());
        assertNotNull("Unables to find monitor for svc "+svcName+" in origonal config", m_pollerConfig.getServiceMonitor(svcName));
        assertNotNull("Unable to find monitor for svc "+svcName, mgr.getServiceMonitor(svcName));
        
        assertNotNull("Unable to find protocol plugin in capsdConfig for svc "+svcName, m_capsdConfig.getProtocolPlugin(svcName));
        assertNotNull("Unable to find service table entry in capsdConfig for svc "+svcName, m_capsdConfig.getServiceIdentifier(svcName));

        return configParams;
    }

    public void testGetServiceConfigNullPkgName() {
        try {
            Map configParams = m_provisioner.getServiceConfiguration(null, "ICMP");
            fail("Expected exception");
        } catch (NullPointerException e) {

        }
    }

    public void testGetServiceConfigNullServiceId() {
        try {
            Map configParams = m_provisioner.getServiceConfiguration("default", null);
            fail("Expected exception");
        } catch (NullPointerException e) {

        }
    }

    public void testGetServiceConfigInvalidPkg() {
        try {
            Map configParams = m_provisioner.getServiceConfiguration("invalid", "ICMP");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }
    }

    public void testGetServiceConfigInvalidServiceId() {
        try {
            Map configParams = m_provisioner.getServiceConfiguration("default", "invalid");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }
    }

    public void xtestAddServiceIcmp() throws Exception {

        m_provisioner.addServiceICMP("MyIcmp", 77, 1066, 36, 5, 1812);
        checkServiceConfiguration("MyIcmp", "MyIcmp", 77, 1066, 36, 5, 1812);
        
        TestPollerConfigManager mgr = new TestPollerConfigManager(m_pollerConfig.getXml(), "localhost", false);
        
        Package pkg = mgr.getPackage("MyIcmp");
        assertNotNull(pkg);
        assertNotNull(mgr.getServiceInPackage("MyIcmp", pkg));
        
        

    }

    // TODO: Add test for exception on save of XML file
    
    public void testAddServiceDatabase() throws Exception {
        expectRrdInitialize();

        m_provisioner.addServiceDatabase("MyDB", 13, 2001, 54321, 71, 23456, "dbuser", "dbPasswd", "org.mydb.MyDriver", "jdbc://mydbhost:2");
        checkDatabaseConfiguration("MyDB", "MyDB", 13, 2001, 54321, 71, 23456, "dbuser", "dbPasswd", "org.mydb.MyDriver", "jdbc://mydbhost:2");

    }
    
    public void testAddServiceDNS() throws Exception {
        expectRrdInitialize();
        m_provisioner.addServiceDNS("MyDNS", 11, 1111, 11111, 111, 111111, 101, "www.opennms.org");
        checkDNSConfiguration("MyDNS", "MyDNS", 11, 1111, 11111, 111, 111111, 101, "www.opennms.org");
    }

    public void testAddServiceHTTP() throws Exception {
        expectRrdInitialize();
        m_provisioner.addServiceHTTP("MyHTTP", 22, 2222, 22222, 222, 222222, 212, 202, "Home", "/index.html");
        checkHTTPConfiguration("MyHTTP", "MyHTTP", 22, 2222, 22222, 222, 222222, 212, 202, "Home", "/index.html");
    }

    private Map checkHTTPConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, int responseCode, String contentCheck, String url) throws Exception {
        Map configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(new Integer(port), configParams.get("port"));
        assertEquals(new Integer(responseCode), configParams.get("response"));
        assertEquals(contentCheck, configParams.get("response_text"));
        assertEquals(url, configParams.get("url"));
        return configParams;
    }

    public void testAddServiceHTTPS() throws Exception {
        expectRrdInitialize();
        m_provisioner.addServiceHTTPS("MyHTTPS", 33, 3333, 33333, 333, 333333, 313, 303, "Secure", "/secure.html");
        checkHTTPSConfiguration("MyHTTPS", "MyHTTPS", 33, 3333, 33333, 333, 333333, 313, 303, "Secure", "/secure.html");
    }
    
    private Map checkHTTPSConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, int responseCode, String contentCheck, String url) throws Exception {
        Map configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(new Integer(port), configParams.get("port"));
        assertEquals(new Integer(responseCode), configParams.get("response"));
        assertEquals(contentCheck, configParams.get("response_text"));
        assertEquals(url, configParams.get("url"));
        return configParams;
    }
    
    public void testAddServiceTCP() throws Exception {
        expectRrdInitialize();
        m_provisioner.addServiceTCP("MyTCP", 4, 44, 444, 4444, 44444, 404, "HELO");
        checkTCPConfiguration("MyTCP", "MyTCP", 4, 44, 444, 4444, 44444, 404, "HELO");
    }
    
    public void testReaddServiceTCP() throws Exception {
        testAddServiceTCP();
        m_provisioner.addServiceTCP("MyTCP", 5, 55, 555, 5555, 55555, 505, "AHOY");
        checkTCPConfiguration("MyTCP", "MyTCP", 5, 55, 555, 5555, 55555, 505, "AHOY");
    }
    
    // TODO: If a service is not in capsd it gets deleted at startup.. test that
    // adding one adds it to casd as well
    
    // TODO: make sure we add a monitor to pollerConfig
    
    // TODO: make sure we add a plugin to capsdConfig

    // TODO: Test adding as well as updating a service

    // TODO: ensure the data gets saved to the config file

    private Map checkTCPConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, String contentCheck) throws Exception {
        Map configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(new Integer(port), configParams.get("port"));
        assertEquals(contentCheck, configParams.get("banner"));
        return configParams;
    }

    private Map checkDNSConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, int port, String lookup) throws Exception {
        Map configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(new Integer(port), configParams.get("port"));
        assertEquals(lookup, configParams.get("lookup"));
        return configParams;
    }

    private Map checkDatabaseConfiguration(String pkgName, String svcName, int retries, int timeout, int interval, int downtimeInterval, int downtimeDuration, String username, String password, String driver, String dbUrl) throws Exception {
        Map configParams = checkServiceConfiguration(pkgName, svcName, retries, timeout, interval, downtimeInterval, downtimeDuration);
        assertEquals(username, configParams.get("user"));
        assertEquals(password, configParams.get("password"));
        assertEquals(driver, configParams.get("driver"));
        assertEquals(dbUrl, configParams.get("url"));
        return configParams;
    }

}
