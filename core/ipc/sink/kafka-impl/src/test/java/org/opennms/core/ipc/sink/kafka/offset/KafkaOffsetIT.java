/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.offset;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.kafka.KafkaSinkConstants;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml" })
@JUnitConfigurationEnvironment
public class KafkaOffsetIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private KafkaOffsetProvider offsetProvider;

    @Before
    public void setup() throws Exception {
        System.setProperty(String.format("%sbootstrap.servers", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset", KafkaSinkConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                "earliest");
        // offsetProvider needs system properties
        offsetProvider = new KafkaOffsetProvider();
        offsetProvider.start();
    }

    @Test
    public void testOffsetGeneration() throws Exception {

        KafkaMessageConsumer kafkaConsumer = new KafkaMessageConsumer(kafkaServer.getKafkaConnectString());
        kafkaConsumer.startConsumer();
        KafkaMessageProducer kafkaProducer = new KafkaMessageProducer(kafkaServer.getKafkaConnectString());
        kafkaProducer.produce();
        await().atMost(30, SECONDS).pollDelay(5, SECONDS).pollInterval(5, SECONDS)
                .until(matchGroupName(offsetProvider));

    }

    @After
    public void destroy() throws InterruptedException {
        offsetProvider.stop();
    }

    public static Callable<Boolean> matchGroupName(KafkaOffsetProvider offsetProvider) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                List<KafkaOffset> kafkaOffsetMonitors = new ArrayList<>();
                Map<Integer, KafkaOffset> map = offsetProvider.getConsumerOffsetMap().get("USER_TOPIC");
                if (map != null) {
                    kafkaOffsetMonitors.addAll(map.values());
                }
                List<String> groupNames = kafkaOffsetMonitors.stream().map(offset -> offset.getConsumerGroupName())
                        .collect(Collectors.toList());
                return groupNames.contains("OpenNMS");
            }
        };
    }

}
