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

package org.opennms.features.topology.app.internal.jung;

import org.junit.Before;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.SimpleGraphBuilder;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SimpleMetaTopologyProvider;
import org.opennms.features.topology.app.internal.VEProviderGraphContainer;
import org.opennms.features.topology.app.internal.service.DefaultTopologyService;
import org.opennms.features.topology.app.internal.service.SimpleServiceLocator;
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

        final VEProviderGraphContainer graphContainer = new VEProviderGraphContainer();
        graphContainer.setTopologyService(topologyService);
        graphContainer.setSelectedNamespace(metaTopologyProvider.getDefaultGraphProvider().getNamespace());
        graphContainer.setMetaTopologyId(metaTopologyProvider.getId());

        m_graphContainer = graphContainer;
    }

    protected abstract GraphProvider getGraphProvider();
}
