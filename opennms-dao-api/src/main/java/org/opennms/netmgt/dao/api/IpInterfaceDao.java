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
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;


/**
 * <p>IpInterfaceDao interface.</p>
 *
 * @author Ted Kazmark
 * @author David Hustace
 * @author Matt Brozowski
 */
public interface IpInterfaceDao extends OnmsDao<OnmsIpInterface, Integer> {

    /**
     * <p>get</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    OnmsIpInterface get(OnmsNode node, String ipAddress);
    
    /**
     * <p>findByNodeIdAndIpAddress</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    OnmsIpInterface findByNodeIdAndIpAddress(Integer nodeId, String ipAddress);

    /**
     * <p>findByForeignKeyAndIpAddress</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    OnmsIpInterface findByForeignKeyAndIpAddress(String foreignSource, String foreignId, String ipAddress);

    /**
     * <p>findByIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findByIpAddress(String ipAddress);

    /**
     * <p>findByNodeId</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findByNodeId(Integer nodeId);

    /**
     * <p>findByServiceType</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findByServiceType(String svcName);

    /**
     * <p>findHierarchyByServiceType</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findHierarchyByServiceType(String svcName);

    /**
     * Returns a map of all IP to node ID mappings in the database.
     *
     * @return a {@link java.util.Map} object.
     */
    Map<InetAddress, Integer> getInterfacesForNodes();

	OnmsIpInterface findPrimaryInterfaceByNodeId(Integer nodeId);

}
