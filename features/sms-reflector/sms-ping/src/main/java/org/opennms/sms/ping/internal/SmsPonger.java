/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.ping.internal;


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.InboundBinaryMessage;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.smslib.Message.MessageTypes;


/**
 * SmsMessenger
 *
 * @author brozow
 * @version $Id: $
 */
public class SmsPonger implements OnmsInboundMessageNotification {
    private static final Logger LOG = LoggerFactory.getLogger(SmsPonger.class);
    
    Map<String,String> s_tokenResponses = buildTokenResponses();
    
    /** {@inheritDoc} */
    @Override
    public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
        LOG.debug("SmsPonger.processInboundMessage");
        
        if (isPingRequest(msg)) {
            LOG.debug("Message is a ping request: {}", msg.getText());
            sendPong(gateway, msg);
        }
    }

    private boolean isPingRequest(InboundMessage msg) {
        return (!(msg instanceof InboundBinaryMessage))
            && msg.getText() != null &&
                (isPseudoPingRequest(msg)
                   || isCanonicalPingRequest(msg));
    }
    
    private boolean isCanonicalPingRequest(InboundMessage msg) {
        return (!(msg instanceof InboundBinaryMessage)) 
            && msg.getText() != null 
            && msg.getText().length() >= 4 
            && "ping".equalsIgnoreCase(msg.getText().substring(0, 4));
    }
    
    private boolean isPseudoPingRequest(InboundMessage msg) {
        if (s_tokenResponses.size() == 0) {
            LOG.debug("No token responses found, not processing pseudo-pings");
            return false;
        }
        
        if (msg instanceof InboundBinaryMessage || msg.getText() == null)
            return false;
        
        for (String token : s_tokenResponses.keySet()) {
            if (msg.getText().matches(token))
                return true;
        }
        return false;
    }
    
    private void sendPong(AGateway gateway, InboundMessage msg) {
        String pongResponse = (isCanonicalPingRequest(msg)) ? "pong" : getPseudoPongResponse(msg);
        LOG.debug("SmsPonger.sendPong: sending string '{}'", pongResponse);
        try {
            OutboundMessage pong = new OutboundMessage(msg.getOriginator(), pongResponse);
            pong.setGatewayId(gateway.getGatewayId());
            if (!gateway.sendMessage(pong)) {
                LOG.error("Failed to send pong request to {}", msg.getOriginator());
            }
        } catch (TimeoutException e) {
            LOG.error("Timeout sending pong request to {}", msg.getOriginator(), e);
        } catch (GatewayException e) {
            LOG.error("Gateway exception sending pong request to {}", msg.getOriginator(), e);
        } catch (IOException e) {
            LOG.error("IOException sending pong request to {}", msg.getOriginator(), e);
        } catch (InterruptedException e) {
            LOG.error("InterruptedException sending poing request to {}", msg.getOriginator(), e);
        } 
    }
    
    private String getPseudoPongResponse(InboundMessage msg) {
        for (Entry<String, String> tuple : s_tokenResponses.entrySet()) {
            if (msg.getText().matches(tuple.getKey())) {
                return tuple.getValue();
            }
        }
        
        LOG.debug("No pseudo-ping response found, defaulting to 'pong' (this should not happen)");
        return "";
    }

    private static Map<String,String> buildTokenResponses() {
        // Use a LinkedHashMap to preserve ordering
        Map<String,String> tokenResponses = new LinkedHashMap<String,String>();
        
        String pseudoPingTokensPsv = System.getProperty("sms.ping.tokens", "");
        String pseudoPingResponsesPsv = System.getProperty("sms.ping.responses", "");
        String[] tokens = pseudoPingTokensPsv.split(";");
        String[] responses = pseudoPingResponsesPsv.split(";");
        if (tokens.length == 0) {
            LOG.debug("No pseudo-ping tokens defined");
            return tokenResponses;
        }
        if (tokens.length != responses.length) {
            LOG.error("Length of sms.ping.tokens ({}) is mismatched with length of sms.ping.responses ({})", tokens.length, responses.length);
            return tokenResponses;
        }
        
        for (int i = 0; i < tokens.length; i++) {
            tokenResponses.put(tokens[i], responses[i]);
            LOG.debug("Setting response '{}' for pseudo-ping token '{}'", responses[i], tokens[i]);
        }
        
        return tokenResponses;
    }
}
