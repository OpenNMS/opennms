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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContext
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollContext {
    
    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName();
    
    /**
     * <p>setServiceName</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void setServiceName(String serviceName);
    
    /**
     * <p>sendEvent</p>
     *
     * @param event the event to send
     */
    public void sendEvent(Event event);

    /**
     * <p>createEvent</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param address a {@link java.lang.String} object.
     * @param netMask a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     * @return the event
     * @param snmpinterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public Event createEvent(String uei, int nodeId, String address, String netMask, Date date, OnmsSnmpInterface snmpinterface);
    
    /**
     * <p>get</p>
     *
     * @param nodeId a int.
     * @param criteria a {@link java.lang.String} object.
     * @return The List of OnmsSnmpInterfaces to be polled
     */
    public List<OnmsSnmpInterface> get(int nodeId, String criteria);

    /**
     * <p>getPollableNodesByIp</p>
     *
     * @param ipaddr the ip address of the node.
     * @return The List of OnmsIpInterfaces to be polled
     */
    public List<OnmsIpInterface> getPollableNodesByIp(String ipaddr);

    /**
     * <p>getPollableNodes</p>
     *
     * @return The List of OnmsIpInterfaces to be polled
     */
    public List<OnmsIpInterface> getPollableNodes();

    /**
     * Update the OnmsSnmpInterface
     *
     * @param snmpinteface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public void update(OnmsSnmpInterface snmpinteface);

    public String getLocation(Integer nodeId);

    public LocationAwareSnmpClient getLocationAwareSnmpClient();

}
