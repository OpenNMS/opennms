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
// Modifications:
//
// 2008 Feb 09: Eliminate warnings. - dj@opennms.org
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
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Filter;
import org.opennms.netmgt.config.poller.IncludeRange;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class PollerConfigFactoryTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PollerConfigFactoryTest.class);
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
            "         <parameter key=\"test-key\" value=\"test-value\"/>\n" +
            "         <parameter key=\"any-parm\">" +
            "            <config>" +
            "              <data/>" +
            "            </config>" +
            "         </parameter>" +
            "       </service>\n" +
            "       <downtime begin=\"0\" end=\"30000\"/>\n" + 
            "   </package>\n" +
            "   <monitor service=\"ICMP\" class-name=\"org.opennms.netmgt.poller.monitors.LdapMonitor\"/>\n"+
            "</poller-configuration>\n";

    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging();
        
        Resource dbConfig = new ClassPathResource("/org/opennms/netmgt/config/test-database-schema.xml");
        InputStream s = dbConfig.getInputStream();
        DatabaseSchemaConfigFactory dscf = new DatabaseSchemaConfigFactory(s);
        s.close();
        DatabaseSchemaConfigFactory.setInstance(dscf);

        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "Router");
        network.addInterface("192.168.1.1");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addInterface("192.168.1.2");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addNode(2, "Server");
        network.addInterface("192.168.1.3");
        network.addService("ICMP");
        network.addService("HTTP");
        network.addNode(3, "Firewall");
        network.addInterface("192.168.1.4");
        network.addService("SMTP");
        network.addService("HTTP");
        network.addInterface("192.168.1.5");
        network.addService("SMTP");
        network.addService("HTTP");
        network.addInterface("192.169.1.5");
        network.addService("SMTP");
        network.addService("HTTP");
        network.addNode(4, "TestNode121");
        network.addInterface("123.12.123.121");
        network.addService("HTTP");
        network.addNode(5, "TestNode122");
        network.addInterface("123.12.123.122");
        network.addService("HTTP");
        
        MockDatabase db = new MockDatabase();
        db.populate(network);
        DataSourceFactory.setInstance(db);
        
    }

    protected void tearDown() throws Exception {
        DataSourceFactory.setInstance(null);
        super.tearDown();
		MockLogAppender.assertNoWarningsOrGreater();
    }
    
    static class TestPollerConfigManager extends PollerConfigManager {
        private String m_xml;

        @SuppressWarnings("deprecation")
        public TestPollerConfigManager(String xml, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
            super(new StringReader(xml), localServer, verifyServer);
            save();
        }

        @SuppressWarnings("deprecation")
        public void update() throws IOException, MarshalException, ValidationException {
            m_config = CastorUtils.unmarshal(PollerConfiguration.class, new StringReader(m_xml));
            setUpInternalData();
        }

        protected void saveXml(String xml) throws IOException {
            m_xml = xml;
        }

        public String getXml() {
            return m_xml;
        }
        
        

        
    }
    
    public void testPollerConfigFactory() throws Exception {
        TestPollerConfigManager factory = new TestPollerConfigManager(POLLER_CONFIG, "localhost", false);
        assertNull(factory.getPackage("TestPkg"));
        Package pkg = new Package();
        pkg.setName("TestPkg");
        
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        
        Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        pkg.setRrd(rrd);
        
        Service svc = new Service();
        svc.setName("TestService");
        svc.setInterval(300000);
        pkg.addService(svc);
        
        Downtime dt = new Downtime();
        dt.setBegin(0);
        pkg.addDowntime(dt);
        
        IncludeRange inclde = new IncludeRange();
        inclde.setBegin("192.169.0.0");
        inclde.setEnd("192.169.255.255");
        pkg.addIncludeRange(inclde);
        
        factory.addPackage(pkg);
        factory.save();
        
        assertNotNull(factory.getPackage("TestPkg"));
        
        TestPollerConfigManager newFactory = new TestPollerConfigManager(factory.getXml(), "localhost", false);
        Package p = newFactory.getPackage("TestPkg");
        assertNotNull(p);
        assertTrue(newFactory.isInterfaceInPackage("192.169.1.5", p));
        assertFalse(newFactory.isInterfaceInPackage("192.168.1.5", p));
        
    }
    
    public void testInterfaceInPackage() throws Exception {
        TestPollerConfigManager factory = new TestPollerConfigManager(POLLER_CONFIG, "localhost", false);
        Package pkg = factory.getPackage("default");
        assertNotNull("Unable to find pkg default", pkg);
        
        assertTrue("Expected 192.168.1.1 to be in the package", factory.isInterfaceInPackage("192.168.1.1", pkg));
        
        
        
    }
    
    public void testSpecific() throws Exception {
        TestPollerConfigManager factory = new TestPollerConfigManager(POLLER_CONFIG, "localhost", false);
        assertNull(factory.getPackage("TestPkg"));
        Package pkg = new Package();
        pkg.setName("TestPkg");
        
        Filter filter = new Filter();
        filter.setContent("IPADDR != '0.0.0.0'");
        pkg.setFilter(filter);
        
        Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        pkg.setRrd(rrd);
        
        Service svc = new Service();
        svc.setName("TestService");
        svc.setInterval(300000);
        pkg.addService(svc);
        
        Downtime dt = new Downtime();
        dt.setBegin(0);
        pkg.addDowntime(dt);
        
        pkg.addSpecific("123.12.123.121");
        pkg.addSpecific("123.12.123.122");
        
        factory.addPackage(pkg);
        factory.save();
        
        assertNotNull(factory.getPackage("TestPkg"));
        
        TestPollerConfigManager newFactory = new TestPollerConfigManager(factory.getXml(), "localhost", false);
        Package p = newFactory.getPackage("TestPkg");
        assertNotNull(p);
        System.out.println(factory.getXml());
        assertTrue("Expect 123.12.123.121 to be part of the package", newFactory.isInterfaceInPackage("123.12.123.121", p));
        assertTrue("Expect 123.12.123.122 to be part of the package", newFactory.isInterfaceInPackage("123.12.123.122", p));
        assertFalse("Expected 192.168.1.1 to be excluded from the package", newFactory.isInterfaceInPackage("192.168.1.1", p));
        
    }

}
