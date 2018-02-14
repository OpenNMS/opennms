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

package org.opennms.core.utils;

public class LocationUtils {

    public static final String DEFAULT_LOCATION_NAME = "Default";

    /**
     * Returns the effective location name, using the default value if the given
     * location name is <b>null</b> or an empty string.
     *
     * @param locationName
     * @return the effective location name
     */
    public static String getEffectiveLocationName(final String locationName) {
        if (locationName == null || locationName.isEmpty()) {
            return DEFAULT_LOCATION_NAME;
        }
        return locationName;
    }

    /**
     * Returns <b>true</b> if the given location name is <b>null</b>, an empty string,
     * or the system default.
     *
     * @param locationName
     * @return
     */
    public static boolean isDefaultLocationName(String locationName) {
        return DEFAULT_LOCATION_NAME.equals(getEffectiveLocationName(locationName));
    }

}
