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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

public class GraphMLMetaTopologyProviderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void load() throws IOException {
        final File graphXml = tempFolder.newFile();
        Resources.asByteSource(Resources.getResource("test-graph.xml")).copyTo(Files.asByteSink(graphXml));

        // Initialize the meta topology provider
        final GraphMLMetaTopologyProvider metaTopoProvider = new GraphMLMetaTopologyProvider();
        metaTopoProvider.setTopologyLocation(graphXml.getAbsolutePath());
        metaTopoProvider.load();

        // We should have two graph providers (one for each graph)
        Collection<GraphProvider> graphProviders = metaTopoProvider.getGraphProviders();
        Iterator<GraphProvider> it = graphProviders.iterator();
        assertEquals(2, graphProviders.size());

        // The first graph should be 'regions'
        GraphProvider regionsGraphProvider = it.next();
        assertEquals("acme:regions", regionsGraphProvider.getVertexNamespace());
        assertEquals("regions", regionsGraphProvider.getTopologyProviderInfo().getName());
        assertNull(metaTopoProvider.getPreferredLayout(regionsGraphProvider));
        assertEquals(GraphMLTopologyProvider.DEFAULT_DESCRIPTION, regionsGraphProvider.getTopologyProviderInfo().getDescription());
        assertEquals(4, regionsGraphProvider.getVertexTotalCount());
        for (String region : Lists.newArrayList("north", "south", "east", "west")) {
            // Every vertex should link to 4 other vertices
            Vertex vertex = regionsGraphProvider.getVertex("acme:regions", region);
            assertEquals(4, metaTopoProvider.getOppositeVertices(vertex).size());
        }

        // The second graph should be 'markets'
        GraphProvider marketsGraphProvider = it.next();
        assertEquals("acme:markets", marketsGraphProvider.getVertexNamespace());
        assertEquals("Markets", marketsGraphProvider.getTopologyProviderInfo().getName());
        assertEquals("The Markets Layer", marketsGraphProvider.getTopologyProviderInfo().getDescription());
        assertEquals("Some Layout", metaTopoProvider.getPreferredLayout(marketsGraphProvider));
        assertEquals(16, marketsGraphProvider.getVertexTotalCount());
    }
}
