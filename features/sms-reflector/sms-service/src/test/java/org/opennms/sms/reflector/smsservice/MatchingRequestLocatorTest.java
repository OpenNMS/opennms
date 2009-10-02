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
