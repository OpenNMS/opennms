/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.test.kafka;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.common.utils.SystemTime;
import org.junit.After;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.admin.AdminUtils;
import kafka.admin.BrokerMetadata;
import kafka.admin.RackAwareMode.Enforced$;
import kafka.metrics.KafkaMetricsReporter;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.mutable.Buffer;

/**
 * This class starts up an embedded Kafka server for use in integration
 * tests.
 * 
 * @author Seth
 */
public class JUnitKafkaServer extends ExternalResource {
    private static final Logger LOG = LoggerFactory.getLogger(JUnitKafkaServer.class);

    private String localhost = "localhost";
    private AtomicInteger kafkaPort = new AtomicInteger(9092);
    private String kafkaLogDir;
    private TemporaryFolder temporaryFolder;

    private KafkaConfig kafkaConfig;
    private KafkaServer kafkaServer;
    private TestingServer zkServer;
    private static final long CLEANER_BUFFER_SIZE = 2 * 1024 * 1024L;

    public JUnitKafkaServer() {
        this("target/kafka-log");
    }

    public JUnitKafkaServer(String kafkaLogDir) {
        this.kafkaLogDir = Objects.requireNonNull(kafkaLogDir);
    }

    public JUnitKafkaServer(TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder;
    }

    @Override
    public void before() throws Exception {
        File zkTempDirectory = null;
        if (temporaryFolder != null) {
            kafkaLogDir = temporaryFolder.newFolder("kafka-log").getAbsolutePath();
            zkTempDirectory = temporaryFolder.newFolder("zookeeper");
        } else {
            FileUtils.deleteDirectory(new File(kafkaLogDir));
        }

        zkServer = new TestingServer(-1, zkTempDirectory, true);
        // Start ZooKeeper, this method will block until the service has started
        zkServer.start();

        getAvailablePort(kafkaPort, 9192);
        localhost = getLocalhost();

        final Properties properties = new Properties();
        properties.put("broker.id", "1");
        properties.put("auto.create.topics.enable", "true");
        properties.put("num.partitions", "10");
        properties.put("enable.zookeeper", "true");
        properties.put("host.name", localhost);
        properties.put("log.dir", kafkaLogDir);
        properties.put("port", String.valueOf(kafkaPort.get()));
        properties.put("zookeeper.connect", zkServer.getConnectString());
        properties.put("offsets.topic.replication.factor", (short)1);
        properties.put("listeners", "PLAINTEXT://" + "localhost:" + String.valueOf(kafkaPort.get()));
        // This was added as kafka by default allocates  128*1024*1024 bytes. Sometimes kafka server is not shutting down properly
        // and this is causing OutOfMemory errors. Since test server doesn't need log deduplication, make it 2MB
        properties.put("log.cleaner.dedupe.buffer.size", CLEANER_BUFFER_SIZE);

        System.err.println("Kafka server properties: " + properties);
        kafkaConfig = new KafkaConfig(properties);

        final List<KafkaMetricsReporter> kmrList = new ArrayList<>();
        final Buffer<KafkaMetricsReporter> metricsList = scala.collection.JavaConversions.asScalaBuffer(kmrList);
        kafkaServer = new KafkaServer(kafkaConfig, new SystemTime(), Option.<String>empty(), metricsList);
        kafkaServer.startup();
        await().atMost(1, MINUTES).until(this::getBrokerMetadatas, hasSize(greaterThanOrEqualTo(1)));

        System.err.println("Kafka Address: " + getKafkaConnectString());
        System.err.println("Zookeeper Address: " + getZookeeperConnectString());
    }

    @After
    public void after() {
        if (kafkaServer != null) {
            kafkaServer.shutdown();
        }

        if (zkServer != null) {
            try {
                zkServer.stop();
            } catch (IOException e) {
                LOG.warn("Failed to stop the ZooKeeper server.", e);
            }
            zkServer = null;
        }
    }

    private List<BrokerMetadata> getBrokerMetadatas() {
        ZkClient zkClient = new ZkClient(getZookeeperConnectString(), 1000, 1000, ZKStringSerializer$.MODULE$);
        ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(getZookeeperConnectString()), false);
        return JavaConversions.seqAsJavaList(AdminUtils.getBrokerMetadatas(zkUtils, Enforced$.MODULE$, Option.empty()));
    }

    private static int getAvailablePort(final AtomicInteger current, final int max) {
        while (current.get() < max) {
            try (final ServerSocket socket = new ServerSocket(current.get())) {
                return socket.getLocalPort();
            } catch (final Throwable e) {}
            current.incrementAndGet();
        }
        throw new IllegalStateException("Can't find an available network port");
    }

    private static String getLocalhost() {
        String address = "localhost";
        try {
            // This is a workaround for people using OS X Lion.  On Lion when a process tries to connect to a link-local
            // address it takes 5 seconds to establish the connection for some reason.  So instead of using 'localhost'
            // which could return the link-local address randomly, we'll manually resolve it and look for an address to
            // return that isn't link-local.  If for some reason we can't find an address that isn't link-local then
            // we'll fall back to the default lof just looking up 'localhost'.
            for (InetAddress a : InetAddress.getAllByName("localhost")) {
                if (!a.isLinkLocalAddress()) {
                    address = a.getHostAddress();
                    break;
                }
            }
        } catch (UnknownHostException e) {
            // Something went wrong, just default to the existing approach of using 'localhost'.
        }
        return address;
    }

    public String getKafkaConnectString() {
        return String.format("%s:%d", localhost, kafkaPort.get());
    }

    public String getZookeeperConnectString() {
        return zkServer.getConnectString();
    }

    public synchronized void stopKafkaServer() {
        kafkaServer.shutdown();
    }

    public synchronized void startKafkaServer() {
        kafkaServer.startup();
        await().atMost(1, MINUTES).until(this::getBrokerMetadatas, hasSize(greaterThanOrEqualTo(1)));
        System.err.println("Kafka Address: " + getKafkaConnectString());
        System.err.println("Zookeeper Address: " + getZookeeperConnectString());
    }
}
