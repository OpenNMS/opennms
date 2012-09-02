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

package org.opennms.netmgt.threshd;

import org.apache.log4j.Level;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.dao.support.NullRrdStrategy;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.threshd.mock.MockThreshdConfigManager;
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

        RrdUtils.setStrategy(new NullRrdStrategy());

        EventIpcManagerFactory.setIpcManager(new MockEventIpcManager());

        String dirName = "target/tmp/192.168.1.1";
        String fileName = "icmp.rrd";
        int nodeId = 1;
        String ipAddress = "192.168.1.1";
        String serviceName = "ICMP";
        String groupName = "icmp-latency";

        setupThresholdConfig(dirName, fileName, nodeId, ipAddress, serviceName, groupName);

        // This call will also ensure that the poll-outages.xml file can parse IPv4
        // and IPv6 addresses.
        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        PollOutagesConfigFactory factory = new PollOutagesConfigFactory(resource);
        factory.afterPropertiesSet();
        PollOutagesConfigFactory.setInstance(factory);
    }

    @Override
    protected void tearDown() throws Exception {
        MockLogAppender.assertNotGreaterOrEqual(Level.ERROR);
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
