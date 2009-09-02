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

import org.junit.Before;
import org.junit.Test;


/**
 * MatchingRequestLocator
 *
 * @author brozow
 */
public class MatchingRequestLocatorTest {
    
    /**
     * PingResponseMatcher
     *
     * @author brozow
     */
    private final class PingResponseMatcher implements SmsResponseMatcher {
        public boolean matches(SmsRequest request, SmsResponse response) {
            return request.getRecipient().equals(response.getOriginator()) 
                && "pong".equalsIgnoreCase(response.getText());
        }
    }

    MatchingRequestLocator m_locator;
    
    @Before
    public void setUp() {
        m_locator = new MatchingRequestLocator();
    }
    
    @Test
    public void testMatchPingPong() {
        
        SmsRequest request = new SmsRequest("+19195551212", "+19195552121", "ping", new PingResponseMatcher());
        SmsRequest request2 = new SmsRequest("+19195551313", "+19195553131", "ping", new PingResponseMatcher());
        
        m_locator.trackRequest(request);
        m_locator.trackRequest(request2);
        
        SmsResponse response = new SmsResponse("+19195552121", "+19195551212", "pong");
        SmsResponse response2 = new SmsResponse("+19195553131", "+19195551313", "pong");
        
        SmsRequest matchedRequest2 = m_locator.locateMatchingRequest(response2);

        assertSame(request2, matchedRequest2);

        m_locator.requestComplete(request2);

        SmsRequest matchedRequest = m_locator.locateMatchingRequest(response);
        
        assertSame(request, matchedRequest);
        
        m_locator.requestComplete(request);
        
        
    }
    
    
    @Test
    public void testMatchPingPingPong() {
        
        SmsRequest request = new SmsRequest("+19195551212", "+19195552121", "ping", new PingResponseMatcher());
        
        m_locator.trackRequest(request);
        
        SmsResponse badResponse = new SmsResponse("+19195552121", "+19195551212", "ping");
        
        SmsRequest matchedRequest = m_locator.locateMatchingRequest(badResponse);

        assertNull(matchedRequest);

        
        SmsResponse goodResponse = new SmsResponse("+19195552121", "+19195551212", "pong");
        
        matchedRequest = m_locator.locateMatchingRequest(goodResponse);

        assertSame(request, matchedRequest);

        
    }
    
    
    @Test
    public void testMatchPingPingTimeoutPong() {
        
        SmsRequest request = new SmsRequest("+19195551212", "+19195552121", "ping", new PingResponseMatcher());
        
        m_locator.trackRequest(request);
        
        SmsResponse badResponse = new SmsResponse("+19195552121", "+19195551212", "ping");
        
        SmsRequest matchedRequest = m_locator.locateMatchingRequest(badResponse);

        assertNull(matchedRequest);

        m_locator.requestTimedOut(request);
        
        SmsResponse goodResponse = new SmsResponse("+19195552121", "+19195551212", "pong");
        
        matchedRequest = m_locator.locateMatchingRequest(goodResponse);

        assertNull(matchedRequest);

        
    }
    
    

}
