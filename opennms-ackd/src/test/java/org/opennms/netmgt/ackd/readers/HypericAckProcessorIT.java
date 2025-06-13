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
package org.opennms.netmgt.ackd.readers;

import static org.mockito.Mockito.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.netmgt.ackd.Ackd;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.ackd.Parameter;
import org.opennms.netmgt.config.ackd.Reader;
import org.opennms.netmgt.dao.api.AckdConfigurationDao;
import org.opennms.netmgt.dao.jaxb.DefaultAckdConfigurationDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Integration test for the Hyperic Acknowledgement Reader Implementation.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-ackd.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class HypericAckProcessorIT implements InitializingBean {

    @Autowired
    private Ackd m_daemon;

    @Autowired
    private HypericAckProcessor m_processor;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    private AckdConfigurationDao createAckdConfigDao() {

        class AckdConfigDao extends DefaultAckdConfigurationDao {

            @Override
            public AckdConfiguration getConfig() {
                AckdConfiguration config = new AckdConfiguration();
                config.setAckExpression("~(?i)^AcK$");
                config.setAlarmidMatchExpression("~(?i).*alarmid:([0-9]+).*");
                config.setAlarmSync(true);
                config.setClearExpression("~(?i)^(Resolve|cleaR)$");
                config.setEscalateExpression("~(?i)^esc$");
                config.setNotifyidMatchExpression("~(?i).*RE:.*Notice #([0-9]+).*");
                config.setUnackExpression("~(?i)^unAck$");

                final List<Reader> readers = new ArrayList<>();
                {
                    Reader reader = new Reader();
                    reader.setEnabled(false);
                    reader.setReaderName("JavaMailReader");

                    Parameter hypericHosts = new Parameter();
                    hypericHosts.setKey("readmail-config");
                    hypericHosts.setValue("localhost");
                    reader.addParameter(hypericHosts);

                    org.opennms.netmgt.config.ackd.ReaderSchedule hypericSchedule = new org.opennms.netmgt.config.ackd.ReaderSchedule();
                    hypericSchedule.setInterval(60);
                    hypericSchedule.setUnit("s");

                    readers.add(reader);
                }

                {
                    Reader reader = new Reader();
                    reader.setEnabled(true);
                    reader.setReaderName(HypericAckProcessor.READER_NAME_HYPERIC);

                    Parameter hypericHosts = new Parameter();
                    hypericHosts.setKey(HypericAckProcessor.PARAMETER_PREFIX_HYPERIC_SOURCE + "HQ-Datacenter");
                    hypericHosts.setValue("http://hqadmin:hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu");
                    reader.addParameter(hypericHosts);

                    hypericHosts = new Parameter();
                    hypericHosts.setKey(HypericAckProcessor.PARAMETER_PREFIX_HYPERIC_SOURCE + "HQ-Corporate-IT");
                    hypericHosts.setValue("http://hqadmin:hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu");
                    reader.addParameter(hypericHosts);

                    org.opennms.netmgt.config.ackd.ReaderSchedule hypericSchedule = new org.opennms.netmgt.config.ackd.ReaderSchedule();
                    hypericSchedule.setInterval(3);
                    hypericSchedule.setUnit("s");
                    reader.setReaderSchedule(hypericSchedule);

                    readers.add(reader);
                }

                config.setReaders(readers);
                return config;
            }

        }

        return new AckdConfigDao();
    }

    @Test
    public void testParseMethods() throws Exception {
        OnmsEvent event = new OnmsEvent();
        event.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event, "platform.id", "10001", "string"),
                new OnmsEventParameter(event, "platform.commentText", "", "string"),
                new OnmsEventParameter(event, "platform.platformType.os", "null", "string"),
                new OnmsEventParameter(event, "platform.platformType.osVersion", "null", "string"),
                new OnmsEventParameter(event, "platform.platformType.arch", "null(", "tring"),
                new OnmsEventParameter(event, "platform.agent.address", "192.0.2.143", "string"),
                new OnmsEventParameter(event, "platform.agent.port", "2144", "string"),
                new OnmsEventParameter(event, "platform.fqdn", "192.0.2.143", "string"),
                new OnmsEventParameter(event, "platform.name", "delta", "string"),
                new OnmsEventParameter(event, "platform.description", "Fedora 12", "string"),
                new OnmsEventParameter(event, "platform.location", "", "string"),
                new OnmsEventParameter(event, "alert.id", "11757", "string"),
                new OnmsEventParameter(event, "alert.fixed", "false", "string"),
                new OnmsEventParameter(event, "alert.ctime", "1267219500000", "string"),
                new OnmsEventParameter(event, "alert.timestamp", "1267219500000", "string"),
                new OnmsEventParameter(event, "alert.ackedBy", "null", "string"),
                new OnmsEventParameter(event, "alert.stateId", "null", "string"),
                new OnmsEventParameter(event, "alert.url", "http://192.168.0.5:7080/alerts/Alerts.do?mode%61viewAlert&eid%611:10001&a%6111757", "string"),
                new OnmsEventParameter(event, "alert.baseURL", "http://192.168.0.5:7080", "string"),
                new OnmsEventParameter(event, "alert.source", "HQ", "string"),
                new OnmsEventParameter(event, "alertDef.id", "10002", "string"),
                new OnmsEventParameter(event, "alertDef.name", "Load Above 2", "string"),
                new OnmsEventParameter(event, "alertDef.description", "", "string"),
                new OnmsEventParameter(event, "alertDef.priority", "2", "string"),
                new OnmsEventParameter(event, "alertDef.appdefType", "1", "string"),
                new OnmsEventParameter(event, "alertDef.appdefId", "10001", "string"),
                new OnmsEventParameter(event, "alertDef.notifyFiltered", "false", "string"),
                new OnmsEventParameter(event, "action.shortReason", "Load Above 2 delta Load Average 5 Minutes (1.4)", "string"),
                new OnmsEventParameter(event, "action.longReason", "If Load Average 5 Minutes > 0.5 (actual value %61 1.4)", "string"),
                new OnmsEventParameter(event, "resource.instanceId", "10001", "string"),
                new OnmsEventParameter(event, "resource.name", "delta", "string"),
                new OnmsEventParameter(event, "resource.url", "http://192.168.0.5:7080/Resource.do?eid%611:10001", "string"),
                new OnmsEventParameter(event, "resource.resourceType.name", "covalentEAMPlatform", "string")));

        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setLastEvent(event);

        assertEquals("Alert source not parsed properly", "HQ", HypericAckProcessor.getAlertSourceParmValue(alarm));
        assertEquals("Alert ID not parsed properly", "11757", HypericAckProcessor.getAlertIdParmValue(alarm));
    }

    @Test
    @JUnitHttpServer(port=7081)
    public void testStartAckd() throws Exception {
        AckdConfigurationDao realDao = createAckdConfigDao();

        AckdConfigurationDao mockDao = mock(AckdConfigurationDao.class);
        when(mockDao.getEnabledReaderCount()).thenReturn(realDao.getEnabledReaderCount());
        when(mockDao.isReaderEnabled("JavaMailReader")).thenReturn(realDao.isReaderEnabled("JavaMailReader"));
        when(mockDao.isReaderEnabled("HypericReader")).thenReturn(realDao.isReaderEnabled("HypericReader"));
        when(mockDao.getReaderSchedule("HypericReader")).thenReturn(realDao.getReaderSchedule("HypericReader"));

        m_daemon.setConfigDao(mockDao);
        m_daemon.start();
        try { Thread.sleep(5000); } catch (InterruptedException e) {}
        m_daemon.destroy();

        verify(mockDao, atLeastOnce()).getEnabledReaderCount();
        verify(mockDao, atLeastOnce()).isReaderEnabled("JavaMailReader");
        verify(mockDao, atLeastOnce()).isReaderEnabled("HypericReader");
        verify(mockDao, atLeastOnce()).getReaderSchedule("HypericReader");
        verifyNoMoreInteractions(mockDao);
    }

    @Test
    public void testFetchUnclearedHypericAlarms() throws Exception {
        List<OnmsAlarm> alarms = m_processor.fetchUnclearedHypericAlarms();
        System.out.println(alarms.size());
    }

    @Test
    @JUnitHttpServer(port=7081,basicAuth=true)
    public void testFetchHypericAlerts() throws Exception {
        // Test reading alerts over the HTTP server
        {
            List<HypericAckProcessor.HypericAlertStatus> alerts = HypericAckProcessor.fetchHypericAlerts("http://hqadmin:hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu", Arrays.asList(new String[] { "1", "2", "3" }));
            assertEquals(5, alerts.size());
            for (HypericAckProcessor.HypericAlertStatus alert : alerts) {
                System.out.println(alert.toString());
            }

            alerts = HypericAckProcessor.fetchHypericAlerts("http://uhohcolons:this%3Apassword%3Ahas%3Acolons@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu", Arrays.asList(new String[] { "1", "2", "3" }));
            assertEquals(5, alerts.size());
        }

        // Try with bad credentials to make sure we get a malformed response
        {
            boolean caughtAuthFailure = false;
            for (String url : new String[] {
                    "http://:badcredentials@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://blankpass@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://blankpass:@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://hqadmin:@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu"
            }) {
                try {
                    HypericAckProcessor.fetchHypericAlerts(url, Arrays.asList(new String[] { "1", "2", "3" }));
                } catch (JAXBException e) {
                    // Expected state
                    caughtAuthFailure = true;
                }
                assertTrue("Did not catch expected authorization failure for URL: " + url, caughtAuthFailure);
            }
        }
    }

    @Test
    public void testParseHypericAlerts() throws Exception {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("hqu/opennms/alertStatus/list.hqu"), StandardCharsets.UTF_8));
        reader.mark(4000);
        try {
            while(true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                } else {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            // End of file
        }
        reader.reset();
        List<HypericAckProcessor.HypericAlertStatus> alerts = HypericAckProcessor.parseHypericAlerts(reader);
        assertEquals(5, alerts.size());
        for (HypericAckProcessor.HypericAlertStatus alert : alerts) {
            System.out.println(alert.toString());
        }
    }
}
