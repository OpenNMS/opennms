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
package org.opennms.netmgt.snmpinterfacepoller;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.config.SnmpInterfacePollerConfigFactory;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-snmpPollerTest.xml"
})
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class SnmpPollerIT implements TemporaryDatabaseAware<MockDatabase> {

    private MockDatabase m_database;

    private MockNetwork m_network;

    private SnmpInterfacePollerConfig m_pollerConfig;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private PollableNetwork m_pollableNetwork;

    private SnmpPoller m_poller;

    //
    // SetUp and TearDown
    //
    @Before
    public void setUp() throws Exception {

        m_network = new MockNetwork();

        m_network.addNode(1, "node1");
        m_network.addInterface(1, "192.168.1.1")
                        .setIfType(6);
        m_network.addInterface(1, "192.168.1.2")
                        .setIfType(2);
        m_database.populate(m_network);

        InputStream configFile = m_resourceLoader.getResource("classpath:/etc/snmp-interface-poller-configuration.xml").getInputStream();
        m_pollerConfig = new SnmpInterfacePollerConfigFactory(1L, configFile);

        m_poller = new SnmpPoller();
        m_poller.setNetwork(m_pollableNetwork);
        m_poller.setPollerConfig(m_pollerConfig);
        m_poller.onInit();

    }

    @Test
    public void testIgnoredInterfaces() throws Exception {
        SnmpInterfaceDao snmpInterfaceDao = ((DefaultPollContext)m_pollableNetwork.getContext()).getSnmpInterfaceDao();

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            var interfaces = snmpInterfaceDao.findAll();

            OnmsSnmpInterface ethIf = interfaces.stream().filter(snmpIf -> snmpIf.getIfType() == 6).findFirst().orElseThrow();
            OnmsSnmpInterface otherIf = interfaces.stream().filter(snmpIf -> snmpIf.getIfType() == 2).findFirst().orElseThrow();

            assertEquals(ethIf.getPoll(), "P");
            assertEquals(otherIf.getPoll(), "I");
        });

    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_database = database;
    }
}
