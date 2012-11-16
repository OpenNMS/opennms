package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.GraphVisitor;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.jung.FRLayoutAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;

public class VEProviderGraphContainer implements GraphContainer {
	
	public class VEGraph implements Graph {

		@Override
		public Layout getLayout() {
			throw new UnsupportedOperationException(
					"VEGraph.getLayout is not yet implemented.");
		}

		@Override
		public Collection<? extends Vertex> getDisplayVertices() {
			throw new UnsupportedOperationException(
					"VEGraph.getDisplayVertices is not yet implemented.");
		}

		@Override
		public Collection<? extends Edge> getDisplayEdges() {
			throw new UnsupportedOperationException(
					"VEGraph.getDisplayEdges is not yet implemented.");
		}

		@Override
		public Edge getEdgeByKey(String edgeKey) {
			throw new UnsupportedOperationException(
					"VEGraph.getEdgeByKey is not yet implemented.");
		}

		@Override
		public Vertex getVertexByKey(String vertexKey) {
			throw new UnsupportedOperationException(
					"VEGraph.getVertexByKey is not yet implemented.");
		}

		@Override
		public void visit(GraphVisitor visitor) throws Exception {
			throw new UnsupportedOperationException(
					"VEGraph.visit is not yet implemented.");
		}

	}

	private static final Logger s_log = LoggerFactory.getLogger(VEProviderGraphContainer.class);
	
	private int m_semanticZoomLevel = 0;
	private double m_scale = 1.0;
	private LayoutAlgorithm m_layoutAlgorithm = new FRLayoutAlgorithm();
	
	private GraphProvider m_baseGraphProvider;
	private final Map<String, VertexProvider> m_vertexProviders = new HashMap<String, VertexProvider>();
	private final Map<String, EdgeProvider> m_edgeProviders = new HashMap<String, EdgeProvider>();
	
	private VEGraph m_graph;

	@Override
	public int getSemanticZoomLevel() {
		return m_semanticZoomLevel;
	}

	@Override
	public void setSemanticZoomLevel(int level) {
		m_semanticZoomLevel = level;
		rebuildGraph();
	}

	@Override
	public double getScale() {
		return m_scale;
	}

	@Override
	public void setScale(double scale) {
		m_scale = scale;
	}
	@Override
	public void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
		m_layoutAlgorithm = layoutAlgorithm;
		redoLayout();
	}

	@Override
	public LayoutAlgorithm getLayoutAlgorithm() {
		return m_layoutAlgorithm;
	}

	@Override
	public void redoLayout() {
        s_log.debug("redoLayout()");
        if(m_layoutAlgorithm != null) {
            m_layoutAlgorithm.updateLayout(this);
            fireChange();
        }
	}

	private void fireChange() {
		throw new UnsupportedOperationException("VEProviderGraphContainer.fireChange is not yet implemented.");
	}

	@Override
	public VertexContainer<?, ?> getVertexContainer() {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertexContainer is not yet implemented.");
	}

	@Override
	public BeanContainer<?, ?> getEdgeContainer() {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getEdgeContainer is not yet implemented.");
	}

	@Override
	public Item getVertexItem(Object vertexId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertexItem is not yet implemented.");
	}

	@Override
	public Item getEdgeItem(Object edgeId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getEdgeItem is not yet implemented.");
	}

	@Override
	public Collection<?> getEndPointIdsForEdge(Object edgeId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getEndPointIdsForEdge is not yet implemented.");
	}

	@Override
	public Collection<?> getEdgeIdsForVertex(Object vertexId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getEdgeIdsForVertex is not yet implemented.");
	}

	@Override
	public Object getVertexItemIdForVertexKey(Object key) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertexItemIdForVertexKey is not yet implemented.");
	}

	@Override
	public GraphProvider getBaseTopology() {
		return m_baseGraphProvider;
	}

	@Override
	public void setBaseTopology(GraphProvider graphProvider) {
		m_baseGraphProvider = graphProvider;
		rebuildGraph();
	}
	
	public void addVertexProvider(VertexProvider vertexProvider) {
		m_vertexProviders.put(vertexProvider.getNamespace(), vertexProvider);
		rebuildGraph();
	}
	
	public void removeVertexProvider(VertexProvider vertexProvider) {
		m_vertexProviders.remove(vertexProvider.getNamespace());
		rebuildGraph();
	}
	
	public void addEdgeProvider(EdgeProvider edgeProvider) {
		m_edgeProviders.put(edgeProvider.getNamespace(), edgeProvider);
		rebuildGraph();
	}
	
	public void removeEdgeProvider(EdgeProvider edgeProvider) {
		m_edgeProviders.remove(edgeProvider.getNamespace());
		rebuildGraph();
	}

	private void rebuildGraph() {
		
		//VEGraph graph;

	}


	@Override
	public Object getGroupId(Object vertexId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getGroupId is not yet implemented.");
	}

	@Override
	public Vertex getParent(VertexRef child) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getParent is not yet implemented.");
	}

	@Override
	public Vertex getVertex(VertexRef ref) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertex is not yet implemented.");
	}

	@Override
	public Edge getEdge(EdgeRef ref) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getEdge is not yet implemented.");
	}

	@Override
	public int getVertexX(VertexRef vertexId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertexX is not yet implemented.");
	}

	@Override
	public void setVertexX(VertexRef vertexId, int x) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.setVertexX is not yet implemented.");
	}

	@Override
	public int getVertexY(VertexRef vertexId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertexY is not yet implemented.");
	}

	@Override
	public void setVertexY(VertexRef vertexId, int y) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.setVertexY is not yet implemented.");
	}

	@Override
	public int getSemanticZoomLevel(Object vertexId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getSemanticZoomLevel is not yet implemented.");
	}

	@Override
	public Object getDisplayVertexId(Object vertexId, int semanticZoomLevel) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getDisplayVertexId is not yet implemented.");
	}

	@Override
	public TopologyProvider getDataSource() {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getDataSource is not yet implemented.");
	}

	@Override
	public void setDataSource(TopologyProvider topologyProvider) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.setDataSource is not yet implemented.");
	}

	@Override
	public Graph getCompleteGraph() {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getCompleteGraph is not yet implemented.");
	}

	@Override
	public Graph getGraph() {
		return m_graph;
	}

	@Override
	public boolean hasChildren(Object itemId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.hasChildren is not yet implemented.");
	}

	@Override
	public Collection<?> getChildren(Object itemId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getChildren is not yet implemented.");
	}

	@Override
	public Object getParentId(Object itemId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getParentId is not yet implemented.");
	}

	@Override
	public boolean containsVertexId(Object vertexId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.containsVertexId is not yet implemented.");
	}

	@Override
	public boolean containsEdgeId(Object edgeId) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.containsEdgeId is not yet implemented.");
	}

	@Override
	public SelectionManager getSelectionManager() {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getSelectionManager is not yet implemented.");
	}

	@Override
	public Collection<?> getVertexForest(Collection<?> vertexIds) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertexForest is not yet implemented.");
	}

	@Override
	public void setVertexItemProperty(Object itemId, String propertyName,
			Object value) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.setVertexItemProperty is not yet implemented.");
	}

	@Override
	public <T> T getVertexItemProperty(Object itemId, String propertyName,
			T defaultValue) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getVertexItemProperty is not yet implemented.");
	}

	@Override
	public Criteria getCriteria(String namespace) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.getCriteria is not yet implemented.");
	}

	@Override
	public void setCriteria(String namespace, Criteria critiera) {
		throw new UnsupportedOperationException(
				"VEProviderGraphContainer.setCriteria is not yet implemented.");
	}


}
