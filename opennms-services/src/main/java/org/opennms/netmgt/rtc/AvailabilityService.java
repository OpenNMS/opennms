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

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.rtc.datablock.RTCCategory;

/**
 * This interface contains all of the methods that RTC needs to fetch availability
 * data when posting the data to the web UI.
 * 
 * @author Seth
 */
public interface AvailabilityService {
    
    /**
     * Get the value(uptime) for the category in the last 'rollingWindow'
     * starting at current time
     *
     * @param catLabel
     *            the category to which the node should belong to
     * @param curTime
     *            the current time
     * @param rollingWindow
     *            the window for which value is to be calculated
     * @return the value(uptime) for the category in the last 'rollingWindow'
     *         starting at current time
     */
    double getValue(RTCCategory category, long curTime, long rollingWindow);

    /**
     * Get the value(uptime) for the nodeid in the last 'rollingWindow' starting
     * at current time in the context of the passed category
     *
     * @param nodeid
     *            the node for which value is to be calculated
     * @param catLabel
     *            the category to which the node should belong to
     * @param curTime
     *            the current time
     * @param rollingWindow
     *            the window for which value is to be calculated
     * @return the value(uptime) for the node in the last 'rollingWindow'
     *         starting at current time in the context of the passed category
     */
    double getValue(int nodeid, RTCCategory category, long curTime, long rollingWindow);

    /**
     * Get the service count for the nodeid in the context of the passed
     * category
     *
     * @param nodeid
     *            the node for which service count is to be calculated
     * @param catLabel
     *            the category to which the node should belong to
     * @return the service count for the nodeid in the context of the passed
     *         category
     */
    int getServiceCount(int nodeid, RTCCategory category);

    /**
     * Get the service down count for the nodeid in the context of the passed
     * category
     *
     * @param nodeid
     *            the node for which service down count is to be calculated
     * @param catLabel
     *            the category to which the node should belong to
     * @return the service down count for the nodeid in the context of the
     *         passed category
     */
    int getServiceDownCount(int nodeid, RTCCategory category);

    /**
     * <p>getCategories</p>
     *
     * @return the categories
     */
    Map<String, RTCCategory> getCategories();

    /**
     * 
     */
    Collection<Integer> getNodes(RTCCategory rtcCat);
}
