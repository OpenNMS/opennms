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
package org.opennms.features.kafka.producer.collection;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
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
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
import org.opennms.netmgt.collection.support.builder.Attribute;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.StringAttribute;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/applicationContext-test-kafka-collection.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = true, tempDbClass = MockDatabase.class, reuseDatabase = false)
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
                    // Confirm that value is set (here to contrast testDefaultValues())
                    assertThat(resource.getNumeric(0).getAllFields().keySet().stream().anyMatch(
                            descriptor -> descriptor.getName().equals("value")), equalTo(Boolean.TRUE));
                    assertThat(resource.getNumeric(0).getMetricValue().getValue(), equalTo(204.0));
                }
        );
        Optional<CollectionSetProtos.CollectionSetResource> nodeLevelResource = resources.stream().filter(CollectionSetProtos.CollectionSetResource::hasNode).findFirst();
        nodeLevelResource.ifPresent(resource -> {
                    assertThat(resource.getNode().getNodeId(), equalTo(node.getId().longValue()));
                    assertThat(resource.getNumericList().size(), equalTo(2));
                    assertThat(resource.getNumeric(0).getValue(), isOneOf(105.0, 1050.0));
                    assertThat(resource.getNumeric(0).getMetricValue().getValue(), isOneOf(105.0, 1050.0));
                    assertThat(resource.getResourceId(), Matchers.containsString(String.valueOf(node.getId())));
                }
        );

    }

    @Test
    public void testDefaultValues() {

        LatencyCollectionResource latencyCollectionResource = new LatencyCollectionResource("ICMP", IP_ADDRESS, LOCATION);
        LatencyCollectionAttributeType attributeType = new LatencyCollectionAttributeType("ICMP", "ICMP");
        latencyCollectionResource.addAttribute(new LatencyCollectionAttribute(latencyCollectionResource, attributeType, "ICMP", 0.0));
        CollectionSet responseTimeCollectionSet = new SingleResourceCollectionSet(latencyCollectionResource, new Date());
        persister.visitCollectionSet(responseTimeCollectionSet);

        await().atMost(1, TimeUnit.MINUTES).pollInterval(15, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(1));
        List<CollectionSetProtos.CollectionSetResource> resources = kafkaConsumer.getCollectionSetValues().stream().map(CollectionSetProtos.CollectionSet::getResourceList).flatMap(Collection::stream).collect(Collectors.toList());
        Optional<CollectionSetProtos.CollectionSetResource> responseTimeResource = resources.stream().filter(CollectionSetProtos.CollectionSetResource::hasResponse).findFirst();
        assertThat(responseTimeResource.isPresent(), equalTo(Boolean.TRUE));
        responseTimeResource.ifPresent(resource -> {
                    assertThat(resource.getResponse().getInstance(), equalTo(IP_ADDRESS));
                    assertThat(resource.getResponse().getLocation(), equalTo(LOCATION));
                    assertThat(resource.getNumeric(0).getValue(), equalTo(0.0));
                    // Confirm that the value does not exist in the message, and we are getting a default value
                    assertThat(resource.getNumeric(0).getAllFields().keySet().stream().anyMatch(
                            descriptor -> descriptor.getName().equals("value")), equalTo(Boolean.FALSE));
                    assertThat(resource.getNumeric(0).getMetricValue().getValue(), equalTo(0.0));
                    assertThat(resource.getNumeric(0).hasMetricValue(), equalTo(Boolean.TRUE));
                }
        );
    }

    @Test
    public void testPersistenceOfResources() throws Exception {

        // Normal CollectionSet with a persistable resource
        OnmsNode node = databasePopulator.getNode5();
        CollectionAgent agent = new MockCollectionAgent(node.getId(), "test", InetAddress.getLocalHost());
        NodeLevelResource nodeResource = new NodeLevelResource(node.getId());

        CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(nodeResource, "group2", "node5", 1050, AttributeType.GAUGE).build();


        // CollectionSet that contains no persisting resources
        LatencyCollectionResource mockEmptySet = new LatencyCollectionResourceMock("SMTP", IP_ADDRESS, LOCATION);
        LatencyCollectionAttributeType attributeType = new LatencyCollectionAttributeType("SMTP", "SMTP");
        mockEmptySet.addAttribute(new LatencyCollectionAttribute(mockEmptySet, attributeType, "SMTP", 9.9));

        CollectionSet responseTimeCollectionSet = new SingleResourceCollectionSet(mockEmptySet, new Date());
        persister.visitCollectionSet(collectionSet);
        persister.visitCollectionSet(responseTimeCollectionSet);

        // Make sure only one resource was persisted
        await().atMost(1, TimeUnit.MINUTES).pollInterval(15, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(1));
        List<CollectionSetProtos.CollectionSetResource> resources = kafkaConsumer.getCollectionSetValues().stream().map(CollectionSetProtos.CollectionSet::getResourceList).flatMap(Collection::stream).collect(Collectors.toList());
        Optional<CollectionSetProtos.CollectionSetResource> responseResource = resources.stream().findFirst();
        assertThat(responseResource.isPresent(), equalTo(Boolean.TRUE));
        responseResource.ifPresent(resource -> {
                    assertThat(resource.getNumeric(0).getValue(), equalTo(1050.0)); // persisted resource
                }
        );
    }

    public void testNMS14740() throws IOException {
        final OnmsNode node = databasePopulator.getNode6();
        final CollectionAgent agent = new MockCollectionAgent(node.getId(), "test", InetAddress.getLocalHost());
        final NodeLevelResource nodeResource = new NodeLevelResource(node.getId());

        // construct a string attribute with a GAUGE attribute type, this triggered the NPE before
        final Attribute<?> brokenAttribute = new StringAttribute("group2", "node6", "foobar", null) {
            @Override
            public AttributeType getType() {
                return AttributeType.GAUGE;
            }
        };

        final CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(nodeResource, "group1", "node6", 105, AttributeType.GAUGE)
                .withAttribute(nodeResource, brokenAttribute).build();

        persister.visitCollectionSet(collectionSet);

        await().atMost(1, TimeUnit.MINUTES).pollInterval(15, TimeUnit.SECONDS).until(() -> kafkaConsumer.getCollectionSetValues().size(), equalTo(1));
        List<CollectionSetProtos.CollectionSetResource> resources = kafkaConsumer.getCollectionSetValues().stream().map(CollectionSetProtos.CollectionSet::getResourceList).flatMap(Collection::stream).collect(Collectors.toList());

        final Optional<CollectionSetProtos.CollectionSetResource> nodeLevelResource = resources.stream().filter(CollectionSetProtos.CollectionSetResource::hasNode).findFirst();
        nodeLevelResource.ifPresent(resource -> {
                    assertThat(resource.getNode().getNodeId(), equalTo(node.getId().longValue()));
                    assertThat(resource.getNumericList().size(), equalTo(2));
                    assertThat(resource.getNumeric(0).getValue(), isOneOf(105.0, 1050.0));
                    // check that a NaN was stored for this invalid value instead of throwing a NPE
                    assertThat(resource.getNumeric(1).getValue(), is(Double.NaN));
                }
        );
    }

    @After
    public void destroy() {
        kafkaConsumer.shutdown();
        executor.shutdown();
    }

    // Resource that should not be persisted
    class LatencyCollectionResourceMock extends LatencyCollectionResource {
        LatencyCollectionResourceMock(String serviceName, String ipAddress, String location) {
            super(serviceName, ipAddress, location);
        }

        @Override
        public boolean shouldPersist(ServiceParameters parameters) {
            return false;
        }
    }
}
