package org.opennms.netmgt.dao.hibernate;

import java.util.Set;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

public class CategoryDaoHibernate extends AbstractDaoHibernate<OnmsCategory, Integer> implements
		CategoryDao {

	public CategoryDaoHibernate() {
		super(OnmsCategory.class);
	}

	public OnmsCategory findByName(String name) {
		return findUnique("from OnmsCategory as category where category.name = ?", name);
	}

    public Set<OnmsCategory> findByNode(OnmsNode node) {
        // XXX brozow/david: write this
        throw new UnsupportedOperationException("method not yet implemented");
    }

}
