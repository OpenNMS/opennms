/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.support.SimpleGraphBuilder;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.jung.GridLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.LayoutManager;
import org.opennms.netmgt.topology.persistence.api.LayoutEntity;
import org.opennms.netmgt.topology.persistence.api.PointEntity;
import org.opennms.netmgt.topology.persistence.api.VertexPositionEntity;

import com.google.common.collect.ImmutableMap;

public class ManualLayoutAlgorithmTest {

    private class ManualTest {
        private final Graph graph;
        private final LayoutManager layoutManager;
        private final DefaultLayout layout;

        private ManualTest(GraphProvider graphProvider) {
            Objects.requireNonNull(graphProvider);

            graph = Mockito.mock(Graph.class);
            layoutManager = Mockito.mock(LayoutManager.class);
            layout = new DefaultLayout();
            Mockito.when(graph.getLayout()).thenReturn(layout);
            Mockito.when(graph.getDisplayVertices()).thenReturn(graphProvider.getVertices());
        }
    }

    /*
     * This test verifies, that the vertex coordinates of x and y are applied to the
     * layout, if they exist.
     */
    @Test
    public void verifyAppliesXAndYCoordinates() {
        final ManualTest test = new ManualTest(new SimpleGraphBuilder("dummy")
                .vertex("1").vX(0).vY(0)
                .vertex("2").vX(1).vY(1)
                .get());
        final LayoutAlgorithm layoutAlgorithm = new ManualLayoutAlgorithm(test.layoutManager);
        layoutAlgorithm.updateLayout(test.graph);
        Assert.assertEquals(ImmutableMap.builder()
            .put(new DefaultVertexRef("dummy", "1"), new Point(0, 0))
            .put(new DefaultVertexRef("dummy", "2"), new Point(1, 1))
            .build(), test.layout.getLocations());
    }

    /*
     * If no coordinates are defined for the vertex and there are no layout coordinates yet, verify that it falls
     * back to the GridLayout.
     */
    @Test
    public void verifyDefaultsToGridLayout() {
        final ManualTest test = new ManualTest(new SimpleGraphBuilder("dummy").vertex("1").vertex("2").get());

        new ManualLayoutAlgorithm(test.layoutManager).updateLayout(test.graph);
        Map<VertexRef, Point> manualLocations = test.layout.getLocations();
        new GridLayoutAlgorithm().updateLayout(test.graph);

        Assert.assertEquals(test.layout.getLocations(), manualLocations);
    }

    /*
     * If persisted layout is defined, verify that it has priority.
     */
    @Test
    public void verifyLayoutCoordinatesHavePriority() {
        final GraphProvider graphProvider = new SimpleGraphBuilder("dummy").vertex("vertex1").vX(1).vY(1).get();
        final ManualTest test = new ManualTest(graphProvider);

        final LayoutEntity persistedLayout = new LayoutEntity();
        int x=5;
        int y=5;
        for (VertexRef eachVertex : graphProvider.getVertices()) {
            VertexPositionEntity vertexPositionEntity = new VertexPositionEntity();
            vertexPositionEntity.setVertexRef(LayoutManager.toVertexRefEntity(eachVertex));
            vertexPositionEntity.setPosition(new PointEntity(x++, y++));
            persistedLayout.addVertexPosition(vertexPositionEntity);
        }
        Mockito.when(test.layoutManager.loadLayout(test.graph)).thenReturn(persistedLayout);

        new ManualLayoutAlgorithm(test.layoutManager).updateLayout(test.graph);
        Assert.assertEquals(ImmutableMap.builder()
                .put(new DefaultVertexRef("dummy", "vertex1"), new Point(5, 5))
                .build(), test.layout.getLocations());

    }
}