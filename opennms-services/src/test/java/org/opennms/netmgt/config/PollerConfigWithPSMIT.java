/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.mockito.junit.MockitoJUnitRunner;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

@RunWith(MockitoJUnitRunner.class)
public class PollerConfigWithPSMIT {

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        DatabaseSchemaConfigFactory.init();

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
    @org.junit.Ignore("Accesses external Mapquest site that no longer works")
    public void testPSM() throws Exception {
        InputStream is = new FileInputStream(new File("src/test/resources/etc/psm-poller-configuration.xml"));
        PollerConfigFactory factory = new PollerConfigFactory(0, is);
        PollerConfigFactory.setInstance(factory);        
        IOUtils.closeQuietly(is);
        ServiceMonitor monitor = PollerConfigFactory.getInstance().getServiceMonitorLocator("MQ_API_DirectRte_v2").orElseThrow()
                                                    .getServiceMonitor(factory.getServiceMonitorRegistry());
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
