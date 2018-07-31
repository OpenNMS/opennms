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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.smoketest.minion.CommandTestUtils;
import org.opennms.smoketest.minion.DetectorsOnMinionIT;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.utils.SshClient;

/** Abstract methods and tests corresponding to kafka persister **/
public abstract class BaseKafkaPersisterIT {
    
    protected static TestEnvironment m_testEnvironment;
    protected static RestClient restClient;
    protected InetSocketAddress opennmsKarafSshAddr;
    
    
    protected  String persistCollectionData(String nodeId, String host) throws Exception {
        String shellOutput;
        try (final SshClient sshClient = new SshClient(opennmsKarafSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            //collection:collect --node #nodeId --persist org.opennms.netmgt.collectd.SnmpCollector #host
            pipe.println("collection:collect --node " + nodeId + " --persist org.opennms.netmgt.collectd.SnmpCollector " + host);
            pipe.println("logout");
            await().atMost(30, SECONDS).until(sshClient.isShellClosedCallable());
            shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
        }
        return shellOutput;
    }
    
    protected void runKafkaPeristerTest() throws Exception {
        
        Date startOfTest = new Date();
        // Enable and install the Kafka producer feature
        String kafkaHost = m_testEnvironment.getContainerInfo(ContainerAlias.KAFKA).networkSettings().ipAddress();
        kafkaHost = kafkaHost + ":9092";
        try (final SshClient sshClient = new SshClient(opennmsKarafSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.features.kafka.producer.client");
            pipe.println("config:property-set bootstrap.servers " + kafkaHost);
            pipe.println("config:update");
            pipe.println("config:edit org.opennms.features.kafka.producer");
            pipe.println("config:property-set forward.metrics true");
            pipe.println("config:property-set metricTopic  metrics");
            pipe.println("config:update");
            pipe.println("feature:install opennms-kafka-producer");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }
        String snmpHost = m_testEnvironment.getContainerInfo(ContainerAlias.SNMPD).networkSettings().ipAddress();
        DetectorsOnMinionIT.addRequisition(restClient, null, snmpHost);
        InetSocketAddress pgsql = m_testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);
        final OnmsNode onmsNode = await().atMost(1, MINUTES).pollInterval(15, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class)
                        .ge("createTime", startOfTest).eq("label", snmpHost).toCriteria()), notNullValue());

        assertNotNull(onmsNode);
        String nodeId = onmsNode.getId().toString();
        KafkaMessageConsumerRunner kafkaConsumer = new KafkaMessageConsumerRunner(kafkaHost, "metrics");
        kafkaConsumer.setNodeId(nodeId);
        Executors.newSingleThreadExecutor().execute(kafkaConsumer);
        await().atMost(2, MINUTES).pollInterval(15, SECONDS)
        .until(() -> persistCollectionData(nodeId, snmpHost), containsString("Persisted collection"));
        
        // Can't get proto3 in here, so only verify non-null
        await().atMost(1, MINUTES).pollInterval(15, SECONDS).until(() -> kafkaConsumer.getValue(), not(nullValue()));
        kafkaConsumer.stop();
    }
    

    public static class KafkaMessageConsumerRunner implements Runnable {

        private String topicName;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private KafkaConsumer<String, byte[]> consumer;
        private byte[] value;
        private String nodeId;

        public KafkaMessageConsumerRunner(String bootstrapServer, String topicName) {
            this.topicName = topicName;
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            props.put(ConsumerConfig.GROUP_ID_CONFIG,
                    KafkaProducerIT.class.getCanonicalName() + "-" + UUID.randomUUID().toString());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.TRUE.toString());
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            consumer = new KafkaConsumer<>(props);
        }

        @Override
        public void run() {

            consumer.subscribe(Arrays.asList(topicName));
            while (!closed.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(1000);
                for (ConsumerRecord<String, byte[]> record : records) {
                    if (record.key().equals(nodeId)) {
                        setValue(record.value());
                    }
                }
            }
            consumer.close();
        }

        public byte[] getValue() {
            return value;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }
        
        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public void stop() {
            closed.set(true);
        }
    }

}
