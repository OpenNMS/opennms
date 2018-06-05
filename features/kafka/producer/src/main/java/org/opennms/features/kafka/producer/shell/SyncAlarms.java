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

package org.opennms.features.kafka.producer.shell;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.kafka.producer.datasync.AlarmDataStore;
import org.opennms.features.kafka.producer.datasync.AlarmSyncResults;

@Command(scope = "kafka-producer", name = "sync-alarms", description = "Triggers a syncrhonization of the alarms topic against the database.")
@Service
public class SyncAlarms implements Action {

    @Reference
    private AlarmDataStore alarmDataStore;

    @Override
    public Object execute() {
        if (!waitForAlarmDataStore(alarmDataStore)) {
            return null;
        }

        System.out.println("Performing synchronization of alarms from the database with those in the ktable.");
        final long start = System.currentTimeMillis();
        final AlarmSyncResults results = alarmDataStore.synchronizeAlarmsWithDb();
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
    }

    protected static boolean waitForAlarmDataStore(AlarmDataStore alarmDataStore) {
        if (alarmDataStore.isEnabled()) {
            System.out.println("The alarm data store is currently disabled and must be enabled for this shell command to function.");
            return false;
        }

        // Wait for the alarm data store to be ready
        if (!isAlarmDataStoreReady(alarmDataStore)) {
            final long startTime = System.currentTimeMillis();
            System.out.println("Waiting for alarm data store to be ready..");
            while (true) {
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
