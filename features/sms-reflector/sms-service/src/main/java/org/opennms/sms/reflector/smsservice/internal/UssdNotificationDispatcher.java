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
import org.smslib.IUSSDNotification;
import org.smslib.USSDResponse;

/**
 * <p>UssdNotificationDispatcher class.</p>
 *
 * @author jeffg
 * @version $Id: $
 */
public class UssdNotificationDispatcher implements IUSSDNotification {

    private static Logger log = LoggerFactory.getLogger(UssdNotificationDispatcher.class);

    private Collection<IUSSDNotification> m_listenerList;

    /**
     * <p>Constructor for UssdNotificationDispatcher.</p>
     */
    public UssdNotificationDispatcher() {
    }

    /**
     * <p>Constructor for UssdNotificationDispatcher.</p>
     *
     * @param listeners a {@link java.util.List} object.
     */
    public UssdNotificationDispatcher(List<IUSSDNotification> listeners){
        m_listenerList = listeners;
    }

    /** {@inheritDoc} */
    @Override
    public void process(AGateway gateway, USSDResponse ussdResponse) {
        log.debug( "Forwarding message to registered listeners: {} : {}", getListeners(), ussdResponse);

        for( IUSSDNotification listener : getListeners() )
        {
            if (listener != this) {
                listener.process( gateway, ussdResponse );
            }
        }
    }

    private Collection<IUSSDNotification> getListeners() {
        return m_listenerList;
    }

    /**
     * <p>setListenerList</p>
     *
     * @param list a {@link java.util.List} object.
     */
    public void setListenerList(List<IUSSDNotification> list){
        m_listenerList = list;
    }


}
