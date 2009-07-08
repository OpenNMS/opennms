/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.Date;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContext 
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface PollContext {
    
    public String getServiceName();
    
    public boolean suppressAdminDownEvent();

    /**
     * @param event the event to send
     * @return the same event
     */
    public void sendEvent(Event event);

    /**
     * @param uei
     * @param nodeId
     * @param address
     * @param svcName
     * @param date
     * @return
     */
    public Event createEvent(String uei, int nodeId, String address, Date date, OnmsSnmpInterface snmpinterface);
    
    public PollableSnmpInterface refresh(PollableSnmpInterface snmpinterface);
    
    public void update(OnmsSnmpInterface snmpinteface);

}
