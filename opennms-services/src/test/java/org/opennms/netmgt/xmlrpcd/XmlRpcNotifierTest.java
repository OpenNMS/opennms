/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 Daniel J. Gregor, Jr..  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.xmlrpcd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcServer;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockLogAppender;

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

    private static final int s_nodeId = 1;
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
        anticipateNotifyReceivedEvent();

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
    
    public void anticipateNotifyReceivedEvent() {
        m_anticipator.anticipateCall("notifyReceivedEvent", "0", "uei.opennms.org/internal/capsd/xmlrpcNotification", "test connection");
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

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendServiceDownEvent", m_notifier.sendServiceDownEvent(e));
        
        finishUp();
    }

    @Test
    public void testFailureSendServiceDownEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertFalse("notifier sendServiceDownEvent", m_notifier.sendServiceDownEvent(e));
    }
    
    @Test
    public void testSendServiceUpEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendServiceUpEvent", s_nodeLabel, s_interface, s_service, "Not Available", s_host, EventConstants.formatToString(date));

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendServiceUpEvent", m_notifier.sendServiceUpEvent(e));
        
        finishUp();
    }
    
    @Test
    public void testFailureSendServiceUpEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertFalse("notifier sendServiceUpEvent", m_notifier.sendServiceUpEvent(e));
    }

    @Test
    public void testSendInterfaceDownEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendInterfaceDownEvent", s_nodeLabel, s_interface, s_host, EventConstants.formatToString(date));

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendInterfaceDownEvent", m_notifier.sendInterfaceDownEvent(e));
        
        finishUp();
    }

    @Test
    public void testFailureSendInterfaceDownEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertFalse("notifier sendInterfaceDownEvent", m_notifier.sendInterfaceDownEvent(e));
    }

    @Test
    public void testSendInterfaceUpEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendInterfaceUpEvent", s_nodeLabel, s_interface, s_host, s_host, EventConstants.formatToString(date));

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendInterfaceUpEvent", m_notifier.sendInterfaceUpEvent(e));
        
        finishUp();
    }

    @Test
    public void testFailureSendInterfaceUpEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertFalse("notifier sendInterfaceUpEvent", m_notifier.sendInterfaceUpEvent(e));
    }

    @Test
    public void testSendNodeDownEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendNodeDownEvent", s_nodeLabel, s_host, EventConstants.formatToString(date));

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendNodeDownEvent", m_notifier.sendNodeDownEvent(e));
        
        finishUp();
    }

    @Test
    public void testFailureSendNodeDownEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertFalse("notifier sendNodeDownEvent", m_notifier.sendNodeDownEvent(e));
    }

    @Test
    public void testSendNodeUpEvent() throws Exception {
        Date date = new Date();
        m_anticipator.anticipateCall("sendNodeUpEvent", s_nodeLabel, s_host, EventConstants.formatToString(date));

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendNodeUpEvent", m_notifier.sendNodeUpEvent(e));
        
        finishUp();
    }

    @Test
    public void testFailureSendNodeUpEvent() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(EventConstants.formatToString(date));
        assertFalse("notifier sendNodeUpEvent", m_notifier.sendNodeUpEvent(e));
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

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    private static Hashtable<String, String> basicEventMap(Date date) {
        Hashtable<String, String> t = new Hashtable<String, String>();
        t.put("uei", s_uei);
        t.put("time", EventConstants.formatToString(date));
        t.put("nodeId", String.valueOf(s_nodeId));
        t.put("nodeLabel", s_nodeLabel);
        return t;
    }

    @Test
    public void testFailureSendEventSimple() throws Exception {
        Date date = new Date();
        finishUp();
        m_anticipator.shutdown();

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        assertFalse("notifier sendEvent", m_notifier.sendEvent(e));
    }

    @Test
    public void testSendEventSource() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("source", s_source);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setSource(s_source);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testSendEventHost() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("host", s_host);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setHost(s_host);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testSendEventInterface() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("interface", s_interface);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setInterface(s_interface);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testSendEventService() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("service", s_service);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setService(s_service);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testSendEventDescription() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("description", s_description);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setDescr(s_description);
        assertTrue("notifier sendEvent",m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testSendEventSeverity() {
        Date date = new Date();
        Hashtable<String, String> eventMap = basicEventMap(date);
        eventMap.put("severity", s_severity);

        m_anticipator.anticipateCall("sendEvent", eventMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setSeverity(s_severity);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testSendEventEmptyParms() {
        Date date = new Date();
        m_anticipator.anticipateCall("sendEvent", basicEventMap(date));

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        Parms p = new Parms();
        e.setParms(p);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
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

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        Parms p = new Parms();
        e.setParms(p);
        p.addParm(makeEventParm(parmZeroName, parmZeroContent, parmZeroType, "text"));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
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

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        Parms p = new Parms();
        e.setParms(p);
        p.addParm(makeEventParm(parmZeroName, parmZeroContent, parmZeroType, "text"));
        p.addParm(makeEventParm(parmOneName, parmOneContent, parmOneType, "text"));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
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
        t.put("time", EventConstants.formatToString(date));
        t.put("nodeId", String.valueOf(s_noNodeId));
        m_anticipator.anticipateCall("sendEvent", t);

        Event e = new Event();
        e.setUei("hi!");
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
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
        t.put("time", EventConstants.formatToString(date));
        t.put("nodeId", String.valueOf(s_unknownNodeId));
        m_anticipator.anticipateCall("sendEvent", t);

        Event e = new Event();
        e.setUei("hi!");
        e.setNodeid(s_unknownNodeId);
        e.setTime(EventConstants.formatToString(date));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
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
    
    public static Event makeBasicEvent(String time) {
        Event e = new Event();
        e.setUei(s_uei);
        e.setNodeid(s_nodeId);
        e.setTime(time);
        return e;
    }
    
    public static Snmp makeBasicTrapEventSnmp(String community, int generic, String enterpriseId, int specific,
            long dateLong, String version) {
        Snmp s = new Snmp();
        s.setCommunity(community);
        s.setGeneric(generic);
        s.setId(enterpriseId);
        s.setSpecific(specific);
        s.setTimeStamp(dateLong);
        s.setVersion(version);
        return s;
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
    
    @Test
    public void testSendTrapSimple() {
        Date date = new Date();
        String enterpriseId = ".1.3.6.4.1.1.1";

        Hashtable<String, String> trapMap = basicTrapMap(date, "public", enterpriseId, 6, 2, date.getTime(), "1");

        m_anticipator.anticipateCall("sendSnmpTrapEvent", trapMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setSnmp(makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, date.getTime(), "1"));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    @Test
    public void testFailureSendTrapSimple() throws Exception {
        Date date = new Date();
        String enterpriseId = ".1.3.6.4.1.1.1";

        finishUp();
        m_anticipator.shutdown();

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        e.setSnmp(makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, date.getTime(), "1"));
        assertFalse("notifier sendEvent", m_notifier.sendEvent(e));
    }
        
    @Test
    public void testSendTrapIdText() {
        Date date = new Date();
        String enterpriseId = ".1.3.6.4.1.1.1";

        Hashtable<String, String> trapMap = basicTrapMap(date, "public", enterpriseId, 6, 2, date.getTime(), "1");
        trapMap.put("enterpriseIdText", "foo!");

        m_anticipator.anticipateCall("sendSnmpTrapEvent", trapMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));

        Snmp s = makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, date.getTime(), "1");
        s.setIdtext("foo!");
        e.setSnmp(s);
        
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }
    
    @Test
    public void testSendTrapEmptySnmp() {
        Date date = new Date();
        Hashtable<String, String> trapMap = basicTrapMap(date, "null", "null", 0, 0, 0, "null");

        m_anticipator.anticipateCall("sendSnmpTrapEvent", trapMap);

        Event e = makeBasicEvent(EventConstants.formatToString(date));
        Snmp s = new Snmp();
        e.setSnmp(s);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

}
