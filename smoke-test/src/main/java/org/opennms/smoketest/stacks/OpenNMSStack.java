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
package org.opennms.smoketest.stacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opennms.smoketest.containers.OpenNMSCassandraContainer;
import org.opennms.smoketest.containers.ElasticsearchContainer;
import org.opennms.smoketest.containers.JaegerContainer;
import org.opennms.smoketest.containers.LocalOpenNMS;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.containers.SentinelContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * This is the highest level interface to a stack. A stack is composed of
 * containers and profiles (i.e. settings) for these.
 *
 * We aim to make it easy to spawn a stack with all the necessary services
 * attached and provide easy access to these services. Access can range from
 * direct API access with proper interfaces or simple references to the sockets.
 *
 * Given a {@link StackModel} this class will create the appropriate containers
 * and chain their initialization in a way that allows the stack to come up cleanly.
 *
 * @author jwhite
 */
public final class OpenNMSStack implements TestRule {

    /**
     * This creates an empty OpenNMS stack for testing with locally-installed components outside of Docker.
     */
    public static final OpenNMSStack NONE = new OpenNMSStack();

	public static final OpenNMSStack MINIMAL = minimal();

	public static final OpenNMSStack MINIMAL_WITH_DEFAULT_LOCALHOST = OpenNMSStack.withModel(StackModel.newBuilder().build());

    public static final OpenNMSStack MINION = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .build());

    public static final OpenNMSStack SENTINEL = OpenNMSStack.withModel(StackModel.newBuilder()
            .withSentinel()
            .build());

    public static final OpenNMSStack ALEC = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinions(MinionProfile.DEFAULT, MinionProfile.newBuilder()
                    .withLocation("BANANA")
                    .build())
            .withSentinel()
            .withElasticsearch()
            .withIpcStrategy(IpcStrategy.KAFKA)
            .build());

    public static OpenNMSStack withModel(StackModel model) {
        return new OpenNMSStack(model);
    }

    private final TestRule delegateTestRule;

    private final PostgreSQLContainer postgreSQLContainer;

    private final JaegerContainer jaegerContainer;

    private final OpenNMSContainer opennmsContainer;

    private final KafkaContainer kafkaContainer;

    private final ElasticsearchContainer elasticsearchContainer;

    private final OpenNMSCassandraContainer cassandraContainer;

    private final List<MinionContainer> minionContainers;

    private final List<SentinelContainer> sentinelContainers;

    public static OpenNMSStack minimal(Consumer<OpenNMSProfile.Builder>... with) {
        var builder = OpenNMSProfile.newBuilder();

        builder.withFile("empty-discovery-configuration.xml", "etc/discovery-configuration.xml");

        for (var w : with) {
            w.accept(builder);
        }

        return OpenNMSStack.withModel(StackModel.newBuilder()
                .withOpenNMS(builder.build())
                .build());
    }

    /**
     * Create an empty OpenNMS stack for testing with locally-installed components outside of Docker.
     */
    private OpenNMSStack() {
        postgreSQLContainer = null;
        jaegerContainer = null;
        elasticsearchContainer = null;
        kafkaContainer = null;
        cassandraContainer = null;
        opennmsContainer = new LocalOpenNMS();
        minionContainers = Collections.EMPTY_LIST;
        sentinelContainers = Collections.EMPTY_LIST;

        delegateTestRule = RuleChain.emptyRuleChain();
    }

    private OpenNMSStack(StackModel model) {
        postgreSQLContainer = new PostgreSQLContainer();
        RuleChain chain = RuleChain.outerRule(postgreSQLContainer);

        if (model.isJaegerEnabled()) {
            jaegerContainer = new JaegerContainer();
            chain = chain.around(jaegerContainer);
        } else {
            jaegerContainer = null;
        }

        if (model.isElasticsearchEnabled()) {
            elasticsearchContainer = new ElasticsearchContainer();
            chain = chain.around(elasticsearchContainer);
        } else {
            elasticsearchContainer = null;
        }

        final boolean shouldEnableKafka = IpcStrategy.KAFKA.equals(model.getIpcStrategy())
                || model.getOpenNMS().isKafkaProducerEnabled();
        if (shouldEnableKafka) {
            kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.0"))
                    // Reduce from the default of 1GB
                    .withEnv("KAFKA_HEAP_OPTS", "-Xms256m -Xmx256m")
                    .withNetwork(Network.SHARED)
                    .withNetworkAliases(OpenNMSContainer.KAFKA_ALIAS);
            chain = chain.around(kafkaContainer);
        } else {
            kafkaContainer = null;
        }

        if (TimeSeriesStrategy.NEWTS.equals(model.getTimeSeriesStrategy())) {
            cassandraContainer = new OpenNMSCassandraContainer();
            cassandraContainer.withNetwork(Network.SHARED)
                    .withNetworkAliases(OpenNMSContainer.CASSANDRA_ALIAS);
            chain = chain.around(cassandraContainer);
        } else {
            cassandraContainer = null;
        }

        opennmsContainer = new OpenNMSContainer(model, model.getOpenNMS());
        chain = chain.around(opennmsContainer);

        final List<MinionContainer> minions = new ArrayList<>(model.getMinions().size());
        for (final MinionProfile profile : model.getMinions()) {
            final MinionContainer minion = new MinionContainer(model, profile);
            minions.add(minion);
            chain = chain.around(minion);
        }
        minionContainers = Collections.unmodifiableList(minions);

        final List<SentinelContainer> sentinels = new ArrayList<>(model.getSentinels().size());
        for (SentinelProfile profile : model.getSentinels()) {
            final SentinelContainer sentinel = new SentinelContainer(model, profile);
            sentinels.add(sentinel);
            chain = chain.around(sentinel);
        }
        sentinelContainers = Collections.unmodifiableList(sentinels);

        delegateTestRule = chain;
    }

    public OpenNMSContainer opennms() {
        return opennmsContainer;
    }

    public MinionContainer minion() {
        if (minionContainers.isEmpty()) {
            throw new IllegalStateException("Minion container is not enabled in this stack.");
        }
        return minionContainers.get(0);
    }

    public MinionContainer minions(int index) {
        return minionContainers.get(index);
    }

    public SentinelContainer sentinel() {
        if (sentinelContainers.isEmpty()) {
            throw new IllegalStateException("Sentinel container is not enabled in this stack.");
        }
        return sentinelContainers.get(0);
    }

    public SentinelContainer sentinels(int index) {
        return sentinelContainers.get(index);
    }

    public JaegerContainer jaeger() {
        if (jaegerContainer == null) {
            throw new IllegalStateException("Jaeger container is not enabled in this stack.");
        }
        return jaegerContainer;
    }
    public ElasticsearchContainer elastic() {
        if (elasticsearchContainer == null) {
            throw new IllegalStateException("Elasticsearch container is not enabled in this stack.");
        }
        return elasticsearchContainer;
    }

    public PostgreSQLContainer postgres() {
        return postgreSQLContainer;
    }

    public KafkaContainer kafka() {
        if (kafkaContainer == null) {
            throw new IllegalStateException("Kafka container is not enabled in this stack.");
        }
        return kafkaContainer;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        // Delegate to the test rule we built during initialization
        return delegateTestRule.apply(base, description);
    }

}
