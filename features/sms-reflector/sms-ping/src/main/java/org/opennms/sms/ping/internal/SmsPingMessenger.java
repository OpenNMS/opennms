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
import java.util.Queue;

import org.opennms.protocols.rt.Messenger;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * SmsMessenger
 *
 * @author brozow
 * @version $Id: $
 */
public class SmsPingMessenger implements Messenger<PingRequest, PingReply>, OnmsInboundMessageNotification, InitializingBean {
    
    Logger log = LoggerFactory.getLogger(getClass());
    
    private SmsService m_smsService;
    
    private Queue<PingReply> m_replyQueue;
    
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
        Assert.notNull(m_smsService, "the smsService property must be set");
    }
    
    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.sms.ping.internal.PingRequest} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void sendRequest(PingRequest request) throws Exception {
    	request.setSentTimestamp(System.currentTimeMillis());
        debugf("SmsMessenger.sendRequest %s", request);
        if (!m_smsService.sendMessage(request.getRequest())) {
            throw new IOException("Failed to send sms message");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void start(Queue<PingReply> replyQueue) {
        debugf("SmsMessenger.start");
        m_replyQueue = replyQueue;
    }
    
    /** {@inheritDoc} */
    @Override
    public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
    	long receiveTime = System.currentTimeMillis();
    	
        debugf("SmsMessenger.processInboundMessage");
        
        if (m_replyQueue != null) {
            m_replyQueue.add(new PingReply(msg, receiveTime));
        }
    }

    private void debugf(String fmt, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(fmt, args));
        }
    }


}
