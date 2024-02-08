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
