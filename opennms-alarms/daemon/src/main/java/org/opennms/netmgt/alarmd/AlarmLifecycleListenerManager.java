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

package org.opennms.netmgt.alarmd;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleSubscriptionService;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@EventListener(name="alarmLifecycleListenerManager", logPrefix="alarmd")
public class AlarmLifecycleListenerManager implements AlarmLifecycleSubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmLifecycleListenerManager.class);

    private final Set<AlarmLifecycleListener> listeners = new LinkedHashSet<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private TransactionTemplate template;

    @EventHandler(ueis = {
            EventConstants.ALARM_CREATED_UEI,
            EventConstants.ALARM_ESCALATED_UEI,
            EventConstants.ALARM_CLEARED_UEI,
            EventConstants.ALARM_UNCLEARED_UEI,
            EventConstants.ALARM_UPDATED_WITH_REDUCED_EVENT_UEI,
            EventConstants.ALARM_DELETED_EVENT_UEI
    })
    public void handleAlarmLifecycleEvents(Event e) {
        rwLock.readLock().lock();
        try {
            if (e == null || listeners.size() < 1) {
                // Return quick if we weren't given an event, or if there are no listeners defined
                // in which case we don't need to perform any further handling
                return;
            }

            final Parm alarmIdParm = e.getParm(EventConstants.PARM_ALARM_ID);
            if (alarmIdParm == null || alarmIdParm.getValue() == null) {
                LOG.warn("The alarmId parameter has no value on event with uei: {}. Ignoring.", e.getUei());
                return;
            }

            int alarmId;
            try {
                alarmId = Integer.parseInt(alarmIdParm.getValue().getContent());
            } catch (NumberFormatException ee) {
                LOG.warn("Failed to retrieve the alarmId for event with uei: {}. Ignoring.", e.getUei(), ee);
                return;
            }

            if (EventConstants.ALARM_DELETED_EVENT_UEI.equals(e.getUei())) {
                final Parm reductionKeyParm = e.getParm(EventConstants.PARM_ALARM_REDUCTION_KEY);
                if (reductionKeyParm == null) {
                    LOG.warn("Received alarm deleted event without reduction key. Ignoring.");
                    return;
                }
                if (reductionKeyParm.getValue() == null) {
                    LOG.warn("Received alarm deleted event with null reduction key value. Ignoring.");
                    return;
                }
                final String reductionKey = reductionKeyParm.getValue().getContent();
                if (reductionKey == null) {
                    LOG.warn("Received alarm deleted event with null reduction key content. Ignoring.");
                    return;
                }

                handleAlarmDeleted(alarmId, reductionKey);
            } else {
                handleAlarmCreatedOrUpdated(e, alarmId);
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void handleAlarmCreatedOrUpdated(Event e, int alarmId) {
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final OnmsAlarm alarm = alarmDao.get(alarmId);
                if (alarm == null) {
                    LOG.error("Could not find alarm with id: {} for event with uei: {}. Ignoring.", alarmId, e.getUei());
                    return;
                }
                listeners.forEach(l -> l.handleNewOrUpdatedAlarm(alarm));
            }
        });
    }

    private void handleAlarmDeleted(int alarmId, String reductionKey) {
        listeners.forEach(l -> l.handleDeletedAlarm(alarmId, reductionKey));
    }

    @Override
    public void addAlarmLifecyleListener(AlarmLifecycleListener listener) {
        rwLock.writeLock().lock();
        try {
            listeners.add(listener);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void removeAlarmLifecycleListener(AlarmLifecycleListener listener) {
        rwLock.writeLock().lock();
        try {
            listeners.remove(listener);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
