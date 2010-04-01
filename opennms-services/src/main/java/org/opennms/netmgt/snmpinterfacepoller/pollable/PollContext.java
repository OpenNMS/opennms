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
 * Modifications:
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContext 
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface PollContext {
    
    public String getServiceName();
    
    public void setServiceName(String serviceName);
    
    /**
     * @param event the event to send
     */
    public void sendEvent(Event event);

    /**
     * @param uei
     * @param nodeId
     * @param address
     * @param svcName
     * @param date
     * @return the event
     */
    public Event createEvent(String uei, int nodeId, String address, Date date, OnmsSnmpInterface snmpinterface);
    
    /**
     * 
     * @param nodeId
     * @param criteria
     * @return The List of OnmsSnmpInterfaces to be polled
     */
    public List<OnmsSnmpInterface> get(int nodeId, String criteria);

    /**
     * Update the pollstatus using the specified criteria for 
     * OnmsSnmpInterfaces having nodeid nodeId
     *
     * @param nodeId
     * @param criteria
     * @param status
     */
    public void updatePollStatus(int nodeId, String criteria, String status);
    
    /**
     * Update the pollstatus for 
     * OnmsSnmpInterfaces having nodeid nodeId
     *
     * @param nodeId
     * @param status
     */
    public void updatePollStatus(int nodeId,String status);

    /**
     * Update the pollstatus for 
     * OnmsSnmpInterfaces 
     *
     * @param status
     */
    public void updatePollStatus(String status);

    /**
     * Update the OnmsSnmpInterface
     * @param snmpinteface
     */
    public void update(OnmsSnmpInterface snmpinteface);

}
