/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.poller.support;

import java.net.InetAddress;

import org.opennms.netmgt.poller.MonitoredService;

/**
 * The Class SimpleMonitoredService.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SimpleMonitoredService implements MonitoredService {
    
    /** The IP address. */
    private final InetAddress ipAddress;
    
    /** The node id. */
    private final int nodeId;

    /** The node label. */
    private final String nodeLabel;

    /** The service name. */
    private final String svcName;

    /** The location name. */
    private final String location;

    public SimpleMonitoredService(InetAddress ipAddress, int nodeId, String nodeLabel, String svcName, String location) {
        this.ipAddress = ipAddress;
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
        this.svcName = svcName;
        this.location = location;
    }

    /**
     * Instantiates a new simple monitored service.
     *
     * @param ipAddress the IP address
     * @param nodeId the node id
     * @param nodeLabel the node label
     * @param svcName the service name
     */
    public SimpleMonitoredService(final InetAddress ipAddress, int nodeId, String nodeLabel, String svcName) {
        this(ipAddress, nodeId, nodeLabel, svcName, null);
    }

    /**
     * Instantiates a new simple monitored service.
     *
     * @param ipAddress the IP address
     * @param svcName the service name
     */
    public SimpleMonitoredService(final InetAddress ipAddress, final String svcName) {
        this(ipAddress, 0, null, svcName, null);
    }

    public SimpleMonitoredService(final InetAddress ipAddress, final String svcName, final String location) {
        this(ipAddress, 0, null, svcName, location);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.MonitoredService#getSvcName()
     */
    @Override
    public String getSvcName() {
        return svcName;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.MonitoredService#getIpAddr()
     */
    @Override
    public String getIpAddr() {
        return ipAddress.getHostAddress();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.MonitoredService#getNodeId()
     */
    @Override
    public int getNodeId() {
        return nodeId;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.MonitoredService#getNodeLabel()
     */
    @Override
    public String getNodeLabel() {
        return nodeLabel;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.MonitoredService#getAddress()
     */
    @Override
    public InetAddress getAddress() {
        return ipAddress;
    }

    @Override
    public String getNodeLocation() {
        return location;
    }

}
