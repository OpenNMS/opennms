/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.model.events;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

/**
 * This is the interface used to send events into the event subsystem - It is
 * typically used by the poller framework plugins that perform service
 * monitoring to send out aprropriate events. Can also be used by capsd,
 * discovery etc.
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public interface EventProxy {
    /**
     * This method is called to send the event out
     * 
     * @param event
     *            the event to be sent out
     * 
     * @exception EventProxyException
     *                thrown if the send fails for any reason
     */
    public void send(Event event) throws EventProxyException;

    /**
     * This method is called to send an event log containing multiple events out
     * 
     * @param eventLog
     *            the events to be sent out
     * 
     * @exception EventProxyException
     *                thrown if the send fails for any reason
     */
    public void send(Log eventLog) throws EventProxyException;
}
