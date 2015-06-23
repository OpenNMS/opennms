/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgmt.alarmd.northbounder.jms;

import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsDestination;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounder;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfigDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the JMS North Bound Interface
 * 
 * @author <a href="mailto:dschlenk@converge-one.com">David Schlenk</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/test-context.xml")
public class JmsNorthBounderTest {
    private JmsTemplate m_template;
    
    @Autowired
    private ConnectionFactory m_jmsNorthbounderConnectionFactory;
    
    @Before
    public void startBroker() throws InterruptedException {
        MockLogAppender.setupLogging();
        // this spawns an embedded broker
        m_template = new JmsTemplate(m_jmsNorthbounderConnectionFactory);
        m_template.setReceiveTimeout(100L);
    }

    @After
    public void assertLogs() throws InterruptedException {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * This tests forwarding of 7 alarms, one for each OpenNMS severity to
     * verify the LOG_LEVEL agrees with the Severity based on our algorithm.
     * 
     * @throws Exception
     */
    @Test
    public void testForwardAlarms() throws Exception {

        String xml = generateConfigXml();

        Resource resource = new ByteArrayResource(xml.getBytes());

        JmsNorthbounderConfigDao dao = new JmsNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        JmsNorthbounderConfig config = dao.getConfig();

        List<JmsDestination> destinations = config.getDestinations();

        List<JmsNorthbounder> nbis = new LinkedList<JmsNorthbounder>();

        for (JmsDestination syslogDestination : destinations) {
            JmsNorthbounder nbi = new JmsNorthbounder(
                                                      config,
                                                      m_jmsNorthbounderConnectionFactory,
                                                      syslogDestination);
            nbi.setNodeDao(new TestNodeDao());
            nbi.afterPropertiesSet();
            nbis.add(nbi);
        }

        int j = 7;
        List<NorthboundAlarm> alarms = new LinkedList<NorthboundAlarm>();

        OnmsDistPoller distpoller = new OnmsDistPoller("barbrady",
                                                       "192.0.2.11");
        OnmsNode node = new OnmsNode(distpoller, TestNodeDao.NODE_LABEL);
        node.setForeignSource("TestGroup");
        node.setForeignId("1");
        node.setId(TestNodeDao.NODE_ID);

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        snmpInterface.setId(1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");

        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<OnmsIpInterface>(
                                                                               j);
        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(1);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName(TestNodeDao.NODE_LABEL);
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);

        ipInterfaces.add(onmsIf);

        node.setIpInterfaces(ipInterfaces);
        // TX via NBIs
        for (JmsNorthbounder nbi : nbis) {

            for (int i = 1; i <= j; ++i) {
                OnmsAlarm onmsAlarm = new OnmsAlarm();
                onmsAlarm.setId(i);
                onmsAlarm.setUei("uei.opennms.org/test/syslogNorthBounder");
                onmsAlarm.setNode(node);
                onmsAlarm.setSeverityId(i);
                onmsAlarm.setIpAddr(InetAddress.getByName("127.0.0.1"));
                onmsAlarm.setCounter(i);
                onmsAlarm.setLogMsg("Node Down");
                onmsAlarm.setX733AlarmType(NorthboundAlarm.x733AlarmType.get(i).name());
                onmsAlarm.setX733ProbableCause(NorthboundAlarm.x733ProbableCause.get(i).getId());
                if (i < j) { // Do not add parameters to the last alarm for
                             // testing NMS-6383
                    String eventparms = "foreignSource=fabric(string,text);foreignId=space-0256012012000038(string,text);reason=Aborting node scan : Agent timed out while scanning the system table(string,text);"
                            + ".1.3.6.1.4.1.2636.3.18.1.7.1.2.732=207795895(TimeTicks,text)";
                    onmsAlarm.setEventParms(eventparms);
                }
                NorthboundAlarm a = new NorthboundAlarm(onmsAlarm);

                Assert.assertFalse(nbi.accepts(a));
                onmsAlarm.setUei("uei.opennms.org/nodes/nodeDown");
                a = new NorthboundAlarm(onmsAlarm);
                Assert.assertTrue(nbi.accepts(a));

                alarms.add(a);
            }
            nbi.forwardAlarms(alarms);
        }

        Thread.sleep(100);

        // Let's become a consumer and receive the messages!
        List<String> messages = new LinkedList<String>();
        Message m = m_template.receive("OpenNMSAlarmQueue");
        while (m != null) {
            Assert.assertTrue(m instanceof TextMessage);
            messages.add(((TextMessage) m).getText());
            m = m_template.receive("OpenNMSAlarmQueue");
        }

        Assert.assertTrue("Log messages sent: 7, Log messages received: "
                + messages.size(), 7 == messages.size());

        for (String message : messages) {
            System.out.println(message);
        }

        int i = 0;
        for (String message : messages) {
            Assert.assertTrue("ALARM ID:" +(i+1), message.contains("ALARM ID:" + (i+1) + " "));
            Assert.assertTrue(message.contains("NODE:"
                    + TestNodeDao.NODE_LABEL));
            i++;
        }
    }

    private String generateConfigXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<jms-northbounder-config>\n"
                + "  <enabled>true</enabled>\n"
                + "  <nagles-delay>1000</nagles-delay>\n"
                + "  <batch-size>30</batch-size>\n"
                + "  <queue-size>30000</queue-size>\n"
                + "  <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}; PARM-1-NAME: ${parm[name-#1]} PARM-1:${parm[#1]} PARM-2-NAME: ${parm[name-#2]} "
                + "PARM-3-NAME: ${parm[name-#3]} PARM-foreignSource:${parm[foreignSource]} PARM-4-NAME: ${parm[name-#4]} PARM-4: ${parm[#4]} ${logMsg}</message-format>\n"
                + "  <destination>\n"
                + "    <jms-destination>OpenNMSAlarmQueue</jms-destination>\n"
                + "    <send-as-object-message>false</send-as-object-message>\n"
                + "    <first-occurence-only>false</first-occurence-only>"
                + "   </destination>\n"
                + "   <uei>uei.opennms.org/nodes/nodeDown</uei>\n"
                + "   <uei>uei.opennms.org/nodes/nodeUp</uei>\n"
                + "</jms-northbounder-config>\n" + "";
    }
}