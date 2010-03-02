/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;


/**
 * UssdMessageParsing
 *
 * @author brozow
 */
public class UssdMessageParsingTest {

    @Test
    public void testUssdSingleLine() {
        parseMessage("+CUSD: 2,\"a message\"\r", 2, "2", "a message", null);
    }
    
    @Test
    public void testUssdSingleLineSpaceAfterStatus() {
        parseMessage("+CUSD: 2, \"a message\"\r", 2, "2", "a message", null);
    }
    
    @Test
    public void testUssdSingleLineWithEncoding() {
        parseMessage("+CUSD: 2,\"a message with an encoding\",15\r", 2, "2", "a message with an encoding", "15");
    }
    
    @Test
    public void testUssdSingleLineWithEncodingSpaceBeforeEncoding() {
        parseMessage("+CUSD: 2,\"a message with an encoding\", 15\r", 2, "2", "a message with an encoding", "15");
    }
    
    @Test
    public void testOperationNotSupprted() {
        parseMessage("+CUSD: 4\r", 2, "4", null, null);
    }
    
    
    public void parseMessage(String msg, int expectedCount, String status, String content, String encoding) {
        //Pattern MSG_PATTERN = Pattern.compile("(?s)^\\+CUSD:\\s+(\\d),\"(.*)(\"(,(\\d+))?\r)?$");
        Pattern MSG_PATTERN = Pattern.compile("(?s)^\\+CUSD:\\s+(\\d)(?:,\\s*\"([^\"]*))?(?:\",\\s*(\\d+))?(?:\"\r|\r)$");
        int STATUS_INDEX = 1;
        int CONTENT_INDEX = 2;
        int ENCODING_INDEX = 3;
        
        Matcher matcher = MSG_PATTERN.matcher(msg);
        assertTrue(matcher.matches());
        //assertEquals(expectedCount, matcher.groupCount());
        
        assertEquals(status, group(matcher, STATUS_INDEX));
        assertEquals(content, group(matcher, CONTENT_INDEX));
        assertEquals(encoding, group(matcher, ENCODING_INDEX));

    }

    private String group(Matcher matcher, int index) {
        return matcher.groupCount() >= index ? matcher.group(index) : null;
    }
    
}
