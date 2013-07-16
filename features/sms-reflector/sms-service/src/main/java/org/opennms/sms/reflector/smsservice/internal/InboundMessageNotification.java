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

import java.util.Collection;
import java.util.List;

import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * <p>InboundMessageNotification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class InboundMessageNotification implements OnmsInboundMessageNotification {

    private static Logger log = LoggerFactory.getLogger(InboundMessageNotification.class);
    
	private Collection<OnmsInboundMessageNotification> m_listenerList;
    // private SmsService m_smsService;
	@SuppressWarnings("unused")
	private ApplicationContext m_applicationContext;

	/**
	 * <p>Constructor for InboundMessageNotification.</p>
	 */
	public InboundMessageNotification() {
	}

	/**
	 * <p>Constructor for InboundMessageNotification.</p>
	 *
	 * @param listeners a {@link java.util.List} object.
	 */
	public InboundMessageNotification(List<OnmsInboundMessageNotification> listeners) {
	    // m_smsService = smsService;
	    m_listenerList = listeners;
	}
	
	/**
	 * <p>getListeners</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<OnmsInboundMessageNotification> getListeners() {
		return m_listenerList;
	}

	/** {@inheritDoc} */
    @Override
	public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
	    
	    deleteMessage(gateway, msg);

	    for(OnmsInboundMessageNotification listener : getListeners()){
	    	if (listener != this) {
	    		notifyListener(gateway, msgType, msg, listener);
	    	}
	    }

	}

    private void notifyListener(AGateway gateway, MessageTypes msgType, InboundMessage msg, OnmsInboundMessageNotification listener) {
        try {
            listener.process(gateway, msgType, msg);
        } catch (Throwable e) {
            log.error("Unexpected exception processing InboundMessage {} listener: {}", msg, listener, e);
        }
    }

    private void deleteMessage(AGateway gateway, InboundMessage msg) {
        try {
            gateway.deleteMessage(msg);
        } catch (Throwable e) {
            log.error("Unable to delete message {}", msg, e);
        }
    }
	
	/**
	 * <p>setListenerList</p>
	 *
	 * @param listeners a {@link java.util.List} object.
	 */
	public void setListenerList(List<OnmsInboundMessageNotification> listeners){
		m_listenerList = listeners;
	}

	/**
	 * <p>setApplicationContext</p>
	 *
	 * @param applicationContext a {@link org.springframework.context.ApplicationContext} object.
	 * @throws org.springframework.beans.BeansException if any.
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
