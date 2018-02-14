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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsEvent;

public interface EventDao extends LegacyOnmsDao<OnmsEvent, Integer> {

    int deletePreviousEventsForAlarm(final Integer id, final OnmsEvent e);

    /**
     * Returns a list of events which have been created
     * AFTER date and the uei of each event matches one uei entry of the ueiList.
     *
     * @param ueiList list with uei's
     * @param date    the date after which all events are loaded.
     * @return a list of events which have been created
     *         AFTER date and the uei of each event matches one uei entry of the ueiList.
     */
    List<OnmsEvent> getEventsAfterDate(List<String> ueiList, Date date);

    List<OnmsEvent> getEventsForEventParameters(final Map<String, String> eventParameters);

}
