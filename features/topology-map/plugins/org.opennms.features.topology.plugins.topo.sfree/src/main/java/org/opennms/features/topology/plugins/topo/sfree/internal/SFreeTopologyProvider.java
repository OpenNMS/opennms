/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.sfree.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFreeTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public enum Type {
        ErdosRenis, BarabasiAlbert;
    }

    private static final Logger LOG = LoggerFactory.getLogger(SFreeTopologyProvider.class);

    private static final String TOPOLOGY_NAMESPACE_SFREE = "sfree";

    private int m_nodeCount = 200;

    private double m_connectedness = 4.0;

    private Type type = Type.BarabasiAlbert;

    public SFreeTopologyProvider() {
        super(TOPOLOGY_NAMESPACE_SFREE);
    }

    public int getNodeCount() {
		return m_nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		m_nodeCount = nodeCount;
	}

	public double getConnectedness() {
		return m_connectedness;
	}

	public void setConnectedness(double connectedness) {
		m_connectedness = connectedness;
	}

	public void setType(Type type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public void refresh() {
        clearVertices();
        clearEdges();

        switch (type) {
            case ErdosRenis:
                createERRandomTopology(m_nodeCount, m_connectedness);
                break;
            case BarabasiAlbert:
                createBARandomTopology(m_nodeCount, m_connectedness);
                break;
            default:
                throw new IllegalStateException("Type not supported");
        }
    }

    @Override
    public Defaults getDefaults() {
        return new Defaults();
    }

    private void createBARandomTopology(int numberOfNodes, double averageNumberofNeighboors) {
        Map<Integer,SimpleLeafVertex> nodes = new HashMap<Integer, SimpleLeafVertex>();
        List<AbstractEdge> edges = new ArrayList<>();

        for(int i=0; i<2*averageNumberofNeighboors; i++){
            LOG.debug("Creating First Cluster from: {}", i);
            int j=(i+1)%((int)Math.round(2*averageNumberofNeighboors));

            SimpleLeafVertex vertexi = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i), 0, 0);
            vertexi.setIconKey("sfree.system");
            vertexi.setLabel("BarabasiAlbertNode"+i);
            if (!nodes.containsKey(i)) {
                nodes.put(i, vertexi);
                LOG.debug("Added Node: {}", vertexi.getId());
            }

            SimpleLeafVertex vertexj = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(j), 0, 0);
            vertexj.setIconKey("sfree.system");
            vertexj.setLabel("BarabasiAlbertNode"+j);
            if (!nodes.containsKey(j)) {
                nodes.put(j, vertexj);
                LOG.debug("Added Node: {}", vertexj.getId());
            }

            String edgeId = "link:"+i+"-"+j;
            SimpleConnector source = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(i).getId()+"-"+edgeId+"-connector", nodes.get(i));
            SimpleConnector target = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(j).getId()+"-"+edgeId+"-connector", nodes.get(j));
            edges.add(new AbstractEdge(TOPOLOGY_NAMESPACE_SFREE, edgeId, source, target));
            LOG.debug("Added Link: {}", edgeId);
        }

        Random r = new Random((new Date()).getTime());
        for(int i=((int)Math.floor(2*averageNumberofNeighboors));i<numberOfNodes;i++){

            SimpleLeafVertex vertexi = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i),0,0);
            vertexi.setIconKey("sfree.system");
            vertexi.setLabel("BarabasiAlbertNode"+i);
            nodes.put(i, vertexi);
            LOG.debug("Adding Node: {}", i);

            for(int times=0; times<averageNumberofNeighboors; times++){
                AbstractEdge edge;
                double d = r.nextDouble()*nodes.size(); // choose node to attach to
                LOG.debug("Generated random position: {}", d);
                Long j = (long)d;
                LOG.debug("Try Adding edge: {}--->{}", j, i);
                String edgeId = "link:"+i+"-"+j.intValue();
                SimpleConnector source = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(i).getId()+"-"+edgeId+"-connector", nodes.get(i));
                SimpleConnector target = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(j.intValue()).getId()+"-"+edgeId+"-connector", nodes.get(j.intValue()));
                edge = new AbstractEdge(TOPOLOGY_NAMESPACE_SFREE, edgeId, source, target);
                if( i == j.intValue() ) continue;
                edges.add(edge);
            }// m links added
        }

        addVertices(nodes.values().toArray(new Vertex[] {}));
        addEdges(edges.toArray(new Edge[] {}));

    }

    private void createERRandomTopology(int numberOfNodes, double averageNumberofNeighboors) {
        Map<Integer,SimpleLeafVertex> nodes = new HashMap<Integer, SimpleLeafVertex>();
        List<AbstractEdge> edges = new ArrayList<>();
        for (Integer i=0; i< numberOfNodes ;i++) {
            SimpleLeafVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i), 0, 0);
            vertex.setIconKey("sfree.system");
            vertex.setLabel("ErdosReniyNode"+i);

            nodes.put(i,vertex);
        }

        Double z = 0.5*(numberOfNodes-1);
        //		Double p = averageNumberofNeighboors/z;

        Random r = new Random((new Date()).getTime());

        for (Integer start=0; start < numberOfNodes; start++) {
            for (Integer end = start+1; end<numberOfNodes;end++) {
                if (z*r.nextDouble()<averageNumberofNeighboors)  {
                    String edgeId = "link:"+start+"-"+end;
                    SimpleConnector source = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(start).getId()+"-"+edgeId+"-connector", nodes.get(start));
                    SimpleConnector target = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(end).getId()+"-"+edgeId+"-connector", nodes.get(end));
                    edges.add(new AbstractEdge(TOPOLOGY_NAMESPACE_SFREE, edgeId, source, target));
                }
            }
        }

        addVertices(nodes.values().toArray(new Vertex[] {}));
        addEdges(edges.toArray(new Edge[] {}));
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return SelectionChangedListener.Selection.NONE;
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return false;
    }
}
