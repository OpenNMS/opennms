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

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.Before;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.features.elastic.client.ElasticRestClient;
import org.opennms.features.elastic.client.ElasticRestClientFactory;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.dao.mock.MockIpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.elastic.agg.AggregatedFlowQueryService;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.flows.processing.impl.DocumentEnricherImpl;
import org.opennms.netmgt.flows.processing.impl.DocumentMangler;

import javax.script.ScriptEngineManager;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class ComposableFlowQueryIT extends FlowQueryIT {

    public  static String relativePathToEtc = Path.of("../../../opennms-base-assembly/src/main/filtered/etc/").toString();

    @Before
    public void setUp() throws MalformedURLException, ExecutionException, InterruptedException {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final ElasticRestClientFactory elasticRestClientFactory = new ElasticRestClientFactory(elasticSearchRule.getUrl(), null, null);
        final ElasticRestClient elasticRestClient = elasticRestClientFactory.createClient();
        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        final JestClientWithCircuitBreaker client = restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(FlowQueryIT.class.getName()), eventForwarder);
        // No specific Index settings should be applied for composable templates, they should be in component settings
        final IndexSettings settings = new IndexSettings();
        final IndexSelector rawIndexSelector = new IndexSelector(settings, RawFlowQueryService.INDEX_NAME,
                IndexStrategy.MONTHLY, 120000);
        final RawFlowQueryService rawFlowRepository = new RawFlowQueryService(client, rawIndexSelector);
        final AggregatedFlowQueryService aggFlowRepository = mock(AggregatedFlowQueryService.class);
        smartQueryService = new SmartQueryService(metricRegistry, rawFlowRepository, aggFlowRepository);
        smartQueryService.setAlwaysUseRawForQueries(true); // Always use RAW values for these tests
        flowRepository = new ElasticFlowRepository(metricRegistry, client, IndexStrategy.MONTHLY,
                new MockIdentity(), new MockTracerRegistry(), settings, 0, 0);

        final var classificationEngine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new RuleBuilder().withName("http").withDstPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withDstPort("443").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("http").withSrcPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withSrcPort("443").withProtocol("tcp,udp").build()),
                FilterService.NOOP);

        documentEnricher = new DocumentEnricherImpl(metricRegistry,
                new MockNodeDao(),
                new MockIpInterfaceDao(),
                new MockInterfaceToNodeCache(),
                new MockSessionUtils(),
                classificationEngine,
                new CacheConfigBuilder()
                        .withName("flows.node")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .build(), 0,
                new DocumentMangler(new ScriptEngineManager()));
        String pathToTemplates = Path.of(relativePathToEtc, "netflow-templates" , "default").toString();
        final ComposableTemplateInitializer initializer = new ComposableTemplateInitializer(elasticRestClient,
                pathToTemplates, true);

        // Here we load the flows by building the documents ourselves,
        // so we must initialize the repository manually
        initializer.initialize();

        // The repository should be empty
        assertThat(smartQueryService.getFlowCount(Collections.singletonList(new TimeRangeFilter(0, 0))).get(), equalTo(0L));
    }
}
