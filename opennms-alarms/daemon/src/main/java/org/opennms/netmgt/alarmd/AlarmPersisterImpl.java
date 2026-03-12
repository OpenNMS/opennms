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
package org.opennms.netmgt.alarmd;

import java.math.BigInteger;
import java.net.InetAddress;
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

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.utils.SystemInfoUtils;

import org.opennms.netmgt.alarmd.api.AlarmPersisterExtension;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.UpdateField;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.ImmutableSet;
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

    public static final String RELATED_REDUCTION_KEY_PREFIX = "related-reductionKey";

    protected static final Integer NUM_STRIPE_LOCKS = SystemProperties.getInteger("org.opennms.alarmd.stripe.locks", Alarmd.THREADS * 4);
    protected static boolean NEW_IF_CLEARED = Boolean.getBoolean("org.opennms.alarmd.newIfClearedAlarmExists");
    protected static boolean LEGACY_ALARM_STATE = Boolean.getBoolean("org.opennms.alarmd.legacyAlarmState");

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private EventUtil m_eventUtil;

    @Autowired
    private TransactionOperations m_transactionOperations;

    @Autowired
    private AlarmEntityNotifier m_alarmEntityNotifier;

    private Striped<Lock> lockStripes = StripedExt.fairLock(NUM_STRIPE_LOCKS);

    private final Set<AlarmPersisterExtension> extensions = Sets.newConcurrentHashSet();

    private boolean m_createNewAlarmIfClearedAlarmExists = LEGACY_ALARM_STATE == true ? false : NEW_IF_CLEARED;
    
    private boolean m_legacyAlarmState = LEGACY_ALARM_STATE;

    @Override
    public OnmsAlarm persist(Event event) {
        Objects.requireNonNull(event, "Cannot create alarm from null event.");
        if (!checkEventSanityAndDoWeProcess(event)) {
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("process: {}; nodeid: {}; ipaddr: {}; serviceid: {}, instanceId={}, eventId={}",
                event.getUei(), event.getNodeid(), event.getInterface(), event.getService(),
                SystemInfoUtils.getInstanceId(), event.getDbid());
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
        } catch (Exception e) {
            LOG.warn("Exception while reducing event to alarm, instanceId={}, eventId={}",
                SystemInfoUtils.getInstanceId(), event.getDbid(), e);
            return null;
        } finally {
            locks.forEach(Lock::unlock);
        }

        return alarm;
    }

    private OnmsAlarm addOrReduceEventAsAlarm(Event event) throws IllegalStateException {
        final String reductionKey = event.getAlarmData().getReductionKey();
        LOG.debug("addOrReduceEventAsAlarm: looking for existing reduction key: {}", reductionKey);

        String key = reductionKey;
        String clearKey = event.getAlarmData().getClearKey();

        boolean didSwapReductionKeyWithClearKey = false;
        if (!m_legacyAlarmState && clearKey != null && isResolutionEvent(event)) {
            key = clearKey;
            didSwapReductionKeyWithClearKey = true;
        }

        OnmsAlarm alarm = m_alarmDao.findByReductionKey(key);

        if (alarm == null && didSwapReductionKeyWithClearKey) {
            alarm = m_alarmDao.findByReductionKey(reductionKey);
        }

        if (alarm == null || (m_createNewAlarmIfClearedAlarmExists && OnmsSeverity.CLEARED.equals(alarm.getSeverity()))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addOrReduceEventAsAlarm: reductionKey:{} not found, instantiating new alarm", reductionKey);
            }

            if (alarm != null) {
                LOG.debug("addOrReduceEventAsAlarm: \"archiving\" cleared Alarm for problem: {}; " +
                        "A new alarm will be instantiated to manage the problem.", reductionKey);
                alarm.archive();
                m_alarmDao.save(alarm);
                m_alarmDao.flush();

                m_alarmEntityNotifier.didArchiveAlarm(alarm, reductionKey);
            }

            alarm = createNewAlarm(event);

            // Trigger extensions, allowing them to mangle the alarm
            try {
                final OnmsAlarm alarmCreated = alarm;
                extensions.forEach(ext -> ext.afterAlarmCreated(alarmCreated, event));
            } catch (Exception ex) {
                LOG.error("An error occurred while invoking the extension callbacks, instanceId={}, alarmId={}",
                    SystemInfoUtils.getInstanceId(), alarm.getId(), ex);
            }

            m_alarmDao.save(alarm);

            m_alarmEntityNotifier.didCreateAlarm(alarm);
        } else {
            LOG.debug("addOrReduceEventAsAlarm: reductionKey:{} found, reducing event to existing alarm: {}, instanceId={}, alarmId={}",
                reductionKey, alarm.getId(), SystemInfoUtils.getInstanceId(), alarm.getId());
            reduceEvent(alarm, event);

            // Trigger extensions, allowing them to mangle the alarm
            try {
                final OnmsAlarm alarmUpdated = alarm;
                extensions.forEach(ext -> ext.afterAlarmUpdated(alarmUpdated, event));
            } catch (Exception ex) {
                LOG.error("An error occurred while invoking the extension callbacks, instanceId={}, alarmId={}",
                    SystemInfoUtils.getInstanceId(), alarm.getId(), ex);
            }

            m_alarmDao.update(alarm);

            m_alarmEntityNotifier.didUpdateAlarmWithReducedEvent(alarm);
        }
        return alarm;
    }

    private void reduceEvent(OnmsAlarm alarm, Event event) {
        // Populate denormalized event fields
        populateAlarmFromEvent(alarm, event);

        if (!isResolutionEvent(event)) {
            incrementCounter(alarm);

            if (isResolvedAlarm(alarm)) {
                resetAlarmSeverity(event, alarm);
            }
        } else {

            if (isResolvedAlarm(alarm)) {
                incrementCounter(alarm);
            } else {
                alarm.setSeverity(OnmsSeverity.CLEARED);
            }
        }
        alarm.setAlarmType(event.getAlarmData().getAlarmType());

        String eventLogMsg = event.getLogmsg() != null ? event.getLogmsg().getContent() : null;

        if (!event.getAlarmData().hasUpdateFields()) {
            alarm.setLogMsg(eventLogMsg);
        } else {
            for (UpdateField field : event.getAlarmData().getUpdateFieldList()) {
                String fieldName = field.getFieldName();

                if (fieldName.equalsIgnoreCase("LogMsg") && !field.isUpdateOnReduction()) {
                    continue;
                } else {
                    alarm.setLogMsg(eventLogMsg);
                }

                if (field.isUpdateOnReduction()) {

                    if (fieldName.toLowerCase().startsWith("distpoller")) {
                        alarm.setDistPoller(resolveDistPoller(event));
                    } else if (fieldName.toLowerCase().startsWith("ipaddr")) {
                        alarm.setIpAddr(resolveIpAddr(event));
                    } else if (fieldName.toLowerCase().startsWith("mouseover")) {
                        alarm.setMouseOverText(event.getMouseovertext());
                    } else if (fieldName.toLowerCase().startsWith("operinstruct")) {
                        alarm.setOperInstruct(event.getOperinstruct());
                    } else if (fieldName.equalsIgnoreCase("severity")) {
                        resetAlarmSeverity(event, alarm);
                    } else if (fieldName.toLowerCase().contains("descr")) {
                        alarm.setDescription(event.getDescr());
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
                    LOG.warn("reduceEvent: The specified field: {}, is not supported, instanceId={}, alarmId={}",
                        fieldName, SystemInfoUtils.getInstanceId(), alarm.getId());
                }
            }
        }

        updateRelatedAlarms(alarm, event);
    }
    
    private void updateRelatedAlarms(OnmsAlarm alarm, Event event) {
        // Retrieve the related alarms as given by the event parameters
        final Set<OnmsAlarm> relatedAlarms = getRelatedAlarms(event.getParmCollection());
        // Index these by id
        final Map<Integer, OnmsAlarm> relatedAlarmsByIds = relatedAlarms.stream()
                .collect(Collectors.toMap(OnmsAlarm::getId, a -> a));

        // Build sets of the related alarm ids for easy comparison
        final Set<Integer> relatedAlarmIdsFromEvent = ImmutableSet.copyOf(relatedAlarmsByIds.keySet());
        final Set<Integer> relatedAlarmIdsFromExistingAlarm = ImmutableSet.copyOf(alarm.getRelatedAlarmIds());

        // Remove alarms that are not referenced in the event -  we treat the event as an
        // authoritative source of the related alarms rather than using the union of the previously known related alarms
        // and the event's related alarms
        Sets.difference(relatedAlarmIdsFromExistingAlarm, relatedAlarmIdsFromEvent)
                .forEach(alarm::removeRelatedAlarmWithId);
        // Add new alarms that are referenced in the event, but are not already associated
        Sets.difference(relatedAlarmIdsFromEvent, relatedAlarmIdsFromExistingAlarm)
                .forEach(relatedAlarmIdToAdd -> {
                    final OnmsAlarm related = relatedAlarmsByIds.get(relatedAlarmIdToAdd);
                    if (related != null) {
                        if (!formingCyclicGraph(alarm, related)) {
                            alarm.addRelatedAlarm(related);
                        } else {
                            LOG.warn("Alarm with id '{}' , reductionKey '{}' is not added as related alarm for id '{}' as it is forming cyclic graph ",
                                    related.getId(), related.getReductionKey(), alarm.getId());
                        }
                    }
                });
    }

    private void resetAlarmSeverity(Event event, OnmsAlarm alarm) {
        if (event.getSeverity() != null) {
            alarm.setSeverity(OnmsSeverity.get(event.getSeverity()));
        }
    }

    private void incrementCounter(OnmsAlarm alarm) {
        alarm.setCounter(alarm.getCounter() + 1);
    }

    private boolean isResolvedAlarm(OnmsAlarm alarm) {
        return alarm.getAlarmType() == OnmsAlarm.RESOLUTION_TYPE;
    }

    private boolean isResolutionEvent(Event event) {
        return Objects.equals(event.getAlarmData().getAlarmType(), Integer.valueOf(OnmsAlarm.RESOLUTION_TYPE));
    }

    private OnmsAlarm createNewAlarm(Event event) {
        OnmsAlarm alarm = new OnmsAlarm();
        // Situations are denoted by the existance of related-reductionKeys
        alarm.setRelatedAlarms(getRelatedAlarms(event.getParmCollection()), event.getTime());
        alarm.setAlarmType(event.getAlarmData().getAlarmType());
        alarm.setClearKey(event.getAlarmData().getClearKey());
        alarm.setCounter(1);
        alarm.setDescription(event.getDescr());
        alarm.setDistPoller(resolveDistPoller(event));
        alarm.setFirstEventTime(event.getTime());
        alarm.setIfIndex(event.getIfIndex());
        alarm.setIpAddr(resolveIpAddr(event));
        alarm.setLogMsg(event.getLogmsg() != null ? event.getLogmsg().getContent() : null);
        alarm.setMouseOverText(event.getMouseovertext());
        alarm.setNode(resolveNode(event));
        alarm.setOperInstruct(event.getOperinstruct());
        alarm.setReductionKey(event.getAlarmData().getReductionKey());
        alarm.setServiceType(resolveServiceType(event));
        if (event.getSeverity() != null) {
            alarm.setSeverity(OnmsSeverity.get(event.getSeverity()));
        }
        alarm.setSuppressedUntil(event.getTime());
        alarm.setSuppressedTime(event.getTime());
        alarm.setUei(event.getUei());
        if (event.getAlarmData().getManagedObject() != null) {
            alarm.setManagedObjectType(event.getAlarmData().getManagedObject().getType());
        }

        // Populate denormalized event fields
        populateAlarmFromEvent(alarm, event);

        return alarm;
    }

    private void populateAlarmFromEvent(OnmsAlarm alarm, Event event) {
        alarm.setEventTsid(event.getDbid());
        alarm.setEventUei(event.getUei());
        alarm.setEventSource(event.getSource());
        if (event.getSeverity() != null) {
            alarm.setEventSeverity(OnmsSeverity.get(event.getSeverity()).getId());
        }
        alarm.setEventTimestamp(event.getTime());
        alarm.setEventNodeId(event.getNodeid());
        if (event.getLogmsg() != null) {
            alarm.setEventLogMsg(event.getLogmsg().getContent());
        }
        alarm.setLastEventData(serializeEventToXml(event));
        alarm.setLastEventTime(event.getTime());
    }

    private String serializeEventToXml(Event event) {
        try {
            return JaxbUtils.marshal(event);
        } catch (Exception e) {
            LOG.warn("Failed to serialize event to XML", e);
            return null;
        }
    }

    private OnmsNode resolveNode(Event event) {
        if (event.getNodeid() == null || event.getNodeid() == 0) {
            return null;
        }
        return m_nodeDao.get(event.getNodeid().intValue());
    }

    private org.opennms.netmgt.model.OnmsMonitoringSystem resolveDistPoller(Event event) {
        if (event.getDistPoller() == null) {
            return m_distPollerDao.whoami();
        }
        org.opennms.netmgt.model.OnmsMonitoringSystem system = m_distPollerDao.get(event.getDistPoller());
        if (system == null) {
            // Minion monitoring systems have discriminator="Minion", not "OpenNMS",
            // so DistPollerDao.get() (which queries OnmsDistPoller) won't find them.
            // In Delta-V, Minions register via Kafka heartbeats, not REST, so they
            // may not be in the monitoringsystems table at all. Fall back to the local
            // system identity to avoid null systemId constraint violations.
            LOG.debug("Monitoring system '{}' not found, falling back to local system", event.getDistPoller());
            system = m_distPollerDao.whoami();
        }
        return system;
    }

    private InetAddress resolveIpAddr(Event event) {
        if (event.getInterface() == null) {
            return null;
        }
        try {
            return InetAddress.getByName(event.getInterface());
        } catch (Exception e) {
            LOG.warn("Failed to resolve IP address: {}", event.getInterface(), e);
            return null;
        }
    }

    private org.opennms.netmgt.model.OnmsServiceType resolveServiceType(Event event) {
        if (event.getService() == null) {
            return null;
        }
        return m_serviceTypeDao.findByName(event.getService());
    }

    private boolean formingCyclicGraph(OnmsAlarm situation, OnmsAlarm relatedAlarm) {

        return situation.getReductionKey().equals(relatedAlarm.getReductionKey()) ||
                relatedAlarm.getRelatedAlarms().stream().anyMatch(ra -> formingCyclicGraph(situation, ra));
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
                && param.getParmName().startsWith(RELATED_REDUCTION_KEY_PREFIX)
                && param.getValue() != null
                && param.getValue().getContent() != null;
    }

    private static boolean checkEventSanityAndDoWeProcess(final Event event) {
        if (event.getLogmsg() != null && LogDestType.DONOTPERSIST.toString().equalsIgnoreCase(event.getLogmsg().getDest())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkEventSanity: uei '{}' marked as '{}'; not processing event, instanceId={}, eventId={}", event.getUei(), LogDestType.DONOTPERSIST, SystemInfoUtils.getInstanceId(), event.getDbid());
            }
            return false;
        }

        if (event.getAlarmData() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkEventSanity: uei '{}' has no alarm data; not processing event, instanceId={}, eventId={}", event.getUei(), SystemInfoUtils.getInstanceId(), event.getDbid());
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

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
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

    public boolean isCreateNewAlarmIfClearedAlarmExists() {
        return m_createNewAlarmIfClearedAlarmExists;
    }

    public void setCreateNewAlarmIfClearedAlarmExists(boolean createNewAlarmIfClearedAlarmExists) {
        m_createNewAlarmIfClearedAlarmExists = createNewAlarmIfClearedAlarmExists;
    }
    public boolean islegacyAlarmState() {
        return m_legacyAlarmState;
    }

    public void setLegacyAlarmState(boolean legacyAlarmState) {
        m_legacyAlarmState = legacyAlarmState;
    }
}
