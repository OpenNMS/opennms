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

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import junit.framework.TestCase;

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
import org.opennms.test.mock.MockUtil;

/**
 * @author mikeh@aiinet.com
 * @author dj@gregor.com
 */
public class XmlRpcNotifierTest extends TestCase {
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

    public void setUp() throws Exception, InterruptedException, IOException  {
        
        super.setUp();
        
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
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
        Vector<Object> v = new Vector<Object>();
        v.add("0");
        v.add("uei.opennms.org/internal/capsd/xmlrpcNotification");
        v.add("test connection");
        m_anticipator.anticipateCall("notifyReceivedEvent", v);
    }

    public void finishUp() {
        m_anticipator.verifyAnticipated();
        
        MockLogAppender.assertNoWarningsOrGreater();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }

    public void tearDown() throws Exception, InterruptedException, IOException {
        m_anticipator.shutdown();

        super.tearDown();
    }

    public void testEventListener() {
        // Do nothing, just test to see if setUp() and tearDown() work
        finishUp();
    }

    public void testNotifySuccess() {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        Vector<Object> v = new Vector<Object>();
        v.add(String.valueOf(txNo));
        v.add(uei);
        v.add(message);
        m_anticipator.anticipateCall("notifySuccess", v);

        assertTrue("notifier notifySuccess", m_notifier.notifySuccess(txNo, uei, message));

        finishUp();
    }
    
    public void testFailureNotifySuccess() throws Exception {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        finishUp();
        m_anticipator.shutdown();

        assertFalse("notifier notifySuccess", m_notifier.notifySuccess(txNo, uei, message));
    }

    public void testNotifyFailure() {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        Vector<Object> v = new Vector<Object>();
        v.add(String.valueOf(txNo));
        v.add(uei);
        v.add(message);
        m_anticipator.anticipateCall("notifyFailure", v);

        assertTrue("notifier notifyFailure", m_notifier.notifyFailure(txNo, uei, message));

        finishUp();
    }
    
    public void testFailureNotifyFailure() throws Exception {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        finishUp();
        m_anticipator.shutdown();

        assertFalse("notifier notifyFailure", m_notifier.notifyFailure(txNo, uei, message));
    }

    public void testNotifyReceivedEvent() {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        Vector<Object> v = new Vector<Object>();
        v.add(String.valueOf(txNo));
        v.add(uei);
        v.add(message);
        m_anticipator.anticipateCall("notifyReceivedEvent", v);

        assertTrue("notifier notifyReceviedEvent", m_notifier.notifyReceivedEvent(txNo, uei, message));

        finishUp();
    }

    public void testFailureNotifyReceivedEvent() throws Exception {
        long txNo = 12345;
        String uei = "uei.opennms.org/something!";
        String message = "hello";

        finishUp();
        m_anticipator.shutdown();

        assertFalse("notifier notifyReceviedEvent", m_notifier.notifyReceivedEvent(txNo, uei, message));
    }

    public void testSendServiceDownEvent() throws Exception {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        v.add(s_nodeLabel);
        v.add(s_interface);
        v.add(s_service);
        v.add("Not Available");
        v.add(s_host);
        v.add(date);
        m_anticipator.anticipateCall("sendServiceDownEvent", v);

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertTrue("notifier sendServiceDownEvent", m_notifier.sendServiceDownEvent(e));
        
        finishUp();
    }

    public void testFailureSendServiceDownEvent() throws Exception {
        String date = new Date().toString();

        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertFalse("notifier sendServiceDownEvent", m_notifier.sendServiceDownEvent(e));
    }
    
    public void testSendServiceUpEvent() throws Exception {
        String date = new Date().toString();
        
        Vector<Object> v = new Vector<Object>();
        v.add(s_nodeLabel);
        v.add(s_interface);
        v.add(s_service);
        v.add("Not Available");
        v.add(s_host);
        v.add(date);
        m_anticipator.anticipateCall("sendServiceUpEvent", v);

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertTrue("notifier sendServiceUpEvent", m_notifier.sendServiceUpEvent(e));
        
        finishUp();
    }
    
    public void testFailureSendServiceUpEvent() throws Exception {
        String date = new Date().toString();
        
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertFalse("notifier sendServiceUpEvent", m_notifier.sendServiceUpEvent(e));
    }

    public void testSendInterfaceDownEvent() throws Exception {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        v.add(s_nodeLabel);
        v.add(s_interface);
        v.add(s_host);
        v.add(date);
        m_anticipator.anticipateCall("sendInterfaceDownEvent", v);

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertTrue("notifier sendInterfaceDownEvent", m_notifier.sendInterfaceDownEvent(e));
        
        finishUp();
    }

    public void testFailureSendInterfaceDownEvent() throws Exception {
        String date = new Date().toString();

        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertFalse("notifier sendInterfaceDownEvent", m_notifier.sendInterfaceDownEvent(e));
    }

    public void testSendInterfaceUpEvent() throws Exception {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        v.add(s_nodeLabel);
        v.add(s_interface);
        v.add(s_host);
        v.add(s_host);
        v.add(date);
        m_anticipator.anticipateCall("sendInterfaceUpEvent", v);

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertTrue("notifier sendInterfaceUpEvent", m_notifier.sendInterfaceUpEvent(e));
        
        finishUp();
    }

    public void testFailureSendInterfaceUpEvent() throws Exception {
        String date = new Date().toString();
        
        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setInterface(s_interface);
        e.setService(s_service);
        e.setHost(s_host);
        e.setTime(date);
        assertFalse("notifier sendInterfaceUpEvent", m_notifier.sendInterfaceUpEvent(e));
    }

    public void testSendNodeDownEvent() throws Exception {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        v.add(s_nodeLabel);
        v.add(s_host);
        v.add(date);
        m_anticipator.anticipateCall("sendNodeDownEvent", v);

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(date);
        assertTrue("notifier sendNodeDownEvent", m_notifier.sendNodeDownEvent(e));
        
        finishUp();
    }

    public void testFailureSendNodeDownEvent() throws Exception {
        String date = new Date().toString();

        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(date);
        assertFalse("notifier sendNodeDownEvent", m_notifier.sendNodeDownEvent(e));
    }

    public void testSendNodeUpEvent() throws Exception {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        v.add(s_nodeLabel);
        v.add(s_host);
        v.add(date);
        m_anticipator.anticipateCall("sendNodeUpEvent", v);

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(date);
        assertTrue("notifier sendNodeUpEvent", m_notifier.sendNodeUpEvent(e));
        
        finishUp();
    }

    public void testFailureSendNodeUpEvent() throws Exception {
        String date = new Date().toString();

        finishUp();
        m_anticipator.shutdown();

        Event e = new Event();
        e.setNodeid(s_nodeId);
        e.setHost(s_host);
        e.setTime(date);
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
    public void testSendEventSimple() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        makeBasicRpcHashtable(v, date);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testFailureSendEventSimple() throws Exception {
        String date = new Date().toString();
        
        finishUp();
        m_anticipator.shutdown();

        Event e = makeBasicEvent(date);
        assertFalse("notifier sendEvent", m_notifier.sendEvent(e));
    }

    public void testSendEventSource() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        t.put("source", s_source);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        e.setSource(s_source);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testSendEventHost() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        t.put("host", s_host);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        e.setHost(s_host);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testSendEventInterface() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        t.put("interface", s_interface);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        e.setInterface(s_interface);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testSendEventService() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        t.put("service", s_service);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        e.setService(s_service);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testSendEventDescription() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        t.put("description", s_description);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        e.setDescr(s_description);
        assertTrue("notifier sendEvent",m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testSendEventSeverity() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        t.put("severity", s_severity);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        e.setSeverity(s_severity);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testSendEventEmptyParms() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        makeBasicRpcHashtable(v, date);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        Parms p = new Parms();
        e.setParms(p);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }


    public void testSendEventOneParm() {
        String date = new Date().toString();
        String parmZeroName = "foo";
        String parmZeroContent = "bar";
        String parmZeroType = "string";

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        addRpcParm(t, 0, parmZeroName, parmZeroContent, parmZeroType);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
        Parms p = new Parms();
        e.setParms(p);
        p.addParm(makeEventParm(parmZeroName, parmZeroContent, parmZeroType, "text"));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testSendEventTwoParms() {
        String date = new Date().toString();
        String parmZeroName = "foo";
        String parmZeroContent = "bar";
        String parmZeroType = "string";
        
        String parmOneName = "baz";
        String parmOneContent = "blam";
        String parmOneType = "string";

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        addRpcParm(t, 0, parmZeroName, parmZeroContent, parmZeroType);
        addRpcParm(t, 1, parmOneName, parmOneContent, parmOneType);
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = makeBasicEvent(date);
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
    public void testSendEventNoNodeId() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("uei", "hi!");
        t.put("time", date);
        t.put("nodeId", String.valueOf(s_noNodeId));
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = new Event();
        e.setUei("hi!");
        e.setTime(date);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }
    
    /**
     * Check that when Event.setNodeid(int) is called with a
     * nodeId that isn't in the database that nodeLabel is not set.
     */
    public void testSendEventNoNodeLabel() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("uei", "hi!");
        t.put("time", date);
        t.put("nodeId", String.valueOf(s_unknownNodeId));
        m_anticipator.anticipateCall("sendEvent", v);

        Event e = new Event();
        e.setUei("hi!");
        e.setNodeid(s_unknownNodeId);
        e.setTime(date);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }


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
    
    public static Hashtable<String, String> makeBasicRpcHashtable(Vector<Object> v, String time) {
        Hashtable<String, String> t = new Hashtable<String, String>();
        t.put("uei", s_uei);
        t.put("time", time);
        t.put("nodeId", String.valueOf(s_nodeId));
        t.put("nodeLabel", s_nodeLabel);
        
        v.add(t);
        return t;
    }
    
    public static Hashtable<String, String> makeBasicRpcTrapHashtable(Vector<Object> v, String date, String community, int generic,
            String enterpriseId, int specific, long dateLong, String version) {
        Hashtable<String, String> t = makeBasicRpcHashtable(v, date);
        t.put("communityString", community);
        t.put("genericTrapNumber", String.valueOf(generic));
        t.put("enterpriseId", enterpriseId);
        t.put("specificTrapNumber", String.valueOf(specific));
        t.put("timeStamp", String.valueOf(dateLong));
        t.put("version", version);
        return t;
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
    
    public void testSendTrapSimple() {
        long dateLong = System.currentTimeMillis();
        String date = EventConstants.formatToString(new Date(dateLong));
        String enterpriseId = ".1.3.6.4.1.1.1";

        Vector<Object> v = new Vector<Object>();
        makeBasicRpcTrapHashtable(v, date, "public", 6, enterpriseId, 2, dateLong, "1");
        m_anticipator.anticipateCall("sendSnmpTrapEvent", v);

        Event e = makeBasicEvent(date);
        e.setSnmp(makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, dateLong, "1"));
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

    public void testFailureSendTrapSimple() throws Exception {
        long dateLong = System.currentTimeMillis();
        String date = new Date(dateLong).toString();
        String enterpriseId = ".1.3.6.4.1.1.1";

        finishUp();
        m_anticipator.shutdown();

        Event e = makeBasicEvent(date);
        e.setSnmp(makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, dateLong, "1"));
        assertFalse("notifier sendEvent", m_notifier.sendEvent(e));
    }
        
    public void testSendTrapIdText() {
        long dateLong = System.currentTimeMillis();
        String date = new Date(dateLong).toString();
        String enterpriseId = ".1.3.6.4.1.1.1";

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = makeBasicRpcTrapHashtable(v, date, "public", 6, enterpriseId, 2, dateLong, "1");
        t.put("enterpriseIdText", "foo!");
        m_anticipator.anticipateCall("sendSnmpTrapEvent", v);

        Event e = makeBasicEvent(date);

        Snmp s = makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, dateLong, "1");
        s.setIdtext("foo!");
        e.setSnmp(s);
        
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }
    
    public void testSendTrapEmptySnmp() {
        String date = new Date().toString();

        Vector<Object> v = new Vector<Object>();
        makeBasicRpcTrapHashtable(v, date, "null", 0, "null", 0, 0, "null");
        
        m_anticipator.anticipateCall("sendSnmpTrapEvent", v);

        Event e = makeBasicEvent(date);
        Snmp s = new Snmp();
        e.setSnmp(s);
        assertTrue("notifier sendEvent", m_notifier.sendEvent(e));
        
        finishUp();
    }

}
