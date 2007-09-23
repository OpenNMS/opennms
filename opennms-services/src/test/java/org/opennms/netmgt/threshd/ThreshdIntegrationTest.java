//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 24: Use RrdTestUtils.initializeNullStrategy instead of
//              poking at RrdConfig directly. - dj@opennms.org
// 2007 Jan 29: Modify to work with TestCase changes; rename to show that it's an integration test. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.threshd;

import java.io.InputStreamReader;
import java.util.Properties;

import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.threshd.mock.MockThreshdConfigManager;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ThreshdIntegrationTest extends ThresholderTestCase {
    
    public static final String THRESHD_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<?castor class-name=\"org.opennms.netmgt.threshd.ThreshdConfiguration\"?>\n" + 
            "<threshd-configuration \n" + 
            "   threads=\"5\">\n" + 
            "   \n" + 
            "   <package name=\"example1\">\n" + 
            "       <filter>IPADDR IPLIKE *.*.*.*</filter>   \n" + 
            "       <specific>0.0.0.0</specific>\n" + 
//            "       <include-range begin=\"192.168.1.1\" end=\"192.168.1.254\"/>\n" + 
            "       \n" + 
            "       <service name=\"SNMP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "           <parameter key=\"thresholding-group\" value=\"default-snmp\"/>\n" + 
            "       </service>\n" + 
            "       \n" + 
            "        <service name=\"ICMP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"icmp-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"HTTP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"http-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"HTTP-8000\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"http-8000-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"HTTP-8080\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"http-8080-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"DNS\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"dns-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"DHCP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"dhcp-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "       <outage-calendar>zzz from poll-outages.xml zzz</outage-calendar>\n" + 
            "   </package>\n" + 
            "   \n" + 
            "   <thresholder service=\"SNMP\"   class-name=\"org.opennms.netmgt.threshd.SnmpThresholder\"/>\n" + 
            "   <thresholder service=\"ICMP\"     class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"HTTP\"     class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"HTTP-8000\"        class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"HTTP-8080\"        class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"DNS\"      class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"DHCP\"     class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "</threshd-configuration>\n";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging();
        
		setupDatabase();
		
        Resource dbConfig = new ClassPathResource("/org/opennms/netmgt/config/test-database-schema.xml");
        InputStreamReader r = new InputStreamReader(dbConfig.getInputStream());
        DatabaseSchemaConfigFactory dscf = new DatabaseSchemaConfigFactory(r);
        r.close();
        DatabaseSchemaConfigFactory.setInstance(dscf);
        
        RrdTestUtils.initializeNullStrategy();
        
        EventIpcManagerFactory.setIpcManager(new MockEventIpcManager());
		
        String dirName = "target/tmp/192.168.1.1";
        String fileName = "icmp.rrd";
        int nodeId = 1;
        String ipAddress = "192.168.1.1";
        String serviceName = "ICMP";
        String groupName = "icmp-latency";
		
		setupThresholdConfig(dirName, fileName, nodeId, ipAddress, serviceName, groupName);
        
		Resource resource = new ClassPathResource("etc/poll-outages.xml");
		InputStreamReader pollOutagesRdr = new InputStreamReader(resource.getInputStream());
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(pollOutagesRdr));
        pollOutagesRdr.close();
        
    }

    @Override
    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }

    public void testThreshd() throws Exception {
        Threshd threshd = new Threshd();
        ThreshdConfigManager config = new MockThreshdConfigManager(THRESHD_CONFIG, "localhost", false);
        threshd.setThreshdConfig(config);
        threshd.init();
        threshd.start();
        
        Thread.sleep(10000);
        
        threshd.stop();
    }
}
