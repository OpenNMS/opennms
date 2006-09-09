package org.opennms.web.svclayer.dao;

import org.opennms.netmgt.config.categories.Category;



public interface CategoryConfigDao {

	public Category getCategoryByLabel(String label);
		
	
	
}
