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
package org.opennms.netmgt.graph.search;

import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainGraph;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainGraphContainer;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainVertex;
import org.opennms.netmgt.graph.service.DefaultGraphService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NodeSearchProviderIT {

    private static final String NAMESPACE = "dummy";

    @Autowired
    private NodeDao nodeDao;

    private NodeSearchProvider searchProvider;

    @Before
    public void setUp() {
        searchProvider = new NodeSearchProvider(nodeDao);
        nodeDao.save(createNode("n1", "OpenNMS"));
        nodeDao.save(createNode("n2", "OpenNMS Test"));
        nodeDao.save(createNode("n3", "Minion Test"));
        nodeDao.save(createNode("n4", "Sentinel Test"));
        nodeDao.save(createNode("n5", "Some DNS Node"));
        nodeDao.save(createNode("n6", "Some HTTP Node"));
    }

    @After
    public void tearDown() {
        nodeDao.findAll().forEach(n -> nodeDao.delete(n));
    }

    @Test
    public void verifySuggestions() {
        final SearchContext context = SearchContext.builder().suggestionsLimit(10).graphService(Mockito.mock(GraphService.class)).build();
        final List<SearchSuggestion> suggestions = searchProvider.getSuggestions(context, NAMESPACE, "test");
        final List<String> suggestionLabels = suggestions.stream().map(s -> s.getLabel()).collect(Collectors.toList());
        assertThat(suggestions, Matchers.hasSize(3));
        assertThat(suggestionLabels, Matchers.hasItems("OpenNMS Test", "Minion Test", "Sentinel Test"));
    }

    @Test
    public void verifyResolve() {
        final SimpleDomainGraphContainer container = SimpleDomainGraphContainer.builder()
                .id(NAMESPACE)
                .addGraph(SimpleDomainGraph.builder()
                        .namespace(NAMESPACE)
                        .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("v1").nodeRef("test:n1").build())
                        .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("v2").build())
                        .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("v3").build())
                        .build())
                .build();
        final DefaultGraphService graphService = new DefaultGraphService();
        graphService.onBind(new GraphContainerProvider() {
            @Override public ImmutableGraphContainer loadGraphContainer() { return container; }
            @Override public GraphContainerInfo getContainerInfo() { return container; }
        }, new HashMap<>());
        final SearchCriteria searchCriteria = new SearchCriteria(searchProvider.getProviderId(), NAMESPACE, nodeDao.findByForeignId("n1").get(0).getId().toString());
        final List<GenericVertex> vertices = searchProvider.resolve(graphService, searchCriteria);
        assertThat(vertices, Matchers.hasSize(1));
        assertThat(vertices, Matchers.hasItem(container.getGraph(NAMESPACE).getVertex("v1").asGenericVertex()));
    }

    private static final OnmsNode createNode(final String foreignId, final String label) {
        final OnmsNode node = new OnmsNode();
        node.setForeignSource("test");
        node.setForeignId(foreignId);
        node.setLabel(label);
        node.setLocation(new OnmsMonitoringLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID));
        return node;
    }

}