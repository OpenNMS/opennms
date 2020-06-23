/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.model.OnmsNode;

public class MonitoringLocationUtils {

    /**
     * Returns <b>true</b> if the given location name is <b>null</b> or the system default.
     *
     * @param locationName
     * @return
     */
    public static boolean isDefaultLocationName(String locationName) {
        return locationName == null || MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID.equals(locationName);
    }

    /**
     * Returns the name to which the given node is associated, or null if the if node
     * is associated to either the default location, or no location.
     *
     * @param node
     * @return
     */
    public static String getLocationNameOrNullIfDefault(OnmsNode node) {
        final String locationName = node.getLocation() != null ? node.getLocation().getLocationName() : null;
        return isDefaultLocationName(locationName) ? null : locationName;
    }

}
