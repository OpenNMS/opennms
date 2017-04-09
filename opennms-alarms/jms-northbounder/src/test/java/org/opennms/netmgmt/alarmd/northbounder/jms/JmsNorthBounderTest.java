/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.ObjectMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsDestination;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounder;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfigDao;
import org.opennms.netmgt.dao.mock.MockMonitoringLocationDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the JMS North Bound Interface.
 *
 * @author <a href="mailto:dschlenk@converge-one.com">David Schlenk</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
        "classpath:/test-context.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
public class JmsNorthBounderTest {
    
    /** The Constant NODE_LABEL. */
    private static final String NODE_LABEL = "schlazor";
    
    /** The JMS template. */
    private JmsTemplate m_template;
    
    /** The JMS northbounder connection factory. */
    @Autowired
    private ConnectionFactory m_jmsNorthbounderConnectionFactory;
    
    /** The Node DAO. */
    @Autowired
    private MockMonitoringLocationDao m_locationDao;
    
    /** The Node DAO. */
    @Autowired
    private MockNodeDao m_nodeDao;
    
    /**
     * Start broker.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Before
    public void startBroker() throws InterruptedException {
        MockLogAppender.setupLogging();
        // this spawns an embedded broker
        m_template = new JmsTemplate(m_jmsNorthbounderConnectionFactory);
        m_template.setReceiveTimeout(100L);
    }

    /**
     * Assert logs.
     *
     * @throws InterruptedException the interrupted exception
     */
    @After
    public void assertLogs() throws InterruptedException {
        //MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * This tests forwarding of 7 alarms, one for each OpenNMS severity to
     * verify the LOG_LEVEL agrees with the Severity based on our algorithm.
     *
     * @throws Exception the exception
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

        for (JmsDestination jmsDestination : destinations) {
            JmsNorthbounder nbi = new JmsNorthbounder(config, m_jmsNorthbounderConnectionFactory, jmsDestination);
            nbi.afterPropertiesSet();
            nbis.add(nbi);
        }

        int j = 7;
        List<NorthboundAlarm> alarms = new LinkedList<NorthboundAlarm>();

        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), NODE_LABEL);
        node.setForeignSource("TestGroup");
        node.setForeignId("1");
        node.setId(m_nodeDao.getNextNodeId());

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
        onmsIf.setIpHostName(NODE_LABEL);
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);

        ipInterfaces.add(onmsIf);

        node.setIpInterfaces(ipInterfaces);
        m_nodeDao.save(node);
        m_nodeDao.flush();
        // TX via NBIs
        for (JmsNorthbounder nbi : nbis) {
            for (int i = 1; i <= j; ++i) {
                OnmsAlarm onmsAlarm = new OnmsAlarm();
                onmsAlarm.setId(i);
                onmsAlarm.setUei("uei.opennms.org/test/jmsNorthBounder");
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

        Assert.assertTrue("Log messages sent: 7, Log messages received: " + messages.size(), 7 == messages.size());

        for (String message : messages) {
            System.out.println(message);
        }

        int i = 0;
        for (String message : messages) {
            Assert.assertTrue("ALARM ID:" +(i+1), message.contains("ALARM ID:" + (i+1) + " "));
            Assert.assertTrue(message.contains("NODE:" + NODE_LABEL));
            i++;
        }
    }
    @Test
    public void testAlarmMappingNoParams() throws Exception {
        String xml = generateMappingConfigXml();

        Resource resource = new ByteArrayResource(xml.getBytes());

        JmsNorthbounderConfigDao dao = new JmsNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        JmsNorthbounderConfig config = dao.getConfig();

        List<JmsDestination> destinations = config.getDestinations();

        List<JmsNorthbounder> nbis = new LinkedList<JmsNorthbounder>();

        for (JmsDestination jmsDestination : destinations) {
            JmsNorthbounder nbi = new JmsNorthbounder(
                                                      config,
                                                      m_jmsNorthbounderConnectionFactory,
                                                      jmsDestination);
            nbi.afterPropertiesSet();
            nbis.add(nbi);
        }

        List<NorthboundAlarm> alarms = new LinkedList<NorthboundAlarm>();
        OnmsNode node = new OnmsNode(null, NODE_LABEL);
        node.setForeignSource("TestGroup");
        node.setForeignId("2");
        node.setId(m_nodeDao.getNextNodeId());
        OnmsIpInterface ip = new OnmsIpInterface("127.0.0.1", node);
        InetAddress ia = null;
        try {
            ia = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e){ }
        m_nodeDao.save(node);
        m_nodeDao.flush();
        // TX via NBIs
        for (JmsNorthbounder nbi : nbis) {
            OnmsEvent event = new OnmsEvent(5, "uei.uei.org/uei", new Date(),
                "eventhost", "eventsource", ia,
                null, "eventssnmphost", null,
                "eventsnmp", null, new Date(),
                "eventdescr", "eventloggroup", "eventlogmsg",
                4, null, null,
                0, "operinstruct",
                null, null,
                null, null,
                "tticketid", 1,
                null, null, null,
                null, null, null,
                null, node,
                null, null,
                null);
            OnmsAlarm alarm = new OnmsAlarm(9, event.getEventUei(), null, 1, 4, new Date(), event);
            alarm.setNode(node);
            alarm.setDescription(event.getEventDescr());
            alarm.setApplicationDN("applicationDN");
            alarm.setLogMsg(event.getEventLogMsg());
            alarm.setManagedObjectInstance("managedObjectInstance");
            alarm.setManagedObjectType("managedObjectType");
            alarm.setOssPrimaryKey("ossPrimaryKey");
            alarm.setQosAlarmState("qosAlarmState");
            alarm.setTTicketId("tticketId");
            alarm.setReductionKey("reductionKey");
            alarm.setClearKey("clearKey");
            alarm.setOperInstruct("operInstruct");
            alarm.setAlarmType(1);
            alarm.setFirstEventTime(new Date(0));
            alarm.setIpAddr(ia);
            alarm.setX733AlarmType(NorthboundAlarm.x733AlarmType.get(1).name());
            alarm.setX733ProbableCause(NorthboundAlarm.x733ProbableCause.get(1).getId());
            NorthboundAlarm a = new NorthboundAlarm(alarm);
            alarms.add(a);
            nbi.forwardAlarms(alarms);
        }

        Thread.sleep(100);

        // Let's become a consumer and receive the message
        Message m = m_template.receive("MappingTestQueue");
        String escapedResponse = "ackUser:  appDn: applicationDN logMsg: eventlogmsg objectInstance: managedObjectInstance objectType: managedObjectType ossKey: ossPrimaryKey\n" +
                " ossState: qosAlarmState ticketId: tticketId alarmUei: uei.uei.org/uei alarmKey: reductionKey clearKey: clearKey description: eventdescr operInstruct: operInstruct ackTime: \n" +
                " alarmType: PROBLEM count: 1 alarmId: 9 ipAddr: 127.0.0.1 lastOccurrence:  nodeId: 1\n" +
                " nodeLabel: schlazor distPoller: 00000000-0000-0000-0000-000000000000 ifService:  severity: WARNING ticketState:  x733AlarmType: other\n"+
                " x733ProbableCause: other firstOccurrence: " + StringUtils.iso8601LocalOffsetString(new Date(0)) + " lastOccurrence  eventParmsXml: <eventParms/>";
        String response = ((TextMessage)m).getText();
        Assert.assertEquals("Contents of message\n'" + response + "'\n not equals\n'" + escapedResponse+"'.", response, escapedResponse);
    }
    @Test
    public void testObjectMessage() throws Exception {
        String xml = objectMessageConfigXml();

        Resource resource = new ByteArrayResource(xml.getBytes());

        JmsNorthbounderConfigDao dao = new JmsNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        JmsNorthbounderConfig config = dao.getConfig();

        List<JmsDestination> destinations = config.getDestinations();

        List<JmsNorthbounder> nbis = new LinkedList<JmsNorthbounder>();

        for (JmsDestination jmsDestination : destinations) {
            JmsNorthbounder nbi = new JmsNorthbounder(
                                                      config,
                                                      m_jmsNorthbounderConnectionFactory,
                                                      jmsDestination);
            //nbi.setNodeDao(m_nodeDao);
            nbi.afterPropertiesSet();
            nbis.add(nbi);
        }

        List<NorthboundAlarm> alarms = new LinkedList<NorthboundAlarm>();
        OnmsNode node = new OnmsNode(null, NODE_LABEL);
        node.setForeignSource("TestGroup");
        node.setForeignId("2");
        node.setId(m_nodeDao.getNextNodeId());
        OnmsIpInterface ip = new OnmsIpInterface("127.0.0.1", node);
        InetAddress ia = null;
        try {
            ia = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e){ }
        m_nodeDao.save(node);
        m_nodeDao.flush();
        // TX via NBIs
        for (JmsNorthbounder nbi : nbis) {
            String eventparms = "syslogmessage=Dec 22 2015 20:12:57.1 UTC :  %UC_CTI-3-CtiProviderOpenFailure: %[CTIconnectionId%61232238][ Login User Id%61pguser][Reason code.%61-1932787616][UNKNOWN_PARAMNAME:IPAddress%61172.17.12.73][UNKNOWN_PARAMNAME:IPv6Address%61][App ID%61Cisco CTIManager][Cluster ID%61SplkCluster][Node ID%61splkcucm6p]: CTI application failed to open provider%59 application startup failed(string,text);severity=Error(string,text);timestamp=Dec 22 14:13:21(string,text);process=229250(string,text);service=local7(string,text)";
            OnmsEvent event = new OnmsEvent(5, "uei.uei.org/uei", new Date(),
                "eventhost", "eventsource", ia,
                null, "eventssnmphost", null,
                "eventsnmp", eventparms, new Date(),
                "eventdescr", "eventloggroup", "eventlogmsg",
                4, null, null,
                0, "operinstruct",
                null, null,
                null, null,
                "tticketid", 1,
                null, null, null,
                null, null, null,
                null, node,
                null, null,
                null);
            OnmsAlarm alarm = new OnmsAlarm(9, event.getEventUei(), null, 1, 4, new Date(), event);
            alarm.setNode(node);
            alarm.setDescription(event.getEventDescr());
            alarm.setApplicationDN("applicationDN");
            alarm.setLogMsg(event.getEventLogMsg());
            alarm.setManagedObjectInstance("managedObjectInstance");
            alarm.setManagedObjectType("managedObjectType");
            alarm.setOssPrimaryKey("ossPrimaryKey");
            alarm.setQosAlarmState("qosAlarmState");
            alarm.setTTicketId("tticketId");
            alarm.setReductionKey("reductionKey");
            alarm.setClearKey("clearKey");
            alarm.setOperInstruct("operInstruct");
            alarm.setFirstEventTime(new Date(0));
            alarm.setAlarmType(1);
            alarm.setIpAddr(ia);
            alarm.setEventParms(eventparms);
            alarm.setX733AlarmType(NorthboundAlarm.x733AlarmType.get(1).name());
            alarm.setX733ProbableCause(NorthboundAlarm.x733ProbableCause.get(1).getId());
            NorthboundAlarm a = new NorthboundAlarm(alarm);
            alarms.add(a);
            nbi.forwardAlarms(alarms);
        }

        Thread.sleep(100);

        // Let's become a consumer and receive the message
        Message m = m_template.receive("ObjectTestQueue");
        Assert.assertNotNull(m);
        Object response = ((ObjectMessage)m).getObject();
        Assert.assertNotNull(response);
        Assert.assertTrue("message\n'" + response + "'\n not a NorthboundAlarm\n'" + "'.", (response instanceof NorthboundAlarm));
        String rk = ((NorthboundAlarm) response).getAlarmKey();
        Assert.assertEquals("received alarm has incorrect reduction key: " + rk, "reductionKey", rk);
    }

    @Test
    public void testAlarmMappings() throws Exception {
        String xml = generateMappingConfigXml();

        Resource resource = new ByteArrayResource(xml.getBytes());

        JmsNorthbounderConfigDao dao = new JmsNorthbounderConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();

        JmsNorthbounderConfig config = dao.getConfig();

        List<JmsDestination> destinations = config.getDestinations();

        List<JmsNorthbounder> nbis = new LinkedList<JmsNorthbounder>();

        for (JmsDestination jmsDestination : destinations) {
            JmsNorthbounder nbi = new JmsNorthbounder(
                                                      config,
                                                      m_jmsNorthbounderConnectionFactory,
                                                      jmsDestination);
            nbi.afterPropertiesSet();
            nbis.add(nbi);
        }

        List<NorthboundAlarm> alarms = new LinkedList<NorthboundAlarm>();
        OnmsNode node = new OnmsNode(null, NODE_LABEL);
        node.setForeignSource("TestGroup");
        node.setForeignId("2");
        node.setId(m_nodeDao.getNextNodeId());
        OnmsIpInterface ip = new OnmsIpInterface("127.0.0.1", node);
        InetAddress ia = null;
        try {
            ia = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e){ }
        m_nodeDao.save(node);
        m_nodeDao.flush();
        // TX via NBIs
        for (JmsNorthbounder nbi : nbis) {
            String eventparms = "syslogmessage=Dec 22 2015 20:12:57.1 UTC :  %UC_CTI-3-CtiProviderOpenFailure: %[CTIconnectionId%61232238][ Login User Id%61pguser][Reason code.%61-1932787616][UNKNOWN_PARAMNAME:IPAddress%61172.17.12.73][UNKNOWN_PARAMNAME:IPv6Address%61][App ID%61Cisco CTIManager][Cluster ID%61SplkCluster][Node ID%61splkcucm6p]: CTI application failed to open provider%59 application startup failed(string,text);severity=Error(string,text);timestamp=Dec 22 14:13:21(string,text);process=229250(string,text);service=local7(string,text)";
            OnmsEvent event = new OnmsEvent(5, "uei.uei.org/uei", new Date(),
                "eventhost", "eventsource", ia,
                null, "eventssnmphost", null,
                "eventsnmp", eventparms, new Date(),
                "eventdescr", "eventloggroup", "eventlogmsg",
                4, null, null,
                0, "operinstruct",
                null, null,
                null, null,
                "tticketid", 1,
                null, null, null,
                null, null, null,
                null, node,
                null, null,
                null);
            OnmsAlarm alarm = new OnmsAlarm(9, event.getEventUei(), null, 1, 4, new Date(), event);
            alarm.setNode(node);
            alarm.setDescription(event.getEventDescr());
            alarm.setApplicationDN("applicationDN");
            alarm.setLogMsg(event.getEventLogMsg());
            alarm.setManagedObjectInstance("managedObjectInstance");
            alarm.setManagedObjectType("managedObjectType");
            alarm.setOssPrimaryKey("ossPrimaryKey");
            alarm.setQosAlarmState("qosAlarmState");
            alarm.setTTicketId("tticketId");
            alarm.setReductionKey("reductionKey");
            alarm.setClearKey("clearKey");
            alarm.setOperInstruct("operInstruct");
            alarm.setAlarmType(1);
            alarm.setFirstEventTime(new Date(0));
            alarm.setIpAddr(ia);
            alarm.setEventParms(eventparms);
            alarm.setX733AlarmType(NorthboundAlarm.x733AlarmType.get(1).name());
            alarm.setX733ProbableCause(NorthboundAlarm.x733ProbableCause.get(1).getId());
            NorthboundAlarm a = new NorthboundAlarm(alarm);
            alarm.setCounter(2);
            NorthboundAlarm b = new NorthboundAlarm(alarm);
            alarms.add(a);
            alarms.add(b);
            nbi.forwardAlarms(alarms);
        }

        Thread.sleep(100);

        // Let's become a consumer and receive the message
        Message m = m_template.receive("MappingTestQueue");
        String escapedResponse = "ackUser:  appDn: applicationDN logMsg: eventlogmsg objectInstance: managedObjectInstance objectType: managedObjectType ossKey: ossPrimaryKey\n" +
                " ossState: qosAlarmState ticketId: tticketId alarmUei: uei.uei.org/uei alarmKey: reductionKey clearKey: clearKey description: eventdescr operInstruct: operInstruct ackTime: \n" +
                " alarmType: PROBLEM count: 1 alarmId: 9 ipAddr: 127.0.0.1 lastOccurrence:  nodeId: 1\n" +
                " nodeLabel: schlazor distPoller: 00000000-0000-0000-0000-000000000000 ifService:  severity: WARNING ticketState:  x733AlarmType: other\n"+
                " x733ProbableCause: other firstOccurrence: " + StringUtils.iso8601LocalOffsetString(new Date(0)) + " lastOccurrence  eventParmsXml: <eventParms>\n" +
                "    <parm name=\"syslogmessage\" value=\"Dec 22 2015 20:12:57.1 UTC :  %UC_CTI-3-CtiProviderOpenFailure: %[CTIconnectionId%61232238][ Login User Id%61pguser][Reason code.%61-1932787616][UNKNOWN_PARAMNAME:IPAddress%61172.17.12.73][UNKNOWN_PARAMNAME:IPv6Address%61][App ID%61Cisco CTIManager][Cluster ID%61SplkCluster][Node ID%61splkcucm6p]: CTI application failed to open provider%59 application startup failed\" type=\"string\"/>\n" +
                "    <parm name=\"severity\" value=\"Error\" type=\"string\"/>\n" +
                "    <parm name=\"timestamp\" value=\"Dec 22 14:13:21\" type=\"string\"/>\n" +
                "    <parm name=\"process\" value=\"229250\" type=\"string\"/>\n" +
                "    <parm name=\"service\" value=\"local7\" type=\"string\"/>\n" +
                "</eventParms>";
        String response = ((TextMessage)m).getText();
        Assert.assertEquals("Contents of message\n'" + response + "'\n not equals\n'" + escapedResponse+"'.", response, escapedResponse);
        // ensure only 1 message received since same reduction key
        m_template.setReceiveTimeout(JmsTemplate.RECEIVE_TIMEOUT_NO_WAIT);
        m = m_template.receive("MappingTestQueue");
        Assert.assertNull(m);
    }
    /**
     * Generate configuration XML.
     *
     * @return the string
     */
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

    private String objectMessageConfigXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<jms-northbounder-config>\n"
                + "  <enabled>true</enabled>\n"
                + "  <nagles-delay>1000</nagles-delay>\n"
                + "  <batch-size>30</batch-size>\n"
                + "  <queue-size>30000</queue-size>\n"
                + "  <destination>\n"
                + "    <jms-destination>ObjectTestQueue</jms-destination>\n"
                + "    <send-as-object-message>true</send-as-object-message>\n"
                + "    <first-occurence-only>false</first-occurence-only>"
                + "   </destination>\n"
                + "</jms-northbounder-config>\n" + "";
    }

    private String generateMappingConfigXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<jms-northbounder-config>\n"
                + "  <enabled>true</enabled>\n"
                + "  <nagles-delay>1000</nagles-delay>\n"
                + "  <batch-size>30</batch-size>\n"
                + "  <queue-size>30000</queue-size>\n"
                + "  <message-format>ackUser: ${ackUser} appDn: ${appDn} logMsg: ${logMsg} objectInstance: ${objectInstance} objectType: ${objectType} ossKey: ${ossKey}\n"
                + " ossState: ${ossState} ticketId: ${ticketId} alarmUei: ${alarmUei} alarmKey: ${alarmKey} clearKey: ${clearKey} description: ${description} operInstruct: ${operInstruct} ackTime: ${ackTime}\n"
                + " alarmType: ${alarmType} count: ${count} alarmId: ${alarmId} ipAddr: ${ipAddr} lastOccurrence: ${lastOccurrence} nodeId: ${nodeId}\n"
                + " nodeLabel: ${nodeLabel} distPoller: ${distPoller} ifService: ${ifService} severity: ${severity} ticketState: ${ticketState} x733AlarmType: ${x733AlarmType}\n"
                + " x733ProbableCause: ${x733ProbableCause} firstOccurrence: ${firstOccurrence} lastOccurrence ${lastOccurrence} eventParmsXml: ${eventParmsXml}</message-format>\n"
                + "  <destination>\n"
                + "    <jms-destination>MappingTestQueue</jms-destination>\n"
                + "    <send-as-object-message>false</send-as-object-message>\n"
                + "    <first-occurence-only>true</first-occurence-only>"
                + "   </destination>\n"
                + "</jms-northbounder-config>\n" + "";
    }
}
