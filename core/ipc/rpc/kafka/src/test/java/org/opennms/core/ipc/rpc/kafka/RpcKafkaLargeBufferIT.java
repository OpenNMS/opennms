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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.ipc.common.kafka.KafkaRpcConstants;
import org.opennms.core.ipc.common.kafka.OsgiKafkaConfigProvider;
import org.opennms.core.rpc.api.RequestTimedOutException;
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

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private MockSnmpClient snmpClient;

    private KafkaRpcClientFactory rpcClient;

    private RpcTestServer kafkaRpcServer;

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
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(KafkaRpcConstants.MAX_BUFFER_SIZE_PROPERTY, MAX_BUFFER_SIZE);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaRpcConstants.KAFKA_RPC_CONFIG_PID).getProperties())
                .thenReturn(kafkaConfig);
        minionIdentity = new MockMinionIdentity(REMOTE_LOCATION_NAME);
        kafkaRpcServer = new RpcTestServer(new OsgiKafkaConfigProvider(KafkaRpcConstants.KAFKA_RPC_CONFIG_PID, configAdmin),
                minionIdentity,tracerRegistry);

        kafkaRpcServer.init();
        kafkaRpcServer.bind(mockSnmpModule);
        rpcClient.start();
    }

    /**
     * This test verifies that the message is processed properly by skipping duplicate chunk
     */
    @Test(timeout = 30000)
    public void testLargeBufferWithDuplicateChunks() throws ExecutionException, InterruptedException {
        SnmpRequestDTO requestDTO = new SnmpRequestDTO();
        requestDTO.setLocation(REMOTE_LOCATION_NAME);
        requestDTO.setTimeToLive(10000L);
        String xmlFile = MockSnmpClient.class.getResource("/snmp-response.xml").getFile();
        SnmpMultiResponseDTO expectedResponseDTO = JaxbUtils.unmarshal(SnmpMultiResponseDTO.class, new File(xmlFile));
        SnmpMultiResponseDTO responseDTO = snmpClient.execute(requestDTO).get();
        Assert.assertTrue(kafkaRpcServer.isSkippedOrDuplicated());
        Assert.assertEquals(expectedResponseDTO, responseDTO);
    }

    /**
     * This test verifies that the message will not be processed if a chunk is missing and rpc client throws
     * timeout exception instead of unmarshal exception.
     */
    @Test(timeout = 30000)
    public void testLargeBufferBySkippingChunks() throws ExecutionException, InterruptedException {
        kafkaRpcServer.setSkipChunks(true);
        SnmpRequestDTO requestDTO = new SnmpRequestDTO();
        requestDTO.setTimeToLive(10000L);
        requestDTO.setLocation(REMOTE_LOCATION_NAME);
        String xmlFile = MockSnmpClient.class.getResource("/snmp-response.xml").getFile();
        SnmpMultiResponseDTO expectedResponseDTO = JaxbUtils.unmarshal(SnmpMultiResponseDTO.class, new File(xmlFile));
        try {
            snmpClient.execute(requestDTO).get();
            fail();
        } catch (ExecutionException e) {
            Assert.assertTrue(kafkaRpcServer.isSkippedOrDuplicated());
            assertEquals(RequestTimedOutException.class, e.getCause().getClass());
        }
    }

    @After
    public void destroy() throws Exception {
        kafkaRpcServer.unbind(mockSnmpModule);
        kafkaRpcServer.destroy();
        rpcClient.stop();
    }

}
