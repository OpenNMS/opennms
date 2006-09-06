package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;

public class CategoryDaoHibernate extends AbstractDaoHibernate<OnmsCategory, Integer> implements
		CategoryDao {

	public CategoryDaoHibernate() {
		super(OnmsCategory.class);
	}

	public OnmsCategory findByName(String name) {
		return findUnique("from OnmsCategory as category where category.name = ?", name);
	}

}
