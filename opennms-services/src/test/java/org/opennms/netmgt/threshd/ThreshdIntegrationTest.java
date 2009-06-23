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
// 2008 Feb 10: Eliminate warnings and move config file into an external resource. - dj@opennms.org
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

import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.threshd.mock.MockThreshdConfigManager;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ThreshdIntegrationTest extends ThresholderTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();

        setupDatabase();

        Resource dbConfig = new ClassPathResource("/org/opennms/netmgt/config/test-database-schema.xml");
        DatabaseSchemaConfigFactory dscf = new DatabaseSchemaConfigFactory(dbConfig.getInputStream());
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
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(resource.getInputStream()));
    }

    @Override
    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }

    public void testThreshd() throws Exception {
        Threshd threshd = new Threshd();
        ThreshdConfigManager config = new MockThreshdConfigManager(ConfigurationTestUtils.getInputStreamForResource(this, "threshd-configuration.xml"), "localhost", false);
        threshd.setThreshdConfig(config);
        threshd.init();
        threshd.start();
        
        Thread.sleep(5000);
        
        threshd.stop();
    }
}
