package org.opennms.features.topology.app.internal.support;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

public class CategoryHopCriteria extends VertexHopCriteria implements CollapsibleCriteria {

	private final String m_categoryName;
	private CategoryDao m_categoryDao;
	private NodeDao m_nodeDao;
	private boolean m_collapsed;
	private CategoryVertex m_collapsedVertex;

	public static class CategoryVertex extends AbstractVertex {
		public CategoryVertex(String namespace, String id, String label) {
			super(namespace, id, label);
		}

		@Override
		public boolean isGroup() {
			return true;
		}
	}

	public CategoryHopCriteria(String categoryName) {
		m_categoryName = categoryName;
        setLabel(m_categoryName);
        m_collapsedVertex = new CategoryVertex("nodes", "category:" + m_categoryName, m_categoryName);
	}

	public CategoryDao getCategoryDao() {
		return m_categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.m_categoryDao = categoryDao;
	}

	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.m_nodeDao = nodeDao;
	}

	/**
	 * TODO: This return value doesn't matter since we just delegate
	 * to the m_delegate provider.
	 */
	@Override
	public String getNamespace() {
		return "category";
	}

    @Override
    public int hashCode() {
        return m_categoryName.hashCode();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CategoryHopCriteria){
            CategoryHopCriteria c = (CategoryHopCriteria) obj;
            return c.m_categoryName.equals(m_categoryName);
        }
        return false;
    }

    public String getCategoryName() {
		return m_categoryName;
	}

	@Override
	public Set<VertexRef> getVertices() {
		OnmsCategory category = m_categoryDao.findByName(m_categoryName);
		if (m_categoryDao == null) {
			return Collections.emptySet();
		} else {
			List<OnmsNode> nodes = m_nodeDao.findByCategory(category);
			Set<VertexRef> retval = new TreeSet<VertexRef>(new RefComparator());
			for (OnmsNode node : nodes) {
				retval.add(new AbstractVertexRef("nodes", String.valueOf(node.getId())));
			}
			return retval;
		}
	}

	@Override
	public boolean isCollapsed() {
		return m_collapsed;
	}

	@Override
	public void setCollapsed(boolean collapsed) {
		this.m_collapsed = collapsed;
	}

	@Override
	public Vertex getCollapsedRepresentation() {
		return m_collapsedVertex;
	}
}
