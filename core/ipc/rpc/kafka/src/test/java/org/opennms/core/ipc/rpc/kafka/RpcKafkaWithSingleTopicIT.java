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
package org.opennms.core.ipc.rpc.kafka;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.KAFKA_RPC_CONFIG_SYS_PROP_PREFIX;
import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.SINGLE_TOPIC_FOR_ALL_MODULES;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.ipc.common.kafka.KafkaRpcConstants;
import org.opennms.core.ipc.common.kafka.OsgiKafkaConfigProvider;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import com.codahale.metrics.MetricRegistry;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * This tests the same tests from RpcKafkaIT with single topic for all modules by setting
 * org.opennms.core.ipc.rpc.kafka.single-topic = true.
 */
public class RpcKafkaWithSingleTopicIT extends RpcKafkaIT {

    private static final String KAFKA_CONFIG_PID = "org.opennms.core.ipc.rpc.kafka.";
    public static final String REMOTE_LOCATION_NAME = "remote";


    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private MockEchoClient echoClient;

    private KafkaRpcClientFactory rpcClient;

    private KafkaRpcServerManager kafkaRpcServer;

    private MinionIdentity minionIdentity;

    private EchoRpcModule echoRpcModule = new EchoRpcModule();

    private Hashtable<String, Object> kafkaConfig = new Hashtable<>();

    private AtomicInteger count = new AtomicInteger(0);

    static TracerRegistry tracerRegistry = new TracerRegistry() {
        @Override
        public Tracer getTracer() {
            return GlobalTracer.get();
        }

        @Override
        public void init(String serviceName) {
        }
    };


    @Before
    @Override
    public void setup() throws Exception {
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(KafkaRpcConstants.SINGLE_TOPIC_FOR_ALL_MODULES, "true");
        kafkaConfig.put(KafkaRpcConstants.MAX_CONCURRENT_CALLS_PROPERTY, MAX_CONCURRENT_CALLS);
        kafkaConfig.put(KafkaRpcConstants.MAX_DURATION_BULK_HEAD, MAX_DURATION_BULK_HEAD);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaRpcConstants.KAFKA_RPC_CONFIG_PID).getProperties())
                .thenReturn(kafkaConfig);
        rpcClient = new KafkaRpcClientFactory();
        rpcClient.setTracerRegistry(tracerRegistry);
        echoClient = new MockEchoClient(rpcClient);
        rpcClient.start();
        minionIdentity = new MockMinionIdentity(REMOTE_LOCATION_NAME);
        kafkaRpcServer = new KafkaRpcServerManager(new OsgiKafkaConfigProvider(KafkaRpcConstants.KAFKA_RPC_CONFIG_PID, configAdmin),
                minionIdentity, tracerRegistry, new MetricRegistry());
        kafkaRpcServer.init();
        kafkaRpcServer.bind(echoRpcModule);

    }

    @After
    @Override
    public void destroy() throws Exception {
        kafkaRpcServer.unbind(echoRpcModule);
        kafkaRpcServer.destroy();
        rpcClient.stop();
    }

    @Override
    public EchoRpcModule getEchoRpcModule() {
        return echoRpcModule;
    }

    @Override
    public MockEchoClient getEchoClient() {
        return echoClient;
    }

    @Override
    public MinionIdentity getMinionIdentity() {
        return minionIdentity;
    }

    @Override
    public Hashtable<String, Object> getKafkaConfig() {
        return kafkaConfig;
    }

    @Override
    public KafkaRpcServerManager getKafkaRpcServer() {
        return kafkaRpcServer;
    }

    @Test
    public void testSingleTopicProperty() {
        //Set False.
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, KafkaRpcConstants.SINGLE_TOPIC_FOR_ALL_MODULES), "false");
        boolean result = SystemProperties.getBooleanWithDefaultAsTrue(String.format("%s%s", KAFKA_RPC_CONFIG_SYS_PROP_PREFIX, SINGLE_TOPIC_FOR_ALL_MODULES));
        assertFalse(result);
        //Invalid falls back to true.
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, KafkaRpcConstants.SINGLE_TOPIC_FOR_ALL_MODULES), "false ");
        result = SystemProperties.getBooleanWithDefaultAsTrue(String.format("%s%s", KAFKA_RPC_CONFIG_SYS_PROP_PREFIX, SINGLE_TOPIC_FOR_ALL_MODULES));
        assertTrue(result);
        //Set True.
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, KafkaRpcConstants.SINGLE_TOPIC_FOR_ALL_MODULES), "true");
        result = SystemProperties.getBooleanWithDefaultAsTrue(String.format("%s%s", KAFKA_RPC_CONFIG_SYS_PROP_PREFIX, SINGLE_TOPIC_FOR_ALL_MODULES));
        assertTrue(result);
    }

}
