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

package org.opennms.netmgt.rtc;

import java.util.Map;

import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.xml.rtc.EuiLevel;

/**
 * This interface contains all of the methods that RTC needs to fetch availability
 * data when posting the data to the web UI.
 * 
 * @author Seth
 */
public interface AvailabilityService {

    /**
     * Builds a map of configured categories, keyed by label.
     *
     * @return the categories
     */
    Map<String, RTCCategory> getCategories();

    /**
     * Retrieves the availability statistics for the given category
     *
     * @return a {@link org.opennms.netmgt.xml.rtc.EuiLevel} object.
     */
    EuiLevel getEuiLevel(RTCCategory category);
}
