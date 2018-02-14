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
