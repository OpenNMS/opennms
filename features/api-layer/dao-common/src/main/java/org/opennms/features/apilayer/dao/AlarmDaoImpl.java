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
package org.opennms.features.apilayer.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.graph.NodeRef;
import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.integration.api.v1.ticketing.Ticket.State;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

public class AlarmDaoImpl implements AlarmDao {

    private final org.opennms.netmgt.dao.api.AlarmDao alarmDao;
    private final AcknowledgmentDao ackDao;
    private final AlarmEntityNotifier alarmEntityNotifier;
    private final SessionUtils sessionUtils;

    public AlarmDaoImpl(org.opennms.netmgt.dao.api.AlarmDao alarmDao, final AcknowledgmentDao ackDao, final AlarmEntityNotifier alarmEntityNotifier, final SessionUtils sessionUtils) {
        this.alarmDao = Objects.requireNonNull(alarmDao);
        this.alarmEntityNotifier = Objects.requireNonNull(alarmEntityNotifier);
        this.ackDao = Objects.requireNonNull(ackDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public Long getAlarmCount() {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class);
        return sessionUtils.withReadOnlyTransaction(() -> (long)alarmDao.countMatching(criteriaBuilder.toCriteria()));
    }

    @Override
    public List<Alarm> getAlarms() {
        return sessionUtils.withReadOnlyTransaction(() ->
                alarmDao.findAll().stream().map(ModelMappers::toAlarm).collect(Collectors.toList()));
    }

    @Override
    public Optional<Alarm> getAlarmWithHighestSeverity(NodeRef nodeRef) {
        final Criteria criteria = new CriteriaBuilder(OnmsAlarm.class)
                .alias("node", "node")
                .orderBy("severity", false)
                .and(Restrictions.eq("node.foreignSource", nodeRef.getForeignSource()),
                     Restrictions.eq("node.foreignId", nodeRef.getForeignId()))
                .limit(1)
                .toCriteria();
        return sessionUtils.withReadOnlyTransaction(() -> {
            final List<OnmsAlarm> matching = alarmDao.findMatching(criteria);
            if (matching.isEmpty()) {
                return Optional.empty();
            }
            final Alarm alarm = ModelMappers.toAlarm(matching.get(0));
            return Optional.of(alarm);
        });
    }

    @Override
    public Optional<Alarm> getAlarmForTicket(final String ticketId) {
        final Criteria criteria = new CriteriaBuilder(OnmsAlarm.class)
                .and(Restrictions.eq("tticketId", ticketId))
                .limit(1)
                .toCriteria();
        return sessionUtils.withReadOnlyTransaction(() -> {
            final List<OnmsAlarm> matching = alarmDao.findMatching(criteria);
            if (matching.isEmpty()) {
                return Optional.empty();
            }
            final Alarm alarm = ModelMappers.toAlarm(matching.get(0));
            return Optional.of(alarm);
        });
    }

    @Override
    public void setTicketState(final State state, final int... alarmIds) {
        sessionUtils.withTransaction(() -> {
            Arrays.stream(alarmIds).boxed().map(alarmDao::get).forEach(alarm -> {
                final TroubleTicketState previousState = alarm.getTTicketState();
                alarm.setTTicketState(ModelMappers.fromTicketState(state));
                alarmDao.saveOrUpdate(alarm);
                alarmEntityNotifier.didChangeTicketStateForAlarm(alarm, previousState);
            });
        });
    }

    @Override
    public void acknowledge(final String user, final int... alarmIds) {
        sessionUtils.withTransaction(() -> {
            final List<OnmsAcknowledgment> acks = Arrays.stream(alarmIds).boxed().map(id -> {
                final OnmsAcknowledgment ack = new OnmsAcknowledgment(user);
                ack.setAckType(AckType.ALARM);
                ack.setAckAction(AckAction.ACKNOWLEDGE);
                ack.setRefId(id);
                return ack;
            }).collect(Collectors.toUnmodifiableList());
            ackDao.processAcks(acks);
        });
    }

    @Override
    public void unacknowledge(final int... alarmIds) {
        sessionUtils.withTransaction(() -> {
            final List<OnmsAcknowledgment> acks = Arrays.stream(alarmIds).boxed().map(id -> {
                final OnmsAcknowledgment ack = new OnmsAcknowledgment();
                ack.setAckType(AckType.ALARM);
                ack.setAckAction(AckAction.UNACKNOWLEDGE);
                ack.setRefId(id);
                return ack;
            }).collect(Collectors.toUnmodifiableList());
            ackDao.processAcks(acks);
        });
    }

    @Override
    public void escalate(final String user, final int... alarmIds) {
        sessionUtils.withTransaction(() -> {
            final List<OnmsAcknowledgment> acks = Arrays.stream(alarmIds).boxed().map(id -> {
                final OnmsAcknowledgment ack = new OnmsAcknowledgment(user);
                ack.setAckType(AckType.ALARM);
                ack.setAckAction(AckAction.ESCALATE);
                ack.setRefId(id);
                return ack;
            }).collect(Collectors.toUnmodifiableList());
            ackDao.processAcks(acks);
        });
    }

    @Override
    public void clear(final int... alarmIds) {
        sessionUtils.withTransaction(() -> {
            final List<OnmsAcknowledgment> acks = Arrays.stream(alarmIds).boxed().map(id -> {
                final OnmsAcknowledgment ack = new OnmsAcknowledgment();
                ack.setAckType(AckType.ALARM);
                ack.setAckAction(AckAction.CLEAR);
                ack.setRefId(id);
                return ack;
            }).collect(Collectors.toUnmodifiableList());
            ackDao.processAcks(acks);
        });
    }

    @Override
    public void setSeverity(final Severity severity, final int... alarmIds) {
        final var onmsSeverity = ModelMappers.fromSeverity(severity);
        sessionUtils.withTransaction(() -> {
            final Criteria criteria = new CriteriaBuilder(OnmsAlarm.class)
                    .and(Restrictions.in("id", Arrays.asList(alarmIds)))
                    .toCriteria();
            final List<OnmsAlarm> alarms = alarmDao.findMatching(criteria);
            alarms.forEach(alarm -> {
                final OnmsSeverity previousSeverity = alarm.getSeverity();
                alarm.setSeverity(onmsSeverity);
                alarmDao.saveOrUpdate(alarm);
                alarmEntityNotifier.didUpdateAlarmSeverity(alarm, previousSeverity);
            });
        });
    }
}
