/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@RunWith(MockitoJUnitRunner.class)
public class PollerConfigWithPSMIT {

    @Before
    public void setUp() throws Exception {
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

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testPSM() throws Exception {
        InputStream is = new FileInputStream(new File("src/test/resources/etc/psm-poller-configuration.xml"));
        PollerConfigFactory factory = new PollerConfigFactory(0, is, "localhost", false);
        PollerConfigFactory.setInstance(factory);        
        IOUtils.closeQuietly(is);
        ServiceMonitor monitor = PollerConfigFactory.getInstance().getServiceMonitor("MQ_API_DirectRte_v2");
        Assert.assertNotNull(monitor);
        Package pkg = PollerConfigFactory.getInstance().getPackage("MapQuest");
        Assert.assertNotNull(pkg);
        Service svc = PollerConfigFactory.getInstance().getServiceInPackage("MQ_API_DirectRte_v2", pkg);
        Assert.assertNotNull(svc);
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (Parameter p : svc.getParameters()) {
            parameters.put(p.getKey(), p.getValue() == null ? p.getAnyObject() : p.getValue());
        }
        PollStatus status = monitor.poll(new MockMonitoredService(1, "www.mapquest.com", InetAddress.getByName("www.mapquest.com"), "MQ_API_DirectRte_v2"), parameters);
        Assert.assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }
}
