/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
