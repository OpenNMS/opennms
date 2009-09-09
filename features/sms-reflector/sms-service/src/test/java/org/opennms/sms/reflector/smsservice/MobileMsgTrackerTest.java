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
package org.opennms.sms.reflector.smsservice;

import static org.junit.Assert.*;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.opennms.protocols.rt.Messenger;
import org.opennms.sms.reflector.smsservice.internal.MobileMsgTrackerImpl;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.USSDDcs;
import org.smslib.USSDRequest;
import org.smslib.USSDResponse;
import org.smslib.USSDSessionStatus;

import static org.opennms.sms.reflector.smsservice.MobileMsgSequenceBuilder.sms;

/**
 * MobileMsgTrackerTeste
 *
 * @author brozow
 */
public class MobileMsgTrackerTest {
    
    /**
     * @author brozow
     *
     */
    public class BalanceCheckMatcher implements MobileMsgResponseMatcher {

        /* (non-Javadoc)
         * @see org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher#matches(org.opennms.sms.reflector.smsservice.MobileMsgRequest, org.opennms.sms.reflector.smsservice.MobileMsgResponse)
         */
        public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
            
            if (response instanceof UssdResponse) {
                UssdResponse ussdResponse = (UssdResponse)response;

                String regex = "^.*[\\d\\.]+ received on \\d\\d/\\d\\d/\\d\\d. For continued service through \\d\\d/\\d\\d/\\d\\d, please pay [\\d\\.]+ by \\d\\d/\\d\\d/\\d\\d.*$";

                return ussdResponse.getContent().matches(regex);

            } else {
                return false;
            }
        }

    }

    /**
     * @author brozow
     *
     */
    public class TestMessenger implements Messenger<MobileMsgRequest, MobileMsgResponse> {
        
        Queue<MobileMsgResponse> m_q;

        /* (non-Javadoc)
         * @see org.opennms.protocols.rt.Messenger#sendRequest(java.lang.Object)
         */
        public void sendRequest(MobileMsgRequest request) throws Exception {
            // fake send this
            request.setSendTimestamp(System.currentTimeMillis());
        }

        /* (non-Javadoc)
         * @see org.opennms.protocols.rt.Messenger#start(java.util.Queue)
         */
        public void start(Queue<MobileMsgResponse> q) {
            m_q = q;
        }
        
        public void sendTestResponse(MobileMsgResponse response) {
            m_q.offer(response);
        }

        /**
         * @param msg1
         */
        public void sendTestResponse(InboundMessage msg) {
            sendTestResponse(new SmsResponse(msg, System.currentTimeMillis()));
        }

        /**
         * @param response
         */
        public void sendTestResponse(String gatewayId, USSDResponse response) {
            sendTestResponse(new UssdResponse(gatewayId, response));
        }

    }
    
    public static class TestCallback implements MobileMsgResponseCallback {
        
        CountDownLatch m_latch = new CountDownLatch(1);
        AtomicReference<MobileMsgResponse> m_response = new AtomicReference<MobileMsgResponse>(null);

        
        MobileMsgResponse getResponse() throws InterruptedException {
            m_latch.await();
            return m_response.get();
        }

        /* (non-Javadoc)
         * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleError(org.opennms.sms.reflector.smsservice.SmsRequest, java.lang.Throwable)
         */
        public void handleError(MobileMsgRequest request, Throwable t) {
            System.err.println("Error processing SmsRequest: " + request);
            m_latch.countDown();
        }

        /* (non-Javadoc)
         * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleResponse(org.opennms.sms.reflector.smsservice.SmsRequest, org.opennms.sms.reflector.smsservice.SmsResponse)
         */
        public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse response) {
            m_response.set(response);
            m_latch.countDown();
            return true;
        }

        /* (non-Javadoc)
         * @see org.opennms.sms.reflector.smsservice.SmsResponseCallback#handleTimeout(org.opennms.sms.reflector.smsservice.SmsRequest)
         */
        public void handleTimeout(MobileMsgRequest request) {
            System.err.println("Timeout waiting for SmsRequest: " + request);
            m_latch.countDown();
        }

        /**
         * @return
         * @throws InterruptedException 
         */
        public InboundMessage getMessage() throws InterruptedException {
            MobileMsgResponse response = getResponse();
            if (response instanceof SmsResponse) {
                return ((SmsResponse)response).getMessage();
            }
            return null;
            
        }
        
        public USSDResponse getUSSDResponse() throws InterruptedException{
            MobileMsgResponse response = getResponse();
            if (response instanceof UssdResponse) {
                return ((UssdResponse)response).getMessage();
            }
            return null;
        }
        
    }
    
    TestMessenger m_messenger;
    MobileMsgTrackerImpl m_tracker;
    
    @Before
    public void setUp() throws Exception {
        m_messenger = new TestMessenger();
        m_tracker = new MobileMsgTrackerImpl("test", m_messenger);
        m_tracker.start();
    }

    @Test
    public void testPing() throws Exception {
        
        OutboundMessage msg = new OutboundMessage("+19195552121", "ping");
        OutboundMessage msg2 = new OutboundMessage("+19195553131", "ping");
        
        TestCallback cb = new TestCallback();
        TestCallback cb2 = new TestCallback();
        
        m_tracker.sendSmsRequest(msg, 60000L, 0, cb, new PingResponseMatcher());
        m_tracker.sendSmsRequest(msg2, 60000, 0, cb2, new PingResponseMatcher());
        
        InboundMessage responseMsg = createInboundMessage("+19195552121", "pong");
        InboundMessage responseMsg2 = createInboundMessage("+19195553131", "pong");
        
        m_messenger.sendTestResponse(responseMsg);
        m_messenger.sendTestResponse(responseMsg2);
        
        
        assertSame(responseMsg, cb.getMessage());
        assertSame(responseMsg2, cb2.getMessage());
        
        
    }
    
    @Test
    public void testTMobileGetBalance() throws Exception {
        
        String gatewayId = "G";
        
        TestCallback cb = new TestCallback();
        
        USSDRequest request = new USSDRequest("#225#");
        
        
        m_tracker.sendUssdRequest(gatewayId, request, 10000, 0, cb, new BalanceCheckMatcher());
        
        
        String content = "37.28 received on 08/31/09. For continued service through 10/28/09, please pay 79.56 by 09/28/09.    ";
        
        USSDResponse response = new USSDResponse();
        response.setContent(content);
        response.setUSSDSessionStatus(USSDSessionStatus.NO_FURTHER_ACTION_REQUIRED);
        response.setDcs(USSDDcs.UNSPECIFIED_7BIT);
        
        m_messenger.sendTestResponse(gatewayId, response);
        
        assertSame(response, cb.getUSSDResponse());
    }

    @Test
    public void testPingWithBuilder() throws Exception {
        String gatewayId = "G";
        
        TestCallback cb = new TestCallback();

        MobileMsgSequenceBuilder builder = new MobileMsgSequenceBuilder();

        builder.addTransaction("SMS ping").sendSms("+19192640655", "ping").expect(sms("^pong$"));
        MobileMsgSequence sequence = builder.getSequence();
        Map<String,Long> timing = sequence.execute();

        assertNotNull(timing);
        assertTrue(timing.size() > 0);
        // sequence.start();
        // sequence.waitFor();
    }

	/**
     * @param originator
     * @param text
     * @return
     */
    private InboundMessage createInboundMessage(String originator, String text) {
        InboundMessage msg = new InboundMessage(new Date(), originator, text, 0, "0");
        return msg;
    }
    

}
