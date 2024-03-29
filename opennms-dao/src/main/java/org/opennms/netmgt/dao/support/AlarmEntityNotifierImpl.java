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
import org.opennms.netmgt.model.TroubleTicketState;
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
    public void didArchiveAlarm(OnmsAlarm alarm, String previousReductionKey) {
        forEachListener(l -> l.onAlarmArchived(alarm, previousReductionKey));
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

    @Override
    public void didChangeTicketStateForAlarm(OnmsAlarm alarm, TroubleTicketState previousState) {
        forEachListener(l -> l.onTicketStateChanged(alarm, previousState));
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
