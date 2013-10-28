package org.opennms.features.topology.app.internal.support;

import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;

public class CategoryHopCriteriaFactory {

	private final CategoryDao m_categoryDao;
	private final NodeDao m_nodeDao;

	public CategoryHopCriteriaFactory(CategoryDao categoryDao, NodeDao nodeDao) {
		m_categoryDao = categoryDao;
		m_nodeDao = nodeDao;
	}
	
	public CategoryHopCriteria getCriteria(String categoryName) {
		CategoryHopCriteria retval = new CategoryHopCriteria(categoryName);
		retval.setCategoryDao(m_categoryDao);
		retval.setNodeDao(m_nodeDao);
		return retval;
	}
}
