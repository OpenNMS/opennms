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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;

import com.google.common.collect.Maps;

public class AlarmLifecycleListenerManagerSnapshotTest {

    /**
     * Verifies that the {@link AlarmLifecycleListener} does invoke
     * the {@link AlarmLifecycleListener#handleNewOrUpdatedAlarm(OnmsAlarm)} or
     * {@link AlarmLifecycleListener#handleDeletedAlarm(int, String)} callbacks while
     * processing a snapshot.
     *
     * @throws InterruptedException
     */
    @Test
    public void canIssueCallbacksWhileSnapshotIsProcessing() throws InterruptedException {
        // Triggered when the snapshot is called in our handler
        CountDownLatch isProcessingSnapshot = new CountDownLatch(1);
        // Set when the snapshot is complete
        AtomicBoolean doneSnapshot = new AtomicBoolean(false);
        // Keeps track of the number of callbacks we received *after* the snapshot is complete
        AtomicInteger newUpdateOrDeleteAfterSnapshot = new AtomicInteger(0);
        // Keeps track of the number of callbacks we received while processing the snapshot
        AtomicInteger newUpdateOrDeleteDuringSnapshot = new AtomicInteger(0);

        AlarmLifecycleListener listener = new AlarmLifecycleListener() {
            @Override
            public void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
                isProcessingSnapshot.countDown();
                try {
                    // Sleep for some arbitrary amount of time, sufficient to invoke the callbacks
                    Thread.sleep(1000);
                    doneSnapshot.set(true);
                } catch (InterruptedException e) {
                    // pass
                }
            }

            @Override
            public void preHandleAlarmSnapshot() {
                // pass
            }

            @Override
            public void postHandleAlarmSnapshot() {
                // pass
            }

            @Override
            public void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
                if (doneSnapshot.get()) {
                    newUpdateOrDeleteAfterSnapshot.incrementAndGet();
                } else {
                    newUpdateOrDeleteDuringSnapshot.incrementAndGet();
                }
            }

            @Override
            public void handleDeletedAlarm(int alarmId, String reductionKey) {
                if (doneSnapshot.get()) {
                    newUpdateOrDeleteAfterSnapshot.incrementAndGet();
                } else {
                    newUpdateOrDeleteDuringSnapshot.incrementAndGet();
                }
            }
        };

        // Bootstrap the ALM with our listener
        AlarmLifecycleListenerManager alm = new AlarmLifecycleListenerManager();
        AlarmDao alarmDao = mock(AlarmDao.class);
        when(alarmDao.findAll()).thenReturn(Collections.emptyList());
        alm.setAlarmDao(alarmDao);
        MockSessionUtils mockSessionUtils = new MockSessionUtils();
        alm.setSessionUtils(mockSessionUtils);
        alm.onListenerRegistered(listener, Maps.newHashMap());

        // Trigger a snapshot on a another thread
        Thread t = new Thread(alm::doSnapshot);
        t.start();

        // Wait for handler to be invoked
        isProcessingSnapshot.await();

        // (the handler was invoked, and is sleeping)

        // Let's attempt to trigger some callbacks - we expect these *not* to block
        // even though the snapshot has not yet completed
        OnmsAlarm alarm = mock(OnmsAlarm.class);
        alm.onAlarmCreated(alarm);
        alm.onAlarmDeleted(alarm);

        // We should have received the callbacks while the snapshot was still in progress
        assertThat(newUpdateOrDeleteAfterSnapshot.get(), equalTo(0));
        assertThat(newUpdateOrDeleteDuringSnapshot.get(), equalTo(2));
    }
}
