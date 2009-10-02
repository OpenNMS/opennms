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
package org.opennms.sms.reflector.smsservice.internal;


import java.io.IOException;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.opennms.protocols.rt.Messenger;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.opennms.sms.reflector.smsservice.SmsRequest;
import org.opennms.sms.reflector.smsservice.SmsResponse;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.opennms.sms.reflector.smsservice.UssdRequest;
import org.opennms.sms.reflector.smsservice.UssdResponse;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IUSSDNotification;
import org.smslib.InboundBinaryMessage;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.smslib.USSDResponse;
import org.smslib.Message.MessageTypes;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * SmsMessenger
 *
 * @author brozow
 */
public class SmsMessenger implements Messenger<MobileMsgRequest, MobileMsgResponse>, OnmsInboundMessageNotification, IUSSDNotification, InitializingBean {
    
    Logger log = Logger.getLogger(getClass());
    
    private SmsService m_smsService;
    
    private Queue<MobileMsgResponse> m_replyQueue;
    
    public void setSmsService(SmsService smsService) {
        m_smsService = smsService;
    }
    
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_smsService, "the smsService must not be null");
    }
    
    public void sendRequest(MobileMsgRequest request) throws Exception {
    	request.setSendTimestamp(System.currentTimeMillis());
    	
    	if (request instanceof SmsRequest) {
    	    SmsRequest smsRequest = (SmsRequest)request;
            debugf("SmsMessenger.send sms message %s", smsRequest);
            if (!m_smsService.sendMessage(smsRequest.getMessage())) {
                throw new IOException("Failed to send sms message");
            }
    	}
    	else if (request instanceof UssdRequest) {
    	    UssdRequest ussdRequest = (UssdRequest)request;
            debugf("SmsMessenger.send ussd message %s", ussdRequest);
            if (!m_smsService.sendUSSDRequest(ussdRequest.getMessage(), ussdRequest.getGatewayId())) {
                throw new IOException("Unable to send ussd message");
            }
    	} 
    	else {
    	    throw new IOException("Unrecognized type of request: " + request);
    	}
    	

    }

    public void start(Queue<MobileMsgResponse> replyQueue) {
        debugf("SmsMessenger.start");
        m_replyQueue = replyQueue;
    }

    public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
    	long receiveTime = System.currentTimeMillis();
    	
        debugf("SmsMessenger.processInboundMessage");
        
        if (m_replyQueue != null) {
            m_replyQueue.add(new SmsResponse(msg, receiveTime));
        }
    }

    public void process(String gatewayId, USSDResponse ussdResponse) {
        m_replyQueue.add(new UssdResponse(gatewayId, ussdResponse, System.currentTimeMillis()));
    }


    private void debugf(String fmt, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(fmt, args));
        }
    }




}
