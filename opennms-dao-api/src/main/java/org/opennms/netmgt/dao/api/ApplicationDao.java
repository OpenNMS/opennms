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

package org.opennms.netmgt.dao.api;

import java.util.List;

import org.opennms.netmgt.model.OnmsApplication;

/**
 * <p>ApplicationDao interface.</p>
 */
public interface ApplicationDao extends OnmsDao<OnmsApplication, Integer> {

    /**
     * <p>findByName</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsApplication} object.
     */
    OnmsApplication findByName(String label);

    /**
     * Determine the application's status.
     * As applications do not have a direct status attached, the status is calculated based on the nodeDown,
     * interfaceDown or serviceLost events/alarms from the application's monitored services.
     *
     * @return the application's status.
     */
    List<ApplicationStatus> getApplicationStatus();

    /**
     * same as {@link #getApplicationStatus()} but only calculates the status for the given applications.
     *
     * @param applications The applications to calculate the status for.
     * @return The application's status.
     */
    List<ApplicationStatus> getApplicationStatus(List<OnmsApplication> applications);

    /**
     * Load all alarms from the alarm table which have a node id, ip address and service type set.
     *
     * @return all alarms from the alarm table which have a node id, ip address and service type set.
     */
    List<ApplicationStatusEntity> getAlarmStatus();
}
