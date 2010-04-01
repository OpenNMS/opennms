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

import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.XmlrpcdConfigFactory;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockLogAppender;

public class XmlrpcdTest extends OpenNMSTestCase {
    private static final int m_port1 = 9000;
    private static final int m_port2 = 9001;
    
    private Xmlrpcd m_xmlrpcd;
    private XmlrpcAnticipator m_anticipator1;
    private XmlrpcAnticipator m_anticipator2;
    
    StringReader m_config = new StringReader(
            "<?xml version=\"1.0\"?>\n" +
            "<xmlrpcd-configuration max-event-queue-size=\"5000\">\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port1 + "\" />\n" +
            "  <serverSubscription>baseEvents</serverSubscription>\n" +
            " </external-servers>\n" +
            " <subscription name=\"baseEvents\">\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeLostService\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeRegainedService\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeUp\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeDown\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/interfaceUp\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/interfaceDown\"/>\n" +
            " </subscription>\n" +
            "</xmlrpcd-configuration>\n");

    StringReader m_configTwo = new StringReader(
            "<?xml version=\"1.0\"?>\n" +
            "<xmlrpcd-configuration max-event-queue-size=\"5000\">\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port1 + "\" />\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port2 + "\" />\n" +
            "  <serverSubscription>baseEvents</serverSubscription>\n" +
            " </external-servers>\n" +
            " <subscription name=\"baseEvents\">\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeLostService\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeRegainedService\"/>\n" +
            " </subscription>\n" +
            "</xmlrpcd-configuration>\n");

    StringReader m_configParallelSame = new StringReader(
            "<?xml version=\"1.0\"?>\n" +
            "<xmlrpcd-configuration max-event-queue-size=\"5000\">\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port1 + "\" />\n" +
            "  <serverSubscription>baseEvents</serverSubscription>\n" +
            " </external-servers>\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port2 + "\" />\n" +
            "  <serverSubscription>baseEvents</serverSubscription>\n" +
            " </external-servers>\n" +
            " <subscription name=\"baseEvents\">\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeLostService\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeRegainedService\"/>\n" +
            " </subscription>\n" +
            "</xmlrpcd-configuration>\n");

    StringReader m_configParallelDifferent = new StringReader(
            "<?xml version=\"1.0\"?>\n" +
            "<xmlrpcd-configuration max-event-queue-size=\"5000\">\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port1 + "\" />\n" +
            "  <serverSubscription>baseEvents1</serverSubscription>\n" +
            " </external-servers>\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port2 + "\" />\n" +
            "  <serverSubscription>baseEvents2</serverSubscription>\n" +
            " </external-servers>\n" +
            " <subscription name=\"baseEvents1\">\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeLostService\"/>\n" +
            " </subscription>\n" +
            " <subscription name=\"baseEvents2\">\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeRegainedService\"/>\n" +
            " </subscription>\n" +
            "</xmlrpcd-configuration>\n");
    
    StringReader m_configGeneric = new StringReader(
            "<?xml version=\"1.0\"?>\n" +
            "<xmlrpcd-configuration max-event-queue-size=\"5000\" generic-msgs=\"true\">\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port1 + "\" />\n" +
            "  <serverSubscription>baseEvents</serverSubscription>\n" +
            " </external-servers>\n" +
            " <subscription name=\"baseEvents\">\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeLostService\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeRegainedService\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/default/trap\"/>\n" +
            " </subscription>\n" +
            "</xmlrpcd-configuration>\n");
    
    StringReader m_configBad = new StringReader(
            "<?xml version=\"1.0\"?>\n" +
            "<xmlrpcd-configuration max-event-queue-size=\"5000\">\n" +
            " <external-servers retries=\"1\" elapse-time=\"100\">\n" +
            "  <xmlrpc-server url=\"http://localhost:" + m_port1 + "\" />\n" +
            "  <serverSubscription>baseEventsBlah</serverSubscription>\n" +
            " </external-servers>\n" +
            " <subscription name=\"baseEvents\">\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeLostService\"/>\n" +
            "  <subscribed-event uei=\"uei.opennms.org/nodes/nodeRegainedService\"/>\n" +
            " </subscription>\n" +
            "</xmlrpcd-configuration>\n");
    
    StringReader m_serverConfig = new StringReader(
            "<local-server server-name=\"nms1\" verify-server=\"false\">\n" +
            "</local-server>\n");
    
    protected void setUp() throws Exception {
        super.setUp();
        
        m_anticipator1 = new XmlrpcAnticipator(m_port1, false);
        // Don't setup the second anticipator since it can take a bit of time; let individual tests do that it if they want it
        
        OpennmsServerConfigFactory.setInstance(new OpennmsServerConfigFactory(m_serverConfig));
        
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_config));
        
        m_xmlrpcd = new Xmlrpcd();
    }
    
    public void finishUp() {
        if (m_anticipator1 != null) {
            m_anticipator1.verifyAnticipated();
        }
        if (m_anticipator2 != null) {
            m_anticipator2.verifyAnticipated();
        }
        
        /*
         * XXX This is a workaround until OpenNMSTestCase.tearDown() no longer
         * calls MockLogAppender.assertNoWarningsOrGreater().
         */
        try {
            MockLogAppender.assertNoWarningsOrGreater();
        } finally {
            MockLogAppender.resetEvents();
        }
    }

    protected void tearDown() throws Exception {
        if (m_anticipator1 != null) {
            m_anticipator1.shutdown();
        }
        if (m_anticipator2 != null) {
            m_anticipator2.shutdown();
        }
        super.tearDown();
    }

    public void anticipateNotifyReceivedEvent(XmlrpcAnticipator anticipator) {
        anticipator.anticipateCall("notifyReceivedEvent", createVector("0", "uei.opennms.org/internal/capsd/xmlrpcNotification", "test connection"));
    }

    public void testDoNothing() {
        super.testDoNothing();
        finishUp();
    }
    
    public void testStart() throws Exception {
        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        Thread.sleep(1000);
        m_xmlrpcd.stop();

        finishUp();
    }
    
    public void testQueueing() throws Exception {
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendServiceDownEvent", createRouterVector(date));

        Event nodeOneEvent = new Event();
        nodeOneEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        nodeOneEvent.setTime(date);
        nodeOneEvent.setNodeid(1);
        nodeOneEvent.setSource("the one true event source");
        nodeOneEvent.setInterface("192.168.1.1");
        nodeOneEvent.setService("ICMP");
        getEventIpcManager().sendNow(nodeOneEvent);

        Thread.sleep(1000);
        m_anticipator1.verifyAnticipated();
        m_anticipator1.shutdown();

        Event nodeTwoEvent = new Event();
        nodeTwoEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        nodeTwoEvent.setTime(date);
        nodeTwoEvent.setNodeid(2);
        nodeTwoEvent.setSource("the one true event source");
        nodeTwoEvent.setInterface("192.168.1.2");
        nodeTwoEvent.setService("SNMP");
        getEventIpcManager().sendNow(nodeTwoEvent);
        
        Thread.sleep(1000);
        
        /*
         * Tell the anticipator to not setup the web server until have
         * anticipated the sendServiceDownEvent call. We don't want to miss the
         * call if xmlrpcd sends the event after the web server comes up but
         * before we have anticipated it.
         */
        m_anticipator1 = new XmlrpcAnticipator(m_port1, true);
        anticipateNotifyReceivedEvent(m_anticipator1);

        m_anticipator1.anticipateCall("sendServiceDownEvent", createServerVector(date));
        
        m_anticipator1.setupWebServer();
        
        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);
        
        LoggingEvent[] errors = MockLogAppender.getEventsGreaterOrEqual(Level.ERROR);
        /*
         * XXX Hack Reset the events now, otherwise any failures below are
         * masked when MockLogAppender.assertNoWarningsOrGreater() is called in
         * OpenNMSTestCase.
         */
        MockLogAppender.resetEvents();

        if (errors.length == 0) {
            fail("No errors received by log4j, however some errors "
                    + "should have been received while the XML-RPC"
                    + "anticipator was down");
        }
        
        for (int i = 0; i < errors.length; i++) {
            String message = errors[i].getMessage().toString();
            if (("Failed to send message to XMLRPC server: http://localhost:" + m_port1).equals(message)) {
                continue;
            }
            if (("Could not successfully communicate with XMLRPC server 'http://localhost:" + m_port1 + "' after 1 tries").equals(message)) {
                continue;
            }
            if ("Can not set up communication with any XMLRPC server".equals(message)) {
                continue;
            }
            fail("Unexpected error logged: [" + errors[i].getLevel().toString() + "] "
                    + errors[i].getLoggerName() +": " + errors[i].getMessage());
        }

        MockLogAppender.resetEvents();

        finishUp();
    }

    public void testSerialFailover() throws Exception {
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_configTwo));
        
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_anticipator2 = new XmlrpcAnticipator(m_port2);
        anticipateNotifyReceivedEvent(m_anticipator2);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendServiceDownEvent", createRouterVector(date));
     
        Event nodeOneEvent = new Event();
        nodeOneEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        nodeOneEvent.setTime(date);
        nodeOneEvent.setNodeid(1);
        nodeOneEvent.setSource("the one true event source");
        nodeOneEvent.setInterface("192.168.1.1");
        nodeOneEvent.setService("ICMP");
        getEventIpcManager().sendNow(nodeOneEvent);

        Thread.sleep(1000);
        
        m_anticipator1.verifyAnticipated();
        m_anticipator1.shutdown();
        
        m_anticipator2.anticipateCall("sendServiceDownEvent", createServerVector(date));

        Event nodeTwoEvent = new Event();
        nodeTwoEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        nodeTwoEvent.setTime(date);
        nodeTwoEvent.setNodeid(2);
        nodeTwoEvent.setSource("the one true event source");
        nodeTwoEvent.setInterface("192.168.1.2");
        nodeTwoEvent.setService("SNMP");
        getEventIpcManager().sendNow(nodeTwoEvent);
        
        Thread.sleep(1000);

        m_xmlrpcd.stop();
        Thread.sleep(2000);
        
        LoggingEvent[] errors = MockLogAppender.getEventsGreaterOrEqual(Level.ERROR);
        /*
         * XXX Hack Reset the events now, otherwise any failures below are
         * masked when MockLogAppender.assertNoWarningsOrGreater() is called in
         * OpenNMSTestCase.
         */
        MockLogAppender.resetEvents();

        if (errors.length == 0) {
            fail("No errors received by log4j, however some errors "
                    + "should have been received while the XML-RPC"
                    + "anticipator was down");
        }
        
        for (int i = 0; i < errors.length; i++) {
            String message = errors[i].getMessage().toString();
            if (("Failed to send message to XMLRPC server: http://localhost:" + m_port1).equals(message)) {
                continue;
            }
            if (("Could not successfully communicate with XMLRPC server 'http://localhost:" + m_port1 + "' after 1 tries").equals(message)) {
                continue;
            }
            if (("Failed to send message to XMLRPC server http://localhost:" + m_port1).equals(message)) {
                continue;
            }
            if ("Can not set up communication with any XMLRPC server".equals(message)) {
                continue;
            }
            fail("Unexpected error logged: [" + errors[i].getLevel().toString() + "] "
                    + errors[i].getLoggerName() +": " + errors[i].getMessage());
        }

        MockLogAppender.resetEvents();

        finishUp();
    }

    
    public void testSerialFailback() throws Exception {
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_configTwo));
        
        String date = EventConstants.formatToString(new Date());
        
        anticipateNotifyReceivedEvent(m_anticipator1);
        m_anticipator2 = new XmlrpcAnticipator(m_port2);
        anticipateNotifyReceivedEvent(m_anticipator2);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendServiceDownEvent", createRouterVector(date));
        
        Event nodeOneEvent = new Event();
        nodeOneEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        nodeOneEvent.setTime(date);
        nodeOneEvent.setNodeid(1);
        nodeOneEvent.setSource("the one true event source");
        nodeOneEvent.setInterface("192.168.1.1");
        nodeOneEvent.setService("ICMP");
        getEventIpcManager().sendNow(nodeOneEvent);

        Thread.sleep(1500);
        
        m_anticipator1.verifyAnticipated();
        m_anticipator1.shutdown();
        
        m_anticipator2.anticipateCall("sendServiceDownEvent", createServerVector(date));

        Event nodeTwoEvent = new Event();
        nodeTwoEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        nodeTwoEvent.setTime(date);
        nodeTwoEvent.setNodeid(2);
        nodeTwoEvent.setSource("the one true event source");
        nodeTwoEvent.setInterface("192.168.1.2");
        nodeTwoEvent.setService("SNMP");
        getEventIpcManager().sendNow(nodeTwoEvent);
        
        Thread.sleep(1500);

        m_anticipator2.verifyAnticipated();
        m_anticipator2.shutdown();
        
        m_anticipator1 = new XmlrpcAnticipator(m_port1);
        anticipateNotifyReceivedEvent(m_anticipator1);

        m_anticipator1.anticipateCall("sendServiceDownEvent", createFirewallVector(date));

        Event nodeThreeEvent = new Event();
        nodeThreeEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        nodeThreeEvent.setTime(date);
        nodeThreeEvent.setNodeid(3);
        nodeThreeEvent.setSource("the one true event source");
        nodeThreeEvent.setInterface("192.168.1.3");
        nodeThreeEvent.setService("Telnet");
        getEventIpcManager().sendNow(nodeThreeEvent);
        
        Thread.sleep(1500);

        m_xmlrpcd.stop();
        Thread.sleep(2000);
        
        LoggingEvent[] errors = MockLogAppender.getEventsGreaterOrEqual(Level.ERROR);
        /*
         * XXX Hack Reset the events now, otherwise any failures below are
         * masked when MockLogAppender.assertNoWarningsOrGreater() is called in
         * OpenNMSTestCase.
         */
        MockLogAppender.resetEvents();

        if (errors.length == 0) {
            fail("No errors received by log4j, however some errors "
                    + "should have been received while the XML-RPC"
                    + "anticipator was down");
        }
        
        for (int i = 0; i < errors.length; i++) {
            String message = errors[i].getMessage().toString();
            if (("Failed to send message to XMLRPC server: http://localhost:" + m_port1).equals(message)) {
                continue;
            }
            if (("Failed to send message to XMLRPC server: http://localhost:" + m_port2).equals(message)) {
                continue;
            }
            if (("Could not successfully communicate with XMLRPC server 'http://localhost:" + m_port1 + "' after 1 tries").equals(message)) {
                continue;
            }
            if (("Could not successfully communicate with XMLRPC server 'http://localhost:" + m_port2 + "' after 1 tries").equals(message)) {
                continue;
            }
            if (("Failed to send message to XMLRPC server http://localhost:" + m_port1).equals(message)) {
                continue;
            }
            if ("Can not set up communication with any XMLRPC server".equals(message)) {
                continue;
            }
            fail("Unexpected error logged: [" + errors[i].getLevel().toString() + "] "
                    + errors[i].getLoggerName() +": " + errors[i].getMessage());
        }

        MockLogAppender.resetEvents();

        finishUp();
    }

    private Vector<Object> createFirewallVector(String date) {
        return createVector("Firewall", "192.168.1.3", "Telnet", "Not Available", "null", date);
    }

    public void testMultipleServersSameEvents() throws Exception {
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_configParallelSame));
        
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_anticipator2 = new XmlrpcAnticipator(m_port2);
        anticipateNotifyReceivedEvent(m_anticipator2);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendServiceDownEvent", createRouterVector(date));
        m_anticipator2.anticipateCall("sendServiceDownEvent", createRouterVector(date));

        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/nodeLostService");
        e.setTime(date);
        e.setNodeid(1);
        e.setSource("the one true event source");
        e.setInterface("192.168.1.1");
        e.setService("ICMP");
        getEventIpcManager().sendNow(e);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }

    public void testMultipleServersDifferentEvents() throws Exception {
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_configParallelDifferent));
        
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_anticipator2 = new XmlrpcAnticipator(m_port2);
        anticipateNotifyReceivedEvent(m_anticipator2);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendServiceDownEvent", createRouterVector(date));

        Event lostEvent = new Event();
        lostEvent.setUei("uei.opennms.org/nodes/nodeLostService");
        lostEvent.setTime(date);
        lostEvent.setNodeid(1);
        lostEvent.setSource("the one true event source");
        lostEvent.setInterface("192.168.1.1");
        lostEvent.setService("ICMP");
        getEventIpcManager().sendNow(lostEvent);

        m_anticipator2.anticipateCall("sendServiceUpEvent", createServerVector(date));

        Event regainedEvent = new Event();
        regainedEvent.setUei("uei.opennms.org/nodes/nodeRegainedService");
        regainedEvent.setTime(date);
        regainedEvent.setNodeid(2);
        regainedEvent.setSource("the one true event source");
        regainedEvent.setInterface("192.168.1.2");
        regainedEvent.setService("SNMP");
        getEventIpcManager().sendNow(regainedEvent);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }

    private Vector<Object> createServerVector(String date) {
        return createVector("Server", "192.168.1.2", "SNMP", "Not Available", "null", date);
    }
    
    public void testEventGeneric() throws Exception {
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_configGeneric));
        
        String date = EventConstants.formatToString(new Date());
        
        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        Hashtable<String, String> t = new Hashtable<String, String>();
        t.put("source", "the one true event source");
        t.put("nodeId", "1");
        t.put("time", date);
        t.put("interface", "192.168.1.1");
        t.put("nodeLabel", "Router");
        t.put("service", "ICMP");
        t.put("uei", "uei.opennms.org/nodes/nodeLostService");
        t.put("description", "\n"
                + "      <p>A ICMP outage was identified on interface\n"
                + "      192.168.1.1.</p> <p>A new Outage record has been\n"
                + "      created and service level availability calculations will be\n"
                + "      impacted until this outage is resolved.</p>\n"
                + "    ");
        t.put("severity", "Minor");
        m_anticipator1.anticipateCall("sendEvent", createVector(t));
        
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/nodeLostService");
        e.setTime(date);
        e.setNodeid(1);
        e.setSource("the one true event source");
        e.setInterface("192.168.1.1");
        e.setService("ICMP");
        getEventIpcManager().sendNow(e);
        
        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }
    
    /** Unless we are in generic mode, we shouldn't be seeing general traps */ 
    public void testSendTrapSimpleNonGeneric() throws Exception {
        long dateLong = System.currentTimeMillis();
        String date = new Date(dateLong).toString();
        String enterpriseId = ".1.3.6.4.1.1.1";

        Event e = XmlRpcNotifierTest.makeBasicEvent(date);
        e.setTime(EventConstants.formatToString(new Date(dateLong)));
        e.setSnmp(XmlRpcNotifierTest.makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, dateLong, "1"));
        e.setSource("the one true source");
        e.setLogmsg(new Logmsg());
        getEventIpcManager().sendNow(e);
        
        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }
    
    public void testSendTrapSimple() throws Exception {
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_configGeneric));

        long dateLong = System.currentTimeMillis();
        String date = EventConstants.formatToString(new Date(dateLong));
        String enterpriseId = ".1.3.6.4.1.1.1";
        
        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = XmlRpcNotifierTest.makeBasicRpcTrapHashtable(v, date, "public", 6, enterpriseId, 2, dateLong, "1");
        t.put("uei", "uei.opennms.org/default/trap");
        t.put("source", "the one true source");
        /*
        t.put("description", "\n" + 
                "      <p>This is the default event format used when an enterprise\n" +
                "      specific event (trap) is received for which no format has been\n" + 
                "      configured (i.e. no event definition exists).</p>\n" + 
                "    ");
                */
        t.put("severity", "Normal");
        m_anticipator1.anticipateCall("sendSnmpTrapEvent", v);

        Event e = XmlRpcNotifierTest.makeBasicEvent(date);
        e.setUei("uei.opennms.org/default/trap");
        e.setTime(EventConstants.formatToString(new Date(dateLong)));
        e.setSnmp(XmlRpcNotifierTest.makeBasicTrapEventSnmp("public", 6, enterpriseId, 2, dateLong, "1"));
        e.setSource("the one true source");
        e.setSeverity("Normal");
        e.setLogmsg(new Logmsg());
        getEventIpcManager().sendNow(e);
        
        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);
                
        finishUp();
    }
    
    public void testServiceDownEvent() throws Exception {
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendServiceDownEvent", createRouterVector(date));
        
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/nodeLostService");
        e.setTime(date);
        e.setNodeid(1);
        e.setSource("the one true event source");
        e.setInterface("192.168.1.1");
        e.setService("ICMP");
        getEventIpcManager().sendNow(e);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }

    private Vector<Object> createRouterVector(String date) {
        return createVector("Router", "192.168.1.1", "ICMP", "Not Available", "null", date);
    }
    
    public void testServiceUpEvent() throws Exception {
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendServiceUpEvent", createRouterVector(date));
        
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/nodeRegainedService");
        e.setTime(date);
        e.setNodeid(1);
        e.setSource("the one true event source");
        e.setInterface("192.168.1.1");
        e.setService("ICMP");
        getEventIpcManager().sendNow(e);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }
    
    public void testInterfaceDownEvent() throws Exception {
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendInterfaceDownEvent", createVector("Router", "192.168.1.1", "null", date));
        
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/interfaceDown");
        e.setTime(date);
        e.setNodeid(1);
        e.setSource("the one true event source");
        e.setInterface("192.168.1.1");
        getEventIpcManager().sendNow(e);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }
    
    public void testInterfaceUpEvent() throws Exception {
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendInterfaceUpEvent", createVector("Router", "192.168.1.1", "null", "null", date));
        
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/interfaceUp");
        e.setTime(date);
        e.setNodeid(1);
        e.setSource("the one true event source");
        e.setInterface("192.168.1.1");
        getEventIpcManager().sendNow(e);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }
    
    public void testNodeDownEvent() throws Exception {
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendNodeDownEvent", createVector("Router", "bar", date));
        
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/nodeDown");
        e.setTime(date);
        e.setNodeid(1);
        e.setHost("bar");
        e.setSource("the one true event source");
        getEventIpcManager().sendNow(e);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }
    
    public void testNodeUpEvent() throws Exception {
        String date = EventConstants.formatToString(new Date());

        anticipateNotifyReceivedEvent(m_anticipator1);
        m_xmlrpcd.init();
        m_xmlrpcd.start();

        m_anticipator1.anticipateCall("sendNodeUpEvent", createVector("Router", "bar", date));
        
        Event e = new Event();
        e.setUei("uei.opennms.org/nodes/nodeUp");
        e.setTime(date);
        e.setNodeid(1);
        e.setHost("bar");
        e.setSource("the one true event source");
        getEventIpcManager().sendNow(e);

        Thread.sleep(1000);
        m_xmlrpcd.stop();
        Thread.sleep(2000);

        finishUp();
    }
    
    
    public void testBadConfig() throws Exception {
        XmlrpcdConfigFactory.setInstance(new XmlrpcdConfigFactory(m_configBad));
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new UndeclaredThrowableException(new ValidationException()));

        try {
            m_xmlrpcd.init();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();

        LoggingEvent[] errors = MockLogAppender.getEventsGreaterOrEqual(Level.ERROR);
        /*
         * XXX Hack Reset the events now, otherwise any failures below are
         * masked when MockLogAppender.assertNoWarningsOrGreater() is called in
         * OpenNMSTestCase.
         */
        MockLogAppender.resetEvents();

        if (errors.length == 0) {
            fail("No errors received by log4j, however some errors "
                    + "should have been received while the XML-RPC"
                    + "anticipator was down");
        }
        
        for (int i = 0; i < errors.length; i++) {
            String message = errors[i].getMessage().toString();
            if ("serverSubscription element baseEventsBlah references a subscription that does not exist".equals(message)) {
                continue;
            }
            if ("Failed to load configuration".equals(message)) {
                continue;
            }
            fail("Unexpected error logged: [" + errors[i].getLevel().toString() + "] "
                    + errors[i].getLoggerName() +": " + errors[i].getMessage());
        }

        MockLogAppender.resetEvents();

        finishUp();
    }
    
    private Vector<Object> createVector(Object... objs) {
        Vector<Object> v = new Vector<Object>(objs.length);
        for (Object obj : objs) {
            v.add(obj);
        }
        return v;
    }

}
