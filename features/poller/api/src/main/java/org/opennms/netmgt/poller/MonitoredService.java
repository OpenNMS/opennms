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
package org.opennms.netmgt.poller;

import java.net.InetAddress;


/**
 * <p>MonitoredService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface MonitoredService {

    /**
     * Returns the svcName associated with this monitored service.
     *
     * @return the svcName
     */
    String getSvcName();

    /**
     * Returns the ipAddr string associated with this monitored service.
     *
     * @return the ipAddr string
     */
    String getIpAddr();

    /**
     * Returns the nodeId of the node that this service is associated with.
     *
     * @return the nodeid
     */
    int getNodeId();

    /**
     * Returns the label of the node that this service is associated with.
     *
     * @return the nodelabel
     */
    String getNodeLabel();

    /**
     * Returns the name of the location of the node that this service is associated with.
     *
     * @return the nodelocation
     */
    String getNodeLocation();

    /**
     * Returns the {@link InetAddress} associated with the service
     *
     * @return the {@link InetAddress}
     */
    InetAddress getAddress();

}
