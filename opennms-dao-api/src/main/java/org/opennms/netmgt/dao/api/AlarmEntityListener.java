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
package org.opennms.netmgt.dao.api;

import java.util.Date;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * Used to get callbacks when alarm entities are created, updated and/or deleted.
 *
 * @author jwhite
 */
public interface AlarmEntityListener {

    void onAlarmCreated(OnmsAlarm alarm);

    void onAlarmUpdatedWithReducedEvent(OnmsAlarm alarm);

    void onAlarmAcknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime);

    void onAlarmUnacknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime);

    void onAlarmSeverityUpdated(OnmsAlarm alarm, OnmsSeverity previousSeverity);

    void onAlarmArchived(OnmsAlarm alarm, String previousReductionKey);

    void onAlarmDeleted(OnmsAlarm alarm);

    void onStickyMemoUpdated(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated);

    void onReductionKeyMemoUpdated(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated);

    void onStickyMemoDeleted(OnmsAlarm alarm, OnmsMemo memo);

    void onReductionKeyMemoDeleted(OnmsAlarm alarm, OnmsReductionKeyMemo memo);

    void onLastAutomationTimeUpdated(OnmsAlarm alarm, Date previousLastAutomationTime);

    void onRelatedAlarmsUpdated(OnmsAlarm alarm, Set<OnmsAlarm> previousRelatedAlarms);

    void onTicketStateChanged(OnmsAlarm alarm, TroubleTicketState previousState);

}
