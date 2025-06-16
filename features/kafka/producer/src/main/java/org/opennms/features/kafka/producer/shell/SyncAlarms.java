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
package org.opennms.features.kafka.producer.shell;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.kafka.producer.datasync.AlarmDataStore;
import org.opennms.features.kafka.producer.datasync.AlarmSyncResults;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;

@Command(scope = "opennms", name = "kafka-sync-alarms", description = "Triggers a syncrhonization of the alarms topic against the database.")
@Service
public class SyncAlarms implements Action {

    @Reference
    private AlarmDataStore alarmDataStore;

    @Reference
    private SessionUtils sessionUtils;

    @Reference
    private AlarmDao alarmDao;

    @Option(name = "-c", aliases = "--clean-state", description = "Restart the streams client with a clean state before performing the sync.")
    private boolean startWithCleanState = false;

    @Override
    public Object execute() throws IOException {
        if (startWithCleanState) {
            alarmDataStore.destroy();
            alarmDataStore.setStartWithCleanState(true);
            alarmDataStore.init();
        }

        if (!waitForAlarmDataStore(alarmDataStore, 15, TimeUnit.SECONDS)) {
            return null;
        }

        return sessionUtils.withReadOnlyTransaction(() -> {
            // Retrieve all of the alarms from the database
            final List<OnmsAlarm> alarmsInDb = alarmDao.findAll();
            System.out.println("Performing synchronization of alarms from the database with those in the ktable.");
            final long start = System.currentTimeMillis();
            final AlarmSyncResults results = alarmDataStore.handleAlarmSnapshot(alarmsInDb);
            final long delay = System.currentTimeMillis() - start;
            System.out.printf("Executed %d updates in %dms.\n\n", results.getNumUpdates(), delay);

            System.out.printf("Number of reduction keys in ktable: %d\n", results.getAlarmsInKtableByReductionKey().size());
            System.out.printf("Number of reduction keys in the db: %d (%d alarms total)\n",
                   results.getAlarmsInDbByReductionKey().size(), results.getAlarmsInDb().size());

            if (results.getNumUpdates() > 0) {
                System.out.print("Reduction keys added to the ktable:");
                printSet(results.getReductionKeysAdded());
                System.out.print("Reduction keys deleted from the ktable:");
                printSet(results.getReductionKeysDeleted());
                System.out.print("Reduction keys updated in the ktable:");
                printSet(results.getReductionKeysUpdated());
            }
            return null;
        });
    }

    protected static boolean waitForAlarmDataStore(AlarmDataStore alarmDataStore, long timeout, TimeUnit unit) {
        if (!alarmDataStore.isEnabled()) {
            System.out.println("The alarm data store is currently disabled and must be enabled for this shell command to function.");
            return false;
        }

        // Wait for the alarm data store to be ready
        if (!isAlarmDataStoreReady(alarmDataStore)) {
            final long startTime = System.currentTimeMillis();
            final long endTime = startTime + unit.toMillis(timeout);
            System.out.println("Waiting for alarm data store to be ready..");
            while (System.currentTimeMillis() < endTime) {
                try {
                    System.out.print(".");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    if (isAlarmDataStoreReady(alarmDataStore)) {
                        System.out.printf("\nReady in %d ms.\n\n", System.currentTimeMillis() - startTime);
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                    return false;
                }
            }
            System.out.printf("\nAlarm data store was not ready in %d %s. Try again and see logs if issue persists.\n", timeout, unit.name());
            return false;
        }
        return true;
    }

    private static boolean isAlarmDataStoreReady(AlarmDataStore alarmDataStore) {
        try {
            return alarmDataStore.isReady();
        } catch (Exception e) {
            return false;
        }
    }

    private static void printSet(Set<String> reductionKeys) {
        if (reductionKeys.size() < 1) {
            System.out.println(" (None)");
        } else {
            System.out.println();
            reductionKeys.forEach(rkey -> System.out.printf("\t%s\n", rkey));
        }
    }
}
