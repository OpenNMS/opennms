/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.accesspointmonitor.rest;

import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsNode;

public class AccessPointDatabasePopulator {
    private NodeDao m_nodeDao;
    private AccessPointDao m_accessPointDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private OnmsNode m_node1;
    private OnmsAccessPoint m_ap1;

    public void populateDatabase() {
        m_node1 = new OnmsNode();
        m_node1.setLabel("AP1");
        getNodeDao().save(m_node1);
        getNodeDao().flush();

        m_ap1 = new OnmsAccessPoint("00:00:00:00:00:01", m_node1.getId(), "default");
        getAccessPointDao().save(m_ap1);
        getAccessPointDao().flush();
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setAccessPointDao(AccessPointDao accessPointDao) {
        m_accessPointDao = accessPointDao;
    }

    public AccessPointDao getAccessPointDao() {
        return m_accessPointDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public OnmsAccessPoint getAP1() {
        return m_ap1;
    }

    public void setAP1(OnmsAccessPoint ap) {
        m_ap1 = ap;
    }
}
