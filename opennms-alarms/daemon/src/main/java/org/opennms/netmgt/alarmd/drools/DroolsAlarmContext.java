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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.drools.core.ClockType;
import org.drools.core.time.SessionPseudoClock;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
public class DroolsAlarmContext implements AlarmLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsAlarmContext.class);

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private AlarmTicketerService alarmTicketerService;

    private boolean usePseudoClock = false;

    private boolean useManualTick = false;

    private KieSession kieSession;

    private Timer timer;

    private SessionPseudoClock clock;

    private final Map<Integer, AlarmAndFact> alarmsById = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    private final ThreadLocal<Boolean> firing = new ThreadLocal<>();

    public void start() {
        final KieServices ks = KieServices.Factory.get();
        final KieContainer kcont = ks.newKieClasspathContainer(getClass().getClassLoader());
        final KieBaseConfiguration kbaseConfig = ks.newKieBaseConfiguration();
        kbaseConfig.setOption(EventProcessingOption.STREAM);
        final KieBase kbase = kcont.newKieBase("alarmKBase", kbaseConfig);

        final KieSessionConfiguration kieSessionConfig = KieServices.Factory.get().newKieSessionConfiguration();
        if (usePseudoClock) {
            kieSessionConfig.setOption(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
        }

        kieSession = kbase.newKieSession(kieSessionConfig, null);
        kieSession.setGlobal("alarmService", alarmService);

        if (usePseudoClock) {
            this.clock = kieSession.getSessionClock();
        } else {
            this.clock = null;
        }

        alarmsById.clear();

        kieSession.insert(alarmTicketerService);

        if (!useManualTick) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    firing.set(true);
                    lock.lock();
                    try {
                        LOG.debug("Firing rules.");
                        kieSession.fireAllRules();
                    } catch (Exception e) {
                        LOG.error("Error occurred while firing rules.", e);
                    } finally {
                        firing.set(false);
                        lock.unlock();
                    }
                }
            }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1));
        }
    }

    @Override
    public void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        if (kieSession == null) {
            LOG.debug("Ignoring alarm snapshot. Drools session is stopped.");
            return;
        }
        lockIfNotFiring();
        try {
            LOG.debug("Handling snapshot for {} alarms.", alarms.size());
            final Map<Integer, OnmsAlarm> alarmsInDbById = alarms.stream()
                    .filter(a -> a.getId() != null)
                    .collect(Collectors.toMap(OnmsAlarm::getId, a -> a));

            final Set<Integer> alarmIdsInDb = alarmsInDbById.keySet();
            final Set<Integer> alarmIdsInWorkingMem = alarmsById.keySet();

            final Set<Integer> alarmIdsToAdd = Sets.difference(alarmIdsInDb, alarmIdsInWorkingMem).immutableCopy();
            final Set<Integer> alarmIdsToRemove = Sets.difference(alarmIdsInWorkingMem, alarmIdsInDb).immutableCopy();
            final Set<Integer> alarmIdsToUpdate = Sets.intersection(alarmIdsInWorkingMem, alarmIdsInDb).immutableCopy();

            for (Integer alarmIdToRemove : alarmIdsToRemove) {
                handleDeletedAlarmNoLock(alarmIdToRemove);
            }
            for (Integer alarmIdToAdd : alarmIdsToAdd) {
                handleNewOrUpdatedAlarmNoLock(alarmsInDbById.get(alarmIdToAdd));
            }
            for (Integer alarmIdToUpdate : alarmIdsToUpdate) {
                handleNewOrUpdatedAlarmNoLock(alarmsInDbById.get(alarmIdToUpdate));
            }
        } finally {
            unlockIfNotFiring();
        }
    }

    @Override
    public void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        if (kieSession == null) {
            LOG.debug("Ignoring new/updated alarm. Drools session is stopped.");
            return;
        }
        lockIfNotFiring();
        try {
            handleNewOrUpdatedAlarmNoLock(alarm);
        } finally {
            unlockIfNotFiring();
        }
    }

    private void handleNewOrUpdatedAlarmNoLock(OnmsAlarm alarm) {
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
            alarmAndFact.setFact(fact);
        }
    }

    @Override
    public void handleDeletedAlarm(int alarmId, String reductionKey) {
        if (kieSession == null) {
            LOG.debug("Ignoring deleted alarm. Drools session is stopped.");
            return;
        }
        lockIfNotFiring();
        try {
            handleDeletedAlarmNoLock(alarmId);
        } finally {
            unlockIfNotFiring();
        }
    }

    private void handleDeletedAlarmNoLock(int alarmId) {
        final AlarmAndFact alarmAndFact = alarmsById.remove(alarmId);
        if (alarmAndFact != null) {
            LOG.debug("Deleting alarm from session: {}", alarmAndFact.getAlarm());
            kieSession.delete(alarmAndFact.getFact());
        }
    }

    public void tick() {
        firing.set(true);
        lock.lock();
        try {
            kieSession.fireAllRules();
        } finally {
            lock.unlock();
            firing.set(false);
        }
    }

    private void lockIfNotFiring() {
        if (Boolean.TRUE.equals(firing.get())) {
            lock.lock();
        }
    }

    private void unlockIfNotFiring() {
        if (Boolean.TRUE.equals(firing.get())) {
            lock.unlock();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        if (kieSession != null) {
            kieSession.halt();
            kieSession = null;
        }
    }

    public SessionPseudoClock getClock() {
        return clock;
    }

    public void setUsePseudoClock(boolean usePseudoClock) {
        this.usePseudoClock = usePseudoClock;
    }

    public void setUseManualTick(boolean useManualTick) {
        this.useManualTick = useManualTick;
    }

    public void setAlarmService(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    public void setAlarmTicketerService(AlarmTicketerService alarmTicketerService) {
        this.alarmTicketerService = alarmTicketerService;
    }

}
