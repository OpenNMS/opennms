/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ackd.readers;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
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
import org.opennms.netmgt.config.ackd.Readers;
import org.opennms.netmgt.dao.api.AckdConfigurationDao;
import org.opennms.netmgt.dao.jaxb.DefaultAckdConfigurationDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class HypericAckProcessorTest implements InitializingBean {

    @Autowired
    private Ackd m_daemon;

    @Autowired
    private HypericAckProcessor m_processor;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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

                Readers readers = new Readers();
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

                    readers.addReader(reader);
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

                    readers.addReader(reader);
                }

                config.setReaders(readers);
                return config;
            }

        }

        return new AckdConfigDao();
    }

    @Test
    public void testParseMethods() throws Exception {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setEventParms("platform.id=10001(string,text);platform.commentText=(string,text);platform.platformType.os=null(string,text);platform.platformType.osVersion=null(string,text);platform.platformType.arch=null(string,text);platform.agent.address=172.20.1.143(string,text);platform.agent.port=2144(string,text);platform.fqdn=172.20.1.143(string,text);platform.name=delta(string,text);platform.description=Fedora 12(string,text);platform.location=(string,text);alert.id=11757(string,text);alert.fixed=false(string,text);alert.ctime=1267219500000(string,text);alert.timestamp=1267219500000(string,text);alert.ackedBy=null(string,text);alert.stateId=null(string,text);alert.url=http://192.168.0.5:7080/alerts/Alerts.do?mode%61viewAlert&eid%611:10001&a%6111757(string,text);alert.baseURL=http://192.168.0.5:7080(string,text);alert.source=HQ(string,text);alertDef.id=10002(string,text);alertDef.name=Load Above 2(string,text);alertDef.description=(string,text);alertDef.priority=2(string,text);alertDef.appdefType=1(string,text);alertDef.appdefId=10001(string,text);alertDef.notifyFiltered=false(string,text);action.shortReason=Load Above 2 delta Load Average 5 Minutes (1.4)(string,text);action.longReason=If Load Average 5 Minutes > 0.5 (actual value %61 1.4)(string,text);resource.instanceId=10001(string,text);resource.name=delta(string,text);resource.url=http://192.168.0.5:7080/Resource.do?eid%611:10001(string,text);resource.resourceType.name=covalentEAMPlatform(string,text)");
        /*
        OnmsAlarm alarm = createMock(OnmsAlarm.class);
        expect(alarm.getEventParms()).andReturn(
                "platform.id=10001(string,text);platform.commentText=(string,text);platform.platformType.os=null(string,text);platform.platformType.osVersion=null(string,text);platform.platformType.arch=null(string,text);platform.agent.address=172.20.1.143(string,text);platform.agent.port=2144(string,text);platform.fqdn=172.20.1.143(string,text);platform.name=delta(string,text);platform.description=Fedora 12(string,text);platform.location=(string,text);alert.id=11757(string,text);alert.fixed=false(string,text);alert.ctime=1267219500000(string,text);alert.timestamp=1267219500000(string,text);alert.ackedBy=null(string,text);alert.stateId=null(string,text);alert.url=http://192.168.0.5:7080/alerts/Alerts.do?mode%61viewAlert&eid%611:10001&a%6111757(string,text);alert.baseURL=http://192.168.0.5:7080(string,text);alert.source=HQ(string,text);alertDef.id=10002(string,text);alertDef.name=Load Above 2(string,text);alertDef.description=(string,text);alertDef.priority=2(string,text);alertDef.appdefType=1(string,text);alertDef.appdefId=10001(string,text);alertDef.notifyFiltered=false(string,text);action.shortReason=Load Above 2 delta Load Average 5 Minutes (1.4)(string,text);action.longReason=If Load Average 5 Minutes > 0.5 (actual value %61 1.4)(string,text);resource.instanceId=10001(string,text);resource.name=delta(string,text);resource.url=http://192.168.0.5:7080/Resource.do?eid%611:10001(string,text);resource.resourceType.name=covalentEAMPlatform(string,text)"
        ).times(2);
        replay(alarm);
         */

        assertEquals("Alert source not parsed properly", "HQ", HypericAckProcessor.getAlertSourceParmValue(alarm));
        assertEquals("Alert ID not parsed properly", "11757", HypericAckProcessor.getAlertIdParmValue(alarm));
    }

    @Test
    @JUnitHttpServer(port=7081)
    public void testStartAckd() throws Exception {
        AckdConfigurationDao realDao = createAckdConfigDao();

        AckdConfigurationDao mockDao = createMock(AckdConfigurationDao.class);
        expect(mockDao.getEnabledReaderCount()).andDelegateTo(realDao);
        expect(mockDao.isReaderEnabled("JavaMailReader")).andDelegateTo(realDao).times(2);
        expect(mockDao.isReaderEnabled("HypericReader")).andDelegateTo(realDao).times(2);
        expect(mockDao.getReaderSchedule("HypericReader")).andDelegateTo(realDao).times(2);
        replay(mockDao);

        m_daemon.setConfigDao(mockDao);
        m_daemon.start();
        try { Thread.sleep(5000); } catch (InterruptedException e) {}
        m_daemon.destroy();
        verify(mockDao);
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
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("hqu/opennms/alertStatus/list.hqu"), "UTF-8"));
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
