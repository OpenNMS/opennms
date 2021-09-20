/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.xml.event.Event;

/**
 * This interface allows extensions to modify the alarm
 * after alarmd has created the alarm, or updated it with a reduced event, but
 * before it is persisted.
 *
 * This can be used to add additional logic and set additional fields on the alarms
 * before other processes and components are notified.
 *
 * Implementations must be thread safe since alarmd will issue these callbacks over many threads.
 *
 * @author jwhite
 */
public interface AlarmPersisterExtension {

    /**
     * Invoked by the AlarmPersister after the alarm has been created, but *before*
     * the call the save the object via the DAO is made.
     *
     * @param alarm the alarm that was created
     * @param event the event that triggered the alarm
     * @param dbEvent the database entity associated with the given event
     */
    void afterAlarmCreated(OnmsAlarm alarm, Event event, OnmsEvent dbEvent);

    /**
     * Invoked by the AlarmPersister after the alarm has been updated, but *before*
     * the call the save the object via the DAO is made.
     *
     * @param alarm the alarm that was update
     * @param event the event that triggered the update to the alarm
     * @param dbEvent the database entity associated with the given event
     */
    void afterAlarmUpdated(OnmsAlarm alarm, Event event, OnmsEvent dbEvent);

}
