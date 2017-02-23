/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContext
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollContext {
    
    /**
     * <p>getCriticalServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCriticalServiceName();

    /**
     * <p>isNodeProcessingEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isNodeProcessingEnabled();

    /**
     * <p>isPollingAllIfCritServiceUndefined</p>
     *
     * @return a boolean.
     */
    public boolean isPollingAllIfCritServiceUndefined();

    /**
     * <p>sendEvent</p>
     *
     * @param event the event to send
     * @return the same event
     */
    public PollEvent sendEvent(Event event);

    /**
     * <p>createEvent</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param address a {@link java.net.InetAddress} object.
     * @param svcName a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason);

    /**
     * <p>openOutage</p>
     *
     * @param pSvc a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     * @param svcLostEvent a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public void openOutage(PollableService pSvc, PollEvent svcLostEvent);

    /**
     * <p>resolveOutage</p>
     *
     * @param pSvc a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     * @param svcRegainEvent a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public void resolveOutage(PollableService pSvc, PollEvent svcRegainEvent);
    /**
     * <p>isServiceUnresponsiveEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isServiceUnresponsiveEnabled();

}
