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

package org.opennms.core.ipc.rpc.kafka;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.ipc.common.kafka.OsgiKafkaConfigProvider;
import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.rpc.echo.MyEchoException;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.common.base.Strings;

public class RpcKafkaIT {

    private static final String KAFKA_CONFIG_PID = "org.opennms.core.ipc.rpc.kafka.";
    public static final String REMOTE_LOCATION_NAME = "remote";
    public static final String MAX_BUFFER_SIZE = "500000";

    static {
        System.setProperty(String.format("%sttl", KAFKA_CONFIG_PID), "20000");
        System.setProperty(String.format("%smax.buffer.size", KAFKA_CONFIG_PID), MAX_BUFFER_SIZE);
    }

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer();

    private MockEchoClient echoClient;

    private KafkaRpcClientFactory rpcClient;

    private KafkaRpcServerManager kafkaRpcServer;

    private MinionIdentity minionIdentity;

    private EchoRpcModule echoRpcModule = new EchoRpcModule();
    
    private Hashtable<String, Object> kafkaConfig = new Hashtable<>();

    private AtomicInteger count = new AtomicInteger(0);
    

    @Before
    public void setup() throws Exception {
        System.setProperty(String.format("%sbootstrap.servers", KAFKA_CONFIG_PID), kafkaServer.getKafkaConnectString());
        System.setProperty(String.format("%sauto.offset.reset", KAFKA_CONFIG_PID), "earliest");
        rpcClient = new KafkaRpcClientFactory();
        echoClient = new MockEchoClient(rpcClient);
        rpcClient.start();
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put("rpcid.cache.config", "maximumSize=1000,expireAfterWrite=15s");
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(KafkaRpcConstants.KAFKA_CONFIG_PID).getProperties())
                .thenReturn(kafkaConfig);
        minionIdentity = new MockMinionIdentity(REMOTE_LOCATION_NAME);
        kafkaRpcServer = new KafkaRpcServerManager(new OsgiKafkaConfigProvider(KafkaRpcConstants.KAFKA_CONFIG_PID, configAdmin), minionIdentity);
        kafkaRpcServer.init();
        kafkaRpcServer.bind(echoRpcModule);
    }

    @Test(timeout = 30000)
    public void testKafkaRpcAtDefaultLocation() throws InterruptedException, ExecutionException {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        EchoResponse expectedResponse = new EchoResponse("Kafka-RPC");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(timeout = 30000)
    public void testKafkaRpcAtRemoteLocationWithSystemId() throws InterruptedException, ExecutionException {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.setSystemId(minionIdentity.getId());
        request.setLocation(REMOTE_LOCATION_NAME);
        EchoResponse expectedResponse = new EchoResponse("Kafka-RPC");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(timeout = 30000)
    public void testKafkaRpcAtRemoteLocation() throws InterruptedException, ExecutionException {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.setLocation(REMOTE_LOCATION_NAME);
        request.setDelay(5000L);
        EchoResponse expectedResponse = new EchoResponse("Kafka-RPC");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(timeout = 30000)
    public void testKafkaRpcAtRemoteLocationWithWrongSystemId() throws InterruptedException {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.setSystemId("!" + minionIdentity.getId());
        request.setLocation(REMOTE_LOCATION_NAME);
        try {
            echoClient.execute(request).get();
            fail("Did not get ExecutionException");
        } catch (ExecutionException e) {
            assertTrue("Cause is not of type TimedOutException: " + ExceptionUtils.getStackTrace(e),
                    e.getCause() instanceof RequestTimedOutException);
        }
    }

    @Test(timeout = 30000)
    public void testExceptionWhileExecutingLocally() throws InterruptedException {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.shouldThrow(true);
        try {
            echoClient.execute(request).get();
            fail();
        } catch (ExecutionException e) {
            assertEquals("Kafka-RPC", e.getCause().getMessage());
            assertEquals(MyEchoException.class, e.getCause().getClass());
        }

    }

    @Test(timeout = 30000)
    public void testExceptionWhileExecutingOnRemoteLocation() throws InterruptedException {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.shouldThrow(true);
        request.setSystemId(minionIdentity.getId());
        request.setLocation(REMOTE_LOCATION_NAME);
        try {
            echoClient.execute(request).get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getMessage(), e.getCause().getMessage().contains("Kafka-RPC"));
            assertEquals(RemoteExecutionException.class, e.getCause().getClass());
        }
    }

    @Test(timeout = 60000)
    public void stressTestKafkaRpcWithDifferentTimeouts() {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.setId(System.currentTimeMillis());
        request.setLocation(REMOTE_LOCATION_NAME);
        request.setSystemId("xxxxxxxx");
        // Send 100 requests.
        int maxRequests = 100;
        count.set(0);
        for (int i = 0; i < maxRequests; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(5, 30);
            sendRequestAndVerifyResponse(request, randomNum*1000);
        }
        await().atMost(45, TimeUnit.SECONDS).untilAtomic(count, equalTo(maxRequests));
    }
    
    @Test(timeout = 60000)
    public void stressTestKafkaRpc() {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.setId(System.currentTimeMillis());
        request.setLocation(REMOTE_LOCATION_NAME);
        count.set(0);
        // Send 100 requests.
        int maxRequests = 100;
        for (int i = 0; i < maxRequests; i++) {
            sendRequestAndVerifyResponse(request, 0);
        }
        await().atMost(45, TimeUnit.SECONDS).untilAtomic(count, equalTo(maxRequests));
    }

    @SuppressWarnings("unused")
    @Test(timeout = 60000)
    public void stressTestKafkaRpcWithSystemIdAndTestMinionSingleRequest() {
        EchoRequest request = new EchoRequest("Kafka-RPC");
        request.setId(System.currentTimeMillis());
        request.setLocation(REMOTE_LOCATION_NAME);
        request.setSystemId(minionIdentity.getId());
        count.set(0);
        // Send 100 requests.
        int maxRequests = 100;
        for (int i = 0; i < maxRequests; i++) {
            sendRequestAndVerifyResponse(request, 0);
        }
        // Try to consume response messages to test single request on minion.
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "testMinionCache");
        kafkaConfig.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaConfig);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicInteger messageCount = new AtomicInteger(0);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Pattern pattern = Pattern.compile(".*rpc-response.*");
                kafkaConsumer.subscribe(pattern);
                while (!closed.get()) {
                    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        messageCount.incrementAndGet();
                    }
                }
            } catch (Exception e) {
               //Ignore
            } finally {
               kafkaConsumer.close();
            }
            
        });
        await().atMost(45, TimeUnit.SECONDS).untilAtomic(count, equalTo(maxRequests));
        await().atMost(45, TimeUnit.SECONDS).untilAtomic(messageCount, equalTo(maxRequests));
        await().atMost(45, TimeUnit.SECONDS).pollDelay(15, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() ->   {
            // In real world scenario, this will be done by cache in read/write operations. Don't need explicit cleanUp.
            kafkaRpcServer.getRpcIdCache().cleanUp();
            return kafkaRpcServer.getRpcIdCache().size() == 0;
        } );
        closed.set(true);
    }


    @Test(timeout = 45000)
    public void testLargeMessages() throws ExecutionException, InterruptedException {
        final EchoRequest request = new EchoRequest();
        request.setLocation(REMOTE_LOCATION_NAME);
        List<String> largeMessage = new ArrayList<>();
        for(int i=0; i < 100; i ++) {
            String message = Strings.repeat("*", Integer.parseInt(MAX_BUFFER_SIZE));
            largeMessage.add(message);
        }
        request.setLargeMessage(largeMessage);
        EchoResponse expectedResponse = new EchoResponse();
        expectedResponse.setLargeMessage(largeMessage);
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(timeout = 45000)
    public void testLargeMessagesWithSystemId() throws ExecutionException, InterruptedException {
        final EchoRequest request = new EchoRequest();
        request.setLocation(REMOTE_LOCATION_NAME);
        request.setSystemId(minionIdentity.getId());
        List<String> largeMessage = new ArrayList<>();
        for(int i=0; i < 100; i ++) {
            String message = Strings.repeat("*", Integer.parseInt(MAX_BUFFER_SIZE));
            largeMessage.add(message);
        }
        request.setLargeMessage(largeMessage);
        EchoResponse expectedResponse = new EchoResponse();
        expectedResponse.setLargeMessage(largeMessage);
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }

    private void sendRequestAndVerifyResponse(EchoRequest request, long ttl) {
        request.setTimeToLiveMs(ttl);
        echoClient.execute(request).whenComplete((response, e) -> {
            if (e != null) {
                long responseTime = System.currentTimeMillis() - request.getId();
                assertThat(responseTime, greaterThan(ttl));
                assertThat(responseTime, lessThan(ttl + 10000));
                count.getAndIncrement();
            } else {
                count.getAndIncrement();
            }
        });
    }

    @After
    public void destroy() throws Exception {
        kafkaRpcServer.unbind(echoRpcModule);
        rpcClient.stop();
    }

}
