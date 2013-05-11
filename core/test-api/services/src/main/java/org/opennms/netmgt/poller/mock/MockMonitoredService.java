/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

public class MockMonitoredService implements MonitoredService {
    private final int m_nodeId;
    private String m_nodeLabel;
    private final String m_ipAddr;
    private final String m_svcName;
    private InetAddress m_inetAddr;

    public MockMonitoredService(int nodeId, String nodeLabel, InetAddress inetAddress, String svcName) throws UnknownHostException {
        m_nodeId = nodeId;
        m_nodeLabel = nodeLabel;
        m_inetAddr = inetAddress;
        m_svcName = svcName;
        m_ipAddr = InetAddressUtils.str(m_inetAddr);
    }

    @Override
    public String getSvcName() {
        return m_svcName;
    }

    @Override
    public String getIpAddr() {
        return m_ipAddr;
    }

    @Override
    public int getNodeId() {
        return m_nodeId;
    }

    @Override
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    @Override
    public NetworkInterface<InetAddress> getNetInterface() {
        return new InetNetworkInterface(m_inetAddr);
    }

    @Override
    public InetAddress getAddress() {
        return m_inetAddr;
    }

    @Override
    public String getSvcUrl() {
        return null;
    }
}
