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

package org.opennms.features.kafka.producer.datasync;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

@Command(scope = "kafka", name = "alarms", description = "Alarm Data from Kafka")
@Service
public class AlarmDataStoreCommand implements Action {

    @Reference
    private AlarmDataStore alarmDataStore;

    @Option(name = "-key", aliases = "--reduction-key", description = "Reduction Key")
    private String key;

    @Override
    public Object execute() throws Exception {
        CompletableFuture<ReadOnlyKeyValueStore<String, byte[]>> future = alarmDataStore.getAlarmDataStore();
        ReadOnlyKeyValueStore<String, byte[]> alarmStore = null;
        while (true) {
            try {
                alarmStore = future.get(1, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
        }
        if (Objects.isNull(alarmStore)) {
            System.out.println("Alarm store not initialized within 1 minute");
            return null;
        }
        if (key != null && alarmStore.get(key) != null) {
            return OpennmsModelProtos.Alarm.parseFrom(alarmStore.get(key)).toString();
        } else {
            System.out.println("List of reduction-keys in alarm_store :");
            List<String> keys = new ArrayList<>();
            alarmStore.all().forEachRemaining(alarmData -> keys.add(alarmData.key));
            return keys;
        }
    }

}
