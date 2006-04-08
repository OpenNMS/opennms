/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.category;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsCategory;

public class CategoryFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new CategoryFactory(dataSource);
	}

	public CategoryFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public CategoryFactory() {
        super(OnmsCategory.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsCategory)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyCategory(getDataSource());
	}

	
}