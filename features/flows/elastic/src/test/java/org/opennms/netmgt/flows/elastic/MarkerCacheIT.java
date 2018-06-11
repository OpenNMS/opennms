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

package org.opennms.netmgt.flows.elastic;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class MarkerCacheIT {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private TransactionOperations transactionOperations;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();
        this.interfaceToNodeCache.dataSourceSync();
    }

    private Flow getMockFlow() {
        final Flow flow = mock(Flow.class);
        when(flow.getNetflowVersion()).thenReturn(Flow.NetflowVersion.V5);
        when(flow.getDirection()).thenReturn(Flow.Direction.INGRESS);
        when(flow.getIpProtocolVersion()).thenReturn(4);
        when(flow.getSrcAddr()).thenReturn("192.168.1.2");
        when(flow.getInputSnmp()).thenReturn(2);
        when(flow.getDstAddr()).thenReturn("192.168.2.2");
        when(flow.getOutputSnmp()).thenReturn(3);
        when(flow.getVlan()).thenReturn(null);
        return flow;
    }

    private FlowSource getMockFlowSource() {
        final FlowSource flowSource = mock(FlowSource.class);
        when(flowSource.getLocation()).thenReturn(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
        when(flowSource.getSourceAddress()).thenReturn("192.168.1.1");
        return flowSource;
    }

    @Test
    public void testMarkerCache() throws Exception {
        stubFor(post("/_bulk")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new RuleBuilder().withName("http").withDstPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withDstPort("443").withProtocol("tcp,udp").build()
        ), FilterService.NOOP);

        final DocumentEnricher documentEnricher = new DocumentEnricher(
                new MetricRegistry(), nodeDao, interfaceToNodeCache, transactionOperations, classificationEngine,
                new CacheConfigBuilder()
                        .withName("flows.node")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .build());


        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:" + wireMockRule.port()).build());

        try (JestClient client = factory.getObject()) {
            final ElasticFlowRepository elasticFlowRepository = new ElasticFlowRepository(new MetricRegistry(),
                    client, IndexStrategy.MONTHLY, documentEnricher, classificationEngine,
                    transactionOperations, nodeDao, snmpInterfaceDao,
                    3, 12000);

            Assert.assertThat(nodeDao.findAllHavingFlows(), is(empty()));
            Assert.assertThat(snmpInterfaceDao.findAllHavingFlows(1), is(empty()));

            elasticFlowRepository.persist(Lists.newArrayList(getMockFlow()), getMockFlowSource());

            Assert.assertThat(nodeDao.findAllHavingFlows(), contains(hasProperty("id", is(1))));
            Assert.assertThat(snmpInterfaceDao.findAllHavingFlows(1), containsInAnyOrder(
                    hasProperty("ifIndex", is(2)),
                    hasProperty("ifIndex", is(3))));
        }
    }
}