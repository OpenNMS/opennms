/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.ipc.rpc.kafka.KafkaRpcConstants.DEFAULT_TTL_PROPERTY;
import static org.opennms.core.ipc.rpc.kafka.KafkaRpcConstants.MAX_BUFFER_SIZE_PROPERTY;

import java.io.File;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.ipc.common.kafka.OsgiKafkaConfigProvider;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.netmgt.snmp.proxy.common.SnmpMultiResponseDTO;
import org.opennms.netmgt.snmp.proxy.common.SnmpRequestDTO;
import org.osgi.service.cm.ConfigurationAdmin;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

public class RpcKafkaLargeBufferIT {

    private static final String KAFKA_CONFIG_PID = "org.opennms.core.ipc.rpc.kafka.";
    private static final String REMOTE_LOCATION_NAME = "remote";
    private static final String MAX_BUFFER_SIZE = "1000";
    static final Integer MAX_BUFFER_SIZE_CONFIGURED = 1000;

    static {
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, DEFAULT_TTL_PROPERTY), "20000");
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, MAX_BUFFER_SIZE_PROPERTY), MAX_BUFFER_SIZE);
    }

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private MockSnmpClient snmpClient;

    private KafkaRpcClientFactory rpcClient;

    private MockKafkaServerManager kafkaRpcServer;

    private MinionIdentity minionIdentity;

    private MockSnmpModule mockSnmpModule = new MockSnmpModule();

    private Hashtable<String, Object> kafkaConfig = new Hashtable<>();

    private TracerRegistry tracerRegistry = new TracerRegistry() {
        @Override
        public Tracer getTracer() {
            return GlobalTracer.get();
        }

        @Override
        public void init(String serviceName) {
        }
    };


    @Before
    public void setup() throws Exception {
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%s%s", KAFKA_CONFIG_PID, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        rpcClient = new KafkaRpcClientFactory();
        rpcClient.setTracerRegistry(tracerRegistry);
        snmpClient = new MockSnmpClient(rpcClient, mockSnmpModule);
        rpcClient.start();
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(KafkaRpcConstants.MAX_BUFFER_SIZE_PROPERTY, MAX_BUFFER_SIZE);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaRpcConstants.KAFKA_CONFIG_PID).getProperties())
                .thenReturn(kafkaConfig);
        minionIdentity = new MockMinionIdentity(REMOTE_LOCATION_NAME);
        kafkaRpcServer = new MockKafkaServerManager(new OsgiKafkaConfigProvider(KafkaRpcConstants.KAFKA_CONFIG_PID, configAdmin), minionIdentity);
        kafkaRpcServer.init();
        kafkaRpcServer.bind(mockSnmpModule);
    }


    @Test
    public void testLargeBufferWithDuplicateChunks() throws ExecutionException, InterruptedException {
        SnmpRequestDTO requestDTO = new SnmpRequestDTO();
        requestDTO.setLocation(REMOTE_LOCATION_NAME);
        String xmlFile = MockSnmpClient.class.getResource("/snmp-response.xml").getFile();
        SnmpMultiResponseDTO expectedResponseDTO = JaxbUtils.unmarshal(SnmpMultiResponseDTO.class, new File(xmlFile));
        SnmpMultiResponseDTO responseDTO = snmpClient.execute(requestDTO).get();
        Assert.assertEquals(expectedResponseDTO, responseDTO);
    }

    @After
    public void destroy() throws Exception {
        kafkaRpcServer.unbind(mockSnmpModule);
        rpcClient.stop();
    }
}
