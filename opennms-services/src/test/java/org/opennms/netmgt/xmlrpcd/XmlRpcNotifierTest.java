/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xmlrpcd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcServer;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author brozow@opennms.org
 * @author mikeh@aiinet.com
 * @author dj@gregor.com
 */
public class XmlRpcNotifierTest  {
    private XmlrpcAnticipator m_anticipator;
    private XmlRpcNotifier m_notifier;
    private MockDatabase m_db;
    private MockNetwork m_network;

    private static final String s_uei = "uei!";
    
    private static final int s_noNodeId = 0;

    static final int s_nodeId = 1;
    private static final String s_nodeLabel = "Router";
    private static final String s_source = XmlRpcNotifierTest.class.getName();
    private static final String s_host = "bar";
    private static final String s_interface = "192.168.1.1";
    private static final String s_service = "ICMP";
    private static final String s_description = "the ICMP service";
    private static final String s_severity = "Critical";

    private static final int s_unknownNodeId = 2;

    private static int s_port = 9000;
    
    private static final boolean USE_DIFFERENT_PORT_PER_TEST = false;

    @Before
    public void setUp() throws Exception, InterruptedException, IOException  {
        
        
        MockLogAppender.setupLogging();

        int port = s_port;
        if (USE_DIFFERENT_PORT_PER_TEST) {
            s_port++;
        }
        
        m_anticipator = new XmlrpcAnticipator(port);
        m_anticipator.anticipateCall("notifyReceivedEvent", "0", "uei.opennms.org/internal/capsd/xmlrpcNotification", "test connection");

        XmlrpcServer remoteServer = new XmlrpcServer();
        remoteServer.setUrl("http://localhost:" + port);
        
        m_notifier = new XmlRpcNotifier(new XmlrpcServer[] { remoteServer }, 1, 1500, false, "");

        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        
        m_db = new MockDatabase();
        m_db.populate(m_network);
        DataSourceFactory.setInstance(m_db);
    }
    
    public void finishUp() {
        m_anticipator.verifyAnticipated();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @After
    public void tearDown() throws Exception, InterruptedException, IOException {
        m_anticipator.shutdown();
    }

    @Test
    public void testEventListener() {
        // Do nothing, just test to see if setUp() and tearDown() work
        finishUp();
    }

    @Test
    public void testNotifySuccess() {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        m_anticipator.anticipateCall("notifySuccess", String.valueOf(txNo), uei, message);

        assertTrue("notifier notifySuccess", m_notifier.notifySuccess(txNo, uei, message));

        finishUp();
    }
    
    @Test
    public void testFailureNotifySuccess() throws Exception {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        finishUp();
        m_anticipator.shutdown();

        assertFalse("notifier notifySuccess", m_notifier.notifySuccess(txNo, uei, message));
    }

    @Test
    public void testNotifyFailure() {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        m_anticipator.anticipateCall("notifyFailure", String.valueOf(txNo), uei, message);

        assertTrue("notifier notifyFailure", m_notifier.notifyFailure(txNo, uei, message));

        finishUp();
    }
    
    @Test
    public void testFailureNotifyFailure() throws Exception {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        finishUp();
        m_anticipator.shutdown();

        assertFalse("notifier notifyFailure", m_notifier.notifyFailure(txNo, uei, message));
    }

    @Test
    public void testNotifyReceivedEvent() {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        m_anticipator.anticipateCall("notifyReceivedEvent", String.valueOf(txNo), uei, message);

        assertTrue("notifier notifyReceviedEvent", m_notifier.notifyReceivedEvent(txNo, uei, message));

        finishUp();
    }

    @Test
    public void testFailureNotifyReceivedEvent() throws Exception {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        finishUp();
        m_anticipator.shutdown();

        assertFalse("notifier notifyReceviedEvent", m_notifier.notifyReceivedEvent(txNo, uei, message));
    }

    @Test
    public void testSendServiceDownEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendServiceDownEvent", s_nodeLabel, s_interface, s_service, "Not Available", s_host, EventConstants.formatToString(date));

        EventBuilder bldr = serviceEventBuilder(date);
        assertTrue("notifier sendServiceDownEvent", m_notifier.sendServiceDownEvent(bldr.getEvent()));
        
        finishUp();
    }
    
    @Test
    public void testFailureSendServiceDownEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = serviceEventBuilder(date);
        assertFalse("notifier sendServiceDownEvent", m_notifier.sendServiceDownEvent(bldr.getEvent()));
    }
    
    @Test
    public void testSendServiceUpEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendServiceUpEvent", s_nodeLabel, s_interface, s_service, "Not Available", s_host, EventConstants.formatToString(date));

        EventBuilder bldr = serviceEventBuilder(date);
        assertTrue("notifier sendServiceUpEvent", m_notifier.sendServiceUpEvent(bldr.getEvent()));
        
        finishUp();
    }
    
    @Test
    public void testFailureSendServiceUpEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = serviceEventBuilder(date);
        assertFalse("notifier sendServiceUpEvent", m_notifier.sendServiceUpEvent(bldr.getEvent()));
    }

    @Test
    public void testSendInterfaceDownEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendInterfaceDownEvent", s_nodeLabel, s_interface, s_host, EventConstants.formatToString(date));

        EventBuilder bldr = serviceEventBuilder(date);
        assertTrue("notifier sendInterfaceDownEvent", m_notifier.sendInterfaceDownEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testFailureSendInterfaceDownEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = serviceEventBuilder(date);
        assertFalse("notifier sendInterfaceDownEvent", m_notifier.sendInterfaceDownEvent(bldr.getEvent()));
    }

    @Test
    public void testSendInterfaceUpEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendInterfaceUpEvent", s_nodeLabel, s_interface, s_host, s_host, EventConstants.formatToString(date));

        EventBuilder bldr = serviceEventBuilder(date);
        assertTrue("notifier sendInterfaceUpEvent", m_notifier.sendInterfaceUpEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testFailureSendInterfaceUpEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = serviceEventBuilder(date);
        assertFalse("notifier sendInterfaceUpEvent", m_notifier.sendInterfaceUpEvent(bldr.getEvent()));
    }

    @Test
    public void testSendNodeDownEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendNodeDownEvent", s_nodeLabel, s_host, EventConstants.formatToString(date));

        EventBuilder bldr = basicEventBuilder(date);
        assertTrue("notifier sendNodeDownEvent", m_notifier.sendNodeDownEvent(bldr.getEvent()));
        
        finishUp();
    }


    @Test
    public void testFailureSendNodeDownEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = basicEventBuilder(date);
        assertFalse("notifier sendNodeDownEvent", m_notifier.sendNodeDownEvent(bldr.getEvent()));
    }

    @Test
    public void testSendNodeUpEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendNodeUpEvent", s_nodeLabel, s_host, EventConstants.formatToString(date));

        EventBuilder bldr = basicEventBuilder(date);
        assertTrue("notifier sendNodeUpEvent", m_notifier.sendNodeUpEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testFailureSendNodeUpEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = basicEventBuilder(date);
        assertFalse("notifier sendNodeUpEvent", m_notifier.sendNodeUpEvent(bldr.getEvent()));
    }

    /**
     * 
     * This tests the case when the following are null:
     * <ul>
     * <li>source</li>
     * <li>host</li>
     * <li>interface<li>
     * <li>service<li>
     * <li>description<li>
     * <li>severity<li>
     * <li>parameters<li>
     * <li>SNMP trap-specific fields (communityString, genericTrapNumber, enterpriseId, enterpriseIdText, specificTrapNumber, timeStamp, version)<li>
     * </ul>
     */
    @Test
    public void testSendEventSimple() {
        Date date = new Date();
        m_anticipator.anticipateCall("sendEvent", basicEventMap(date));

        EventBuilder bldr = basicEventBuilder(date);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testFailureSendEventSimple() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = basicEventBuilder(date);
        assertFalse("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
    }

    @Test
    public void testSendEventSource() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("source", "some other source");

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);
        bldr.setSource("some other source");
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testSendEventHost() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("host", "some other host");

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);
        bldr.setHost("some other host");
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testSendEventInterface() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("interface", s_interface);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);
        bldr.setInterface(addr(s_interface));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testSendEventService() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("service", s_service);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);
        bldr.setService(s_service);

        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testSendEventDescription() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("description", s_description);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);
        bldr.setDescription(s_description);
        assertTrue("notifier sendEvent",m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testSendEventSeverity() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("severity", s_severity);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);
        bldr.setSeverity(s_severity);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testSendEventEmptyParms() {
        Date date = new Date();
        m_anticipator.anticipateCall("sendEvent", basicEventMap(date));

        EventBuilder bldr = basicEventBuilder(date);
        bldr.setParms(new ArrayList<Parm>());
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }


    @Test
    public void testSendEventOneParm() {
        Date date = new Date();
        String parmZeroName = "foo";
        String parmZeroContent = "bar";
        String parmZeroType = "string";

        Hashtable<String, String> eventMap = basicEventMap(date);
        addRpcParm(eventMap, 0, parmZeroName, parmZeroContent, parmZeroType);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);
        bldr.addParam(parmZeroName, parmZeroContent, parmZeroType, "text");
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    @Test
    public void testSendEventTwoParms() {
        String parmZeroName = "foo";
        String parmZeroContent = "bar";
        String parmZeroType = "string";
        
        String parmOneName = "baz";
        String parmOneContent = "blam";
        String parmOneType = "string";

        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        addRpcParm(eventMap, 0, parmZeroName, parmZeroContent, parmZeroType);
        addRpcParm(eventMap, 1, parmOneName, parmOneContent, parmOneType);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        EventBuilder bldr = basicEventBuilder(date);

        bldr.addParam(parmZeroName, parmZeroContent, parmZeroType, "text");
        bldr.addParam(parmOneName, parmOneContent, parmOneType, "text");

        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }

    /**
     * Check that when Event.setNodeid(int) is not called that the
     * nodeId in the received RPC call is 0.
     */
    @Test
    public void testSendEventNoNodeId() {
        Date date = new Date();
        Hashtable<String, String> t = new Hashtable<String, String>();
        t.put("uei", "hi!");
        t.put("source", s_source);
        t.put("time", EventConstants.formatToString(date));
        t.put("nodeId", String.valueOf(s_noNodeId));
        m_anticipator.anticipateCall("sendEvent", t);

        EventBuilder bldr = new EventBuilder("hi!", s_source, date);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }



    
    /**
     * Check that when Event.setNodeid(int) is called with a
     * nodeId that isn't in the database that nodeLabel is not set.
     */
    @Test
    public void testSendEventNoNodeLabel() {
        Date date = new Date();
        Hashtable<String, String> t = new Hashtable<String, String>();
        t.put("uei", "hi!");
        t.put("source", s_source);
        t.put("time", EventConstants.formatToString(date));
        t.put("nodeId", String.valueOf(s_unknownNodeId));
        m_anticipator.anticipateCall("sendEvent", t);

        EventBuilder bldr = new EventBuilder("hi!", s_source, date);
        bldr.setNodeid(s_unknownNodeId);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(bldr.getEvent()));
        
        finishUp();
    }


    @Test
    public void testSendEventNullEvent() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("event object must not be null"));
        try {
            m_notifier.sendEvent(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
        finishUp();
    }
    
    public static void addSnmpAttributes(EventBuilder bldr, String community, String enterpriseId, int generic, int specific, long dateLong, String version) {
        bldr.setCommunity(community);
        bldr.setGeneric(generic);
        bldr.setEnterpriseId(enterpriseId);
        bldr.setSpecific(specific);
        bldr.setSnmpTimeStamp(dateLong);
        bldr.setSnmpVersion(version);
    }
    
    @Test
    public void testSendTrapSimple() {
        Date date = new Date();
        String enterpriseId = ".1.3.6.4.1.1.1";

        Hashtable<String, String> trapMap = basicTrapMap(date, "public", enterpriseId, 6, 2, date.getTime(), "1");

        m_anticipator.anticipateCall("sendSnmpTrapEvent", trapMap);

        EventBuilder bldr = basicEventBuilder(date);
        Event e = bldr.getEvent();
        addSnmpAttributes(bldr, "public", enterpriseId, 6, 2, date.getTime(), "1");
        Snmp snmp = bldr.getEvent().getSnmp();
        e.setSnmp(snmp);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testFailureSendTrapSimple() throws Exception {
        Date date = new Date();
        String enterpriseId = ".1.3.6.4.1.1.1";

        finishUp();
        m_anticipator.shutdown();

        EventBuilder bldr = basicEventBuilder(date);
        Event e = bldr.getEvent();
        addSnmpAttributes(bldr, "public", enterpriseId, 6, 2, date.getTime(), "1");
        Snmp snmp = bldr.getEvent().getSnmp();
        e.setSnmp(snmp);
        assertFalse("notifier sendEvent", m_notifier.sendEvent(e));
    }
        
    @Test
    public void testSendTrapIdText() {
        Date date = new Date();
        String enterpriseId = ".1.3.6.4.1.1.1";

        Hashtable<String, String> trapMap = basicTrapMap(date, "public", enterpriseId, 6, 2, date.getTime(), "1");
        trapMap.put("enterpriseIdText", "foo!");

        m_anticipator.anticipateCall("sendSnmpTrapEvent", trapMap);

        EventBuilder bldr = basicEventBuilder(date);
        Event e = bldr.getEvent();
        addSnmpAttributes(bldr, "public", enterpriseId, 6, 2, date.getTime(), "1");
        Snmp snmp = bldr.getEvent().getSnmp();
        snmp.setIdtext("foo!");
        e.setSnmp(snmp);
        
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }
    
    @Test
    public void testSendTrapEmptySnmp() {
        Date date = new Date();
        Hashtable<String, String> trapMap = basicTrapMap(date, "null", "null", 0, 0, 0, "null");

        m_anticipator.anticipateCall("sendSnmpTrapEvent", trapMap);

        Event e = basicEventBuilder(date).getEvent();
        Snmp s = new Snmp();
        e.setSnmp(s);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }
    
    public static EventBuilder basicEventBuilder(Date date) {
        return new EventBuilder(s_uei, s_source, date)
            .setHost(s_host)
            .setNodeid(s_nodeId);
    }

    private EventBuilder serviceEventBuilder(Date date) {
        return basicEventBuilder(date).setInterface(addr(s_interface))
            .setService(s_service);
    }
    
    private static Hashtable<String, String> basicEventMap(Date date) {
        Hashtable<String, String> t = new Hashtable<String, String>();
        t.put("uei", s_uei);
        t.put("source", s_source);
        t.put("time", EventConstants.formatToString(date));
        t.put("host", s_host);
        t.put("nodeId", String.valueOf(s_nodeId));
        t.put("nodeLabel", s_nodeLabel);
        return t;
    }


    static Hashtable<String, String> basicTrapMap(Date date, String community, String enterpriseId, int generic, int specific, long dateLong, String version) {
        Hashtable<String, String> trapMap = basicEventMap(date);
                
        trapMap.put("communityString", community);
        trapMap.put("genericTrapNumber", String.valueOf(generic));
        trapMap.put("enterpriseId", enterpriseId);
        trapMap.put("specificTrapNumber", String.valueOf(specific));
        trapMap.put("timeStamp", String.valueOf(dateLong));
        trapMap.put("version", version);
        return trapMap;
    }
    
    public Parm makeEventParm(String name, String content, String type, String encoding) {
        Parm p = new Parm();
        p.setParmName(name);
        
        Value v = new Value();
        v.setContent(content);
        v.setType(type);
        v.setEncoding(encoding);
        p.setValue(v);
        
        return p;
    }
    
    
    public static void addRpcParm(Hashtable<String, String> t, int index, String name, String content, String type) {
        String prefix = "param" + index + " ";
        t.put(prefix + "name", name);
        t.put(prefix + "value", content);
        t.put(prefix + "type", type);
    }
    



}
