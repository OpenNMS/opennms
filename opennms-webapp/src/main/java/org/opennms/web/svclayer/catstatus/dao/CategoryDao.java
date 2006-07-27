package org.opennms.web.svclayer.catstatus.dao;

import org.opennms.netmgt.config.categories.Category;



public interface CategoryDao {

	public Category getCategoryByLabel(String label);
		
	
	
}
