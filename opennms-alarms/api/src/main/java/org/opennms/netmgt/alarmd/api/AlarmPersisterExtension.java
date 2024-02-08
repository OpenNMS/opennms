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
