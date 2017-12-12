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

package org.opennms.features.kafka.offset;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka-offset.xml" })
@JUnitConfigurationEnvironment
public class KafkaOffsetIT {

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private KafkaOffsetProvider offsetProvider;
  

    @Before
    public void setup() throws Exception {
        System.setProperty(String.format("%sbootstrap.servers", KafkaOffsetConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset", KafkaOffsetConstants.KAFKA_CONFIG_SYS_PROP_PREFIX),
                "earliest");
        // offsetProvider needs system properties, so we set these manually insted of spring doing it for us
        offsetProvider = new KafkaOffsetProvider();
        offsetProvider.start();
    }

    @Test
    public void testOffsetGeneration() throws Exception {

        KafkaMessageConsumer kafkaConsumer = new KafkaMessageConsumer(kafkaServer.getKafkaConnectString());
        kafkaConsumer.startConsumer();
        KafkaMessageProducer kafkaProducer = new KafkaMessageProducer(kafkaServer.getKafkaConnectString());
        kafkaProducer.produce();
        Thread.sleep(30000);
        List<KafkaOffset> kafkaOffsetMonitors = new ArrayList<>();
        Map<String, KafkaOffset> map = offsetProvider.getNewConsumer().get("USER_TOPIC");
        if (map != null) {
            kafkaOffsetMonitors.addAll(map.values());
        }
        assertThat(kafkaOffsetMonitors, not(IsEmptyCollection.empty()));

/*        await().atMost(1, MINUTES).until(() -> groupName.contains(kafkaConsumer.getGroupName()));
        assertTrue(groupName.contains(kafkaConsumer.getGroupName()));*/
    }

}
