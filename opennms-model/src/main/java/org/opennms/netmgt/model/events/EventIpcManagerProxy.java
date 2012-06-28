/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.util.Assert;

/**
 * Late initializing proxy to another EventIpcManager object.
 * This lets us use this class in a Spring application context
 * that is shared amongst the daemons without having to do all of
 * the fairly heavy work to initialize a real EventIpcManager in
 * the share application context.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class EventIpcManagerProxy implements EventIpcManager {
    private EventIpcManager m_delegate = null;

    /** {@inheritDoc} */
    public void addEventListener(EventListener listener) {
        assertState();
        m_delegate.addEventListener(listener);
    }

    /**
     * <p>addEventListener</p>
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param ueiList a {@link java.util.List} object.
     */
    public void addEventListener(EventListener listener, List<String> ueiList) {
        assertState();
        m_delegate.addEventListener(listener, ueiList);
    }

    /**
     * <p>addEventListener</p>
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void addEventListener(EventListener listener, String uei) {
        assertState();
        m_delegate.addEventListener(listener, uei);
    }

    /** {@inheritDoc} */
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        assertState();
        m_delegate.addEventListener(listener, ueis);
    }

    /** {@inheritDoc} */
    public void removeEventListener(EventListener listener) {
        assertState();
        m_delegate.removeEventListener(listener);
    }

    /**
     * <p>removeEventListener</p>
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param ueiList a {@link java.util.List} object.
     */
    public void removeEventListener(EventListener listener, List<String> ueiList) {
        assertState();
        m_delegate.removeEventListener(listener, ueiList);
    }

    /**
     * <p>removeEventListener</p>
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void removeEventListener(EventListener listener, String uei) {
        assertState();
        m_delegate.removeEventListener(listener, uei);
    }

    /** {@inheritDoc} */
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        assertState();
        m_delegate.removeEventListener(listener, ueis);
    }
    
    /** {@inheritDoc} */
    public void send(Event event) throws EventProxyException {
        assertState();
        m_delegate.send(event);
    }

    /**
     * <p>send</p>
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public void send(Log eventLog) throws EventProxyException {
        assertState();
        m_delegate.send(eventLog);
    }

    /** {@inheritDoc} */
    public void sendNow(Event event) {
        assertState();
        m_delegate.sendNow(event);
    }

    /**
     * <p>sendNow</p>
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     */
    public void sendNow(Log eventLog) {
        assertState();
        m_delegate.sendNow(eventLog);
    }
    
    private void assertState() {
        Assert.state(m_delegate != null, "property delegate not set; has the event daemon successfully started?");
    }

    /**
     * <p>getDelegate</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public EventIpcManager getDelegate() {
        return m_delegate;
    }

    /**
     * <p>setDelegate</p>
     *
     * @param delegate a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setDelegate(EventIpcManager delegate) {
        m_delegate = delegate;
    }

}
