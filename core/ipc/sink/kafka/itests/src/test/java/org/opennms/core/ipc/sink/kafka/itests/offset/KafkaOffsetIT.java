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
package org.opennms.core.ipc.sink.kafka.itests.offset;

import static org.awaitility.Awaitility.await;
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
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.sink.kafka.server.offset.KafkaOffset;
import org.opennms.core.ipc.sink.kafka.server.offset.KafkaOffsetProvider;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-test-ipc-sink-kafka.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-opennms-identity.xml"})
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
    public void destroy() { 
        try {
            offsetProvider.stop();
        } catch (Throwable e) {
            //Ignore
        }
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
                return groupNames.contains(SystemInfoUtils.getInstanceId());
            }
        };
    }

}
