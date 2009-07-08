/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2004-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContext 
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface PollContext {
    
    public String getCriticalServiceName();

    /**
     * @return
     */
    public boolean isNodeProcessingEnabled();

    /**
     * @return
     */
    public boolean isPollingAllIfCritServiceUndefined();

    /**
     * @param event the event to send
     * @return the same event
     */
    public PollEvent sendEvent(Event event);

    /**
     * @param uei
     * @param nodeId
     * @param address
     * @param svcName
     * @param date
     * @return
     */
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason);

    /**
     * @param outage
     */
    public void openOutage(PollableService pSvc, PollEvent svcLostEvent);

    /**
     * @param outage
     */
    public void resolveOutage(PollableService pSvc, PollEvent svcRegainEvent);
    /**
     * @return
     */
    public boolean isServiceUnresponsiveEnabled();

    /**
     * @param iface
     * @param newNode
     */
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId);

}
