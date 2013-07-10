/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;

/**
 * <p>MonitoredServiceDao interface.</p>
 *
 * @author Craig Gallen
 * @author David Hustace
 */
public interface MonitoredServiceDao extends OnmsDao<OnmsMonitoredService, Integer> {

    /**
     * <p>get</p>
     * 
     * @deprecated Use {@link #get(Integer, InetAddress, Integer, Integer)} instead
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     * @param serviceId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService get(Integer nodeId, String ipAddr, Integer ifIndex, Integer serviceId);

    /**
     * <p>get</p> 
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService get(Integer nodeId, String ipAddr, Integer serviceId);

    

    /**
     * <p>get</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.net.InetAddress} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     * @param serviceId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService get(Integer nodeId, InetAddress ipAddr, Integer ifIndex, Integer serviceId);

    /**
     * <p>get</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, String svcName);

    /**
     * <p>findByType</p>
     *
     * @param typeName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsMonitoredService> findByType(String typeName);

    /**
     * <p>findMatchingServices</p>
     *
     * @param serviceSelector a {@link org.opennms.netmgt.model.ServiceSelector} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsMonitoredService> findMatchingServices(ServiceSelector serviceSelector);

    /**
     * <p>findByApplication</p>
     *
     * @param application a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link java.util.Collection} object.
     */
    Set<OnmsMonitoredService> findByApplication(OnmsApplication application);
    
    /**
     * <p>getPrimaryService</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService getPrimaryService(Integer nodeId, String svcName);

}
