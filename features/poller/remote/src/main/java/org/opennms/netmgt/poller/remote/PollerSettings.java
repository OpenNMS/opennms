/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

/**
 * This interface holds configuration parameters for the current
 * Remote Poller monitoring system instance (such as its ID).
 *
 * @author Seth
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface PollerSettings {

    /**
     * Get the monitoring system ID of this system. This value
     * will be a UUID that can uniquely identify this system
     * among a cluster of OpenNMS monitoring systems.
     *
     * @return a {@link java.lang.String} object.
     */
    String getMonitoringSystemId();

    /**
     * Set the monitoring system ID of this system. This value
     * must be a UUID that is unique among all of the systems in
     * the OpenNMS cluster with which this system is communicating.
     *
     * @param monitoringSystemId a {@link java.lang.String} object.
     */
    void setMonitoringSystemId(String monitoringSystemId);

}
