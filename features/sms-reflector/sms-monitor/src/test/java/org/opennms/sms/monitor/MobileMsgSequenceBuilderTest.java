/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.sms.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.sms.monitor.internal.MobileSequenceConfigBuilder;
import org.opennms.sms.monitor.internal.MobileSequenceExecution;
import org.opennms.sms.reflector.smsservice.MobileMsgTrackerImpl;
import org.smslib.USSDSessionStatus;

/**
 * MobileMsgTrackerTeste
 *
 * @author brozow
 */
public class MobileMsgSequenceBuilderTest {
    
	private static final String PHONE_NUMBER = "+19195551212";
    public static final String TMOBILE_RESPONSE = "37.28 received on 08/31/09. For continued service through 10/28/09, please pay 79.56 by 09/28/09.    ";
    public static final String TMOBILE_USSD_MATCH = "^.*[\\d\\.]+ received on \\d\\d/\\d\\d/\\d\\d. For continued service through \\d\\d/\\d\\d/\\d\\d, please pay [\\d\\.]+ by \\d\\d/\\d\\d/\\d\\d.*$";

	TestMessenger m_messenger;
    MobileMsgTrackerImpl m_tracker;
	DefaultTaskCoordinator m_coordinator;
	MobileSequenceSession m_session;
    
    @Before
    public void setUp() throws Exception {
        
        m_messenger = new TestMessenger();
        
        m_tracker = new MobileMsgTrackerImpl("test", m_messenger);
        m_tracker.start();
        
        m_session = new MobileSequenceSession(m_tracker);
        
        m_coordinator = new DefaultTaskCoordinator("MobileMsgSequenceBuilderTest", Executors.newSingleThreadExecutor());

        System.err.println("=== STARTING TEST ===");
    }

	@Test(expected=java.net.SocketTimeoutException.class)
    public void testPingTimeoutWithBuilder() throws Throwable {
        MobileSequenceConfigBuilder bldr = new MobileSequenceConfigBuilder();
        
        ping(bldr);

        MobileSequenceExecution execution = bldr.getSequence().start(m_session, m_coordinator);
        
        bldr.getSequence().waitFor(m_session, execution);

    }

    @Test
    public void testPingWithBuilder() throws Throwable {
        MobileSequenceConfigBuilder bldr = new MobileSequenceConfigBuilder();
        
        ping(bldr);

        MobileSequenceExecution execution = bldr.getSequence().start(m_session, m_coordinator);
        
        Thread.sleep(500);
        
        sendPong();
        bldr.getSequence().waitFor(m_session, execution);
        
        Map<String,Number> timing = execution.getResponseTimes();

        assertNotNull(timing);
        assertTrue(latency(timing, "SMS Ping") > 400);
    }

    @Test
    public void testUssdWithBuilder() throws Throwable {
        MobileSequenceConfigBuilder bldr = new MobileSequenceConfigBuilder();

        balanceInquiry(bldr);
		
		MobileSequenceExecution execution = bldr.getSequence().start(m_session, m_coordinator);

		Thread.sleep(500);
		
        sendBalance();
        
        bldr.getSequence().waitFor(m_session, execution);
        
        Map<String,Number> timing = execution.getResponseTimes();

        assertNotNull(timing);
        assertTrue(latency(timing, "USSD request") > 400);
    }

    @Test
    public void testMultipleStepSequenceBuilder() throws Throwable {
        MobileSequenceConfigBuilder bldr = new MobileSequenceConfigBuilder();
        
        ping(bldr);
        balanceInquiry(bldr);

		MobileSequenceExecution execution = bldr.getSequence().start(m_session, m_coordinator);
		
		Thread.sleep(100);
		
		sendPong();
		
        Thread.sleep(100);

        sendBalance();
        
        bldr.getSequence().waitFor(m_session, execution);

        Map<String,Number> timing = execution.getResponseTimes();
        
        
        assertNotNull(m_session);
        assertEquals(PHONE_NUMBER, m_session.substitute("${SMS Pong.smsOriginator}"));
        
        assertNotNull(timing);
        assertTrue(latency(timing, "response-time") > 150);
        assertTrue(latency(timing, "SMS Ping") > 50);
        assertTrue(latency(timing, "USSD request") > 50);
        assertEquals("Unexpected size for timing " + timing, 3, timing.size());
    }

    private double latency(Map<String, Number> timing, String label) {
        Number latency = timing.get(label);
        assertNotNull("no latency found for " + label, latency);
        return latency.doubleValue();
    }

    private void ping(MobileSequenceConfigBuilder bldr) {
        bldr.smsRequest("SMS Ping", "G", PHONE_NUMBER, "ping").expectSmsResponse("SMS Pong").matching("^pong$");
    }

    private void sendPong() {
        m_messenger.sendTestResponse(PHONE_NUMBER, "pong");
    }
    
    private void balanceInquiry(MobileSequenceConfigBuilder bldr) {
        bldr.ussdRequest("USSD request","G", "#225#").expectUssdResponse("USSD response")
            .matching(TMOBILE_USSD_MATCH)
            .withSessionStatus(USSDSessionStatus.NO_FURTHER_ACTION_REQUIRED);
    }

    private void sendBalance() {
        m_messenger.sendTestResponse("G", TMOBILE_RESPONSE, USSDSessionStatus.NO_FURTHER_ACTION_REQUIRED);
    }


}
