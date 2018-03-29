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

package org.opennms.features.kafka.producer;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.kafka.streams.kstream.KTable;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.kafka.producer.datasync.KafkaAlarmDataView;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.protobuf.InvalidProtocolBufferException;

public class KafkaStreamTest {
    private static final String KAFKA_CLIENT_PID = "org.opennms.features.kafka.producer.client";

    private KafkaAlarmDataView alarmDataView;

    @Before
    public void setup() throws IOException {
        Hashtable<String, Object> streamsConfig = new Hashtable<String, Object>();
        streamsConfig.put("group.id", "OpenNMS");
        streamsConfig.put("bootstrap.servers", "kafka:9092");
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KAFKA_CLIENT_PID).getProperties()).thenReturn(streamsConfig);
        alarmDataView = new KafkaAlarmDataView(configAdmin);
        alarmDataView.setAlarmTopic("alarms");
        alarmDataView.init();
    }

    @Test
    public void testKafkaStream() {

        KTable<String, byte[]> alarmBytesTable = alarmDataView.getAlarmStreamTable();

        final KTable<String, OpennmsModelProtos.Alarm> alarmTable = alarmBytesTable.mapValues(alarmBytes -> {
            try {
                return OpennmsModelProtos.Alarm.parseFrom(alarmBytes);
            } catch (InvalidProtocolBufferException ex) {
                throw new RuntimeException(ex);
            }
        });

        alarmTable.toStream().foreach(this::checkForAlarm);

    }

    private void checkForAlarm(String reductionKey, OpennmsModelProtos.Alarm alarm) {

    }
}
