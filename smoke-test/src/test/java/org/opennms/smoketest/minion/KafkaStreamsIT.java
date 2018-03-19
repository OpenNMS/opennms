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

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaStreamsIT {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsIT.class);

    public static final String SENDER_IP = "192.168.1.1";

    private static TestEnvironment m_testEnvironment;

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all().kafka();
            ;
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
    public void verifyEventsForwardedToKafka() throws Exception {

        installKafkaFeaturesOnOpenNMS();
        InetSocketAddress kafkaAddress = m_testEnvironment.getServiceAddress(ContainerAlias.KAFKA, 9092);
        String hostName = kafkaAddress.getHostName();
        String portName = Integer.toString(kafkaAddress.getPort());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        KafkaConsumerRunner kafkaConsumerRunner = new KafkaConsumerRunner(hostName + ":" + portName);
        executor.execute(kafkaConsumerRunner);
        Date startOfTest = new Date();
        InetSocketAddress opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);
        final HttpHost opennmsHttpHost = new HttpHost(opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort());
        // Ignore 302 response to the POST
        HttpClient instance = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

        Executor httpExecutor = Executor.newInstance(instance).auth(opennmsHttpHost, "admin", "admin")
                .authPreemptive(opennmsHttpHost);

        sendnewSuspectEvent(httpExecutor, opennmsHttp, m_testEnvironment, false, startOfTest);

        await().atMost(2, MINUTES).pollDelay(0, SECONDS).pollInterval(15, SECONDS)
                .untilAtomic(kafkaConsumerRunner.getNumberOfEvents(), greaterThan(3));
        kafkaConsumerRunner.shutdown();

    }

    private class KafkaConsumerRunner implements Runnable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private AtomicInteger numberOfEvents = new AtomicInteger(0);
        private String kafkaServer;

        public KafkaConsumerRunner(String kafkaServer) {
            this.kafkaServer = kafkaServer;
        }

        public AtomicInteger getNumberOfEvents() {
            return numberOfEvents;
        }

        public String getKafkaServer() {
            return kafkaServer;
        }

        private KafkaConsumer<String, byte[]> consumer;

        @Override
        public void run() {
            Properties props = new Properties();
            props.put("bootstrap.servers", getKafkaServer());
            props.put("group.id", "opennms");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");

            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList("events"));
            while (!closed.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(1000);
                for (ConsumerRecord<String, byte[]> record : records) {
                    numberOfEvents.incrementAndGet();
                    LOG.info(" Received event with key {} ", record.key());
                }
            }

        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }

    }

    private void installKafkaFeaturesOnOpenNMS() throws Exception {

        InetSocketAddress opennmsSshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8101);

        try (final SshClient sshClient = new SshClient(opennmsSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.features.kafka.producer.client");
            pipe.println("config:property-set group.id opennms");
            pipe.println("config:update");
            pipe.println("feature:install kafka-streams");
            pipe.println("feature:install opennms-kafka-producer");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }

    }

    public static OnmsNode sendnewSuspectEvent(Executor executor, InetSocketAddress opennmsHttp,
            TestEnvironment m_testEnvironment, boolean isMinion, Date startOfTest)
            throws ClientProtocolException, IOException {

        Event minionEvent = new Event();
        minionEvent.setUei("uei.opennms.org/internal/discovery/newSuspect");
        minionEvent.setHost(SENDER_IP);
        minionEvent.setInterface(SENDER_IP);
        minionEvent.setInterfaceAddress(Inet4Address.getByName(SENDER_IP));
        minionEvent.setSource("system-test");
        minionEvent.setSeverity("4");
        if (isMinion) {
            Parm parm = new Parm();
            parm.setParmName("location");
            Value minion = new Value("MINION");
            parm.setValue(minion);
            List<Parm> parms = new ArrayList<>();
            parms.add(parm);
            minionEvent.setParmCollection(parms);
        }

        String xmlString = JaxbUtils.marshal(minionEvent);

        executor.execute(Request.Post(String.format("http://%s:%d/opennms/rest/events",
                opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort()))
                .bodyString(xmlString, ContentType.APPLICATION_XML)).returnContent();

        InetSocketAddress pgsql = m_testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        EventDao eventDao = daoFactory.getDao(EventDaoHibernate.class);
        NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);

        Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI).ge("eventTime", startOfTest)
                .eq("ipAddr", Inet4Address.getByName(SENDER_IP)).toCriteria();

        await().atMost(1, MINUTES).pollInterval(10, SECONDS).until(DaoUtils.countMatchingCallable(eventDao, criteria),
                greaterThan(0));

        final OnmsNode onmsNode = await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class).eq("label", SENDER_IP)
                        .ge("createTime", startOfTest).toCriteria()), notNullValue());

        assertNotNull(onmsNode);

        if (isMinion) {
            assertThat(onmsNode.getLocation().getLocationName(), is("MINION"));
        }

        LOG.info(" New suspect event has been sent and node has been created for IP : {}", SENDER_IP);
        return onmsNode;
    }

}
