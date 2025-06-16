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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class GraphMLMetaTopologyProviderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void load() throws IOException, InvalidGraphException {
        final File graphXml = tempFolder.newFile();
        Resources.asByteSource(Resources.getResource("test-graph.xml")).copyTo(Files.asByteSink(graphXml));

        // Initialize the meta topology provider
        final GraphMLMetaTopologyProvider metaTopoProvider = new GraphMLMetaTopologyProvider(new GraphMLServiceAccessor());
        metaTopoProvider.setTopologyLocation(graphXml.getAbsolutePath());
        metaTopoProvider.reload();

        // Verify Breadcrumb-Strategy
        Assert.assertEquals(BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT, metaTopoProvider.getBreadcrumbStrategy());

        // We should have two graph providers (one for each graph)
        Collection<GraphProvider> graphProviders = metaTopoProvider.getGraphProviders();
        Iterator<GraphProvider> it = graphProviders.iterator();
        assertEquals(2, graphProviders.size());

        // The first graph should be 'regions'
        GraphProvider regionsGraphProvider = it.next();
        assertEquals("acme:regions", regionsGraphProvider.getNamespace());
        assertEquals("regions", regionsGraphProvider.getTopologyProviderInfo().getName());
        assertNull(regionsGraphProvider.getDefaults().getPreferredLayout());
        assertEquals(GraphMLTopologyProvider.DEFAULT_DESCRIPTION, regionsGraphProvider.getTopologyProviderInfo().getDescription());
        assertEquals(Defaults.DEFAULT_SEMANTIC_ZOOM_LEVEL, regionsGraphProvider.getDefaults().getSemanticZoomLevel());
        assertEquals(4, regionsGraphProvider.getCurrentGraph().getVertexTotalCount());
        for (String region : Lists.newArrayList("north", "south", "east", "west")) {
            // Every vertex should link to 4 other vertices
            Vertex vertex = regionsGraphProvider.getCurrentGraph().getVertex("acme:regions", region);
            assertEquals(4, metaTopoProvider.getOppositeVertices(vertex).size());
        }

        // The second graph should be 'markets'
        GraphProvider marketsGraphProvider = it.next();
        assertEquals("acme:markets", marketsGraphProvider.getNamespace());
        assertEquals("Markets", marketsGraphProvider.getTopologyProviderInfo().getName());
        assertEquals("The Markets Layer", marketsGraphProvider.getTopologyProviderInfo().getDescription());
        assertEquals("Some Layout", marketsGraphProvider.getDefaults().getPreferredLayout());
        assertEquals(0, marketsGraphProvider.getDefaults().getSemanticZoomLevel());
        assertEquals(16, marketsGraphProvider.getCurrentGraph().getVertexTotalCount());
    }

    @Test
    public void verifyGetBreadcrumbStrategy() {
        Assert.assertEquals(BreadcrumbStrategy.NONE, GraphMLMetaTopologyProvider.getBreadcrumbStrategy("none"));
        Assert.assertEquals(BreadcrumbStrategy.NONE, GraphMLMetaTopologyProvider.getBreadcrumbStrategy("NONE"));
        Assert.assertEquals(BreadcrumbStrategy.NONE, GraphMLMetaTopologyProvider.getBreadcrumbStrategy("nOne"));

        Assert.assertEquals(null, GraphMLMetaTopologyProvider.getBreadcrumbStrategy(""));
        Assert.assertEquals(null, GraphMLMetaTopologyProvider.getBreadcrumbStrategy(null));

        Assert.assertEquals(BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT, GraphMLMetaTopologyProvider.getBreadcrumbStrategy("SHORTEST_PATH_TO_ROOT"));
        Assert.assertEquals(BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT, GraphMLMetaTopologyProvider.getBreadcrumbStrategy("shortest_path_to_root"));
        Assert.assertEquals(BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT, GraphMLMetaTopologyProvider.getBreadcrumbStrategy("Shortest_Path_To_Root"));
    }
}
