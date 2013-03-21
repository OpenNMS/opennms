package org.opennms.features.topology.plugins.topo.sfree.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.Vertex;

public class SFreeTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

	private static final String TOPOLOGY_NAMESPACE_SFREE = "sfree";
	public static final String ERDOS_RENIS = "ErdosReniy";
	public static final String BARABASI_ALBERT = "BarabasiAlbert";

	public SFreeTopologyProvider() {
		super(TOPOLOGY_NAMESPACE_SFREE);
	}

	@Override
	public void save() {
		// Do nothing
	}

    @Override
    public void refresh() {
        // Do nothing
    }

	@Override
	public void load(String filename) {

		clearVertices();
		clearEdges();

		if (filename.equals(ERDOS_RENIS))
			createERRandomTopology(200,4);		
		else if (filename.equals(BARABASI_ALBERT))
			createBARandomTopology(200,4);
	}

	private void createBARandomTopology(Integer numberOfNodes, Integer averageNumberofNeighboors) {
		Map<Integer,SimpleLeafVertex> nodes = new HashMap<Integer, SimpleLeafVertex>();
		List<AbstractEdge> edges = new ArrayList<AbstractEdge>();

		for(int i=0; i<2*averageNumberofNeighboors; i++){
                    System.err.println("Creating First Cluster from: " + i);
                    int j=(i+1)%(2*averageNumberofNeighboors);
                    
                    SimpleLeafVertex vertexi = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i), 0, 0);
                    vertexi.setIconKey("sfree:system");
                    vertexi.setLabel("BarabasiAlbertNode"+i);
                    if (!nodes.containsKey(i)) {
                        nodes.put(i, vertexi);
                        System.err.println("Added Node: " + nodes.get(i).getId());
                    }
                    
                    SimpleLeafVertex vertexj = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(j), 0, 0);
                    vertexj.setIconKey("sfree:system");
                    vertexj.setLabel("BarabasiAlbertNode"+j);
                    if (!nodes.containsKey(j)) {
                        nodes.put(j, vertexj);
                        System.err.println("Added Node: " + nodes.get(j).getId());
                    }

                    String edgeId = "link:"+i+"-"+j;
		    SimpleConnector source = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(i).getId()+"-"+edgeId+"-connector", nodes.get(i));
		    SimpleConnector target = new SimpleConnector(TOPOLOGY_NAMESPACE_SFREE, nodes.get(j).getId()+"-"+edgeId+"-connector", nodes.get(j));
		    edges.add(new AbstractEdge(TOPOLOGY_NAMESPACE_SFREE, edgeId, source, target));
                    System.err.println("Added Link: " + edgeId);
		}

		Random r = new Random((new Date()).getTime());
		for(int i=2*averageNumberofNeighboors;i<numberOfNodes;i++){
			
		    SimpleLeafVertex vertexi = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i),0,0);
		    vertexi.setIconKey("sfree:system");
		    vertexi.setLabel("BarabasiAlbertNode"+i);
		    nodes.put(i, vertexi);
		    System.err.println("Adding Node: " + i);
			
		    for(int times=0; times<averageNumberofNeighboors; times++){
		        AbstractEdge edge;
		        double d = r.nextDouble()*nodes.size(); // choose node to attach to
		        System.err.println("Generated random position: " + d);
		        Long j = (long)d;
		        System.err.println("Try Adding edge: " + j.intValue() + "--->" + i);
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

	private void createERRandomTopology(Integer numberOfNodes, Integer averageNumberofNeighboors) {
		Map<Integer,SimpleLeafVertex> nodes = new HashMap<Integer, SimpleLeafVertex>();
		List<AbstractEdge> edges = new ArrayList<AbstractEdge>();
		for (Integer i=0; i< numberOfNodes ;i++) {
			SimpleLeafVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i), 0, 0);
			vertex.setIconKey("sfree:system");
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
}
