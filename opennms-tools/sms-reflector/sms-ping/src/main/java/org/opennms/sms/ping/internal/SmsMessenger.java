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
package org.opennms.sms.ping.internal;


import java.io.IOException;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.opennms.protocols.rt.Messenger;
import org.smslib.GatewayException;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundBinaryMessage;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageTypes;


/**
 * SmsMessenger
 *
 * @author brozow
 */
public class SmsMessenger implements Messenger<PingRequest, PingReply>, IInboundMessageNotification, IOutboundMessageNotification {
    
    Logger log = Logger.getLogger(getClass());
    
    private SmsService m_smsService;
    
    private Queue<PingReply> m_replyQueue;
    
    public SmsMessenger(SmsService smsService) {
        debugf("Created SmsMessenger: %s", smsService);
        m_smsService = smsService;
    }

    public void sendRequest(PingRequest request) throws Exception {
        debugf("SmsMessenger.sendRequest %s", request);
        if (!m_smsService.sendMessage(request.getRequest())) {
            throw new IOException("Failed to send sms message");
        }
    }

    public void start(Queue<PingReply> replyQueue) {
        debugf("SmsMessenger.start");
        m_replyQueue = replyQueue;
    }

    public void process(String gatewayId, MessageTypes msgType, InboundMessage msg) {
        debugf("SmsMessenger.processInboundMessage");
        
        if (isPingRequest(msg)) {
            sendPong(gatewayId, msg);
        }
        else if (m_replyQueue != null) {
            m_replyQueue.add(new PingReply(msg));
        }
    }

    private boolean isPingRequest(InboundMessage msg) {
        return (!(msg instanceof InboundBinaryMessage)) 
            && msg.getText() != null 
            && msg.getText().length() >= 4 
            && "ping".equalsIgnoreCase(msg.getText().substring(0, 4));
    }
    
    
    private void sendPong(String gatewayId, InboundMessage msg) {
        try {
            OutboundMessage pong = new OutboundMessage(msg.getOriginator(), "pong");
            pong.setGatewayId(gatewayId);
            if (!m_smsService.sendMessage(pong)) {
                errorf("Failed to send pong request to %s", msg.getOriginator());
            }

        } catch (TimeoutException e) {
            errorf(e, "Timeout sending pong request to %s", msg.getOriginator());
        } catch (GatewayException e) {
            errorf(e, "Gateway exception sending pong request to %s", msg.getOriginator());
        } catch (IOException e) {
            errorf(e, "IOException sending pong request to %s", msg.getOriginator());
        } catch (InterruptedException e) {
            errorf(e, "InterruptedException sending poing request to %s", msg.getOriginator());
        } 
    }

    public void process(String gatewayId, OutboundMessage msg) {
        log.debug("Sent message "+msg);
    }
    
    private void debugf(String fmt, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(fmt, args));
        }
    }
    
    private void errorf(Throwable t, String fmt, Object... args) {
        log.error(String.format(fmt, args), t);
    }

    private void errorf(String fmt, Object... args) {
        log.error(String.format(fmt, args));
    }

}
