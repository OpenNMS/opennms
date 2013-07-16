/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.tl1d;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <code>Tl1MessageProcessor</code> Interface.  This is the default
 * Autonomous Message Parser based on Tl1Messages recorded from the Hitachi GPOND TL1 simulator.
 *
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @version $Id: $
 */
public class Tl1AutonomousMessageProcessor implements Tl1MessageProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(Tl1AutonomousMessageProcessor.class);

    /**
     *  Use ThreadLocal SimpleDateFormat instances because SimpleDateFormat is not thread-safe.
     */
    private static final ThreadLocal<SimpleDateFormat> SDF_4DY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        }
    };

    /**
     *  Use ThreadLocal SimpleDateFormat instances because SimpleDateFormat is not thread-safe.
     */
    private static final ThreadLocal<SimpleDateFormat> SDF_2DY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat( "yy-MM-dd HH:mm:ss" );
        }
    };

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1MessageProcessor#process(java.lang.String, int)
     */
    /** {@inheritDoc} */
    @Override
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
            if (message.getHeader().getDate().matches("^[0-9]{4}")) {
                message.getHeader().setTimestamp(SDF_4DY.get().parse(message.getHeader().getDate()+" "+message.getHeader().getTime()));
            } else {
                message.getHeader().setTimestamp(SDF_2DY.get().parse(message.getHeader().getDate()+" "+message.getHeader().getTime()));
            }
            message.setTimestamp(message.getHeader().getTimestamp());
        } catch (ParseException e) {
            throw new IllegalArgumentException("The line: "+line+", doesn't contain date and time in the format: "+SDF_2DY.get().toLocalizedPattern() + " or " + SDF_4DY.get().toLocalizedPattern());
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
                    foundId = parseAutoBlock(line.trim(), message);
                    //message.getAutoBlock().setBlock(line.trim());
                }
            }
        }
    }
    
    /* Auto block of an Alarm always has the follow form
     *  aid:ntfcncde, followed by additional comma separated parms.
     * ntfcncde may be in the form ntfcncde=code or just the code 
     * where the code is CR, MJ, MN, CL for the severity.
     */
    private boolean parseAutoBlock(String line, Tl1AutonomousMessage message) {
        
        message.getAutoBlock().setBlock(line);

        StringTokenizer autoBlockParser = new StringTokenizer(line,",");
        
        LOG.debug("parseAutoBlock: Autoblock: {}", line);
        
        // should count tokens and see if only aid:code;
        // for now I am assuming more than one parm.
        // Also we could have muliple messages in this block. Need to handle later.
        String aidAndCode = autoBlockParser.nextToken().trim();
        LOG.debug("parseAutoBlock: aidAndCode: {}", aidAndCode);
        
        StringTokenizer aidParser = new StringTokenizer(aidAndCode,":");
        //get the aid. Trimoff the begining "
        message.getAutoBlock().setAid(aidParser.nextToken().substring(1));
        
        // There are two forms that the NTFCNCDE IE can take...
        String ntfcncde = aidParser.nextToken().trim();
        StringTokenizer codeParser;
        if (ntfcncde.startsWith("NTFCNCDE=")) {
            LOG.info("NTFCNCDE appears to be of form: NTFCNCDE=<CODE>");
            codeParser = new StringTokenizer(ntfcncde,"=");
            if(codeParser.countTokens() >= 2) {
                codeParser.nextToken();
                ntfcncde = codeParser.nextToken().trim();
                LOG.debug("Determined NTFCNCDE is {}", ntfcncde);
            } else {
                LOG.warn("NTFCNCDE could not be determined from auto block: {}", ntfcncde);
            }
        } else if (ntfcncde.matches("^(CL|CR|MJ|MN|NA|NR),")) {
            LOG.info("NTFCNCDE appears to be of form: <CODE>");
            codeParser = new StringTokenizer(ntfcncde, ",");
            if (codeParser.hasMoreTokens()) {
                ntfcncde = codeParser.nextToken().trim();
                LOG.debug("Determined NTFCNCDE is {}", ntfcncde);
            } else {
                LOG.warn("NTFCNCDE could not be determined from auto block: {}", ntfcncde);
            }

        }
          
        message.getAutoBlock().setNtfcncde(ntfcncde);
        
        //build other params.
        //This needs to be configurable or able to override.
        final StringBuffer sb = new StringBuffer();
        while (autoBlockParser.hasMoreTokens())
        {
            sb.append(autoBlockParser.nextToken()).append(",");
        }

        message.getAutoBlock().setAdditionalParams(sb.toString().trim());
   
        return true;
    }
}
