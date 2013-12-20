package org.opennms.features.topology.app.internal.support;

import java.util.*;

import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

public class CategoryHopCriteria extends VertexHopCriteria implements CollapsibleCriteria {

	private final String m_categoryName;
	private CategoryDao m_categoryDao;
	private NodeDao m_nodeDao;
	private boolean m_collapsed = false;
	private CategoryVertex m_collapsedVertex;

	public static class CategoryVertex extends AbstractVertex implements GroupRef {
		private Set<VertexRef> m_children = new HashSet<VertexRef>();

        public CategoryVertex(String namespace, String id, String label) {
			super(namespace, id, label);
			setIconKey("group");
		}

		@Override
		public boolean isGroup() {
			return true;
		}

        @Override
        public Set<VertexRef> getChildren() {
            return m_children;
        }

        public void setChildren(Set<VertexRef> children) {
            m_children = children;
        }
    }

	public CategoryHopCriteria(String categoryName) {
		m_categoryName = categoryName;
		setLabel(m_categoryName);
		m_collapsedVertex = new CategoryVertex("category", "category:" + m_categoryName, m_categoryName);

	}

    public CategoryHopCriteria(String categoryName, NodeDao nodeDao, CategoryDao categoryDao){
        this(categoryName);
        setNodeDao(nodeDao);
        setCategoryDao(categoryDao);
        m_collapsedVertex.setChildren(getVertices());
    }

	public CategoryDao getCategoryDao() {
		return m_categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.m_categoryDao = categoryDao;
        if(getId().endsWith("")){
            setId(m_categoryDao.findByName(m_categoryName).getId().toString());
        }
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
		if (category == null) {
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
		if (collapsed != isCollapsed()) {
			this.m_collapsed = collapsed;
			setDirty(true);
		}
	}

	@Override
	public Vertex getCollapsedRepresentation() {
		return m_collapsedVertex;
	}
}
