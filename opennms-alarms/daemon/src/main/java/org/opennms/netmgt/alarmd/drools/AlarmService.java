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

package org.opennms.netmgt.alarmd.drools;

import java.util.Date;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;

/**
 * This API is intended to provide RHS functionality for Drools Alarmd and
 * Situation rules.
 */
public interface AlarmService {

    void clearAlarm(OnmsAlarm alarm, Date now);

    void deleteAlarm(OnmsAlarm alarm);

    void unclearAlarm(OnmsAlarm alarm, Date now);

    void escalateAlarm(OnmsAlarm alarm, Date now);

    void acknowledgeAlarm(OnmsAlarm alarm, Date now);

    void unacknowledgeAlarm(OnmsAlarm alarm, Date now);

    void setSeverity(OnmsAlarm alarm, OnmsSeverity severity, Date now);

    void debug(String message, Object... objects);

    void info(String message, Object... objects);

    void warn(String message, Object... objects);

    /**
     * Asynchronously broadcast the given event.
     *
     * @param e event to broadcast
     */
    void sendEvent(Event e);

}
