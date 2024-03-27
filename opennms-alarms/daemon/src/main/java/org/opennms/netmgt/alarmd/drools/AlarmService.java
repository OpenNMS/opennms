/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
