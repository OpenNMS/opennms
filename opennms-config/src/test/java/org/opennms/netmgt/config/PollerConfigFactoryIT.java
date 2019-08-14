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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Filter;
import org.opennms.netmgt.config.poller.IncludeRange;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;

public class PollerConfigFactoryIT {

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
            "         <parameter key=\"any-parm\" />" +
            "       </service>\n" +
            "       <downtime begin=\"0\" end=\"30000\"/>\n" + 
            "   </package>\n" +
            "   <monitor service=\"ICMP\" class-name=\"org.opennms.netmgt.mock.MockMonitor\"/>\n"+
            "</poller-configuration>\n";

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        final DataSource dsmock = Mockito.mock(DataSource.class);
        DataSourceFactory.setInstance(dsmock);

        final FilterDao filterdao = Mockito.mock(FilterDao.class);
        final List<InetAddress> addrs = Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5", "192.169.1.5", "123.12.123.121", "123.12.123.122").stream().map(InetAddressUtils::addr).collect(Collectors.toList());
        Mockito.when(filterdao.getActiveIPAddressList(Mockito.anyString())).thenReturn(addrs);
        FilterDaoFactory.setInstance(filterdao);

    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    static class TestPollerConfigManager extends PollerConfigManager {
        private String m_xml;

        public TestPollerConfigManager(String xml) throws IOException {
            super(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            save();
        }

        @Override
        public void update() throws IOException {
            m_config = JaxbUtils.unmarshal(PollerConfiguration.class, m_xml);
            super.update();
        }

        @Override
        protected void saveXml(String xml) throws IOException {
            m_xml = xml;
        }

        public String getXml() {
            return m_xml;
        }
    }
    
    @Test
    public void testPollerConfigFactory() throws Exception {
        TestPollerConfigManager factory = new TestPollerConfigManager(POLLER_CONFIG);
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
        svc.setInterval(300000l);
        pkg.addService(svc);
        
        Downtime dt = new Downtime();
        dt.setBegin(0l);
        pkg.addDowntime(dt);
        
        IncludeRange inclde = new IncludeRange();
        inclde.setBegin("192.169.0.0");
        inclde.setEnd("192.169.255.255");
        pkg.addIncludeRange(inclde);
        
        factory.addPackage(pkg);
        factory.save();
        
        assertNotNull(factory.getPackage("TestPkg"));
        
        TestPollerConfigManager newFactory = new TestPollerConfigManager(factory.getXml());
        Package p = newFactory.getPackage("TestPkg");
        assertNotNull("package for 'TestPkg'", p);

        assertTrue("Expected 192.169.1.5 to be in the package", newFactory.isInterfaceInPackage("192.169.1.5", p));
        assertFalse("Expected 192.168.1.5 to *not* be in the package", newFactory.isInterfaceInPackage("192.168.1.5", p));
    }
    
    @Test
    public void testInterfaceInPackage() throws Exception {
        TestPollerConfigManager factory = new TestPollerConfigManager(POLLER_CONFIG);
        Package pkg = factory.getPackage("default");
        assertNotNull("Unable to find pkg default", pkg);
        
        assertTrue("Expected 192.168.1.1 to be in the package", factory.isInterfaceInPackage("192.168.1.1", pkg));
    }

    @Test
    public void testSpecific() throws Exception {
        TestPollerConfigManager factory = new TestPollerConfigManager(POLLER_CONFIG);
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
        svc.setInterval(300000l);
        pkg.addService(svc);
        
        Downtime dt = new Downtime();
        dt.setBegin(0l);
        pkg.addDowntime(dt);
        
        pkg.addSpecific("123.12.123.121");
        pkg.addSpecific("123.12.123.122");
        
        factory.addPackage(pkg);
        factory.save();
        
        assertNotNull(factory.getPackage("TestPkg"));
        
        TestPollerConfigManager newFactory = new TestPollerConfigManager(factory.getXml());
        Package p = newFactory.getPackage("TestPkg");
        assertNotNull("package 'TestPkg' from new factory", p);

        assertTrue("Expect 123.12.123.121 to be part of the package", newFactory.isInterfaceInPackage("123.12.123.121", p));
        assertTrue("Expect 123.12.123.122 to be part of the package", newFactory.isInterfaceInPackage("123.12.123.122", p));
        assertFalse("Expected 192.168.1.1 to be excluded from the package", newFactory.isInterfaceInPackage("192.168.1.1", p));
    }

    @Test
    public void testIncludeUrl() throws Exception {
        TestPollerConfigManager factory = new TestPollerConfigManager(POLLER_CONFIG);
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
        svc.setInterval(300000l);
        pkg.addService(svc);
        
        Downtime dt = new Downtime();
        dt.setBegin(0l);
        pkg.addDowntime(dt);

        File iplistFile = new File(System.getProperty("java.io.tmpdir"), "iplist.txt");
        FileWriter fw = new FileWriter(iplistFile);
        fw.write("#192.168.1.1");
        fw.write(System.lineSeparator());
        fw.write("123.12.123.121");
        fw.write(System.lineSeparator());
        fw.write("123.12.123.122");
        fw.close();
        pkg.addIncludeUrl("file:" + iplistFile.getAbsolutePath());
        
        factory.addPackage(pkg);
        factory.save();
        
        assertNotNull(factory.getPackage("TestPkg"));
        
        TestPollerConfigManager newFactory = new TestPollerConfigManager(factory.getXml());
        Package p = newFactory.getPackage("TestPkg");
        assertNotNull(p);
        System.out.println(factory.getXml());
        assertTrue("Expect 123.12.123.121 to be part of the package", newFactory.isInterfaceInPackage("123.12.123.121", p));
        assertTrue("Expect 123.12.123.122 to be part of the package", newFactory.isInterfaceInPackage("123.12.123.122", p));
        assertFalse("Expected 192.168.1.1 to be excluded from the package", newFactory.isInterfaceInPackage("192.168.1.1", p));
        iplistFile.delete();
    }
}
