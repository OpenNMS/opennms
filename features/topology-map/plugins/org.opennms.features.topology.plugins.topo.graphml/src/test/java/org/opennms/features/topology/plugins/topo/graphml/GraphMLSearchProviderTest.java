/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
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
import org.opennms.osgi.OnmsServiceManager;

public class GraphMLSearchProviderTest {

    @Test
    public void verifyContributesTo() {
        GraphMLGraph graph = new GraphMLGraph();
        graph.setProperty(GraphMLProperties.NAMESPACE, "namespace1:graph1");

        GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(null, graph, new GraphMLServiceAccessor());
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
                .map(eachProvider -> new GraphMLSearchProvider(metaTopologyProvider.getRawTopologyProvider(eachProvider.getNamespace())))
                .collect(Collectors.toList());
        Assert.assertEquals(2, searchProviders.size());

        DefaultTopologyService defaultTopologyService = new DefaultTopologyService();
        defaultTopologyService.setServiceLocator(new SimpleServiceLocator(metaTopologyProvider));
        VEProviderGraphContainer graphContainer = new VEProviderGraphContainer();
        graphContainer.setTopologyService(defaultTopologyService);
        graphContainer.setMetaTopologyId(metaTopologyProvider.getId());
        graphContainer.setSelectedNamespace(metaTopologyProvider.getDefaultGraphProvider().getNamespace());

        OperationContext operationContext = EasyMock.niceMock(OperationContext.class);
        EasyMock.expect(operationContext.getGraphContainer()).andReturn(graphContainer).anyTimes();

        OnmsServiceManager onmsServiceManager = EasyMock.niceMock(OnmsServiceManager.class);
        EasyMock.expect(onmsServiceManager.getServices(SearchProvider.class, null, new Hashtable<>())).andReturn(searchProviders).anyTimes();
        EasyMock.replay(onmsServiceManager, operationContext);

        SearchBox searchBox = new SearchBox(onmsServiceManager, operationContext);
        List<SearchSuggestion> results = searchBox.getQueryResults("North");
        Assert.assertEquals(5, results.size());
    }
}
