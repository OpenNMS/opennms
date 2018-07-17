/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import org.opennms.netmgt.alarmd.api.AlarmPersisterExtension;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.UpdateField;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;

/**
 * Singleton to persist OnmsAlarms.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class AlarmPersisterImpl implements AlarmPersister {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmPersisterImpl.class);

    protected static final Integer NUM_STRIPE_LOCKS = Integer.getInteger("org.opennms.alarmd.stripe.locks", Alarmd.THREADS * 4);

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private EventUtil m_eventUtil;

    @Autowired
    private TransactionOperations m_transactionOperations;

    @Autowired
    private AlarmEntityNotifier m_alarmEntityNotifier;

    private Striped<Lock> lockStripes = StripedExt.fairLock(NUM_STRIPE_LOCKS);

    private final Set<AlarmPersisterExtension> extensions = Sets.newConcurrentHashSet();

    @Override
    public OnmsAlarm persist(Event event) {
        Objects.requireNonNull(event, "Cannot create alarm from null event.");
        if (!checkEventSanityAndDoWeProcess(event)) {
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("process: {}; nodeid: {}; ipaddr: {}; serviceid: {}", event.getUei(), event.getNodeid(), event.getInterface(), event.getService());
        }

        // Lock both the reduction and clear keys (if set) using a fair striped lock
        // We do this to ensure that clears and triggers are processed in the same order
        // as the calls are made
        final Iterable<Lock> locks = lockStripes.bulkGet(getLockKeys(event));
        final OnmsAlarm alarm;
        try {
            locks.forEach(Lock::lock);
            // Process the alarm inside a transaction
            alarm = m_transactionOperations.execute((action) -> addOrReduceEventAsAlarm(event));
        } finally {
            locks.forEach(Lock::unlock);
        }

        return alarm;
    }

    private OnmsAlarm addOrReduceEventAsAlarm(Event event) {
        final OnmsEvent e = m_eventDao.get(event.getDbid());
        if (e == null) {
            throw new IllegalStateException("Event with id " + event.getDbid() + " was deleted before we could retrieve it and create an alarm.");
        }

        final String reductionKey = event.getAlarmData().getReductionKey();
        LOG.debug("addOrReduceEventAsAlarm: looking for existing reduction key: {}", reductionKey);
        OnmsAlarm alarm = m_alarmDao.findByReductionKey(reductionKey);

        if (alarm == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addOrReduceEventAsAlarm: reductionKey:{} not found, instantiating new alarm", reductionKey);
            }
            alarm = createNewAlarm(e, event);

            // Trigger extensions, allowing them to mangle the alarm
            try {
                final OnmsAlarm alarmCreated = alarm;
                extensions.forEach(ext -> ext.afterAlarmCreated(alarmCreated, event, e));
            } catch (Exception ex) {
                LOG.error("An error occurred while invoking the extension callbacks.", ex);
            }

            m_alarmDao.save(alarm);
            m_eventDao.saveOrUpdate(e);

            m_alarmEntityNotifier.didCreateAlarm(alarm);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addOrReduceEventAsAlarm: reductionKey:{} found, reducing event to existing alarm: {}", reductionKey, alarm.getIpAddr());
            }
            reduceEvent(e, alarm, event);

            // Trigger extensions, allowing them to mangle the alarm
            try {
                final OnmsAlarm alarmCreated = alarm;
                extensions.forEach(ext -> ext.afterAlarmUpdated(alarmCreated, event, e));
            } catch (Exception ex) {
                LOG.error("An error occurred while invoking the extension callbacks.", ex);
            }

            m_alarmDao.update(alarm);
            m_eventDao.update(e);

            if (event.getAlarmData().isAutoClean()) {
                m_eventDao.deletePreviousEventsForAlarm(alarm.getId(), e);
            }

            m_alarmEntityNotifier.didUpdateAlarmWithReducedEvent(alarm);
        }
        return alarm;
    }

    private void reduceEvent(OnmsEvent e, OnmsAlarm alarm, Event event) {
        // Always set these
        alarm.setLastEvent(e);
        alarm.setLastEventTime(e.getEventTime());
        alarm.setCounter(alarm.getCounter() + 1);

        if (!event.getAlarmData().hasUpdateFields()) {

            //We always set these even if there are not update fields specified
            alarm.setLogMsg(e.getEventLogMsg());
        } else {
            for (UpdateField field : event.getAlarmData().getUpdateFieldList()) {
                String fieldName = field.getFieldName();

                //Always set these, unless specified not to, in order to maintain current behavior
                if (fieldName.equalsIgnoreCase("LogMsg") && !field.isUpdateOnReduction()) {
                    continue;
                } else {
                    alarm.setLogMsg(e.getEventLogMsg());
                }

                //Set these others
                if (field.isUpdateOnReduction()) {

                    if (fieldName.toLowerCase().startsWith("distpoller")) {
                        alarm.setDistPoller(e.getDistPoller());
                    } else if (fieldName.toLowerCase().startsWith("ipaddr")) {
                        alarm.setIpAddr(e.getIpAddr());
                    } else if (fieldName.toLowerCase().startsWith("mouseover")) {
                        alarm.setMouseOverText(e.getEventMouseOverText());
                    } else if (fieldName.toLowerCase().startsWith("operinstruct")) {
                        alarm.setOperInstruct(e.getEventOperInstruct());
                    } else if (fieldName.equalsIgnoreCase("severity")) {
                        alarm.setSeverity(OnmsSeverity.valueOf(e.getSeverityLabel()));
                    } else if (fieldName.toLowerCase().contains("descr")) {
                        alarm.setDescription(e.getEventDescr());
                    } else if (fieldName.toLowerCase().startsWith("acktime")) {
                        final String expandedAckTime = m_eventUtil.expandParms(field.getValueExpression(), event);
                        if ("null".equalsIgnoreCase(expandedAckTime) && alarm.isAcknowledged()) {
                            alarm.unacknowledge("admin");
                        } else if ("".equals(expandedAckTime) || expandedAckTime.toLowerCase().startsWith("now")) { 
                            alarm.setAlarmAckTime(Calendar.getInstance().getTime());
                        } else if (expandedAckTime.matches("^\\d+$")) {
                            final long ackTimeLong;
                            try {
                                ackTimeLong = Long.valueOf(expandedAckTime);
                                // Heuristic: values < 2**31 * 1000 (10 Jan 2004) are probably whole seconds, otherwise milliseconds
                                if (ackTimeLong < 1073741824000L) {
                                    alarm.setAlarmAckTime(new java.util.Date(ackTimeLong * 1000));
                                } else {
                                    alarm.setAlarmAckTime(new java.util.Date(ackTimeLong));
                                }
                            } catch (NumberFormatException nfe) {
                                LOG.warn("Could not parse update-field 'acktime' value '{}' as a Long. Using current time instead.", expandedAckTime);
                                alarm.setAlarmAckTime(Calendar.getInstance().getTime());
                            }
                        } else if (expandedAckTime.toLowerCase().matches("^0x[0-9a-f]{22}$") || expandedAckTime.toLowerCase().matches("^0x[0-9a-f]{16}$")) {
                            alarm.setAlarmAckTime(m_eventUtil.decodeSnmpV2TcDateAndTime(new BigInteger(expandedAckTime.substring(2), 16)));
                        } else {
                            LOG.warn("Not sure what to do with update-field 'acktime' value '{}'. Using current time instead.", expandedAckTime);
                            alarm.setAlarmAckTime(Calendar.getInstance().getTime());
                        }
                    } else if (fieldName.toLowerCase().startsWith("ackuser")) {
                        final String expandedAckUser = m_eventUtil.expandParms(field.getValueExpression(), event);
                        if ("null".equalsIgnoreCase(expandedAckUser) || "".equals(expandedAckUser)) {
                            alarm.unacknowledge("admin");
                        } else {
                            alarm.setAlarmAckUser(expandedAckUser);
                        }
                    }
                } else {
                    LOG.warn("reduceEvent: The specified field: {}, is not supported.", fieldName);
                }
            }
        }

        Set<OnmsAlarm> relatedAlarms = getRelatedAlarms(event.getParmCollection());
        if (relatedAlarms != null && !relatedAlarms.isEmpty()) {
            // alarm.relatedAlarms becomes the union of any existing alarms and any in the event.
            for (OnmsAlarm related : relatedAlarms) {
                alarm.addRelatedAlarm(related);
            }
        }

        e.setAlarm(alarm);
    }

    private OnmsAlarm createNewAlarm(OnmsEvent e, Event event) {
        OnmsAlarm alarm = new OnmsAlarm();
        // Situations are denoted by the existance of related-reductionKeys
        alarm.setRelatedAlarms(getRelatedAlarms(event.getParmCollection()));
        alarm.setAlarmType(event.getAlarmData().getAlarmType());
        alarm.setClearKey(event.getAlarmData().getClearKey());
        alarm.setCounter(1);
        alarm.setDescription(e.getEventDescr());
        alarm.setDistPoller(e.getDistPoller());
        alarm.setFirstEventTime(e.getEventTime());
        alarm.setIfIndex(e.getIfIndex());
        alarm.setIpAddr(e.getIpAddr());
        alarm.setLastEventTime(e.getEventTime());
        alarm.setLastEvent(e);
        alarm.setLogMsg(e.getEventLogMsg());
        alarm.setMouseOverText(e.getEventMouseOverText());
        alarm.setNode(e.getNode()); 
        alarm.setOperInstruct(e.getEventOperInstruct());
        alarm.setReductionKey(event.getAlarmData().getReductionKey());
        alarm.setServiceType(e.getServiceType());
        alarm.setSeverity(OnmsSeverity.get(e.getEventSeverity()));
        alarm.setSuppressedUntil(e.getEventTime()); //UI requires this be set
        alarm.setSuppressedTime(e.getEventTime()); // UI requires this be set
        alarm.setUei(e.getEventUei());
        e.setAlarm(alarm);
        return alarm;
    }
    
    private Set<OnmsAlarm> getRelatedAlarms(List<Parm> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> reductionKeys = list.stream().filter(AlarmPersisterImpl::isRelatedReductionKeyWithContent).map(p -> p.getValue().getContent()).collect(Collectors.toSet());
        // Only existing alarms are returned. Reduction Keys for non-existing alarms are dropped.
        return reductionKeys.stream().map(reductionKey -> m_alarmDao.findByReductionKey(reductionKey)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static boolean isRelatedReductionKeyWithContent(Parm param) {
        return param.getParmName() != null
                // TOOD revisit using equals() when event_parameters table supports multiple params with the same name (see NMS-10214)
                && param.getParmName().startsWith("related-reductionKey")
                && param.getValue() != null
                && param.getValue().getContent() != null;
    }

    private static boolean checkEventSanityAndDoWeProcess(final Event event) {
        if (event.getLogmsg() != null && LogDestType.DONOTPERSIST.toString().equalsIgnoreCase(event.getLogmsg().getDest())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkEventSanity: uei '{}' marked as '{}'; not processing event.", event.getUei(), LogDestType.DONOTPERSIST);
            }
            return false;
        }

        if (event.getAlarmData() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkEventSanity: uei '{}' has no alarm data; not processing event.", event.getUei());
            }
            return false;
        }

        if (event.getDbid() <= 0) {
            throw new IllegalArgumentException("Incoming event has an illegal dbid (" + event.getDbid() + "), aborting");
        }

        return true;
    }

    private static Collection<String> getLockKeys(Event event) {
        if (event.getAlarmData().getClearKey() == null) {
            return Collections.singletonList(event.getAlarmData().getReductionKey());
        } else {
            return Arrays.asList(event.getAlarmData().getReductionKey(), event.getAlarmData().getClearKey());
        }
    }

    public TransactionOperations getTransactionOperations() {
        return m_transactionOperations;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        m_transactionOperations = transactionOperations;
    }

    /**
     * <p>setAlarmDao</p>
     *
     * @param alarmDao a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
     */
    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    /**
     * <p>getAlarmDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
     */
    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    /**
     * <p>setEventDao</p>
     *
     * @param eventDao a {@link org.opennms.netmgt.dao.api.EventDao} object.
     */
    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }

    /**
     * <p>getEventDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.EventDao} object.
     */
    public EventDao getEventDao() {
        return m_eventDao;
    }
    
    /**
     * <p>setEventUtil</p>
     * 
     * @param eventUtil
     */
    public void setEventUtil(EventUtil eventUtil) {
        m_eventUtil = eventUtil;
    }
    
    /**
     * <p>getEventUtil</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventUtil} object.
     */
    public EventUtil getEventUtil() {
        return m_eventUtil;
    }

    public AlarmEntityNotifier getAlarmChangeListener() {
        return m_alarmEntityNotifier;
    }

    public void setAlarmChangeListener(AlarmEntityNotifier alarmEntityNotifier) {
        m_alarmEntityNotifier = alarmEntityNotifier;
    }

    public void onExtensionRegistered(final AlarmPersisterExtension ext, final Map<String,String> properties) {
        LOG.debug("onExtensionRegistered: {} with properties: {}", ext, properties);
        extensions.add(ext);
    }

    public void onExtensionUnregistered(final AlarmPersisterExtension ext, final Map<String,String> properties) {
        LOG.debug("onExtensionUnregistered: {} with properties: {}", ext, properties);
        extensions.remove(ext);
    }
}
