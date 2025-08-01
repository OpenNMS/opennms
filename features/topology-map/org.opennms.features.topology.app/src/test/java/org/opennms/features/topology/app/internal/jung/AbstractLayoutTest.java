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
package org.opennms.features.topology.app.internal.jung;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.simple.SimpleGraphBuilder;
import org.opennms.features.topology.api.topo.simple.SimpleGraphProvider;
import org.opennms.features.topology.api.topo.simple.SimpleMetaTopologyProvider;
import org.opennms.features.topology.app.internal.VEProviderGraphContainer;
import org.opennms.features.topology.app.internal.service.DefaultTopologyService;
import org.opennms.features.topology.app.internal.service.SimpleServiceLocator;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLayoutTest {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected GraphContainer m_graphContainer;

    @Before
    public void setUp(){
        SimpleGraphBuilder bldr = new SimpleGraphBuilder("nodes");

        for(int i = 0; i < 100; i++) {
            bldr.vertex("v"+i).vLabel("vertex"+i).vIconKey("server").vTooltip("tooltip").vStyleName("vertex");
        }

        bldr.edge("e1", "v1", "v2").eStyleName("edge")
                .edge("e2", "v1", "v3").eStyleName("edge")
                .edge("e3", "v1", "v4").eStyleName("edge")
                .edge("e4", "v1", "v5").eStyleName("edge")
                .edge("e5", "v1", "v6").eStyleName("edge")
                .edge("e6", "v1", "v7").eStyleName("edge")
                .edge("e7", "v1", "v8").eStyleName("edge")
                .edge("e8", "v1", "v8").eStyleName("edge")
                .edge("e9", "v2", "v8").eStyleName("edge")
                .edge("e10", "v2", "v7").eStyleName("edge")
                .edge("e11", "v3", "v8").eStyleName("edge")
                .edge("e12", "v5", "v8").eStyleName("edge")
                .edge("e13", "v6", "v8").eStyleName("edge")
                .edge("e14", "v7", "v8").eStyleName("edge");



        final DefaultTopologyService topologyService = new DefaultTopologyService();
        final MetaTopologyProvider metaTopologyProvider = new SimpleMetaTopologyProvider(getGraphProvider());
        topologyService.setServiceLocator(new SimpleServiceLocator(metaTopologyProvider));
        topologyService.setTopologyEntityCache(mock(TopologyEntityCache.class));

        final VEProviderGraphContainer graphContainer = new VEProviderGraphContainer();
        graphContainer.setTopologyService(topologyService);
        graphContainer.setSelectedNamespace(metaTopologyProvider.getDefaultGraphProvider().getNamespace());
        graphContainer.setMetaTopologyId(metaTopologyProvider.getId());

        final VertexRef defaultFocus = getDefaultFocus();
        if (defaultFocus != null) {
            graphContainer.addCriteria(new DefaultVertexHopCriteria(defaultFocus));
        }

        m_graphContainer = graphContainer;
    }

    protected GraphProvider getGraphProvider() {
        final BackendGraph graph = getGraph();
        final SimpleGraphProvider graphProvider = new SimpleGraphProvider(graph);
        return graphProvider;
    }

    protected abstract BackendGraph getGraph();

    protected abstract VertexRef getDefaultFocus();
}
