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
 * This interface provide functions that should be called
 * immediately after changing the alarm entities while maintaining
 * an open transaction.
 *
 * The implementation should in turn notify any interested listeners
 * i.e. northbounders, correlation engines, etc... about the state change.
 *
 * The implementation should be thread safe.
 *
 * @author jwhite
 */
public interface AlarmEntityNotifier {

    void didCreateAlarm(OnmsAlarm alarm);

    void didUpdateAlarmWithReducedEvent(OnmsAlarm alarm);

    void didAcknowledgeAlarm(OnmsAlarm alarm, String previousAckUser, Date previousAckTime);

    void didUnacknowledgeAlarm(OnmsAlarm alarm, String previousAckUser, Date previousAckTime);

    void didUpdateAlarmSeverity(OnmsAlarm alarm, OnmsSeverity previousSeverity);

    void didArchiveAlarm(OnmsAlarm alarm, String previousReductionKey);

    void didDeleteAlarm(OnmsAlarm alarm);

    void didUpdateStickyMemo(OnmsAlarm onmsAlarm, String previousBody, String previousAuthor, Date previousUpdated);

    void didUpdateReductionKeyMemo(OnmsAlarm onmsAlarm, String previousBody, String previousAuthor, Date previousUpdated);

    void didDeleteStickyMemo(OnmsAlarm onmsAlarm, OnmsMemo memo);

    void didDeleteReductionKeyMemo(OnmsAlarm onmsAlarm, OnmsReductionKeyMemo memo);

    void didUpdateLastAutomationTime(OnmsAlarm alarm, Date previousLastAutomationTime);

    void didUpdateRelatedAlarms(OnmsAlarm alarm, Set<OnmsAlarm> previousRelatedAlarms);

    void didChangeTicketStateForAlarm(OnmsAlarm alarm, TroubleTicketState previousState);

}
