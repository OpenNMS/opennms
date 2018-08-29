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

package org.opennms.netmgt.dao.support;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.opennms.netmgt.dao.api.AlarmEntityListener;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class AlarmEntityNotifierImpl implements AlarmEntityNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmEntityNotifierImpl.class);

    private Set<AlarmEntityListener> listeners = Sets.newConcurrentHashSet();

    @Override
    public void didCreateAlarm(OnmsAlarm alarm) {
        forEachListener(l -> l.onAlarmCreated(alarm));
    }

    @Override
    public void didUpdateAlarmWithReducedEvent(OnmsAlarm alarm) {
        forEachListener(l -> l.onAlarmUpdatedWithReducedEvent(alarm));
    }

    @Override
    public void didAcknowledgeAlarm(OnmsAlarm alarm, String previousAckUser, Date previousAckTime) {
        forEachListener(l -> l.onAlarmAcknowledged(alarm, previousAckUser, previousAckTime));
    }

    @Override
    public void didUnacknowledgeAlarm(OnmsAlarm alarm, String previousAckUser, Date previousAckTime) {
        forEachListener(l -> l.onAlarmUnacknowledged(alarm, previousAckUser, previousAckTime));
    }

    @Override
    public void didUpdateAlarmSeverity(OnmsAlarm alarm, OnmsSeverity previousSeverity) {
        forEachListener(l -> l.onAlarmSeverityUpdated(alarm, previousSeverity));
    }

    @Override
    public void didDeleteAlarm(OnmsAlarm alarm) {
        forEachListener(l -> l.onAlarmDeleted(alarm));
    }

    @Override
    public void didUpdateStickyMemo(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated) {
        forEachListener(l -> l.onStickyMemoUpdated(alarm, previousBody, previousAuthor, previousUpdated));
    }

    @Override
    public void didUpdateReductionKeyMemo(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated) {
        forEachListener(l -> l.onReductionKeyMemoUpdated(alarm, previousBody, previousAuthor, previousUpdated));
    }

    @Override
    public void didDeleteStickyMemo(OnmsAlarm alarm, OnmsMemo memo) {
        forEachListener(l -> l.onStickyMemoDeleted(alarm, memo));
    }

    @Override
    public void didDeleteReductionKeyMemo(OnmsAlarm alarm, OnmsReductionKeyMemo memo) {
        forEachListener(l -> l.onReductionKeyMemoDeleted(alarm, memo));
    }

    @Override
    public void didUpdateLastAutomationTime(OnmsAlarm alarm, Date previousLastAutomationTime) {
        forEachListener(l -> l.onLastAutomationTimeUpdated(alarm, previousLastAutomationTime));
    }

    @Override
    public void didUpdateRelatedAlarms(OnmsAlarm alarm, Set<OnmsAlarm> previousRelatedAlarms) {
        forEachListener(l -> l.onRelatedAlarmsUpdated(alarm, previousRelatedAlarms));
    }

    private void forEachListener(Consumer<AlarmEntityListener> callback) {
        for (AlarmEntityListener listener : listeners) {
            try {
                callback.accept(listener);
            } catch (Exception e) {
                LOG.error("Error occurred while invoking listener: {}. Skipping.", listener, e);
            }
        }
    }

    public void onListenerRegistered(final AlarmEntityListener listener, final Map<String,String> properties) {
        LOG.debug("onListenerRegistered: {} with properties: {}", listener, properties);
        listeners.add(listener);
    }

    public void onListenerUnregistered(final AlarmEntityListener listener, final Map<String,String> properties) {
        LOG.debug("onListenerUnregistered: {} with properties: {}", listener, properties);
        listeners.remove(listener);
    }

}
