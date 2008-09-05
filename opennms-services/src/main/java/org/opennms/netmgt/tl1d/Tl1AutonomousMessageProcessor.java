/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

/**
 * Implementation of the <code>Tl1MessageProcessor</code> Interface.  This is the default
 * Autonomous Message Parser based on Tl1Messages recorded from the Hitachi GPOND TL1 simulator.
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 *
 */
public class Tl1AutonomousMessageProcessor implements Tl1MessageProcessor {

    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1MessageProcessor#process(java.lang.String, int)
     */
    public Tl1AutonomousMessage process(String rawMessage, int messageType) {

        StringTokenizer lineParser = new StringTokenizer(rawMessage, "\n");
        Tl1AutonomousMessage message = new Tl1AutonomousMessage(rawMessage);
        try {
            processHeader(lineParser, message);
            processId(lineParser, message);
            processAutoBlock(lineParser, message);
        } catch (IllegalStateException e) {
            return null;
        }
        
        return message;
        
    }

    private void processHeader(StringTokenizer lineParser, Tl1AutonomousMessage message) {
        boolean foundHeader = false;
        while(!foundHeader ) {
            while (lineParser.hasMoreTokens() && !foundHeader) {
                String line = (String) lineParser.nextToken();
                if (line != null && !(line.equals(message.getTerminator()))) {
                    try {
                        foundHeader = parseHeader(line, message);
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
        if (!foundHeader) {
            throw new IllegalStateException("No TL1 Header found in: "+lineParser.toString());
        }
    }

    private static boolean parseHeader(String line, Tl1AutonomousMessage message) throws IllegalArgumentException {
        StringTokenizer headerParser = new StringTokenizer(line);

        if (headerParser.countTokens() != 3) {
            throw new IllegalArgumentException("The line: "+line+" is not an Autonomous message header");
        }

        message.getHeader().setRawMessage(line);
        message.getHeader().setSid(headerParser.nextToken());
        message.setHost(message.getHeader().getSid());
        message.getHeader().setDate(headerParser.nextToken());
        message.getHeader().setTime(headerParser.nextToken());
        
        try {
            message.getHeader().setTimestamp(SDF.parse(message.getHeader().getDate()+" "+message.getHeader().getTime()));
            message.setTimestamp(message.getHeader().getTimestamp());
        } catch (ParseException e) {
            throw new IllegalArgumentException("The line: "+line+", doesn't contain date and time in the format: "+SDF.toLocalizedPattern());
        }
        
        return true;
    }

    private void processId(StringTokenizer lineParser, Tl1AutonomousMessage message) {
        boolean foundId = false;
        while(!foundId ) {
            while (lineParser.hasMoreElements() && !foundId) {
                String line = (String) lineParser.nextElement();
                if (line != null && !(line.equals(message.getTerminator()))) {
                    foundId = parseId(line, message);
                }
            }
        }
    }

    private boolean parseId(String line, Tl1AutonomousMessage message) {
        StringTokenizer idParser = new StringTokenizer(line);
        if (idParser.countTokens() < 3) {
            throw new IllegalArgumentException("The line: "+line+" is not an Autonomouse message id.  Expected 3 or more tokens " +
                    "and received: "+idParser.countTokens());
        }
        
        message.getId().setRawMessage(line);
        message.getId().setAlarmCode(idParser.nextToken());
        message.getId().setAlarmTag(idParser.nextToken());
        StringBuilder bldr = new StringBuilder();
        while(idParser.hasMoreTokens()) {
            bldr.append(idParser.nextToken());
            bldr.append(" ");
        }
        message.getId().setVerb(bldr.toString().trim());
        return true;
    }

    private void processAutoBlock(StringTokenizer lineParser, Tl1AutonomousMessage message) {
        boolean foundId = false;
        while(!foundId ) {
            while (lineParser.hasMoreElements() && !foundId) {
                String line = (String) lineParser.nextElement();
                if (line != null && !(line.equals(message.getTerminator()))) {
                    foundId = true;
                    message.getAutoBlock().setBlock(line.trim());
                }
            }
        }
    }

}
