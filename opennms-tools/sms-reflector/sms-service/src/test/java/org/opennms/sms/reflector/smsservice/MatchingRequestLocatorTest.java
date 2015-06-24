/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.sms.reflector.smsservice;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;


/**
 * MatchingRequestLocator
 *
 * @author brozow
 */
public class MatchingRequestLocatorTest {
    
    MatchingRequestLocator m_locator;
    
    @Before
    public void setUp() {
        m_locator = new MatchingRequestLocator();
    }
    
    @Test
    public void testMatchPingPong() {
        
        MobileMsgRequest request = new SmsRequest(getMessage("+19195551212", "+19195552121", "ping"), 60000, 0, null, new PingResponseMatcher());
        MobileMsgRequest request2 = new SmsRequest(getMessage("+19195551313", "+19195553131", "ping"), 60000, 0, null, new PingResponseMatcher());
        
        m_locator.trackRequest(request);
        m_locator.trackRequest(request2);
        
        MobileMsgResponse response = createResponse("+19195552121", "+19195551212", "pong");
        MobileMsgResponse response2 = createResponse("+19195553131", "+19195551313", "pong");
        
        MobileMsgRequest matchedRequest2 = m_locator.locateMatchingRequest(response2);

        assertSame(request2, matchedRequest2);

        m_locator.requestComplete(request2);

        MobileMsgRequest matchedRequest = m_locator.locateMatchingRequest(response);
        
        assertSame(request, matchedRequest);
        
        m_locator.requestComplete(request);
        
        
    }
    
    OutboundMessage getMessage(String originator, String recipient, String text) {
        OutboundMessage msg = new OutboundMessage(recipient, text);
        msg.setValidityPeriod(1);
        msg.setFrom(originator);
        return msg;
    }
    
    
    @Test
    public void testMatchPingPingPong() {
        
        MobileMsgRequest request = new SmsRequest(getMessage("+19195551212", "+19195552121", "ping"), 60000, 0, null, new PingResponseMatcher());
        
        m_locator.trackRequest(request);
        
        MobileMsgResponse badResponse = createResponse("+19195552121", "+19195551212", "ping");
        
        MobileMsgRequest matchedRequest = m_locator.locateMatchingRequest(badResponse);

        assertNull(matchedRequest);

        
        MobileMsgResponse goodResponse = createResponse("+19195552121", "+19195551212", "pong");
        
        matchedRequest = m_locator.locateMatchingRequest(goodResponse);

        assertSame(request, matchedRequest);

        
    }
    
    public SmsResponse createResponse(String originator, String recipient, String text) {
        InboundMessage msg = new InboundMessage(new Date(), originator, text, 0, "0");
        return new SmsResponse(msg, System.currentTimeMillis());
        
    }
    
    
    @Test
    public void testMatchPingPingTimeoutPong() {
        
        MobileMsgRequest request = new SmsRequest(getMessage("+19195551212", "+19195552121", "ping"), 60000, 0, null, new PingResponseMatcher());
        
        m_locator.trackRequest(request);
        
        MobileMsgResponse badResponse = createResponse("+19195552121", "+19195551212", "ping");
        
        MobileMsgRequest matchedRequest = m_locator.locateMatchingRequest(badResponse);

        assertNull(matchedRequest);

        m_locator.requestTimedOut(request);
        
        MobileMsgResponse goodResponse = createResponse("+19195552121", "+19195551212", "pong");
        
        matchedRequest = m_locator.locateMatchingRequest(goodResponse);

        assertNull(matchedRequest);

        
    }
    
    

}
