/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;

public class TestTopologyProvider extends AbstractTopologyProvider implements GraphProvider {
    private int m_vertexCounter = 0;
    private int m_edgeCounter = 0;
    private int m_groupCounter = 0;
    
    public TestTopologyProvider(String namespace) {
    	super("test");
        
        String vId1 = getNextVertexId();
        TestVertex v1 = new TestLeafVertex(vId1, 0, 0);
        v1.setLabel("a leaf");
        
        addVertices(v1);
        
        String vId2 = getNextVertexId();
        TestVertex v2 = new TestLeafVertex(vId2, 0, 0);
        v2.setLabel("another leaf");
        addVertices(v2);
        
        String edgeId = getNextEdgeId();
        TestEdge edge = new TestEdge(edgeId, v1, v2);
        addEdges(edge);
        
    }
    
    @Override
    public Vertex addVertex(int x, int y) {
        String id = getNextVertexId();
        TestVertex vert = new TestLeafVertex(id, x, y);
        vert.setLabel("a vertex");
        addVertices(vert);
        return vert;
        
    }
    
    private String getNextEdgeId() {
        return "e" + m_edgeCounter++;
    }

    private String getNextVertexId() {
        return "v" + m_vertexCounter++;
    }

    @Override
    public Vertex addGroup(String groupLabel, String groupIcon) {
        String nextGroupId = getNextGroupId();
        return addGroup(nextGroupId, groupIcon, groupLabel);
    }

    private Vertex addGroup(String groupId, String groupIcon, String groupLabel) {
        if(containsVertexId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        TestVertex vertex = new TestGroup(groupId);
        vertex.setLabel(groupLabel);
        addVertices(vertex);
        return vertex;
    }

    private String getNextGroupId() {
        return "g" + m_groupCounter++;
    }

    @Override
    public void save(String filename) {
        // Do nothing
    }

    @Override
    public void load(String filename) {

        clearEdges();
        clearVertices();
        
        List<TestVertex> vertices = new ArrayList<TestVertex>();
        List<TestEdge> edges = new ArrayList<TestEdge>();
        
        String vId1 = getNextVertexId();
        TestVertex v1 = new TestLeafVertex(vId1, 0, 0);
        v1.setLabel("a leaf vertex");
        
        vertices.add(v1);
        //Item beanItem = m_vertexContainer.addBean(v1);
        
        String vId2 = getNextVertexId();
        TestVertex v2 = new TestLeafVertex(vId2, 0, 0);
        v2.setLabel("another leaf");
        vertices.add(v2);
        //Item beanItem2 = m_vertexContainer.addBean(v2);
        
        String edgeId = getNextEdgeId();
        TestEdge edge = new TestEdge(edgeId, v1, v2);
        edges.add(edge);
        //m_edgeContainer.addBean(edge);
                 
        addVertices(vertices.toArray(new Vertex[] {}));
        addEdges(edges.toArray(new Edge[] {}));
    }
}
