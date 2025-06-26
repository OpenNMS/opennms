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
import org.junit.Before;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.features.elastic.client.ElasticRestClient;
import org.opennms.features.elastic.client.ElasticRestClientFactory;
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
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCache;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCacheImpl;

import javax.script.ScriptEngineManager;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opennms.netmgt.flows.elastic.ComposableFlowQueryIT.relativePathToEtc;

public class ComposableAggFlowQueryIT extends  AggregatedFlowQueryIT {

    @Before
    public void setUp() throws MalformedURLException, ExecutionException, InterruptedException {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final ElasticRestClientFactory elasticRestClientFactory = new ElasticRestClientFactory(elasticSearchRule.getUrl(), null, null);
        final ElasticRestClient elasticRestClient = elasticRestClientFactory.createClient();
        final IndexSettings rawIndexSettings = new IndexSettings();
        final IndexSettings aggIndexSettings = new IndexSettings();


        final IndexSelector rawIndexSelector = new IndexSelector(rawIndexSettings, RawFlowQueryService.INDEX_NAME,
                IndexStrategy.MONTHLY, 120000);
        rawFlowQueryService = new RawFlowQueryService(elasticRestClient, rawIndexSelector);

        // Use composable templates
        String pathToTemplates = Path.of(relativePathToEtc, "netflow-templates").toString();
        final ComposableTemplateInitializer initializer = new ComposableTemplateInitializer(elasticRestClient,
                pathToTemplates, true);
        initializer.initialize();

        final IndexSelector aggIndexSelector = new IndexSelector(aggIndexSettings, AggregatedFlowQueryService.INDEX_NAME,
                IndexStrategy.MONTHLY, 120000);
        aggFlowQueryService = new AggregatedFlowQueryService(elasticRestClient, aggIndexSelector);

        smartQueryService = new SmartQueryService(metricRegistry, rawFlowQueryService, aggFlowQueryService);
        // Prefer aggregated queries, but fallback to raw when unsupported by agg.
        smartQueryService.setAlwaysUseAggForQueries(false);
        smartQueryService.setAlwaysUseRawForQueries(false);
        smartQueryService.setTimeRangeDurationAggregateThresholdMs(1);

        flowRepository = new ElasticFlowRepository(metricRegistry, elasticRestClient, IndexStrategy.MONTHLY,
                new MockIdentity(), new MockTracerRegistry(), rawIndexSettings, 0, 0);

        final var classificationEngine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new RuleBuilder().withName("http").withDstPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withDstPort("443").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("http").withSrcPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withSrcPort("443").withProtocol("tcp,udp").build()),
                FilterService.NOOP);

        final NodeInfoCache nodeInfoCache = new NodeInfoCacheImpl(
                new CacheConfigBuilder()
                        .withName("nodeInfoCache")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .withExpireAfterRead(300)
                        .build(),
                true,
                new MetricRegistry(),
                new MockNodeDao(),
                new MockIpInterfaceDao(),
                new MockInterfaceToNodeCache(),
                new MockSessionUtils()
        );

        documentEnricher = new DocumentEnricherImpl(new MockSessionUtils(),
                classificationEngine,
                0,
                new DocumentMangler(new ScriptEngineManager()),
                nodeInfoCache);

        // The repository should be empty
        assertThat(smartQueryService.getFlowCount(Collections.singletonList(new TimeRangeFilter(0, System.currentTimeMillis()))).get(), equalTo(0L));
    }

}
