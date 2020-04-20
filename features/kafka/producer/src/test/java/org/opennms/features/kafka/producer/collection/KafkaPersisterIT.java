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

package org.opennms.features.kafka.producer.collection;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.kafka.JUnitKafkaServer;
import org.opennms.features.kafka.producer.KafkaForwarderIT.KafkaMessageConsumerRunner;
import org.opennms.features.kafka.producer.OpennmsKafkaProducer;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.LatencyCollectionAttribute;
import org.opennms.netmgt.collection.api.LatencyCollectionAttributeType;
import org.opennms.netmgt.collection.api.LatencyCollectionResource;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/applicationContext-test-kafka-collection.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false, tempDbClass = MockDatabase.class, reuseDatabase = false)
public class KafkaPersisterIT {

    static final String IP_ADDRESS = "172.0.0.1";

    static final String LOCATION = "MINION";

    @Autowired
    private DatabasePopulator databasePopulator;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public JUnitKafkaServer kafkaServer = new JUnitKafkaServer(tempFolder);

    @Autowired
    private CollectionSetMapper collectionSetMapper;

    private KafkaPersisterFactory kafkaPersisterFactory;

    private Persister persister;

    private ExecutorService executor;

    private KafkaMessageConsumerRunner kafkaConsumer;

    @Before
    public void setup() throws IOException {

        databasePopulator.populateDatabase();
        kafkaPersisterFactory = new KafkaPersisterFactory();
        Hashtable<String, Object> producerConfig = new Hashtable<>();
        producerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaPersisterIT.class.getCanonicalName());
        producerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer.getKafkaConnectString());
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(OpennmsKafkaProducer.KAFKA_CLIENT_PID).getProperties())
                .thenReturn(producerConfig);
        kafkaPersisterFactory.setConfigAdmin(configAdmin);
        kafkaPersisterFactory.setCollectionSetMapper(collectionSetMapper);
        kafkaPersisterFactory.setTopicName("test-metrics");
        kafkaPersisterFactory.init();
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repository = new RrdRepository();
        persister = kafkaPersisterFactory.createPersister(params, repository);
        executor = Executors.newSingleThreadExecutor();
        kafkaConsumer = new KafkaMessageConsumerRunner(kafkaServer.getKafkaConnectString());
        executor.execute(kafkaConsumer);

    }

    @Test
    public void testKafkaCollection() throws IOException {

        OnmsNode node = databasePopulator.getNode5();
        CollectionAgent agent = new MockCollectionAgent(node.getId(), "test", InetAddress.getLocalHost());
        NodeLevelResource nodeResource = new NodeLevelResource(node.getId());

        CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(nodeResource, "group1", "node5", 105, AttributeType.GAUGE)
                .withNumericAttribute(nodeResource, "group2", "node5", 1050, AttributeType.GAUGE).build();

        LatencyCollectionResource latencyCollectionResource = new LatencyCollectionResource("ICMP", IP_ADDRESS, LOCATION);
        LatencyCollectionAttributeType attributeType = new LatencyCollectionAttributeType("ICMP", "ICMP");
        latencyCollectionResource.addAttribute(new LatencyCollectionAttribute(latencyCollectionResource, attributeType, "ICMP", 204.0));
        CollectionSet responseTimeCollectionSet = new SingleResourceCollectionSet(latencyCollectionResource, new Date());
        persister.visitCollectionSet(collectionSet);
        persister.visitCollectionSet(responseTimeCollectionSet);
        
        await().atMost(1, TimeUnit.MINUTES).pollInterval(15, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(2));
        List<CollectionSetProtos.CollectionSetResource> resources = kafkaConsumer.getCollectionSetValues().stream().map(CollectionSetProtos.CollectionSet::getResourceList).flatMap(Collection::stream).collect(Collectors.toList());
        Optional<CollectionSetProtos.CollectionSetResource> responseTimeResource = resources.stream().filter(CollectionSetProtos.CollectionSetResource::hasResponse).findFirst();
        responseTimeResource.ifPresent(resource -> {
                    assertThat(resource.getResponse().getInstance(), equalTo(IP_ADDRESS));
                    assertThat(resource.getResponse().getLocation(), equalTo(LOCATION));
                    assertThat(resource.getNumeric(0).getValue(), equalTo(204.0));
                }
        );
        Optional<CollectionSetProtos.CollectionSetResource> nodeLevelResource = resources.stream().filter(CollectionSetProtos.CollectionSetResource::hasNode).findFirst();
        nodeLevelResource.ifPresent(resource -> {
                    assertThat(resource.getNode().getNodeId(), equalTo(node.getId().longValue()));
                    assertThat(resource.getNumericList().size(), equalTo(2));
                    assertThat(resource.getNumeric(0).getValue(), isOneOf(105.0, 1050.0));
                }
        );

    }

}
