package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.support.SelectionTree;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

public class NCSSelectionTree extends SelectionTree implements EdgeProvider {

	private static final long serialVersionUID = 8778577903128733601L;

	private NCSComponentRepository m_dao;
	private NodeDao m_nodeDao;

	private Map<String,Edge> m_edges = new HashMap<String,Edge>();

	public NCSSelectionTree(FilterableHierarchicalContainer container) {
		super(container);
	}

	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao dao) {
		m_nodeDao = dao;
	}

	public NCSComponentRepository getNcsComponentRepository() {
		return m_dao;
	}

	public void setNcsComponentRepository(NCSComponentRepository dao) {
		m_dao = dao;
	}

	@Override
	public String getTitle() {
		return "Services";
	}

	@Override
	public void addEdgeListener(EdgeListener vertexListener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Edge getEdge(String id) {
		return m_edges.get(id);
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		return getEdge(reference.getId());
	}

	@Override
	public List<? extends Edge> getEdges() {
		return Collections.list(Collections.enumeration(m_edges.values()));
	}

	@Override
	public List<? extends Edge> getEdges(Collection<? extends EdgeRef> references) {
		List<Edge> retval = new ArrayList<Edge>();
		for (EdgeRef reference : references) {
			Edge edge = getEdge(reference);
			if (edge != null) {
				retval.add(edge);
			}
		}
		return retval;
	}
	
	@Override
	public void select(Object itemId){
		// TODO: Create edge references that correspond to the selected items
		super.select(itemId);
	}

	@Override
	public void unselect(Object itemId){
		// TODO: Remove edge references that correspond to the unselected items
		super.unselect(itemId);
	}

	@Override
	public String getNamespace() {
		return "ncs";
	}

	@Override
	public void removeEdgeListener(EdgeListener vertexListener) {
		throw new UnsupportedOperationException();
	}
}
