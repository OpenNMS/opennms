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
package org.opennms.netmgt.flows.elastic;


import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.core.test.elastic.ExecutionTime;
import org.opennms.features.elastic.client.ElasticRestClient;
import org.opennms.features.elastic.client.ElasticRestClientFactory;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.executors.DefaultRequestExecutor;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Ignore("Doesn't work after template initialization changed from Jest to Elastic, need to revisit")
public class ElasticFlowRepositoryRetryIT {

    private static final long START_DELAY = 10000; // in ms
    private static final long EXECUTION_TIME_DIFF = 2500; // in ms
    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final long RETRY_COOLDOWN = 500;

    // The timeout is needed because starting the elasticsearch server may fail, but we will not get notified
    // The timeout will ensure that the test will not block for ever, but fail eventually
    @Rule
    public Timeout timeout = new Timeout(START_DELAY * 5, TimeUnit.MILLISECONDS);

    @Rule
    public ExecutionTime executionTime = new ExecutionTime(START_DELAY, TimeUnit.MILLISECONDS, EXECUTION_TIME_DIFF);

    @Rule
    public ElasticSearchRule elasticServerRule = new ElasticSearchRule(
            new ElasticSearchServerConfig()
                .withStartDelay(START_DELAY)
                .withManualStartup()
    );

    @Test
    public void verifySaveSucceedsWhenServerBecomesAvailable() throws Exception {
        // try persisting data
        apply((repository) -> repository.persist(
                Lists.newArrayList(EnrichedFlow.from(FlowDocumentTest.getMockFlow()))));
    }

    private void apply(FlowRepositoryConsumer consumer) throws Exception {
        Objects.requireNonNull(consumer);

        final ElasticRestClientFactory elasticRestClientFactory = new ElasticRestClientFactory(elasticServerRule.getUrl(), null, null);
        final ElasticRestClient elasticRestClient = elasticRestClientFactory.createClient();
        final RestClientFactory restClientFactory = new RestClientFactory(elasticServerRule.getUrl());
        restClientFactory.setRequestExecutorSupplier(() -> new DefaultRequestExecutor(RETRY_COOLDOWN));
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        try (JestClientWithCircuitBreaker client = restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(ElasticFlowRepositoryRetryIT.class.getName()), eventForwarder)) {
            executionTime.resetStartTime();
            elasticServerRule.startServer();

            final FlowRepository elasticFlowRepository = new InitializingFlowRepository(
                    new ElasticFlowRepository(new MetricRegistry(), elasticRestClient, IndexStrategy.MONTHLY,
                            new MockIdentity(), new MockTracerRegistry(), new IndexSettings()), elasticRestClient);

            consumer.accept(elasticFlowRepository);

        } catch (FlowException e) {
            throw Throwables.propagate(e);
        }
        elasticServerRule.stopServer();

    }

    @FunctionalInterface
    interface FlowRepositoryConsumer {
        void accept(FlowRepository flowRepository) throws FlowException;
    }
}
