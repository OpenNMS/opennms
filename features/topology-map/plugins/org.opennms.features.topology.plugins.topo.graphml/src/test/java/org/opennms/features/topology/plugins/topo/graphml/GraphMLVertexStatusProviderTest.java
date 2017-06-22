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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class GraphMLVertexStatusProviderTest {

    @Test
    public void testStatusProvider() throws InvalidGraphException {
        GraphML graphML = GraphMLReader.read(getClass().getResourceAsStream("/test-graph.xml"));
        GraphMLTopologyProvider topologyProvider = new GraphMLTopologyProvider(null, graphML.getGraphs().get(0), new GraphMLServiceAccessor());
        GraphMLDefaultVertexStatusProvider statusProvider = new GraphMLDefaultVertexStatusProvider(topologyProvider, nodeIds -> Lists.newArrayList(
                createSummary(1, "North", OnmsSeverity.WARNING, 1),
                createSummary(2, "West", OnmsSeverity.MINOR, 2),
                createSummary(3, "South", OnmsSeverity.MAJOR, 3)
        ));

        List<VertexRef> vertices = topologyProvider.getVertices().stream().map(eachVertex -> (VertexRef) eachVertex).collect(Collectors.toList());
        Assert.assertEquals(4, vertices.size());
        Assert.assertEquals(topologyProvider.getVertexNamespace(), statusProvider.getNamespace());
        Assert.assertEquals(Boolean.TRUE, statusProvider.contributesTo(topologyProvider.getVertexNamespace()));

        Map<VertexRef, Status> statusForVertices = statusProvider.getStatusForVertices(topologyProvider, vertices, new Criteria[0]);
        Assert.assertEquals(4, statusForVertices.size());
        Assert.assertEquals(ImmutableMap.of(
                createVertexRef(topologyProvider.getVertexNamespace(), "north"), createStatus(OnmsSeverity.WARNING, 1),
                createVertexRef(topologyProvider.getVertexNamespace(), "west"), createStatus(OnmsSeverity.MINOR, 2),
                createVertexRef(topologyProvider.getVertexNamespace(), "south"), createStatus(OnmsSeverity.MAJOR, 3),
                createVertexRef(topologyProvider.getVertexNamespace(), "east"), createStatus(OnmsSeverity.NORMAL, 0)), statusForVertices);
    }

    private static Status createStatus(OnmsSeverity severity, long count) {
        return new GraphMLVertexStatus(severity, count);
    }

    private static DefaultVertexRef createVertexRef(String namespace, String id) {
        return new DefaultVertexRef(namespace, id);
    }

    private static AlarmSummary createSummary(int nodeId, String label, OnmsSeverity maxSeverity, long count) {
        return new AlarmSummary(nodeId, label, new Date(), maxSeverity, count);
    }
}
