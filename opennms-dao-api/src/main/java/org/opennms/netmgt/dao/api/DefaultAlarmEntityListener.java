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

package org.opennms.netmgt.dao.api;

import java.util.Date;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.OnmsSeverity;

public class DefaultAlarmEntityListener implements AlarmEntityListener {
    @Override
    public void onAlarmCreated(OnmsAlarm alarm) {

    }

    @Override
    public void onAlarmUpdatedWithReducedEvent(OnmsAlarm alarm) {

    }

    @Override
    public void onAlarmAcknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime) {

    }

    @Override
    public void onAlarmUnacknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime) {

    }

    @Override
    public void onAlarmSeverityUpdated(OnmsAlarm alarm, OnmsSeverity previousSeverity) {

    }

    @Override
    public void onAlarmDeleted(OnmsAlarm alarm) {

    }

    @Override
    public void onStickyMemoUpdated(OnmsAlarm onmsAlalarmarm, String previousBody, String previousAuthor, Date previousUpdated) {

    }

    @Override
    public void onReductionKeyMemoUpdated(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated) {

    }

    @Override
    public void onStickyMemoDeleted(OnmsAlarm alarm, OnmsMemo memo) {

    }

    @Override
    public void onReductionKeyMemoDeleted(OnmsAlarm alarm, OnmsReductionKeyMemo memo) {

    }

    @Override
    public void onLastAutomationTimeUpdated(OnmsAlarm alarm, Date previousLastAutomationTime) {

    }

    @Override
    public void onRelatedAlarmsUpdated(OnmsAlarm alarm, Set<OnmsAlarm> previousRelatedAlarms) {

    }
}
