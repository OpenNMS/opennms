package org.opennms.netmgt.flows.elastic;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.searchbox.client.JestResult;
import io.searchbox.indices.CreateIndex;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.painless.PainlessPlugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.elasticsearch.plugin.DriftPlugin;
import org.opennms.features.elastic.client.DefaultElasticRestClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class ComposableTemplatesFlowQueryIT extends FlowQueryIT
{

    private static final Logger LOG = LoggerFactory.getLogger(ComposableTemplatesFlowQueryIT.class);

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withPlugins(PainlessPlugin.class, DriftPlugin.class));

    // Elasticsearch version used for testing
    private static final String ES_VERSION = "7.17.9";
    private static final String DRIFT_PLUGIN_VERSION = "2.0.5";
    
   /* @ClassRule
    public static ElasticsearchMavenPluginContainer elasticsearchContainer;
    
    static {
        try {
            elasticsearchContainer = new ElasticsearchMavenPluginContainer("docker.elastic.co/elasticsearch/elasticsearch:" + ES_VERSION)
                    // We only need to add the drift plugin - the Painless plugin is built into the Elasticsearch image
                    .withPlugin("org.opennms.elasticsearch", "elasticsearch-drift-plugin-" + ES_VERSION, DRIFT_PLUGIN_VERSION);
                    
            LOG.info("Initialized ElasticsearchMavenPluginContainer using downloaded plugin from Maven");
            LOG.info("If container fails to start, run: mvn generate-test-resources");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize ElasticsearchMavenPluginContainer", e);
        }
    }

    @BeforeClass
    public static void setUpClass() {
        // Ensure container is started before any test method is run
        LOG.info("Elasticsearch container started at URL: {}", elasticsearchContainer.getHttpHostAddress());
        
        // Verify plugins were correctly installed
        boolean pluginsInstalled = elasticsearchContainer.verifyPluginsInstalled();
        LOG.info("Elasticsearch plugins successfully installed: {}", pluginsInstalled);
        
        if (!pluginsInstalled) {
            throw new RuntimeException("Failed to install required Elasticsearch plugins. Test cannot continue.");
        }
    }*/
    
    @Before
    public void setUp() throws MalformedURLException, ExecutionException, InterruptedException {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final ElasticRestClientFactory elasticRestClientFactory = new ElasticRestClientFactory(elasticSearchRule.getUrl(), null, null);
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        final JestClientWithCircuitBreaker client = restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(FlowQueryIT.class.getName()), eventForwarder);
        final IndexSettings settings = new IndexSettings();
        // Use empty string prefix - our template pattern is netflow-*
        // This avoids double prefix like "netflownetflow-*"
        settings.setIndexPrefix("");
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
                
/*        // Delete any existing indices before initializing to ensure a clean state
        try {
            LOG.info("Deleting any existing flow indices before test");
            io.searchbox.indices.DeleteIndex deleteIndex = new io.searchbox.indices.DeleteIndex.Builder("netflow*").build();
            client.execute(deleteIndex);
            LOG.info("Successfully deleted existing indices");
        } catch (Exception e) {
            LOG.info("No existing indices to delete or error during deletion: {}", e.getMessage());
        }*/

        String pathToTemplates = Objects.requireNonNull(this.getClass().getResource("/flows/templates")).getPath();
        final DefaultElasticRestClient elasticRestClient = (DefaultElasticRestClient)elasticRestClientFactory.createClient();
        final ComposableTemplateInitializer initializer = new ComposableTemplateInitializer
                (elasticRestClient, pathToTemplates, true);

        // Here we load the flows by building the documents ourselves,
        // so we must initialize the repository manually
        initializer.initialize();

        try {

            LOG.info("Checking flow count with filter TimeRangeFilter(0, 0)");
            Long flowCount = smartQueryService.getFlowCount(Collections.singletonList(new TimeRangeFilter(0, 0))).get();
            LOG.info("Flow count: {}", flowCount);
            assertThat(flowCount, equalTo(0L));
        } catch (Exception e) {
            LOG.error("Error checking flow count: {}", e.getMessage(), e);
            throw e;
        }
    }

}
