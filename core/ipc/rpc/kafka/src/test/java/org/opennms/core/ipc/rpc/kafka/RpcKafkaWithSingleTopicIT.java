/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
