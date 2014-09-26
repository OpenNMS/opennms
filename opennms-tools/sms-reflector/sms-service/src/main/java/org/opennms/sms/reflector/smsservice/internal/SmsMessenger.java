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

package org.opennms.sms.reflector.smsservice.internal;


import java.io.IOException;
import java.util.Queue;

import org.opennms.protocols.rt.Messenger;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.opennms.sms.reflector.smsservice.SmsRequest;
import org.opennms.sms.reflector.smsservice.SmsResponse;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.opennms.sms.reflector.smsservice.UssdRequest;
import org.opennms.sms.reflector.smsservice.UssdResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.IUSSDNotification;
import org.smslib.InboundMessage;
import org.smslib.USSDResponse;
import org.smslib.Message.MessageTypes;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * SmsMessenger
 *
 * @author brozow
 * @version $Id: $
 */
public class SmsMessenger implements Messenger<MobileMsgRequest, MobileMsgResponse>, OnmsInboundMessageNotification, IUSSDNotification, InitializingBean {
    
    Logger log = LoggerFactory.getLogger(getClass());
    
    private SmsService m_smsService;
    
    private Queue<MobileMsgResponse> m_replyQueue;
    
    /**
     * <p>setSmsService</p>
     *
     * @param smsService a {@link org.opennms.sms.reflector.smsservice.SmsService} object.
     */
    public void setSmsService(SmsService smsService) {
        m_smsService = smsService;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_smsService, "the smsService must not be null");
    }
    
    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     * @throws java.lang.Exception if any.
     */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void start(Queue<MobileMsgResponse> replyQueue) {
        debugf("SmsMessenger.start");
        m_replyQueue = replyQueue;
    }

    /** {@inheritDoc} */
    @Override
    public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
        long receiveTime = System.currentTimeMillis();
        
        debugf("SmsMessenger.processInboundMessage");
        
        if (m_replyQueue != null) {
            m_replyQueue.add(new SmsResponse(msg, receiveTime));
        }
    }

    @Override
    public void process(AGateway gateway, USSDResponse ussdResponse) {
        long receiveTime = System.currentTimeMillis();

        debugf("SmsMessenger.processUSSDResponse");

        if (m_replyQueue != null) {
            m_replyQueue.add(new UssdResponse(gateway.getGatewayId(), ussdResponse, receiveTime));
        }
    }

    private void debugf(String fmt, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(fmt, args));
        }
    }


}
