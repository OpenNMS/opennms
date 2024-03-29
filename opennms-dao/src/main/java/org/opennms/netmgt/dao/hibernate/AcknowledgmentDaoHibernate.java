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
package org.opennms.netmgt.dao.hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.hibernate.ObjectNotFoundException;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate implementation of Acknowledgment DAO
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class AcknowledgmentDaoHibernate extends AbstractDaoHibernate<OnmsAcknowledgment, Integer> implements AcknowledgmentDao {

    private static final Logger LOG = LoggerFactory.getLogger(AcknowledgmentDaoHibernate.class);

    @Autowired
    private AlarmEntityNotifier alarmEntityNotifier;

    /**
     * <p>Constructor for AcknowledgmentDaoHibernate.</p>
     */
    public AcknowledgmentDaoHibernate() {
        super(OnmsAcknowledgment.class);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAckable(Acknowledgeable ackable) {
        getHibernateTemplate().update(ackable);
    }

    /** {@inheritDoc} */
    @Override
    public List<Acknowledgeable> findAcknowledgables(final OnmsAcknowledgment ack) {
        List<Acknowledgeable> ackables = new ArrayList<>();
        
        if (ack == null || ack.getAckType() == null) {
            return ackables;
        }

        if (ack.getAckType().equals(AckType.ALARM)) {
            final OnmsAlarm alarm = findAlarm(ack);

            try {
                if (alarm != null && alarm.getAckId() != null) {
                    ackables.add(alarm);
                    List<OnmsNotification> notifs = findRelatedNotifications(alarm);
                    
                    if (notifs != null) {
                        for (OnmsNotification notif : notifs) {
                            try {
                                if (notif.getAckId() != null) {
                                    ackables.add(notif);
                                }
                            } catch (final ObjectNotFoundException e) {
                                LOG.warn("found ackables for alarm #{} but ackable was invalid", ack.getRefId(), e);
                            }
                        }
                    }
                }
            } catch (final ObjectNotFoundException e) {
                LOG.warn("unable to find alarm with ID {}", ack.getRefId(), e);
            }
        }

        else if (ack.getAckType().equals(AckType.NOTIFICATION)) {
            final OnmsNotification notif = findNotification(ack);

            try {
                if (notif != null && notif.getAckId() != null) {
                    ackables.add(notif);
                    try {
                        if (notif.getEvent() != null) {
                            final OnmsAlarm alarm = notif.getEvent().getAlarm();
                            if (alarm != null) {
                                ackables.add(alarm);
                            }
                        }
                    } catch (final ObjectNotFoundException e) {
                        LOG.warn("unable to find alarm for notification #{}", notif.getNotifyId(), e);
                    }
                }
            } catch (final ObjectNotFoundException e) {
                LOG.warn("unable to find notification with ID {}", ack.getRefId(), e);
            }
        }
        
        return ackables;
    }
    
    private List<OnmsNotification> findRelatedNotifications(final OnmsAlarm alarm) {
        final String hql = "from OnmsNotification as n where n.event.alarm = ?";
        return findObjects(OnmsNotification.class, hql, alarm);
    }

    private OnmsAlarm findAlarm(final OnmsAcknowledgment ack) {
//      hql = "from OnmsAlarm as alarms where alarms.id = ?";        
//      return findUnique(OnmsAlarm.class, hql, ack.getRefId());
        try {
            if (ack != null) {
                return (OnmsAlarm) getHibernateTemplate().load(OnmsAlarm.class, ack.getRefId());
            }
        } catch (final Exception e) {
            LOG.warn("unable to find alarm with ID {}", ack.getRefId(), e);
        }
        return null;
    }

    private OnmsNotification findNotification(final OnmsAcknowledgment ack) {
//      hql = "from OnmsAlarm as alarms where alarms.id = ?";        
//      return findUnique(OnmsAlarm.class, hql, ack.getRefId());
        try {
            if (ack != null) {
                return (OnmsNotification) getHibernateTemplate().load(OnmsNotification.class, ack.getRefId());
            }
        } catch (final Exception e) {
            LOG.warn("unable to find notification with ID {}", ack.getRefId(), e);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=false)
    @Override
    public void processAcks(Collection<OnmsAcknowledgment> acks) {
        LOG.info("processAcks: Processing {} acknowledgements...", acks.size());
        for (OnmsAcknowledgment ack : acks) {
            processAck(ack);
        }
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=false)
    @Override
    public void processAck(OnmsAcknowledgment ack) {
        LOG.info("processAck: Searching DB for acknowledgables for ack: {}", ack);
        List<Acknowledgeable> ackables = findAcknowledgables(ack);
        
        if (ackables == null || ackables.size() < 1) {
            LOG.debug("processAck: No acknowledgables found.");
            throw new IllegalStateException("No acknowlegables in the database for ack: "+ack);
        }

        LOG.debug("processAck: Found {}. Acknowledging...", ackables.size());
        
        Iterator<Acknowledgeable> it = ackables.iterator();
        while (it.hasNext()) {
            try {
                Acknowledgeable ackable = it.next();

                final boolean isAlarm = ackable instanceof OnmsAlarm;
                Consumer<OnmsAlarm> callback = null;

                switch (ack.getAckAction()) {
                case ACKNOWLEDGE:
                    LOG.debug("processAck: Acknowledging ackable: {}...", ackable);
                    if (isAlarm) {
                        final String ackUser = ackable.getAckUser();
                        final Date ackTime = ackable.getAckTime();
                        callback = (alarm) -> alarmEntityNotifier.didAcknowledgeAlarm(alarm, ackUser, ackTime);
                    }
                    ackable.acknowledge(ack.getAckUser());
                    LOG.debug("processAck: Acknowledged ackable: {}", ackable);
                    break;
                case UNACKNOWLEDGE:
                    LOG.debug("processAck: Unacknowledging ackable: {}...", ackable);
                    if (isAlarm) {
                        final String ackUser = ackable.getAckUser();
                        final Date ackTime = ackable.getAckTime();
                        callback = (alarm) -> alarmEntityNotifier.didUnacknowledgeAlarm(alarm, ackUser, ackTime);
                    }
                    ackable.unacknowledge(ack.getAckUser());
                    LOG.debug("processAck: Unacknowledged ackable: {}", ackable);
                    break;
                case CLEAR:
                    LOG.debug("processAck: Clearing ackable: {}...", ackable);
                    if (isAlarm) {
                        ((OnmsAlarm) ackable).getRelatedAlarms().forEach(relatedAlarm -> clearRelatedAlarm(relatedAlarm));
                        final OnmsSeverity previousSeverity = ackable.getSeverity();
                        callback = (alarm) -> alarmEntityNotifier.didUpdateAlarmSeverity(alarm, previousSeverity);
                    }
                    ackable.clear(ack.getAckUser());
                    LOG.debug("processAck: Cleared ackable: {}", ackable);
                    break;
                case ESCALATE:
                    LOG.debug("processAck: Escalating ackable: {}...", ackable);
                    if (isAlarm) {
                        final OnmsSeverity previousSeverity = ackable.getSeverity();
                        callback = (alarm) -> alarmEntityNotifier.didUpdateAlarmSeverity(alarm, previousSeverity);
                    }
                    ackable.escalate(ack.getAckUser());
                    LOG.debug("processAck: Escalated ackable: {}", ackable);
                    break;
                default:
                    break;
                }

                updateAckable(ackable);
                save(ack);
                flush();

                if (callback != null) {
                    callback.accept((OnmsAlarm)ackable);
                }
            } catch (Throwable t) {
                LOG.error("processAck: exception while processing: {}; {}", ack, t);
            }
            
        }
        LOG.info("processAck: Found and processed acknowledgables for the acknowledgement: {}", ack);
    }

    private void clearRelatedAlarm(OnmsAlarm alarm) {
        OnmsAcknowledgment clear = new OnmsAcknowledgment(alarm);
        clear.setAckAction(AckAction.CLEAR);
        processAck(clear);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<OnmsAcknowledgment> findLatestAcks(Date from) {
        final String hqlQuery = "SELECT acks FROM OnmsAcknowledgment acks " +
                "WHERE acks.ackTime = (" +
                    "SELECT MAX(filteredAcks.ackTime) " +
                    "FROM OnmsAcknowledgment filteredAcks " +
                    "WHERE filteredAcks.refId = acks.refId) " +
                "AND acks.id = (" +
                    "SELECT MAX(filteredAcks.id) FROM OnmsAcknowledgment filteredAcks " +
                    "WHERE filteredAcks.refId = acks.refId) " +
                "AND acks.ackTime >= (:minAckTimeParm)";
        return (List<OnmsAcknowledgment>) getHibernateTemplate().findByNamedParam(hqlQuery, "minAckTimeParm", from);
    }

    @Override
    @Transactional
    public Optional<OnmsAcknowledgment> findLatestAckForRefId(Integer refId) {
        CriteriaBuilder builder = new CriteriaBuilder(OnmsAcknowledgment.class)
                .eq("refId", refId)
                .limit(1)
                .orderBy("ackTime").desc()
                .orderBy("id").desc();
        List<OnmsAcknowledgment> acks = findMatching(builder.toCriteria());

        return acks.size() == 1 ? Optional.of(acks.get(0)) : Optional.empty();
    }
}
