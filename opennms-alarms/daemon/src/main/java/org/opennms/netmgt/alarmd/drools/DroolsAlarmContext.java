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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.alarmd.api.AlarmCallbackStateTracker;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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

    private static final String LOCK_TIMEOUT_MS_SYS_PROP = "org.opennms.alarms.drools.lock.timeout.ms";
    protected static final long LOCK_TIMEOUT_MS = Long.getLong(LOCK_TIMEOUT_MS_SYS_PROP, TimeUnit.SECONDS.toMillis(20));

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private AlarmTicketerService alarmTicketerService;

    @Autowired
    private TransactionTemplate template;

    @Autowired
    private AlarmDao alarmDao;

    private final AlarmCallbackStateTracker stateTracker = new AlarmCallbackStateTracker();

    private final Map<Integer, AlarmAndFact> alarmsById = new HashMap<>();

    public DroolsAlarmContext() {
        this(getDefaultRulesFolder());
    }

    public DroolsAlarmContext(File rulesFolder) {
        super(rulesFolder, Alarmd.NAME, "DroolsAlarmContext");
        setOnNewKiewSessionCallback(kieSession -> {
            kieSession.setGlobal("alarmService", alarmService);
            kieSession.insert(alarmTicketerService);

            // Rebuild the alarm id -> fact handle map
            alarmsById.clear();
            for (FactHandle fact : kieSession.getFactHandles()) {
                final Object objForFact = kieSession.getObject(fact);
                if (objForFact instanceof OnmsAlarm) {
                    final OnmsAlarm alarmInSession = (OnmsAlarm)objForFact;
                    alarmsById.put(alarmInSession.getId(), new AlarmAndFact(alarmInSession, fact));
                }
            }
        });
    }

    public static File getDefaultRulesFolder() {
        return Paths.get(ConfigFileConstants.getHome(), "etc", "alarmd", "drools-rules.d").toFile();
    }

    @Override
    public void onStart() {
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread seedThread = new Thread(() -> {
            // Seed the engine with the current set of alarms asynchronously
            // We do this async since we don't want to block the whole system from starting up
            // while we wait on the database (particularly for systems with large amounts of alarms)
            getLock().lock();
            latch.countDown();
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
                    }
                });
            } finally {
                postHandleAlarmSnapshot();
                getLock().unlock();
            }
        });
        seedThread.setName("DroolAlarmContext-InitialSeed");
        seedThread.start();

        try {
            // Wait until the seed thread has acquired the session lock before returning
            latch.await();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for seed thread to acquire session lock. "
                    + "The engine may not have a complete view of the state on startup.");
        }
    }

    @Override
    public void preHandleAlarmSnapshot() {
        getLock().lock();
        try {
            // Start tracking alarm callbacks via the state tracker
            // Do this while holding on a lock to make sure that we don't miss a callback that's in flight
            stateTracker.startTrackingAlarms();
        } finally {
            getLock().unlock();
        }
    }

    @Override
    public void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        if (!isStarted()) {
            LOG.debug("Ignoring alarm snapshot. Drools session is stopped.");
            return;
        }

        // Lock while handling the snapshot and unlock in the callback to {@link #postHandleAlarmSnapshot}
        // This prevents the rules from firing immediately after the snapshot was processed, but
        // before the session & transaction were closed, which can cause Hibernate related exceptions.
        getLock().lock();
        LOG.debug("Handling snapshot for {} alarms.", alarms.size());
        final Map<Integer, OnmsAlarm> alarmsInDbById = alarms.stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(OnmsAlarm::getId, a -> a));

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
            handleDeletedAlarmNoLock(alarmIdToRemove, alarmsById.get(alarmIdToRemove).getAlarm().getReductionKey());
        }
        for (Integer alarmIdToAdd : alarmIdsToAdd) {
            handleNewOrUpdatedAlarmNoLock(alarmsInDbById.get(alarmIdToAdd));
        }
        for (Integer alarmIdToUpdate : alarmIdsToUpdate) {
            handleNewOrUpdatedAlarmNoLock(alarmsInDbById.get(alarmIdToUpdate));
        }

        LOG.debug("Done handling snapshot.");
    }

    @Override
    public void postHandleAlarmSnapshot() {
        stateTracker.resetStateAndStopTrackingAlarms();
        // If an error occurred while preparing the snapshot, it is possible that
        // this post function is called  without having handled the snapshot.
        // To avoid an IllegalMonitorStateException in this case, we only
        // unlock the session if it was locked.
        final ReentrantLock lock = getLock();
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
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
        tryWithLock(alarm.getId(), alarm.getReductionKey(),
                (id, rkey) -> handleNewOrUpdatedAlarmNoLock(alarm),
                "Add or update");
    }

    private void tryWithLock(int alarmId, String reductionKey, BiConsumer<Integer, String> callback, String action) {
        try {
            // It is possible that the session is currently locked while waiting for the
            // transaction that this thread is holding to be committed, so we limit the time
            // we spend waiting for the lock in order to avoid deadlocks.
            // If we were not able to successfully acquire the lock, then log a warning.
            // The alarm snapshot handling will ensure that the state of the context is eventually consistent.
            if (getLock().tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                try {
                    callback.accept(alarmId, reductionKey);
                } finally {
                    getLock().unlock();
                }
            } else {
                LOG.warn("Failed to acquire Drools session lock within {}ms. " +
                                "{} for alarm with id={} and reduction-key={} will not be immediately reflected in the context.",
                        LOCK_TIMEOUT_MS, action, alarmId, reductionKey);
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for Drools session lock. " +
                            "{} for alarm with id={} and reduction-key={} will not be immediately reflected in the context.",
                   action, alarmId, reductionKey);
            // Propagate the interrupt
            Thread.currentThread().interrupt();
        }
    }

    private void handleNewOrUpdatedAlarmNoLock(OnmsAlarm alarm) {
        final KieSession kieSession = getKieSession();
        // Initialize any related objects that are needed for rule execution
        Hibernate.initialize(alarm.getRelatedAlarms());
        if (alarm.getLastEvent() != null) {
            // The last event may be null in unit tests
            Hibernate.initialize(alarm.getLastEvent().getEventParameters());
        }
        final AlarmAndFact alarmAndFact = alarmsById.get(alarm.getId());
        if (alarmAndFact == null) {
            LOG.debug("Inserting alarm into session: {}", alarm);
            final FactHandle fact = getKieSession().insert(alarm);
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
        stateTracker.trackNewOrUpdatedAlarm(alarm.getId(), alarm.getReductionKey());
    }

    @Override
    public void handleDeletedAlarm(int alarmId, String reductionKey) {
        if (!isStarted()) {
            LOG.debug("Ignoring deleted alarm. Drools session is stopped.");
            return;
        }
        tryWithLock(alarmId, reductionKey, (id, rkey) -> handleDeletedAlarmNoLock(alarmId, reductionKey), "Delete");
    }

    private void handleDeletedAlarmNoLock(int alarmId, String reductionKey) {
        final AlarmAndFact alarmAndFact = alarmsById.remove(alarmId);
        if (alarmAndFact != null) {
            LOG.debug("Deleting alarm from session: {}", alarmAndFact.getAlarm());
            getKieSession().delete(alarmAndFact.getFact());
        }
        stateTracker.trackDeletedAlarm(alarmId, reductionKey);
    }

    public void setAlarmService(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    public void setAlarmTicketerService(AlarmTicketerService alarmTicketerService) {
        this.alarmTicketerService = alarmTicketerService;
    }

    public void setTransactionTemplate(TransactionTemplate template) {
        this.template = template;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }
}
