//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;

/**
 * <p>MonitoredServiceDao interface.</p>
 *
 * @author Craig Gallen
 * @author David Hustace
 * @version $Id: $
 */
public interface MonitoredServiceDao extends OnmsDao<OnmsMonitoredService, Integer> {

    /**
     * <p>get</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     * @param serviceId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public abstract OnmsMonitoredService get(Integer nodeId, String ipAddr, Integer ifIndex, Integer serviceId);

    /**
     * <p>get</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public abstract OnmsMonitoredService get(Integer nodeId, String ipAddress, String svcName);

    /**
     * <p>findByType</p>
     *
     * @param typeName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<OnmsMonitoredService> findByType(String typeName);

    /**
     * <p>findMatchingServices</p>
     *
     * @param serviceSelector a {@link org.opennms.netmgt.model.ServiceSelector} object.
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<OnmsMonitoredService> findMatchingServices(ServiceSelector serviceSelector);

    /**
     * <p>findByApplication</p>
     *
     * @param application a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<OnmsMonitoredService> findByApplication(OnmsApplication application);

}
