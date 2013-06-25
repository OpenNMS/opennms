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

import org.smslib.AGateway;
import org.smslib.IGatewayStatusNotification;
import org.smslib.AGateway.GatewayStatuses;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>GatewayStatusNotification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GatewayStatusNotification implements IGatewayStatusNotification, ApplicationContextAware {
	
	private Collection<IGatewayStatusNotification> m_listenerList;
	private ApplicationContext m_applicationContext;
	
	/**
	 * <p>Constructor for GatewayStatusNotification.</p>
	 */
	public GatewayStatusNotification() {
	}
	
	/**
	 * <p>Constructor for GatewayStatusNotification.</p>
	 *
	 * @param listeners a {@link java.util.List} object.
	 */
	public GatewayStatusNotification(List<IGatewayStatusNotification> listeners) {
	    m_listenerList = listeners;
	}

    @Override
    public void process(AGateway gateway, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
        for(IGatewayStatusNotification listener : getListeners()){
            if (listener != this) {
                listener.process(gateway, oldStatus, newStatus);
            }
        }
    }

	private Collection<IGatewayStatusNotification> getListeners() {
		if ( m_listenerList == null ) {
			m_listenerList = m_applicationContext.getBeansOfType(IGatewayStatusNotification.class).values();
		}
		return m_listenerList;
	}
	
	/**
	 * <p>setListenerList</p>
	 *
	 * @param list a {@link java.util.List} object.
	 */
	public void setListenerList(List<IGatewayStatusNotification> list){
		m_listenerList = list;
	}

	/** {@inheritDoc} */
        @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
