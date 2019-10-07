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
