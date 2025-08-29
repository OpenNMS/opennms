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
package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.config.syslogd.ProcessMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.codahale.metrics.MetricRegistry;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/mockSinkConsumerManager.xml",
        "classpath:/META-INF/opennms/mockMessageDispatcherFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-syslogDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-dns.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
@Transactional
public class SyslogReloadDaemonIT implements InitializingBean {
    
    private SyslogdConfigFactory m_config;

    @Autowired
    private Syslogd m_syslogd;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private MockMessageDispatcherFactory<SyslogConnection, SyslogMessageLogDTO> m_messageDispatcherFactory;

    @Autowired
    private LocationAwareDnsLookupClient locationAwareDnsLookupClient;

    private SyslogSinkConsumer m_syslogSinkConsumer;

    private SyslogSinkModule m_syslogSinkModule;
    
    private SyslogReceiverCamelNettyImpl m_receiver;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        MockLogAppender.resetState();

        File opennmsHome = Paths.get("src", "test", "resources", "opennms-home").toFile();
        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());
        m_config = new SyslogdConfigFactory();

        // Verify that the test syslogd-configuration.xml file was loaded
        boolean foundMalt = false;
        assertEquals(10514, m_config.getSyslogPort());
        for (final UeiMatch match : m_config.getUeiList()) {
            if (match.getProcessMatch().isPresent()) {
                final ProcessMatch processMatch = match.getProcessMatch().get();
                if (!foundMalt && "maltd".equals(processMatch.getExpression())) {
                    foundMalt = true;
                }
            }
        }
        assertTrue(foundMalt);

        m_syslogSinkConsumer = new SyslogSinkConsumer(new MetricRegistry());
        m_syslogSinkConsumer.setDistPollerDao(m_distPollerDao);
        m_syslogSinkConsumer.setSyslogdConfig(m_config);
        m_syslogSinkConsumer.setEventForwarder(m_eventIpcManager);
        m_syslogSinkConsumer.setLocationAwareDnsLookupClient(locationAwareDnsLookupClient);
        m_syslogSinkModule = m_syslogSinkConsumer.getModule();
        m_messageDispatcherFactory.setConsumer(m_syslogSinkConsumer);

        m_receiver = new SyslogReceiverCamelNettyImpl(m_config);
        m_receiver.setDistPollerDao(m_distPollerDao);
        m_receiver.setMessageDispatcherFactory(m_messageDispatcherFactory);
        m_syslogd.setSyslogReceiver(m_receiver);
        m_syslogd.init();
        SyslogdTestUtils.startSyslogdGracefully(m_syslogd);
    }

    @After
    public void tearDown() throws Exception {
        m_syslogd.stop();
        m_eventIpcManager.reset();
        MockLogAppender.assertNoErrorOrGreater();
    }
    
    @Test
    public void testReloadForPortAndFacilitySeverityProcess() throws Exception {
        // change configuration file location.
        File opennmsHome = Paths.get("src", "test", "resources", "opennms-home-reload").toFile();
        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());
        EventBuilder eventBuilder = new EventBuilder("uei.opennms.org/internal/reloadDaemonConfig", "syslog-test");
        eventBuilder.addParam("daemonName", "Syslogd");
        m_syslogd.handleReloadEvent(ImmutableMapper.fromMutableEvent(eventBuilder.getEvent()));
        SyslogdTestUtils.waitForSyslogdToReload();
        // test new port change in config 
        assertEquals(10515, m_config.getSyslogPort());
        MockLogAppender.setupLogging(true, "TRACE");
        // Match changes in configuration file
        final String testPDU = "2018-01-01 127.0.0.1 reload: check reload severity process match";
        final String testUEI = "uei.opennms.org/tests/syslogd/nonMessageMatch/facilitySeverityProcess";
        final String testMsg = "check reload severity process match";
    
        final EventBuilder expectedEventBldr = new EventBuilder(testUEI, "syslogd");
        expectedEventBldr.setInterface(InetAddressUtils.ONE_TWENTY_SEVEN);
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(testMsg);
        
        expectedEventBldr.addParam("process", "reload");
        expectedEventBldr.addParam("service", "local1");
        expectedEventBldr.addParam("severity", "Warning");
    
        m_eventIpcManager.getEventAnticipator().anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient s = new SyslogClient("coco", 10, SyslogClient.LOG_LOCAL1, InetAddressUtils.ONE_TWENTY_SEVEN);
        s.setSyslogPort(m_config.getSyslogPort());
        s.syslog(SyslogClient.LOG_WARNING, testPDU);
    
        m_eventIpcManager.getEventAnticipator().verifyAnticipated(5000, 0, 0, 0, 1);
    }

}
