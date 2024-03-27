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
package org.opennms.features.topology.plugins.topo.graphml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.app.internal.VEProviderGraphContainer;
import org.opennms.features.topology.app.internal.gwt.client.SearchSuggestion;
import org.opennms.features.topology.app.internal.service.DefaultTopologyService;
import org.opennms.features.topology.app.internal.service.SimpleServiceLocator;
import org.opennms.features.topology.app.internal.ui.SearchBox;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.osgi.OnmsServiceManager;

public class GraphMLSearchProviderTest {

    @Test
    public void verifyContributesTo() {
        GraphMLGraph graph = new GraphMLGraph();
        graph.setProperty(GraphMLProperties.NAMESPACE, "namespace1:graph1");

        GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(graph, new GraphMLServiceAccessor());
        GraphMLSearchProvider searchProvider = new GraphMLSearchProvider(topologyProvider);
        Assert.assertEquals(true, searchProvider.contributesTo("namespace1:graph1"));
        Assert.assertEquals(true, searchProvider.contributesTo("namespace1:graph2"));
        Assert.assertEquals(true, searchProvider.contributesTo("namespace1:graph3"));
        Assert.assertEquals(false, searchProvider.contributesTo("namespace1"));
    }

    @Test
    public void canSearchAllSearchProviders() throws IOException, InvalidGraphException {
        final GraphMLMetaTopologyProvider metaTopologyProvider = new GraphMLMetaTopologyProvider(new GraphMLServiceAccessor());
        metaTopologyProvider.setTopologyLocation("target/test-classes/test-graph.xml");
        metaTopologyProvider.reload();
        Assert.assertNotNull(metaTopologyProvider.getDefaultGraphProvider());

        List<SearchProvider> searchProviders = metaTopologyProvider.getGraphProviders().stream()
                .map(eachProvider -> new GraphMLSearchProvider(metaTopologyProvider.getGraphProvider(eachProvider.getNamespace())))
                .collect(Collectors.toList());
        Assert.assertEquals(2, searchProviders.size());

        DefaultTopologyService defaultTopologyService = new DefaultTopologyService();
        defaultTopologyService.setServiceLocator(new SimpleServiceLocator(metaTopologyProvider));
        defaultTopologyService.setTopologyEntityCache(mock(TopologyEntityCache.class));
        VEProviderGraphContainer graphContainer = new VEProviderGraphContainer();
        graphContainer.setTopologyService(defaultTopologyService);
        graphContainer.setMetaTopologyId(metaTopologyProvider.getId());
        graphContainer.setSelectedNamespace(metaTopologyProvider.getDefaultGraphProvider().getNamespace());

        OperationContext operationContext = mock(OperationContext.class);
        when(operationContext.getGraphContainer()).thenReturn(graphContainer);

        OnmsServiceManager onmsServiceManager = mock(OnmsServiceManager.class);
        when(onmsServiceManager.getServices(SearchProvider.class, null, new Hashtable<>())).thenReturn(searchProviders);

        SearchBox searchBox = new SearchBox(onmsServiceManager, operationContext);
        List<SearchSuggestion> results = searchBox.getQueryResults("North");
        Assert.assertEquals(5, results.size());
    }
}
