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
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.opennms.sms.reflector.smsservice.SmsService;
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
    
    Logger log = Logger.getLogger(getClass());
    
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
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_smsService, "the smsService property must be set");
    }
    
    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.sms.ping.internal.PingRequest} object.
     * @throws java.lang.Exception if any.
     */
    public void sendRequest(PingRequest request) throws Exception {
    	request.setSentTimestamp(System.currentTimeMillis());
        debugf("SmsMessenger.sendRequest %s", request);
        if (!m_smsService.sendMessage(request.getRequest())) {
            throw new IOException("Failed to send sms message");
        }
    }

    /** {@inheritDoc} */
    public void start(Queue<PingReply> replyQueue) {
        debugf("SmsMessenger.start");
        m_replyQueue = replyQueue;
    }
    
    /** {@inheritDoc} */
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
