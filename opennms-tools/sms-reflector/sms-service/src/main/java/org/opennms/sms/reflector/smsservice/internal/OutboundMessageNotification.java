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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>OutboundMessageNotification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OutboundMessageNotification implements IOutboundMessageNotification, ApplicationContextAware {
    
    private static Logger log = LoggerFactory.getLogger(OutboundMessageNotification.class);
	
	private Collection<IOutboundMessageNotification> m_listenerList;
	private ApplicationContext m_applicationContext;
	
	/**
	 * <p>Constructor for OutboundMessageNotification.</p>
	 */
	public OutboundMessageNotification() {
	}
	
	/**
	 * <p>Constructor for OutboundMessageNotification.</p>
	 *
	 * @param listeners a {@link java.util.List} object.
	 */
	public OutboundMessageNotification(List<IOutboundMessageNotification> listeners){
	    m_listenerList = listeners;
	}

    @Override
    public void process(AGateway gateway, OutboundMessage msg) {

        log.debug( "Forwarding message to registered listeners: {} : {}", getListeners(), msg);

        for( IOutboundMessageNotification listener : getListeners() )
        {
            if (listener != this) {
                listener.process( gateway, msg );
            }
        }
    }

	private Collection<IOutboundMessageNotification> getListeners() {
		if (m_listenerList == null) {
			m_listenerList = m_applicationContext.getBeansOfType(IOutboundMessageNotification.class).values();
		}
		return m_listenerList;
	}
	
	/**
	 * <p>setListenerList</p>
	 *
	 * @param list a {@link java.util.List} object.
	 */
	public void setListenerList(List<IOutboundMessageNotification> list){
		m_listenerList = list;
	}

	/** {@inheritDoc} */
    @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
