package org.opennms.features.topology.plugins.topo.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;

public class TPGraphProvider implements GraphProvider {
	
	static final String ICON_KEY = "iconKey";
	static final String LABEL = "label";
	static final String TOOLTIP_TEXT = "tooltipText";
	static final String STYLE_NAME = "styleName";
    //private static final String LEAF = "leaf";
	//private static final String ICON = "icon";
	//private static final String IP_ADDR = "ipAddr";
	//private static final String NODE_ID = "nodeID";
	
	private class VertexItemFinder implements ItemFinder {

		@Override
		public Item getItem(Object itemId) {
			return m_topoProvider.getVertexItem(itemId);
		}
		
	}
	
	private class EdgeItemFinder implements ItemFinder {

		@Override
		public Item getItem(Object itemId) {
			return m_topoProvider.getEdgeItem(itemId);
		}
	}
	
	private final TopologyProvider m_topoProvider;
	
	private final IdTracker m_vertexIdTracker;
	private final VertexItemFinder m_vertexItemFinder = new VertexItemFinder();
	private final Set<VertexListener> m_vertexListeners = new CopyOnWriteArraySet<VertexListener>();

	private final IdTracker m_edgeIdTracker;
	private final EdgeItemFinder m_edgeItemFinder = new EdgeItemFinder();
	private final Set<EdgeListener> m_edgeListeners = new CopyOnWriteArraySet<EdgeListener>();
	
	public TPGraphProvider(TopologyProvider topoProvider) {
		m_topoProvider = topoProvider;
		m_vertexIdTracker = new IdTracker(m_topoProvider.getVertexIds());
		m_edgeIdTracker = new IdTracker(m_topoProvider.getEdgeIds());
		
		m_topoProvider.getVertexContainer().addListener(vertexItemSetListener());
		
		m_topoProvider.getEdgeContainer().addListener(edgeItemSetListener());
	}

	@SuppressWarnings("serial")
	private ItemSetChangeListener vertexItemSetListener() {
		return new ItemSetChangeListener() {
			
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				ChangeSet changes = m_vertexIdTracker.setItemIds(m_topoProvider.getVertexIds());
				fireVertexChanges(changes);
			}
		};
	}

	@SuppressWarnings("serial")
	private ItemSetChangeListener edgeItemSetListener() {
		return new ItemSetChangeListener() {
			
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				ChangeSet changes = m_vertexIdTracker.setItemIds(m_topoProvider.getVertexIds());
				fireEdgeChanges(changes);
			}
		};
	}

	@Override
	public String getNamespace() {
		return m_topoProvider.getNamespace();
	}
	

	@Override
	public ItemVertex getVertex(String id) {
		Object itemId = m_vertexIdTracker.getItemId(id);
		return new ItemVertex(getNamespace(), id, itemId, m_vertexItemFinder);
	}
	
	@Override
	public ItemVertex getVertex(VertexRef reference) {
		if (!isInNamespace(reference));
		
		return getVertex(reference.getId());
	}

	@Override
	public List<ItemVertex> getVertices() {
		return getVerticesForItemIds(m_topoProvider.getVertexIds());
	}

	private List<ItemVertex> getVerticesForItemIds(Collection<?> itemIds) {
		List<ItemVertex> vertices = new ArrayList<ItemVertex>();
		for(Object itemId : itemIds) {
			vertices.add(getVertexForItemId(itemId));
		}
		
		return vertices;
	}
	
	private List<ItemVertex> getVertices(List<String> ids) {
		List<ItemVertex> vertices = new ArrayList<ItemVertex>();
		for(String id : ids) {
			vertices.add(getVertex(id));
		}
		return vertices;
	}
	

	private ItemVertex getVertexForItemId(Object itemId) {
		return new ItemVertex(getNamespace(), m_vertexIdTracker.getId(itemId), itemId, m_vertexItemFinder);
	}

	@Override
	public List<ItemVertex> getVertices(Collection<? extends VertexRef> references) {
		List<ItemVertex> vertices = new ArrayList<ItemVertex>();
		for(VertexRef ref : references) {
			if (isInNamespace(ref)) {
				vertices.add(getVertex(ref));
			}
		}
		return vertices;
	}

	@Override
	public List<ItemVertex> getRootGroup() {
		return getVerticesForItemIds(m_topoProvider.getVertexContainer().rootItemIds());
	}

	@Override
	public boolean hasChildren(VertexRef group) {
		assertInNamespace(group);

		Object itemId = m_vertexIdTracker.getItemId(group.getId());
		return m_topoProvider.getVertexContainer().hasChildren(itemId);
		
	}
	
	private void assertInNamespace(Ref ref) {
		if (!isInNamespace(ref)) throw new IllegalArgumentException(String.format("reference with id %s in namespace %s but provider in namespace %s", ref.getId(), ref.getNamespace(), getNamespace()));
	}

	private boolean isInNamespace(Ref group) {
		return getNamespace().equals(group.getNamespace());
	}

	@Override
	public ItemVertex getParent(VertexRef vertex) {
		assertInNamespace(vertex);
		
		Object itemId = m_vertexIdTracker.getItemId(vertex.getId());
		Object parentItemId = m_topoProvider.getVertexContainer().getParent(itemId);
		return parentItemId == null ? null : getVertexForItemId(parentItemId);
		
	}

	@Override
	public List<ItemVertex> getChildren(VertexRef group) {
		assertInNamespace(group);
		
		Object groupId = m_vertexIdTracker.getItemId(group.getId());
		Collection<?> childItemsIds = m_topoProvider.getVertexContainer().getChildren(groupId);
		return getVerticesForItemIds(childItemsIds);
	}

	@Override
	public void addVertexListener(VertexListener vertexListener) {
		m_vertexListeners.add(vertexListener);
	}

	@Override
	public void removeVertexListener(VertexListener vertexListener) {
		m_vertexListeners.remove(vertexListener);
	}
	
	public void fireVertexChanges(ChangeSet changes) {
		for(VertexListener listener : m_vertexListeners) {
			listener.vertexSetChanged(this, getVertices(changes.getAddedIds()), getVertices(changes.getUpdatedIds()), changes.getRemovedIds());
		}
	}

	@Override
	public ItemEdge getEdge(String x) {
		return getEdgeForItemId(m_edgeIdTracker.getItemId(x));
	}

	private ItemEdge getEdgeForItemId(Object itemId) {
		String id = m_edgeIdTracker.getId(itemId);
		List<Object> endPoints = new ArrayList<Object>(m_topoProvider.getEndPointIdsForEdge(itemId));

        Object sourceId = endPoints.get(0);
        Object targetId = endPoints.get(1);

		ItemEdge edge = new ItemEdge(getNamespace(), id, itemId, m_edgeItemFinder);
		
		ItemVertex source = getVertexForItemId(sourceId);
		ItemVertex target = getVertexForItemId(targetId);
		
		ItemConnector sourceConnector = new ItemConnector(getNamespace(), edge.getId()+":"+source.getId(), source, edge);
		ItemConnector targetConnector = new ItemConnector(getNamespace(), edge.getId()+":"+target.getId(), target, edge);

		edge.setSource(sourceConnector);
		edge.setTarget(targetConnector);

		return edge;
	}
	
	private List<ItemEdge> getEdgesForItemIds(Collection<?> itemIds)  {
		List<ItemEdge> edges = new ArrayList<ItemEdge>();
		for(Object itemId : itemIds) {
			edges.add(getEdgeForItemId(itemId));
		}
		
		return edges;

	}

	@Override
	public ItemEdge getEdge(EdgeRef reference) {
		if (isInNamespace(reference)) {
			return getEdge(reference.getId());
		}
		return null;
	}

	@Override
	public List<ItemEdge> getEdges() {
		return getEdgesForItemIds(m_topoProvider.getEdgeIds());
	}

	@Override
	public List<ItemEdge> getEdges(Collection<? extends EdgeRef> references) {
		List<ItemEdge> edges = new ArrayList<ItemEdge>(references.size());
		for(EdgeRef ref : references) {
			if (isInNamespace(ref)) {
				edges.add(getEdge(ref));
			}
		}
		return edges;

	}
	
	private List<ItemEdge> getEdges(List<String> ids) {
		List<ItemEdge> edges = new ArrayList<ItemEdge>(ids.size());
		for(String id : ids) {
			edges.add(getEdge(id));
		}
		return edges;
	}

	@Override
	public void addEdgeListener(EdgeListener edgeListener) {
		m_edgeListeners.add(edgeListener);
	}

	@Override
	public void removeEdgeListener(EdgeListener edgeListener) {
		m_edgeListeners.remove(edgeListener);
	}
	
	public void fireEdgeChanges(ChangeSet changes) {
		for(EdgeListener listener : m_edgeListeners) {
			listener.edgeSetChanged(this, getEdges(changes.getAddedIds()), getEdges(changes.getUpdatedIds()), changes.getRemovedIds());
		}

	}


}
