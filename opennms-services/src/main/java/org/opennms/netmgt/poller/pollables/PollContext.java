/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created December 31, 2004
 *
 * Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
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
