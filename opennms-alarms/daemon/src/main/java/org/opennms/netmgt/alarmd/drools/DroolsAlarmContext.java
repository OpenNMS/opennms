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

package org.opennms.netmgt.alarmd.drools;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.alarmd.api.AlarmCallbackStateTracker;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AlarmAssociation;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

/**
 * This class maintains the Drools context used to manage the lifecycle of the alarms.
 *
 * We drive the facts in the Drools context using callbacks provided by the {@link AlarmLifecycleListener}.
 *
 * We use a lock updating alarms in the context in order to avoid triggering the rules while an incomplete
 * view of the alarms is present in the working memory.
 *
 * @author jwhite
 */
public class DroolsAlarmContext extends ManagedDroolsContext implements AlarmLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsAlarmContext.class);

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private AcknowledgmentDao acknowledgmentDao;

    @Autowired
    private AlarmTicketerService alarmTicketerService;

    @Autowired
    private TransactionTemplate template;

    @Autowired
    private AlarmDao alarmDao;

    private final AlarmCallbackStateTracker stateTracker = new AlarmCallbackStateTracker();

    private final Map<Integer, AlarmAndFact> alarmsById = new HashMap<>();

    private final Map<Integer, AlarmAcknowledgementAndFact> acknowledgementsByAlarmId = new HashMap<>();

    private final Map<Integer, Map<Integer, AlarmAssociationAndFact>> alarmAssociationById = new HashMap<>();

    private final CountDownLatch seedSubmittedLatch = new CountDownLatch(1);

    public DroolsAlarmContext() {
        this(getDefaultRulesFolder());
    }

    public DroolsAlarmContext(File rulesFolder) {
        super(rulesFolder, Alarmd.NAME, "DroolsAlarmContext");
        setOnNewKiewSessionCallback(kieSession -> {
            kieSession.setGlobal("alarmService", alarmService);
            kieSession.insert(alarmTicketerService);

            // Rebuild the fact handle maps
            alarmsById.clear();
            acknowledgementsByAlarmId.clear();
            alarmAssociationById.clear();
            for (FactHandle fact : kieSession.getFactHandles()) {
                final Object objForFact = kieSession.getObject(fact);
                if (objForFact instanceof OnmsAlarm) {
                    final OnmsAlarm alarmInSession = (OnmsAlarm)objForFact;
                    alarmsById.put(alarmInSession.getId(), new AlarmAndFact(alarmInSession, fact));
                } else if (objForFact instanceof OnmsAcknowledgment) {
                    final OnmsAcknowledgment ackInSession = (OnmsAcknowledgment)objForFact;
                    acknowledgementsByAlarmId.put(ackInSession.getRefId(), new AlarmAcknowledgementAndFact(ackInSession, fact));
                } else if (objForFact instanceof AlarmAssociation) {
                    final AlarmAssociation associationInSession = (AlarmAssociation)objForFact;
                    final Integer situationId = associationInSession.getSituationAlarm().getId();
                    final Integer alarmId = associationInSession.getRelatedAlarm().getId();
                    final Map<Integer, AlarmAssociationAndFact> associationFacts = alarmAssociationById.computeIfAbsent(situationId, (sid) -> new HashMap<>());
                    associationFacts.put(alarmId, new AlarmAssociationAndFact(associationInSession, fact));
                }
            }
        });
    }

    public static File getDefaultRulesFolder() {
        return Paths.get(ConfigFileConstants.getHome(), "etc", "alarmd", "drools-rules.d").toFile();
    }

    @Override
    public void onStart() {
        final Thread seedThread = new Thread(() -> {
            // Seed the engine with the current set of alarms asynchronously
            // We do this async since we don't want to block the whole system from starting up
            // while we wait on the database (particularly for systems with large amounts of alarms)
            try {
                preHandleAlarmSnapshot();
                template.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        LOG.info("Loading all alarms to seed Drools context.");
                        final List<OnmsAlarm> allAlarms = alarmDao.findAll();
                        LOG.info("Done loading {} alarms.", allAlarms.size());
                        // Leverage the existing snapshot processing function to see the engine
                        handleAlarmSnapshot(allAlarms);
                        // Seed was submitted as an atomic action
                        seedSubmittedLatch.countDown();
                    }
                });
            } finally {
                postHandleAlarmSnapshot();
            }
        });
        seedThread.setName("DroolAlarmContext-InitialSeed");
        seedThread.start();
    }

    @Override
    public void preHandleAlarmSnapshot() {
        // Start tracking alarm callbacks via the state tracker
        stateTracker.startTrackingAlarms();
    }

    @Override
    public void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        if (!isStarted()) {
            LOG.debug("Ignoring alarm snapshot. Drools session is stopped.");
            return;
        }

        LOG.debug("Handling snapshot for {} alarms.", alarms.size());
        final Map<Integer, OnmsAlarm> alarmsInDbById = alarms.stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(OnmsAlarm::getId, a -> a));

        // Eagerly initialize the alarms
        for (OnmsAlarm alarm : alarms) {
            eagerlyInitializeAlarm(alarm);
        }

        // Retrieve the acks from the database for the set of the alarms we've been given
        final Map<Integer, OnmsAcknowledgment> acksByRefId = fetchAcks(alarms);

        getKieSession().submit(kieSession -> {
            final Set<Integer> alarmIdsInDb = alarmsInDbById.keySet();
            final Set<Integer> alarmIdsInWorkingMem = alarmsById.keySet();

            final Set<Integer> alarmIdsToAdd = Sets.difference(alarmIdsInDb, alarmIdsInWorkingMem).stream()
                    // The snapshot contains an alarm which we don't have in working memory.
                    // It is possible that the alarm was in fact deleted some time after the
                    // snapshot was processed. We should only add it, if we did not explicitly
                    // delete the alarm after the snapshot was taken.
                    .filter(alarmId -> !stateTracker.wasAlarmWithIdDeleted(alarmId))
                    .collect(Collectors.toSet());
            final Set<Integer> alarmIdsToRemove = Sets.difference(alarmIdsInWorkingMem, alarmIdsInDb).stream()
                    // We have an alarm in working memory that is not contained in the snapshot.
                    // Only remove it from memory if the fact we have dates before the snapshot.
                    .filter(alarmId -> !stateTracker.wasAlarmWithIdUpdated(alarmId))
                    .collect(Collectors.toSet());
            final Set<Integer> alarmIdsToUpdate = Sets.intersection(alarmIdsInWorkingMem, alarmIdsInDb).stream()
                    // This stream contains the set of all alarms which are both in the snapshot
                    // and in working memory
                    .filter(alarmId -> {
                        final AlarmAndFact alarmAndFact = alarmsById.get(alarmId);
                        // Don't bother updating the alarm in memory if the fact we have is more recent than the snapshot
                        if (stateTracker.wasAlarmWithIdUpdated(alarmId)) {
                            return false;
                        }
                        final OnmsAlarm alarmInMem = alarmAndFact.getAlarm();
                        final OnmsAlarm alarmInDb = alarmsInDbById.get(alarmId);
                        // Only update the alarms if they are different
                        return shouldUpdateAlarmForSnapshot(alarmInMem, alarmInDb);
                    })
                    .collect(Collectors.toSet());

            // Log details that help explain what actions are being performed, if any
            if (LOG.isDebugEnabled()) {
                if (!alarmIdsToAdd.isEmpty() || !alarmIdsToRemove.isEmpty() || !alarmIdsToUpdate.isEmpty()) {
                    LOG.debug("Adding {} alarms, removing {} alarms and updating {} alarms for snapshot.",
                            alarmIdsToAdd.size(), alarmIdsToRemove.size(), alarmIdsToUpdate.size());
                } else {
                    LOG.debug("No actions to perform for alarm snapshot.");
                }
                // When TRACE is enabled, include diagnostic information to help explain why
                // the alarms are being updated
                if (LOG.isTraceEnabled()) {
                    for (Integer alarmIdToUpdate : alarmIdsToUpdate) {
                        LOG.trace("Updating alarm with id={}. Alarm from DB: {} vs Alarm from memory: {}",
                                alarmIdToUpdate,
                                alarmsInDbById.get(alarmIdToUpdate),
                                alarmsById.get(alarmIdToUpdate));
                    }
                }
            }

            for (Integer alarmIdToRemove : alarmIdsToRemove) {
                handleDeletedAlarmForAtomic(kieSession, alarmIdToRemove, alarmsById.get(alarmIdToRemove).getAlarm().getReductionKey());
            }

            final Set<OnmsAlarm> alarmsToUpdate = Sets.union(alarmIdsToAdd, alarmIdsToUpdate).stream()
                    .map(alarmsInDbById::get)
                    .collect(Collectors.toSet());
            for (OnmsAlarm alarm : alarmsToUpdate) {
                handleNewOrUpdatedAlarmForAtomic(kieSession, alarm, acksByRefId.get(alarm.getId()));

            }

            stateTracker.resetStateAndStopTrackingAlarms();
            LOG.debug("Done handling snapshot.");
        });
    }

    @Override
    public void postHandleAlarmSnapshot() {
        // pass
    }

    /**
     * Used to determine if an alarm that is presently in the working memory should be updated
     * with the given alarm, when handling alarm snapshots.
     *
     * @param alarmInMem the alarm that is currently in the working memory
     * @param alarmInDb the alarm that is currently in the database
     * @return true if the alarm in the working memory should be updated, false otherwise
     */
    protected static boolean shouldUpdateAlarmForSnapshot(OnmsAlarm alarmInMem, OnmsAlarm alarmInDb) {
        return !Objects.equals(alarmInMem.getLastEventTime(), alarmInDb.getLastEventTime()) ||
                !Objects.equals(alarmInMem.getAckTime(), alarmInDb.getAckTime());
    }

    @Override
    public void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        if (!isStarted()) {
            LOG.debug("Ignoring new/updated alarm. Drools session is stopped.");
            return;
        }
        eagerlyInitializeAlarm(alarm);

        // Retrieve the acks from the database for the set of the alarms we've been given
        final Map<Integer, OnmsAcknowledgment> acksByRefId = fetchAcks(Collections.singletonList(alarm));

        getKieSession().submit(kieSession -> {
            handleNewOrUpdatedAlarmForAtomic(kieSession, alarm, acksByRefId.get(alarm.getId()));
            stateTracker.trackNewOrUpdatedAlarm(alarm.getId(), alarm.getReductionKey());
        });
    }

    /**
     * Fetches an {@link OnmsAcknowledgment ack} via the {@link #acknowledgmentDao ack DAO} for all the given alarms.
     * For any alarm for which an ack does not exist, a default ack is generated.
     */
    private Map<Integer, OnmsAcknowledgment> fetchAcks(Collection<OnmsAlarm> alarms) {
        if (alarms.isEmpty()) {
            return Collections.emptyMap();
        }
        final Set<OnmsAcknowledgment> acks = new HashSet<>();

        // Update acks depending on if we are interested in one or many alarms
        if (alarms.size() == 1) {
            acknowledgmentDao.findLatestAckForRefId(alarms.iterator()
                    .next()
                    .getId())
                    .ifPresent(acks::add);
        } else {
            // Calculate the creation time of the earliest alarm
            final Date earliestAlarm  = alarms.stream()
                    .map(OnmsAlarm::getFirstEventTime)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElseGet(() -> {
                        // We didn't find any dates - either the set is empty (in which case this function
                        // wouldn't be called) or all the dates are null (which they shouldn't be.)
                        // Let's log an error, and return some date for sanity
                        final LocalDateTime oneMonthAgoLdt = LocalDateTime.now().minusMonths(1);
                        final Date oneMonthAgo = Date.from(oneMonthAgoLdt.atZone(ZoneId.systemDefault()).toInstant());
                        LOG.error("Could not find minimum alarm creation time for alarms: {}. Using: {}", alarms, oneMonthAgo);
                        return oneMonthAgo;
                    });
            acks.addAll(acknowledgmentDao.findLatestAcks(earliestAlarm));
        }

        // Handle all the alarms for which an ack could be found
        Map<Integer, OnmsAcknowledgment> acksById =
                acks.stream().collect(Collectors.toMap(OnmsAcknowledgment::getRefId, ack -> ack));

        // Handle all the alarms that no ack could be found for by generating a default ack
        acksById.putAll(alarms.stream()
                .filter(alarm -> !acksById.containsKey(alarm.getId()))
                .collect(Collectors.toMap(OnmsAlarm::getId, alarm -> {
                    // For the purpose of making rule writing easier, we fake an
                    // Un-Acknowledgment for Alarms that have never been Acknowledged.
                    OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, DefaultAlarmService.DEFAULT_USER,
                            alarm.getFirstEventTime());
                    ack.setAckAction(AckAction.UNACKNOWLEDGE);
                    ack.setId(0);
                    return ack;
                })));

        return acksById;
    }

    private void eagerlyInitializeAlarm(OnmsAlarm alarm) {
        // Initialize any related objects that are needed for rule execution
        Hibernate.initialize(alarm.getAssociatedAlarms());
        if (alarm.getLastEvent() != null) {
            // The last event may be null in unit tests
            Hibernate.initialize(alarm.getLastEvent().getEventParameters());
        }
        if (alarm.getNode() != null) {
            // Allow rules to use the categories on the associated node
            Hibernate.initialize(alarm.getNode().getCategories());
        }
    }

    private void handleNewOrUpdatedAlarmForAtomic(KieSession kieSession, OnmsAlarm alarm, OnmsAcknowledgment ack) {
            final AlarmAndFact alarmAndFact = alarmsById.get(alarm.getId());
            if (alarmAndFact == null) {
                LOG.debug("Inserting alarm into session: {}", alarm);
                final FactHandle fact = kieSession.insert(alarm);
                alarmsById.put(alarm.getId(), new AlarmAndFact(alarm, fact));
            } else {
                // Updating the fact doesn't always give us to expected results so we resort to deleting it
                // and adding it again instead
                LOG.trace("Deleting alarm from session (for re-insertion): {}", alarm);
                kieSession.delete(alarmAndFact.getFact());
                // Reinsert
                LOG.trace("Re-inserting alarm into session: {}", alarm);
                final FactHandle fact = kieSession.insert(alarm);
                alarmsById.put(alarm.getId(), new AlarmAndFact(alarm, fact));
            }

            // Ack
            final AlarmAcknowledgementAndFact acknowledgmentFact = acknowledgementsByAlarmId.get(alarm.getId());
            if (acknowledgmentFact == null) {
                LOG.debug("Inserting first alarm acknowledgement into session: {}", ack);
                final FactHandle fact = kieSession.insert(ack);
                acknowledgementsByAlarmId.put(alarm.getId(), new AlarmAcknowledgementAndFact(ack, fact));
            } else {
                FactHandle fact = acknowledgmentFact.getFact();
                LOG.trace("Updating acknowledgment in session: {}", ack);
                kieSession.update(fact, ack);
                acknowledgementsByAlarmId.put(alarm.getId(), new AlarmAcknowledgementAndFact(ack, fact));
            }

            if (alarm.isSituation()) {
                final OnmsAlarm situation = alarm;
                final Map<Integer, AlarmAssociationAndFact> associationFacts = alarmAssociationById.computeIfAbsent(situation.getId(), (sid) -> new HashMap<>());
                for (AlarmAssociation association : situation.getAssociatedAlarms()) {
                    Integer alarmId = association.getRelatedAlarm().getId();
                    AlarmAssociationAndFact associationFact = associationFacts.get(alarmId);
                    if (associationFact == null) {
                        LOG.debug("Inserting alarm association into session: {}", association);
                        final FactHandle fact = kieSession.insert(association);
                        associationFacts.put(alarmId, new AlarmAssociationAndFact(association, fact));
                    } else {
                        FactHandle fact = associationFact.getFact();
                        LOG.trace("Updating alarm association in session: {}", associationFact);
                        kieSession.update(fact, association);
                        associationFacts.put(alarmId, new AlarmAssociationAndFact(association, fact));
                    }
                }
                // Remove Fact for any Alarms no longer in the Situation
                Set<Integer> deletedAlarmIds = associationFacts.values().stream()
                        .map(fact -> fact.getAlarmAssociation().getRelatedAlarm().getId())
                        .filter(alarmId -> !situation.getRelatedAlarmIds().contains(alarmId))
                        .collect(Collectors.toSet());
                deletedAlarmIds.forEach(alarmId -> {
                    final AlarmAssociationAndFact associationAndFact = associationFacts.remove(alarmId);
                    if (associationAndFact != null) {
                        LOG.debug("Deleting AlarmAssociationAndFact from session: {}", associationAndFact.getAlarmAssociation());
                        kieSession.delete(associationAndFact.getFact());
                    }
                });
            }
    }

    @Override
    public void handleDeletedAlarm(int alarmId, String reductionKey) {
        if (!isStarted()) {
            LOG.debug("Ignoring deleted alarm. Drools session is stopped.");
            return;
        }
        getKieSession().submit(kieSession -> {
            handleDeletedAlarmForAtomic(kieSession, alarmId, reductionKey);
            stateTracker.trackDeletedAlarm(alarmId, reductionKey);
        });
    }

    private void handleDeletedAlarmForAtomic(KieSession kieSession, int alarmId, String reductionKey) {
        final AlarmAndFact alarmAndFact = alarmsById.remove(alarmId);
        if (alarmAndFact != null) {
            LOG.debug("Deleting alarm from session: {}", alarmAndFact.getAlarm());
            kieSession.delete(alarmAndFact.getFact());
        }

        final AlarmAcknowledgementAndFact acknowledgmentFact = acknowledgementsByAlarmId.remove(alarmId);
        if (acknowledgmentFact != null) {
            LOG.debug("Deleting ack from session: {}", acknowledgmentFact.getAcknowledgement());
            kieSession.delete(acknowledgmentFact.getFact());
        }

        final Map<Integer, AlarmAssociationAndFact> associationFacts = alarmAssociationById.remove(alarmId);
        if (associationFacts == null) {
            return;
        }
        for (Integer association : associationFacts.keySet()) {
            AlarmAssociationAndFact associationFact = associationFacts.get(association);
            if (associationFact != null) {
                LOG.debug("Deleting association from session: {}", associationFact.getAlarmAssociation());
                kieSession.delete(associationFact.getFact());
            }
        }
    }

    public void setAlarmService(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    public void setAcknowledgmentDao(AcknowledgmentDao acknowledgmentDao) {
        this.acknowledgmentDao = acknowledgmentDao;
    }

    public void setAlarmTicketerService(AlarmTicketerService alarmTicketerService) {
        this.alarmTicketerService = alarmTicketerService;
    }

    @VisibleForTesting
    OnmsAcknowledgment getAckByAlarmId(Integer id) {
        return acknowledgementsByAlarmId.get(id).getAcknowledgement();
    }

    @VisibleForTesting
    public void waitForInitialSeedToBeSubmitted() throws InterruptedException {
        seedSubmittedLatch.await();
    }

    public void setTransactionTemplate(TransactionTemplate template) {
        this.template = template;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }
}
