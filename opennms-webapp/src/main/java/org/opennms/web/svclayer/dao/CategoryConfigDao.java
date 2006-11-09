package org.opennms.web.svclayer.dao;

import java.util.Collection;

import org.opennms.netmgt.config.categories.Category;

public interface CategoryConfigDao {

	public Category getCategoryByLabel(String label);
	
	public Collection<Category> findAll();
	
}
