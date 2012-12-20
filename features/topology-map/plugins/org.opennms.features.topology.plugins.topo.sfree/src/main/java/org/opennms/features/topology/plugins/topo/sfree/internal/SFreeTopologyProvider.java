package org.opennms.features.topology.plugins.topo.sfree.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.opennms.features.topology.api.SimpleEdge;
import org.opennms.features.topology.api.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.DelegatingVertexEdgeProvider;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;

public class SFreeTopologyProvider extends DelegatingVertexEdgeProvider implements GraphProvider {

	private static final String TOPOLOGY_NAMESPACE_SFREE = "sfree";
	public static final String ERDOS_RENIS = "ErdosReniy";
	public static final String BARABASI_ALBERT = "BarabasiAlbert";

	public SFreeTopologyProvider() {
		super(TOPOLOGY_NAMESPACE_SFREE);
	}

	@Override
	public Vertex addGroup(String groupName, String groupIcon) {
		return null;
	}

	@Override
	public void save(String filename) {
		// Do nothing
	}

	@Override
	public void load(String filename) {

		clearVertices();
		clearEdges();

		if (filename.equals(ERDOS_RENIS))
			createERRandomTopology(100,3);		
		else if (filename.equals(BARABASI_ALBERT))
			createBARandomTopology(100,3);
	}

	private void createBARandomTopology(Integer numberOfNodes, Integer averageNumberofNeighboors) {
		Map<Integer,SimpleLeafVertex> nodes = new HashMap<Integer, SimpleLeafVertex>();
		List<SimpleEdge> edges = new ArrayList<SimpleEdge>();

		for(int i=0; i<2*averageNumberofNeighboors; i++){
			int j=(i+1)%(2*averageNumberofNeighboors);
			SimpleLeafVertex vertexi = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i), 0, 0);
			vertexi.setIconKey("sfree:system");
			vertexi.setLabel("BarabasiAlbertNode"+i);
			SimpleLeafVertex vertexj = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(j), 0, 0);
			vertexj.setIconKey("sfree:system");
			vertexj.setLabel("BarabasiAlbertNode"+j);
			nodes.put(i, vertexi);
			nodes.put(j, vertexj);
			System.err.println("Creating First Cluster: " + i);
			System.err.println("Creating First Cluster: " + j);
			edges.add(new SimpleEdge(TOPOLOGY_NAMESPACE_SFREE, "link:"+i+"-"+j, nodes.get(i), nodes.get(j)));
		}

		Random r = new Random((new Date()).getTime());
		for(int i=2*averageNumberofNeighboors;i<numberOfNodes;i++){
			SimpleLeafVertex vertexi = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_SFREE, Integer.toString(i),0,0);
			vertexi.setIconKey("sfree:system");
			vertexi.setLabel("BarabasiAlbertNode"+i);
			nodes.put(i, vertexi);
			System.err.println("Adding Node: " + i);
			for(int times=0; times<averageNumberofNeighboors; times++){
				SimpleEdge edge;
				double d = r.nextDouble()*nodes.size(); // choose node to attach to
				System.err.println("Generated random position: " + d);
				Long j = (long)d;
				System.err.println("Try Adding edge: " + j + "--->" + i);
				edge = new SimpleEdge(TOPOLOGY_NAMESPACE_SFREE, "link:"+i+"-"+j, nodes.get(i), nodes.get(j.intValue()));
				if( i==j.intValue() ) continue;
				edges.add(edge);
			}// m links added
		}

		addVertices(nodes.values().toArray(new Vertex[] {}));
		addEdges(edges.toArray(new Edge[] {}));

	}

	private void createERRandomTopology(Integer numberOfNodes, Integer averageNumberofNeighboors) {
		Map<Integer,SimpleLeafVertex> nodes = new HashMap<Integer, SimpleLeafVertex>();
		List<SimpleEdge> edges = new ArrayList<SimpleEdge>();
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
					edges.add(new SimpleEdge(TOPOLOGY_NAMESPACE_SFREE, "link:"+start+"-"+end, nodes.get(start), nodes.get(end)));
				}
			}
		}

		addVertices(nodes.values().toArray(new Vertex[] {}));
		addEdges(edges.toArray(new Edge[] {}));

	}
}
