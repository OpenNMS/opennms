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
package org.opennms.features.alarms.history.rest.impl;

import java.util.Collection;
import java.util.Objects;

import org.opennms.features.alarms.history.api.AlarmHistoryRepository;
import org.opennms.features.alarms.history.api.AlarmState;
import org.opennms.features.alarms.history.rest.api.AlarmHistoryRestService;

public class AlarmHistoryRestServiceImpl implements AlarmHistoryRestService {

    public static final String REDUCTION_KEY_MATCH_TYPE = "reduction-key";

    private final AlarmHistoryRepository alarmHistoryRepository;

    public AlarmHistoryRestServiceImpl(AlarmHistoryRepository alarmHistoryRepository) {
        this.alarmHistoryRepository = Objects.requireNonNull(alarmHistoryRepository);
    }

    @Override
    public Collection<AlarmState> getStatesForAlarm(String alarmId, String matchType) {
        if (REDUCTION_KEY_MATCH_TYPE.equals(matchType)) {
            return alarmHistoryRepository.getStatesForAlarmWithReductionKey(matchType);
        }
        return alarmHistoryRepository.getStatesForAlarmWithDbId(Integer.valueOf(alarmId));
    }

    @Override
    public AlarmState getAlarm(String alarmId, String matchType, Long time) {
        long timestampInMillis = time == null ? System.currentTimeMillis() : time;
        if (REDUCTION_KEY_MATCH_TYPE.equals(matchType)) {
            return alarmHistoryRepository.getAlarmWithReductionKeyIdAt(alarmId, timestampInMillis).orElse(null);
        }
        return alarmHistoryRepository.getAlarmWithDbIdAt(Integer.valueOf(alarmId), timestampInMillis).orElse(null);
    }

    @Override
    public Collection<AlarmState> getActiveAlarmsAt(Long time) {
        long timestampInMillis = time == null ? System.currentTimeMillis() : time;
        return alarmHistoryRepository.getActiveAlarmsAt(timestampInMillis);
    }

}
